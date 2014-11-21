/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "ble_uri.h"
#include <string.h>
#include "nordic_common.h"
#include "ble_srv_common.h"
#include "app_util.h"
#include "pstorage_mod.h"
#include "softdevice_handler.h"

// Beacon service UUID   0xb3 5d 7d a6 ee d4 4d 59 8f 89 f6 57 3e de a9 67
#define URI_UUID_BASE {0x67, 0xa9, 0xde, 0x3e, 0x57, 0xf6, 0x89, 0x8f, 0x59, 0x4d, 0xd4, 0xee, 0xa6, 0x7d, 0x5d, 0xb3}

#define URI_UUID_BEACON_DATA_1_CHAR   0x7da7
#define URI_UUID_BEACON_DATA_2_CHAR    0x7da8
#define URI_UUID_BEACON_DATA_SIZE_CHAR     0x7da9

#define MAGIC_FLASH_BYTE 0x42                                           /**< Magic byte used to recognise that flash has been written */



// three types of characteristics specifically defined for Beacon URI project
typedef enum {
  beacon_data_1,
  beacon_data_2,
  beacon_data_size
}beacon_data_type_t;

// Forward declaration of the ble_uri_t type.
typedef struct ble_uri_s ble_uri_t;

// configuration for gatt characteristics and services
uint16_t                     service_handle;
ble_gatts_char_handles_t     beacon_data_1_char_handles;
ble_gatts_char_handles_t     beacon_data_2_char_handles;
ble_gatts_char_handles_t     beacon_data_size_char_handles;
uint8_t                      uuid_type;
uint16_t                     conn_handle;
bool                         is_notifying;

// layout for persistent storage
typedef struct
{
  uint8_t  magic_byte;                      // indicates new or onfigured tags
  uint8_t  adv_data[APP_ADV_DATA_MAX_LEN];  // adv data, not available for newly configured tags
  uint8_t  adv_data_len;                    // length of adv data, 0 on initial start up
}flash_db_layout_t;

// byte aligned for persisten storage
typedef union
{
  flash_db_layout_t data;
  uint32_t padding[CEIL_DIV(sizeof(flash_db_layout_t), 4)];
}flash_db_t;

// persistent storage
flash_db_t           *p_flash_db;
static pstorage_handle_t    pstorage_block_id;
static flash_db_t adv_flash;

/**@brief Connect event handler.
 *
 * @param[in]   p_uri       Beacon Configuration Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_connect(ble_evt_t * p_ble_evt)
{
  conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
}

/**@brief Disconnect event handler.
 *
 * @param[in]   p_uri       Beacon Configuration Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_disconnect(ble_evt_t * p_ble_evt)
{
  UNUSED_PARAMETER(p_ble_evt);
  conn_handle = BLE_CONN_HANDLE_INVALID;
}

void wait_for_flash_and_reset(void)
{
  uint32_t err_code;

  err_code = pstorage_access_wait();
  APP_ERROR_CHECK(err_code);

  NVIC_SystemReset();
}

/**@brief Get uri adv data
 *
 * @param[in]   app_adv_data        pointer to data location.
 * @param[in]   app_adv_data_len    pointer to length of adv data.
 */
void get_adv_data (uint8_t* app_adv_data, uint8_t* app_adv_data_len) {
  memcpy(app_adv_data, adv_flash.data.adv_data, adv_flash.data.adv_data_len);
  *app_adv_data_len = adv_flash.data.adv_data_len;
}

/**@brief Update data size characteristic
 *
 */
static void uri_update_adv_len() {
  uint16_t set_data_len = 1;
  uint8_t set_data_value = adv_flash.data.adv_data_len;
  
  sd_ble_gatts_value_set(beacon_data_size_char_handles.value_handle,0,
    &set_data_len, &set_data_value);  
}

/**@brief Update data size characteristic
 *
 */
static void flash_adv_data() {
    uint32_t err_code;

    adv_flash.data.magic_byte = MAGIC_FLASH_BYTE;
    
    err_code = pstorage_clear(&pstorage_block_id, sizeof(flash_db_t));
    APP_ERROR_CHECK(err_code);
  
    err_code = pstorage_store(&pstorage_block_id, (uint8_t *)&adv_flash, sizeof(flash_db_t), 0);
    APP_ERROR_CHECK(err_code);

    err_code = pstorage_access_wait();
    APP_ERROR_CHECK(err_code);
}

/**@brief Write event handler.
 *
 * @param[in]   p_uri       Beacon Configuration Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_write(ble_evt_t * p_ble_evt)
{
  ble_gatts_evt_write_t * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
  
  if ((p_evt_write->handle == beacon_data_1_char_handles.value_handle) &&
   (p_evt_write->len <=APP_ADV_DATA_1_LEN))
  {    
    memcpy(adv_flash.data.adv_data, p_evt_write->data, p_evt_write->len);
    adv_flash.data.adv_data_len = p_evt_write->len;

    uri_update_adv_len();
    flash_adv_data();
  }

  if ((p_evt_write->handle == beacon_data_2_char_handles.value_handle) &&
   (p_evt_write->len <=APP_ADV_DATA_2_LEN))
  {
    memcpy(&adv_flash.data.adv_data[APP_ADV_DATA_1_LEN], p_evt_write->data, p_evt_write->len);
    adv_flash.data.adv_data_len = APP_ADV_DATA_1_LEN+p_evt_write->len;

    uri_update_adv_len();
    flash_adv_data();
  }
}


void ble_uri_on_ble_evt(ble_evt_t * p_ble_evt)
{
  switch (p_ble_evt->header.evt_id)
  {
    case BLE_GAP_EVT_CONNECTED:
      on_connect(p_ble_evt);
      break;

    case BLE_GAP_EVT_DISCONNECTED:
      on_disconnect(p_ble_evt);
      break;

    case BLE_GATTS_EVT_WRITE:
      on_write(p_ble_evt);
      break;

    default:
      break;
  }
}


/**@brief Add Beacon Configuration characteristic.
 *
 * @param[in]   p_uri        Beacon Configuration Service structure.
 * @param[in]   p_uri_init   Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */
static uint32_t beacon_data_1_char_add(void)
{
  ble_gatts_char_md_t char_md;
  ble_gatts_attr_t    attr_char_value;
  ble_uuid_t          ble_uuid;
  ble_gatts_attr_md_t attr_md;

  memset(&char_md, 0, sizeof(char_md));

  char_md.char_props.read   = 1;
  char_md.char_props.write  = 1;
  char_md.char_props.write_wo_resp = 1;

  ble_uuid.type = uuid_type;
  ble_uuid.uuid = URI_UUID_BEACON_DATA_1_CHAR;

  memset(&attr_md, 0, sizeof(attr_md));

  BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
  BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);

  attr_md.vloc       = BLE_GATTS_VLOC_STACK;
  attr_md.vlen       = 1;

  memset(&attr_char_value, 0, sizeof(attr_char_value));

  attr_char_value.p_uuid       = &ble_uuid;
  attr_char_value.p_attr_md    = &attr_md;
  if (adv_flash.data.adv_data_len > APP_ADV_DATA_1_LEN)
    attr_char_value.init_len = APP_ADV_DATA_1_LEN;
  else
    attr_char_value.init_len     = adv_flash.data.adv_data_len;
  attr_char_value.init_offs    = 0;
  attr_char_value.max_len      = APP_ADV_DATA_1_LEN;
  attr_char_value.p_value      = adv_flash.data.adv_data;

  return sd_ble_gatts_characteristic_add(service_handle, &char_md,
                                             &attr_char_value,
                                             &beacon_data_1_char_handles);
}

/**@brief Add Beacon Configuration characteristic.
 *
 * @param[in]   p_uri        Beacon Configuration Service structure.
 * @param[in]   p_uri_init   Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */
static uint32_t beacon_data_2_char_add(void)
{
  ble_gatts_char_md_t char_md;
  ble_gatts_attr_t    attr_char_value;
  ble_uuid_t          ble_uuid;
  ble_gatts_attr_md_t attr_md;


  memset(&char_md, 0, sizeof(char_md));

  char_md.char_props.read   = 1;
  char_md.char_props.write  = 1;
  char_md.char_props.write_wo_resp = 1;

  ble_uuid.type = uuid_type;
  ble_uuid.uuid = URI_UUID_BEACON_DATA_2_CHAR;

  memset(&attr_md, 0, sizeof(attr_md));

  BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
  BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);

  attr_md.vloc       = BLE_GATTS_VLOC_STACK;
  attr_md.vlen       = 1;

  memset(&attr_char_value, 0, sizeof(attr_char_value));

  attr_char_value.p_uuid       = &ble_uuid;
  attr_char_value.p_attr_md    = &attr_md;
  if (adv_flash.data.adv_data_len > APP_ADV_DATA_1_LEN)
    attr_char_value.init_len     = adv_flash.data.adv_data_len - APP_ADV_DATA_1_LEN;
  else
    attr_char_value.init_len     = 0;
  attr_char_value.init_offs    = 0;
  attr_char_value.max_len      = APP_ADV_DATA_2_LEN;
  attr_char_value.p_value      = &adv_flash.data.adv_data[APP_ADV_DATA_1_LEN];

  return sd_ble_gatts_characteristic_add(service_handle, &char_md,
                                             &attr_char_value,
                                             &beacon_data_2_char_handles);
}

/**@brief Add Beacon Configuration characteristic.
 *
 * @param[in]   p_uri        Beacon Configuration Service structure.
 * @param[in]   p_uri_init   Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on success, otherwise an error code.
 */
static uint32_t beacon_data_size_char_add(void)
{
  ble_gatts_char_md_t char_md;
  ble_gatts_attr_t    attr_char_value;
  ble_uuid_t          ble_uuid;
  ble_gatts_attr_md_t attr_md;

  memset(&char_md, 0, sizeof(char_md));

  char_md.char_props.read   = 1;
  char_md.char_props.write_wo_resp = 1;

  ble_uuid.type = uuid_type;
  ble_uuid.uuid = URI_UUID_BEACON_DATA_SIZE_CHAR;

  memset(&attr_md, 0, sizeof(attr_md));

  BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);

  attr_md.vloc       = BLE_GATTS_VLOC_STACK;

  memset(&attr_char_value, 0, sizeof(attr_char_value));

  attr_char_value.p_uuid       = &ble_uuid;
  attr_char_value.p_attr_md    = &attr_md;
  attr_char_value.init_len     = 1;
  attr_char_value.init_offs    = 0;
  attr_char_value.max_len      = 1;
  attr_char_value.p_value      = &adv_flash.data.adv_data_len;

  return sd_ble_gatts_characteristic_add(service_handle, &char_md,
                                             &attr_char_value,
                                             &beacon_data_size_char_handles);
}

static void pstorage_ntf_cb(pstorage_handle_t *  p_handle,
                            uint8_t              op_code,
                            uint32_t             result,
                            uint8_t *            p_data,
                            uint32_t             data_len)
{
  APP_ERROR_CHECK(result);
}
/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in]   sys_evt   System stack event.
 */
static void sys_evt_dispatch(uint32_t sys_evt)
{
    pstorage_sys_event_handler(sys_evt);
}

/**@brief Get uuid type for gatt service
 *
 */
uint8_t get_uuid_type (void){
  return uuid_type;
}

void ble_uri_storage_init(void) {
  uint32_t   err_code;
  // Register with the SoftDevice handler module for BLE events.
  err_code = softdevice_sys_evt_handler_set(sys_evt_dispatch);
  APP_ERROR_CHECK(err_code);

  err_code = pstorage_init();
  APP_ERROR_CHECK(err_code);

  pstorage_module_param_t pstorage_param;
  pstorage_param.cb = pstorage_ntf_cb;
  pstorage_param.block_size = sizeof(flash_db_t);
  pstorage_param.block_count = 1;

  err_code = pstorage_register(&pstorage_param, &pstorage_block_id);
  APP_ERROR_CHECK(err_code);

  p_flash_db = (flash_db_t *)pstorage_block_id.block_id;

  // The first time a device is started after a full reset (erasing persistent data). the MAGIC_FLASH_BYTE
  // is not set. In this case, initialize the persistent memory.
  if (p_flash_db->data.magic_byte != MAGIC_FLASH_BYTE) {
    // copy info from parameter definitions into persistent memory
    memset(adv_flash.data.adv_data, 0, APP_ADV_DATA_MAX_LEN);
    adv_flash.data.adv_data_len  = 0;

    flash_adv_data();
  } else {
    // any further initializations read data from pesistent memory into clbeacon_info
    // copy data from persisten memory into parameter definitions
    memcpy(adv_flash.data.adv_data, p_flash_db->data.adv_data, APP_ADV_DATA_MAX_LEN);
    adv_flash.data.adv_data_len = p_flash_db->data.adv_data_len;
  }  
}


uint32_t ble_uri_init(void)
{
  uint32_t   err_code;
  ble_uuid_t ble_uuid;

  // Initialize service structure
  conn_handle       = BLE_CONN_HANDLE_INVALID;
  
  // Add base UUID to softdevice's internal list.
  ble_uuid128_t base_uuid = URI_UUID_BASE;
  err_code = sd_ble_uuid_vs_add(&base_uuid, &uuid_type);
  if (err_code != NRF_SUCCESS)
  {
    return err_code;
  }

  ble_uuid.type = uuid_type;
  ble_uuid.uuid = URI_UUID_SERVICE;

  err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY, &ble_uuid, &service_handle);
  if (err_code != NRF_SUCCESS)
  {
    return err_code;
  }

  err_code = beacon_data_1_char_add();
  if (err_code != NRF_SUCCESS)
  {
    return err_code;
  }

  err_code = beacon_data_2_char_add();
  if (err_code != NRF_SUCCESS)
  {
    return err_code;
  }

  err_code = beacon_data_size_char_add();
  if (err_code != NRF_SUCCESS)
  {
    return err_code;
  }

  return NRF_SUCCESS;
}


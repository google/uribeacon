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

#include <stdbool.h>
#include <stdint.h>
#include "ble_conn_params.h"
#include "ble.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "nordic_common.h"
#include "softdevice_handler.h"
#include "app_gpiote.h"
#include "app_timer.h"
#include "app_button.h"
#include "pca20006.h"
#include "ble_uri.h"
#include "nrf_soc.h"

#define LED_R_MSK  (1UL << LED_RED)
#define LED_G_MSK  (1UL << LED_GREEN)
#define LED_B_MSK  (1UL << LED_BLUE)

#define BOOTLOADER_BUTTON_PIN           BUTTON_0                                    /**< Button used to enter DFU mode. */
#define CONFIG_MODE_BUTTON_PIN          BUTTON_1                                    /**< Button used to enter config mode. */
#define APP_GPIOTE_MAX_USERS            2
#define BUTTON_DETECTION_DELAY          APP_TIMER_TICKS(50, APP_TIMER_PRESCALER)

#define ASSERT_LED_PIN_NO      LED_RED
#define ADVERTISING_LED_PIN_NO LED_BLUE
#define CONNECTED_LED_PIN_NO   LED_GREEN

#define APP_CFG_NON_CONN_ADV_TIMEOUT  0                                             /**< Time for which the device must be advertising in non-connectable mode (in seconds). 0 disables timeout. */
#define NON_CONNECTABLE_ADV_INTERVAL  MSEC_TO_UNITS(1000, UNIT_0_625_MS)            /**< The advertising interval for non-connectable advertisement (852 ms). This value can vary between 100ms to 10.24s). */

// -----------------------------------

#define APP_ADV_INTERVAL                MSEC_TO_UNITS(1000, UNIT_0_625_MS)           /**< The advertising interval (in units of 0.625 ms. This value corresponds to 1000 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS      30                                          /**< The advertising timeout (in units of seconds). */

#define MIN_CONN_INTERVAL               MSEC_TO_UNITS(500, UNIT_1_25_MS)            /**< Minimum acceptable connection interval (0.5 seconds). */
#define MAX_CONN_INTERVAL               MSEC_TO_UNITS(1000, UNIT_1_25_MS)           /**< Maximum acceptable connection interval (1 second). */
#define SLAVE_LATENCY                   0                                           /**< Slave latency. */
#define CONN_SUP_TIMEOUT                MSEC_TO_UNITS(4000, UNIT_10_MS)             /**< Connection supervisory timeout (4 seconds). */

#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(20000, APP_TIMER_PRESCALER) /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (15 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(5000, APP_TIMER_PRESCALER)  /**< Time between each call to sd_ble_gap_conn_param_update after the first (5 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                           /**< Number of attempts before giving up the connection parameter negotiation. */

#define SEC_PARAM_TIMEOUT               30                                          /**< Timeout for Pairing Request or Security Request (in seconds). */
#define SEC_PARAM_BOND                  0                                           /**< Perform bonding. */
#define SEC_PARAM_MITM                  0                                           /**< Man In The Middle protection not required. */
#define SEC_PARAM_IO_CAPABILITIES       BLE_GAP_IO_CAPS_NONE                        /**< No I/O capabilities. */
#define SEC_PARAM_OOB                   0                                           /**< Out Of Band data not available. */
#define SEC_PARAM_MIN_KEY_SIZE          7                                           /**< Minimum encryption key size. */
#define SEC_PARAM_MAX_KEY_SIZE          16                                          /**< Maximum encryption key size. */

#define APP_TIMER_PRESCALER         0                                   /**< RTC prescaler value used by app_timer */
#define APP_TIMER_MAX_TIMERS        3                                   /**< One for each module + one for ble_conn_params + a few extra */
#define APP_TIMER_OP_QUEUE_SIZE     3                                   /**< Maximum number of timeout handlers pending execution */

#define SCHED_MAX_EVENT_DATA_SIZE       sizeof(app_timer_event_t)       /**< Maximum size of scheduler events. Note that scheduler BLE stack events do not contain any data, as the events are being pulled from the stack in the event handler. */
#define SCHED_QUEUE_SIZE                10                              /**< Maximum number of events in the scheduler queue. */

#define DEAD_BEEF                     0xDEADBEEF                        /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

#define ADV_FLAGS_LEN                 3

// Normal: send configured ADV packet
// Config: enable GATT characteristic configuration
typedef enum {
  beacon_mode_config,
  beacon_mode_normal
}beacon_mode_t;

static ble_gap_sec_params_t m_sec_params;                               /**< Security requirements for this application. */
static ble_gap_adv_params_t m_adv_params;                               /**< Parameters to be passed to the stack when starting advertising. */
static uint16_t             m_conn_handle = BLE_CONN_HANDLE_INVALID;    /**< Handle of the current connection. */

static uint8_t adv_flags[ADV_FLAGS_LEN] = {0x02, 0x01, 0x04};

/**@brief Function for error handling, which is called when an error has occurred.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of error.
 *
 * @param[in] error_code  Error code supplied to the handler.
 * @param[in] line_num    Line number where the handler is called.
 * @param[in] p_file_name Pointer to the file name.
 */
void app_error_handler(uint32_t error_code, uint32_t line_num, const uint8_t * p_file_name)
{
  nrf_gpio_pin_clear(ASSERT_LED_PIN_NO);

  // This call can be used for debug purposes during application development.
  // @note CAUTION: Activating this code will write the stack to flash on an error.
  //                This function should NOT be used in a final product.
  //                It is intended STRICTLY for development/debugging purposes.
  //                The flash write will happen EVEN if the radio is active, thus interrupting
  //                any communication.
  //                Use with care. Un-comment the line below to use.
  //ble_debug_assert_handler(error_code, line_num, p_file_name);

  // On assert, the system can only recover on reset.
  NVIC_SystemReset();
}

/**@brief Callback function for asserts in the SoftDevice.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in]   line_num   Line number of the failing ASSERT call.
 * @param[in]   file_name  File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}


/**@brief Function for the LEDs initialization.
 *
 * @details Initializes all LEDs to be off 
 */
static void leds_init(void)
{
    nrf_gpio_cfg_output(ADVERTISING_LED_PIN_NO);
    nrf_gpio_cfg_output(CONNECTED_LED_PIN_NO);
    nrf_gpio_cfg_output(ASSERT_LED_PIN_NO);

    nrf_gpio_pin_set(ADVERTISING_LED_PIN_NO);
    nrf_gpio_pin_set(CONNECTED_LED_PIN_NO);
    nrf_gpio_pin_set(ASSERT_LED_PIN_NO);
}

/**@brief Function for initializing the Advertising functionality.
 *
 * @details Encodes the required advertising data and passes it to the stack.
 *          Also builds a structure to be passed to the stack when starting advertising.
 */
static void advertising_init(beacon_mode_t mode)
{
  if (mode == beacon_mode_normal) {
    // in normal mode, get the adv_data to create an ADV packet
    uint8_t  adv_data[ADV_FLAGS_LEN+APP_ADV_DATA_MAX_LEN];
    uint8_t  adv_data_len;

    memcpy(adv_data, adv_flags, ADV_FLAGS_LEN);
    get_adv_data(&adv_data[ADV_FLAGS_LEN], &adv_data_len); 
    
    // data_len is 0 for uninitialized tags
    if ((adv_data_len > 0) && (adv_data_len <= APP_ADV_DATA_MAX_LEN)) {
      uint32_t err_code;

      err_code = sd_ble_gap_adv_data_set(adv_data, adv_data_len+ADV_FLAGS_LEN, NULL, NULL);
      APP_ERROR_CHECK(err_code);

      // Initialize advertising parameters (used when starting advertising).
      memset(&m_adv_params, 0, sizeof(m_adv_params));

      m_adv_params.type        = BLE_GAP_ADV_TYPE_ADV_NONCONN_IND;
      m_adv_params.p_peer_addr = NULL;                             // Undirected advertisement.
      m_adv_params.fp          = BLE_GAP_ADV_FP_ANY;
      m_adv_params.interval    = NON_CONNECTABLE_ADV_INTERVAL;
      m_adv_params.timeout     = APP_CFG_NON_CONN_ADV_TIMEOUT;
    }
  }
  else if (mode == beacon_mode_config)
  {
    // in config mode, set up the ADV packet to enable connections to GATT service
    uint32_t      err_code;
    ble_advdata_t advdata;
    ble_advdata_t scanrsp;
    uint8_t       flags = BLE_GAP_ADV_FLAGS_LE_ONLY_LIMITED_DISC_MODE;

    ble_uuid_t adv_uuids[] = {{URI_UUID_SERVICE, get_uuid_type()}};

    // Build and set advertising data
    memset(&advdata, 0, sizeof(advdata));

    advdata.include_appearance      = true;
    advdata.flags.size              = sizeof(flags);
    advdata.flags.p_data            = &flags;

    memset(&scanrsp, 0, sizeof(scanrsp));
    scanrsp.uuids_complete.uuid_cnt = sizeof(adv_uuids) / sizeof(adv_uuids[0]);
    scanrsp.uuids_complete.p_uuids  = adv_uuids;

    err_code = ble_advdata_set(&advdata, &scanrsp);
    APP_ERROR_CHECK(err_code);

    // Initialize advertising parameters (used when starting advertising).
    memset(&m_adv_params, 0, sizeof(m_adv_params));

    m_adv_params.type        = BLE_GAP_ADV_TYPE_ADV_IND;
    m_adv_params.p_peer_addr = NULL;
    m_adv_params.fp          = BLE_GAP_ADV_FP_ANY;
    m_adv_params.interval    = APP_ADV_INTERVAL;
    m_adv_params.timeout     = APP_ADV_TIMEOUT_IN_SECONDS;
  }
  else
  {
    APP_ERROR_CHECK_BOOL(false);
  }
}

/**@brief Function for starting advertising.
 */
static void advertising_start(void)
{
  uint32_t err_code;

  err_code = sd_ble_gap_adv_start(&m_adv_params);
  APP_ERROR_CHECK(err_code);
}

/**@brief Function for the Power manager.
 */
static void power_manage(void)
{
  uint32_t err_code = sd_app_evt_wait();
  APP_ERROR_CHECK(err_code);
}

/**@brief Function for handeling button presses.
 */
static void button_handler(uint8_t pin_no)
{
  if(pin_no == CONFIG_MODE_BUTTON_PIN) {
      wait_for_flash_and_reset();
  }
  else if (pin_no == BOOTLOADER_BUTTON_PIN) {
      wait_for_flash_and_reset();
  }
  else {
      APP_ERROR_CHECK_BOOL(false);
  }
}

/**@brief Function for initializing the app_button module.
 */
static void buttons_init(void)
{
  // @note: Array must be static because a pointer to it will be saved in the Button handler
  // module.
  static app_button_cfg_t buttons[] =
  {
    {CONFIG_MODE_BUTTON_PIN, false, BUTTON_PULL, button_handler},
    {BOOTLOADER_BUTTON_PIN, false, BUTTON_PULL, button_handler}
  };

  APP_BUTTON_INIT(buttons, sizeof(buttons) / sizeof(buttons[0]), BUTTON_DETECTION_DELAY, true);
}


/**@brief Function for handling the writes to the configuration characteristics of the beacon configuration service.
 * @detail A pointer to this function is passed to the service in its init structure.
 */

/**@brief Function for the GAP initialization.
 *
 * @details This function shall be used to setup all the necessary GAP (Generic Access Profile)
 *          parameters of the device. It also sets the permissions and appearance.
 */
static void gap_params_init(void)
{
  uint32_t                err_code;
  ble_gap_conn_params_t   gap_conn_params;

  memset(&gap_conn_params, 0, sizeof(gap_conn_params));

  gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
  gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
  gap_conn_params.slave_latency     = SLAVE_LATENCY;
  gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

  err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
  APP_ERROR_CHECK(err_code);
}

/**@brief Function for initializing security parameters.
 */
static void sec_params_init(void)
{
  m_sec_params.timeout      = SEC_PARAM_TIMEOUT;
  m_sec_params.bond         = SEC_PARAM_BOND;
  m_sec_params.mitm         = SEC_PARAM_MITM;
  m_sec_params.io_caps      = SEC_PARAM_IO_CAPABILITIES;
  m_sec_params.oob          = SEC_PARAM_OOB;
  m_sec_params.min_key_size = SEC_PARAM_MIN_KEY_SIZE;
  m_sec_params.max_key_size = SEC_PARAM_MAX_KEY_SIZE;
}

/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module which
 *          are passed to the application.
 *          @note All this function does is to disconnect. This could have been done by simply
 *                setting the disconnect_on_fail config parameter, but instead we use the event
 *                handler mechanism to demonstrate its use.
 *
 * @param[in]   p_evt   Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
  uint32_t err_code;

  if(p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED) {
    err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
    APP_ERROR_CHECK(err_code);
  }
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in]   nrf_error   Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
  uint32_t               err_code;
  ble_conn_params_init_t cp_init;

  memset(&cp_init, 0, sizeof(cp_init));

  cp_init.p_conn_params                  = NULL;
  cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
  cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
  cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
  cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
  cp_init.disconnect_on_fail             = false;
  cp_init.evt_handler                    = on_conn_params_evt;
  cp_init.error_handler                  = conn_params_error_handler;

  err_code = ble_conn_params_init(&cp_init);
  APP_ERROR_CHECK(err_code);
}

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
static void on_ble_evt(ble_evt_t * p_ble_evt)
{
  uint32_t                         err_code = NRF_SUCCESS;
  static ble_gap_evt_auth_status_t m_auth_status;
  ble_gap_enc_info_t *             p_enc_info;

  switch (p_ble_evt->header.evt_id) {
    case BLE_GAP_EVT_CONNECTED:
      m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
      break;

    case BLE_GAP_EVT_DISCONNECTED:
      m_conn_handle = BLE_CONN_HANDLE_INVALID;
      wait_for_flash_and_reset();
      break;

    case BLE_GAP_EVT_SEC_PARAMS_REQUEST:
      err_code = sd_ble_gap_sec_params_reply(m_conn_handle,
                                             BLE_GAP_SEC_STATUS_SUCCESS,
                                             &m_sec_params);
      break;

    case BLE_GATTS_EVT_SYS_ATTR_MISSING:
      err_code = sd_ble_gatts_sys_attr_set(m_conn_handle, NULL, 0);
      break;

    case BLE_GAP_EVT_AUTH_STATUS:
      m_auth_status = p_ble_evt->evt.gap_evt.params.auth_status;
      break;

    case BLE_GAP_EVT_SEC_INFO_REQUEST:
      p_enc_info = &m_auth_status.periph_keys.enc_info;
      if (p_enc_info->div == p_ble_evt->evt.gap_evt.params.sec_info_request.div) {
          err_code = sd_ble_gap_sec_info_reply(m_conn_handle, p_enc_info, NULL);
      } else {
          // No keys found for this device
          err_code = sd_ble_gap_sec_info_reply(m_conn_handle, NULL, NULL);
      }
      break;

    case BLE_GAP_EVT_TIMEOUT:
      if (p_ble_evt->evt.gap_evt.params.timeout.src == BLE_GAP_TIMEOUT_SRC_ADVERTISEMENT)
      {
          wait_for_flash_and_reset();
      }
      break;

    default:
      break;
  }

  APP_ERROR_CHECK(err_code);
}

/**@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the scheduler in the main loop after a BLE stack
 *          event has been received.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
static void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
  on_ble_evt(p_ble_evt);
  ble_conn_params_on_ble_evt(p_ble_evt);
  ble_uri_on_ble_evt(p_ble_evt);
}

/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
  uint32_t err_code;

  // Initialize the SoftDevice handler module.
  SOFTDEVICE_HANDLER_INIT(NRF_CLOCK_LFCLKSRC_XTAL_20_PPM, true);

  // Register with the SoftDevice handler module for BLE events.
  err_code = softdevice_ble_evt_handler_set(ble_evt_dispatch);
  APP_ERROR_CHECK(err_code);

}

//------------------------------------------------------------------//
/**
 * @brief Function for handling pstorage events
 */
/**
 * @brief Function for application main entry.
 */
int main(void)
{
  uint32_t err_code;
  bool config_mode = true;

  // Initialize.
  APP_SCHED_INIT(SCHED_MAX_EVENT_DATA_SIZE, SCHED_QUEUE_SIZE);
  APP_TIMER_INIT(APP_TIMER_PRESCALER, APP_TIMER_MAX_TIMERS, APP_TIMER_OP_QUEUE_SIZE, false);
  APP_GPIOTE_INIT(APP_GPIOTE_MAX_USERS);

  buttons_init();
  leds_init();

  err_code = app_button_enable();
  APP_ERROR_CHECK(err_code);

  err_code = app_button_is_pushed(CONFIG_MODE_BUTTON_PIN, &config_mode);
  APP_ERROR_CHECK(err_code);

  ble_stack_init();
  ble_uri_storage_init();

  if(config_mode) {
    gap_params_init();
    err_code = ble_uri_init();
    APP_ERROR_CHECK(err_code);
    advertising_init(beacon_mode_config);
    conn_params_init();
    sec_params_init();
  } else {
    advertising_init(beacon_mode_normal);
  }

  // Start execution.
  advertising_start();

  // Enter main loop.
  for (;;) {
    app_sched_execute();
    power_manage();
  }
}

/**
 * @}
 */

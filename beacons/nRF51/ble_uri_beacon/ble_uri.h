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
 
#ifndef BLE_URI_H__
#define BLE_URI_H__

#include <stdint.h>
#include <stdbool.h>
#include "ble.h"
#include "ble_srv_common.h"

// configured service ids
#define URI_UUID_SERVICE               0x7da6

#define APP_ADV_DATA_1_LEN                20
#define APP_ADV_DATA_2_LEN                8

#define APP_ADV_DATA_MAX_LEN              APP_ADV_DATA_1_LEN + APP_ADV_DATA_2_LEN

uint32_t ble_uri_init(void);

void ble_uri_on_ble_evt(ble_evt_t * p_ble_evt);

void get_adv_data (uint8_t* app_adv_data, uint8_t* app_adv_data_len);
uint8_t get_uuid_type (void);
void wait_for_flash_and_reset(void);
void ble_uri_storage_init(void);

#endif // BLE_URI_H__

/** @} */

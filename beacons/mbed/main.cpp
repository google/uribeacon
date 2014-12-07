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

#include "mbed.h"
#include "BLEDevice.h"
#include "URIBeaconConfigService.h"
#include "DFUService.h"
#include "DeviceInformationService.h"

BLEDevice ble;
URIBeaconConfigService *uriBeaconConfig;

void disconnectionCallback(Gap::Handle_t handle, Gap::DisconnectionReason_t reason) {
  ble.startAdvertising();
}

void switchCallback(void)
{
  static bool switched = false;
  if (!switched) {
    printf("executing switch\r\n");
    delete uriBeaconConfig;
    uriBeaconConfig = NULL;
 
    static const uint8_t BEACON_UUID[] = {0xD8, 0xFE};
    static const uint8_t urldata[] = {
      BEACON_UUID[0],
      BEACON_UUID[1],
      0x00, // flags
      0x20, // power
      0x00, // http://www.
      'm',
      'b',
      'e',
      'd',
      0x08, // .".org"
    };
 
    ble.shutdown();
    ble.init();
 
    ble.clearAdvertisingPayload();
    ble.accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LIST_16BIT_SERVICE_IDS, BEACON_UUID, sizeof(BEACON_UUID));
    ble.accumulateAdvertisingPayload(GapAdvertisingData::SERVICE_DATA, urldata, sizeof(urldata));
 
    ble.setAdvertisingType(GapAdvertisingParams::ADV_NON_CONNECTABLE_UNDIRECTED);
    ble.setAdvertisingInterval(1600); /* 1s; in multiples of 0.625ms. */
    ble.startAdvertising();
 
    switched = true;
  }
}
 
int main(void)
{
  Ticker ticker;
  //  ticker.attach(switchCallback, 30);
 
  ble.init();
  ble.onDisconnection(disconnectionCallback);
 
  uriBeaconConfig = new URIBeaconConfigService(ble, "http://uribeacon.org");
  if (!uriBeaconConfig->configuredSuccessfully()) {
    error("failed to accommodate URI");
  }
  /* optional use of the API offered by URIBeaconConfigService */
  const int8_t powerLevels[] = {-20, -4, 0, 10};
  uriBeaconConfig->setTxPowerLevels(powerLevels);
  uriBeaconConfig->setTxPowerMode(URIBeaconConfigService::TX_POWER_MODE_LOW);
 
  /* Setup auxiliary services. */
  DFUService dfu(ble); /* To allow over-the-air firmware udpates. optional. */
  DeviceInformationService deviceInfo(ble, "ARM", "UriBeacon", "SN1", "hw-rev1", "fw-rev1", "soft-rev1"); /* optional */
 
  ble.clearAdvertisingPayload();
  ble.accumulateAdvertisingPayload(GapAdvertisingData::INCOMPLETE_LIST_128BIT_SERVICE_IDS, URIBeacon2ControlServiceUUID, sizeof(URIBeacon2ControlServiceUUID));
  ble.setAdvertisingInterval(Gap::MSEC_TO_ADVERTISEMENT_DURATION_UNITS(1000));
  ble.setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);
  ble.setAdvertisingInterval(1600); /* 1000ms; in multiples of 0.625ms. */
  ble.startAdvertising();
 
  while (true) {
    ble.waitForEvent();
  }
}

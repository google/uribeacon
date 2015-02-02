/*
 * Copyright 2014-2015 Google Inc. All rights reserved.
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

#include <stdint.h>
#include <stddef.h>
#include <nrf_error.h>
#include "mbed.h"
#include "BLEDevice.h"
#include "URIBeaconConfigService.h"
#include "DFUService.h"
#include "pstorage.h"
#include "DeviceInformationService.h"

// Struct to hold persistent data across power cycles
struct PersistentData_t {
    uint32_t magic;
    URIBeaconConfigService::Params_t params;
    uint8_t pad[4];
} __attribute__ ((aligned (4)));

static const int PERSISTENT_DATA_ALIGNED_SIZE = sizeof(PersistentData_t);
// Seconds after power-on that config service is available.
static const int ADVERTISING_TIMEOUT_SECONDS = 60;
// Advertising interval for config service.
static const int ADVERTISING_INTERVAL_MSEC = 1000;
// Maximum size of service data in ADV packets
static const int SERVICE_DATA_MAX = 31;
// Magic that identifies persistent storage
static const uint32_t MAGIC = 0x1BEAC000;
// Values for ADV packets related to firmware levels
static URIBeaconConfigService::PowerLevels_t  defaultAdvPowerLevels = {-20, -4, 0, 10};
// Values for setTxPower() indexed by power mode.
static const int8_t firmwarePowerLevels[] = {-20, -4, 0, 10};

BLEDevice ble;
URIBeaconConfigService *uriBeaconConfig;
pstorage_handle_t pstorageHandle;
PersistentData_t  persistentData;

/* LEDs for indication */
DigitalOut  connectionStateLed(LED1);
DigitalOut  advertisingStateLed(LED2);

void blink(int count) {
    for (int i = 0; i <= count; i++) {
        advertisingStateLed = !advertisingStateLed;
        wait(0.2);
        advertisingStateLed = !advertisingStateLed;
        wait(0.2);
    }
}

/* Dummy callback handler needed by Nordic's pstorage module. */
void pstorageNotificationCallback(pstorage_handle_t *p_handle,
                                  uint8_t            op_code,
                                  uint32_t           result,
                                  uint8_t *          p_data,
                                  uint32_t           data_len) {
    /* APP_ERROR_CHECK(result); */
}

void pstorageLoad() {
    pstorage_init();
    pstorage_module_param_t pstorageParams = {
        .cb          = pstorageNotificationCallback,
        .block_size  = PERSISTENT_DATA_ALIGNED_SIZE,
        .block_count = 1
    };
    pstorage_register(&pstorageParams, &pstorageHandle);
    if (pstorage_load(reinterpret_cast<uint8_t *>(&persistentData),
                      &pstorageHandle, PERSISTENT_DATA_ALIGNED_SIZE, 0) != NRF_SUCCESS) {
        // On failure zero out and let the service reset to defaults
        memset(&persistentData, 0, sizeof(PersistentData_t));
    }
}


void pstorageSave() {
    if (persistentData.magic != MAGIC) {
        persistentData.magic = MAGIC;
        pstorage_store(&pstorageHandle,
                       reinterpret_cast<uint8_t *>(&persistentData),
                       sizeof(PersistentData_t),
                       0 /* offset */);
    } else {
        pstorage_update(&pstorageHandle,
                        reinterpret_cast<uint8_t *>(&persistentData),
                        sizeof(PersistentData_t),
                        0 /* offset */);
    }
}

void startAdvertisingUriBeaconConfig() {
    char  DEVICE_NAME[] = "mUriBeacon Config";

    ble.clearAdvertisingPayload();

    // Stops advertising the UriBeacon Config Service after a delay
    ble.setAdvertisingTimeout(ADVERTISING_TIMEOUT_SECONDS);

    ble.accumulateAdvertisingPayload(
        GapAdvertisingData::BREDR_NOT_SUPPORTED |
        GapAdvertisingData::LE_GENERAL_DISCOVERABLE);

    // UUID is in different order in the ADV frame (!)
    uint8_t reversedServiceUUID[sizeof(UUID_URI_BEACON_SERVICE)];
    for (unsigned int i = 0; i < sizeof(UUID_URI_BEACON_SERVICE); i++) {
        reversedServiceUUID[i] =
            UUID_URI_BEACON_SERVICE[sizeof(UUID_URI_BEACON_SERVICE) - i - 1];
    }
    ble.accumulateAdvertisingPayload(
        GapAdvertisingData::COMPLETE_LIST_128BIT_SERVICE_IDS,
        reversedServiceUUID,
        sizeof(reversedServiceUUID));

    ble.accumulateAdvertisingPayload(GapAdvertisingData::GENERIC_TAG);
    ble.accumulateScanResponse(
        GapAdvertisingData::COMPLETE_LOCAL_NAME,
        reinterpret_cast<uint8_t *>(&DEVICE_NAME),
        sizeof(DEVICE_NAME));
    ble.accumulateScanResponse(
        GapAdvertisingData::TX_POWER_LEVEL,
        reinterpret_cast<uint8_t *>(
            &defaultAdvPowerLevels[URIBeaconConfigService::TX_POWER_MODE_LOW]),
        sizeof(uint8_t));

    ble.setTxPower(
        firmwarePowerLevels[URIBeaconConfigService::TX_POWER_MODE_LOW]);

    ble.setDeviceName(reinterpret_cast<uint8_t *>(&DEVICE_NAME));
    ble.setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);
    ble.setAdvertisingInterval(
        Gap::MSEC_TO_ADVERTISEMENT_DURATION_UNITS(ADVERTISING_INTERVAL_MSEC));
    ble.startAdvertising();
}


void startAdvertisingUriBeacon() {
    uint8_t serviceData[SERVICE_DATA_MAX];
    int serviceDataLen = 0;

    advertisingStateLed = 1;
    connectionStateLed = 1;

    ble.shutdown();
    ble.init();

    // Fields from the Service
    int beaconPeriod = persistentData.params.beaconPeriod;
    int txPowerMode = persistentData.params.txPowerMode;
    int uriDataLength = persistentData.params.uriDataLength;
    URIBeaconConfigService::UriData_t &uriData = persistentData.params.uriData;
    URIBeaconConfigService::PowerLevels_t &advPowerLevels =
        persistentData.params.advPowerLevels;
    uint8_t flags = persistentData.params.flags;

    pstorageSave();

    delete uriBeaconConfig;
    uriBeaconConfig = NULL;

    ble.clearAdvertisingPayload();
    ble.setTxPower(firmwarePowerLevels[txPowerMode]);

    ble.setAdvertisingType(
        GapAdvertisingParams::ADV_NON_CONNECTABLE_UNDIRECTED);

    ble.setAdvertisingInterval(
        Gap::MSEC_TO_ADVERTISEMENT_DURATION_UNITS(beaconPeriod));

    ble.accumulateAdvertisingPayload(
        GapAdvertisingData::BREDR_NOT_SUPPORTED |
        GapAdvertisingData::LE_GENERAL_DISCOVERABLE);

    ble.accumulateAdvertisingPayload(
        GapAdvertisingData::COMPLETE_LIST_16BIT_SERVICE_IDS, BEACON_UUID,
        sizeof(BEACON_UUID));

    serviceData[serviceDataLen++] = BEACON_UUID[0];
    serviceData[serviceDataLen++] = BEACON_UUID[1];
    serviceData[serviceDataLen++] = flags;
    serviceData[serviceDataLen++] = advPowerLevels[txPowerMode];
    for (int j=0; j < uriDataLength; j++) {
        serviceData[serviceDataLen++] = uriData[j];
    }

    ble.accumulateAdvertisingPayload(
        GapAdvertisingData::SERVICE_DATA,
        serviceData, serviceDataLen);

    ble.startAdvertising();
}

// After advertising timeout, stop config and switch to UriBeacon
void timeout(void) {
    Gap::GapState_t state;
    state = ble.getGapState();
    if (!state.connected) {
        startAdvertisingUriBeacon();
    }
}

// When connected to config service, change the LEDs
void connectionCallback(Gap::Handle_t handle,
                        Gap::addr_type_t peerAddrType,
                        const Gap::address_t peerAddr,
                        const Gap::ConnectionParams_t *params) {
    advertisingStateLed = 1;
    connectionStateLed = 0;
}

// When disconnected from config service, start advertising UriBeacon
void disconnectionCallback(Gap::Handle_t handle,
                           Gap::DisconnectionReason_t reason) {
    advertisingStateLed = 0;    // on
    connectionStateLed = 1;     // off
    startAdvertisingUriBeacon();
}

int main(void) {
    URIBeaconConfigService::UriData_t uriData  = {
        // http://uribeacon.org
        0x02, 'u', 'r', 'i', 'b', 'e', 'a', 'c', 'o', 'n', 0x08
    };
    int uriDataLength = 11;

    advertisingStateLed = 0;    // on
    connectionStateLed = 1;     // off

    ble.init();
    ble.onDisconnection(disconnectionCallback);
    ble.onConnection(connectionCallback);
    // Advertising timeout
    ble.onTimeout(timeout);

    pstorageLoad();
    bool resetToDefaults = persistentData.magic != MAGIC;
    uriBeaconConfig = new URIBeaconConfigService(
        ble, persistentData.params, resetToDefaults,
        uriData, uriDataLength, defaultAdvPowerLevels);
    if (!uriBeaconConfig->configuredSuccessfully()) {
        error("failed to accommodate URI");
    }

    // Setup auxiliary services to allow over-the-air firmware updates, etc
    DFUService dfu(ble);
    DeviceInformationService deviceInfo(
        ble, "ARM", "UriBeacon", "SN1", "hw-rev1", "fw-rev1", "soft-rev1");


    startAdvertisingUriBeaconConfig();

    while (true) {
        ble.waitForEvent();
    }
}

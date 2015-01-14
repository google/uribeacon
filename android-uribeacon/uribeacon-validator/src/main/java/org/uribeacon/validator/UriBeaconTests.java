/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package org.uribeacon.validator;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;

public class UriBeaconTests {

  private static final String TAG = UriBeaconTests.class.getCanonicalName();
  public static ArrayList<TestHelper> initializeTests(Context context, BluetoothDevice bluetoothDevice, TestCallback testCallback) {

    ArrayList<TestHelper> tests = new ArrayList<>();
    tests.add(
        new Builder()
            .name("Write Short Key")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.LOCK, TestData.SHORT_LOCK_KEY, BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH)
            .insertActions(lockUnlockCheck())
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .connect()
            .insertActions(writeAndReadAllValueCharacteristics())
            .disconnect()
            .build()
    );
    return tests;
  }

  private static Builder writeAndReadAllValueCharacteristics() {
    return new Builder()
        .writeAndRead(ProtocolV2.DATA, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.FLAGS, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.POWER_LEVELS, TestData.BASIC_TX_POWER)
        .writeAndRead(ProtocolV2.POWER_MODE, TestData.BASIC_GENERAL_DATA)
        .writeAndRead(ProtocolV2.PERIOD, TestData.BASIC_PERIOD);
  }

  private static Builder lockUnlockCheck() {
    return new Builder()
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .write(ProtocolV2.LOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.LOCKED_STATE, BluetoothGatt.GATT_SUCCESS)
        .write(ProtocolV2.UNLOCK, TestData.BASIC_LOCK_KEY, BluetoothGatt.GATT_SUCCESS)
        .assertEquals(ProtocolV2.LOCK_STATE, TestData.UNLOCKED_STATE, BluetoothGatt.GATT_SUCCESS);
  }
}

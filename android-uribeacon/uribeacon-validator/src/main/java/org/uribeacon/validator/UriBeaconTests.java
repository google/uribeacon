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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;
import java.util.Arrays;

public class UriBeaconTests {

  private static final String TAG = UriBeaconTests.class.getCanonicalName();

  public static ArrayList<TestHelper> initializeTests(Context context, ScanResult result, TestCallback testCallback) {
    return new ArrayList<>(Arrays.asList(
        new TestHelper.Builder()
            .name("Write & Read URL")
            .setUp(context, result, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build(),
        new TestHelper.Builder()
            .name("Test that's supposed to fail")
            .setUp(context, result, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.DATA, "01234567890123456789".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    ));
  }
}

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
import java.util.ListIterator;
import java.util.UUID;

public class UriBeaconTests {

  private static final String TAG = UriBeaconTests.class.getCanonicalName();

  private static final UUID LOCK_STATE = UUID.fromString("ee0c2081-8786-40ba-ab96-99b91ac981d8");
  private static final UUID LOCK = UUID.fromString("ee0c2082-8786-40ba-ab96-99b91ac981d8");
  private static final UUID UNLOCK = UUID.fromString("ee0c2083-8786-40ba-ab96-99b91ac981d8");
  private static final UUID DATA = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
  private static final UUID FLAGS = UUID.fromString("ee0c2085-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_LEVELS = UUID.fromString("ee0c2086-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_MODE = UUID.fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  private static final UUID PERIOD = UUID.fromString("ee0c2088-8786-40ba-ab96-99b91ac981d8");
  private static final UUID RESET = UUID.fromString("ee0c2089-8786-40ba-ab96-99b91ac981d8");

  public ArrayList<TestHelper> tests;

  public UriBeaconTests(Context context, ScanResult result, TestCallback testCallback) {
    tests = new ArrayList<>(Arrays.asList(
        new TestHelper.Builder()
            .name("Write & Read URL")
            .setUp(context, result, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .connect()
            .assertEquals(DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build(),
        new TestHelper.Builder()
            .name("Test that's supposed to fail")
            .setUp(context, result, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(DATA, "01234567890123456789".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    ));
  }

  public ListIterator<TestHelper> iterator() {
    return tests.listIterator();
  }
}

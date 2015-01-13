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
import android.content.Context;

import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.TestHelper.Builder;
import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;
import java.util.Arrays;

public class UriBeaconTests {

  private static final String TAG = UriBeaconTests.class.getCanonicalName();

  public static ArrayList<TestHelper> initializeTests(Context context, BluetoothDevice bluetoothDevice, TestCallback testCallback) {
    return new ArrayList<>(Arrays.asList(
        new Builder()
            .name("Set flags to 0")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.FLAGS, new byte[]{0}, 0)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.FLAGS, new byte[]{0}, 0)
            .disconnect()
            .assertAdvFlags((byte) 0)
            .build(),
        new Builder()
            .name("Set flags to 10")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.FLAGS, new byte[]{10}, 0)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.FLAGS, new byte[]{10}, 0)
            .disconnect()
            .assertAdvFlags((byte) 10)
            .build(),
        new Builder()
            .name("Test URI writes correct value")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.DATA, new byte[]{0, 1, 2, 3}, 0)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.DATA, new byte[]{0, 1, 2, 3}, 0)
            .disconnect()
            .assertAdvUri(new byte[]{0, 1, 2, 3})
            .build(),
        new Builder()
            .name("Test set tx power")
            .setUp(context, bluetoothDevice, ProtocolV2.CONFIG_SERVICE_UUID, testCallback)
            .connect()
            .write(ProtocolV2.POWER_LEVELS, new byte[]{2, 2, 2, 2}, 0)
            .disconnect()
            .connect()
            .assertEquals(ProtocolV2.POWER_LEVELS, new byte[]{2, 2, 2, 2}, 0)
            .disconnect()
            .assertAdvTxPower((byte) 2)
            .build()
    ));
  }
}

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

package org.uribeacon.scan.compat;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.BLUETOOTH_SERVICE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.app.AlarmManager;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.test.AndroidTestCase;

import org.uribeacon.scan.compat.BluetoothLeScannerCompat;
import org.uribeacon.scan.compat.BluetoothLeScannerCompatProvider;
import org.uribeacon.scan.compat.JbBluetoothLeScannerCompat;

/**
 * Unit tests for the {@link org.uribeacon.scan.compat.BluetoothLeScannerCompatProvider} singleton provider.
 */
public class BluetoothLeScannerCompatProviderTest extends AndroidTestCase {

  private Context context;
  private BluetoothManager bluetoothManager;
  private AlarmManager alarmManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    bluetoothManager = (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);
    alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
    context = new ContextWrapper(getContext()) {
      @Override
      public Object getSystemService(String name) {
        return BLUETOOTH_SERVICE.equals(name) ? bluetoothManager
            : ALARM_SERVICE.equals(name) ? alarmManager
            : super.getSystemService(name);
      }
    };
  }

  /**
   * Make sure that calling the static getter more than once returns the same scanner instance.
   */
  public void testSingleton() {
    BluetoothLeScannerCompat scanner1 =
        BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(context);
    assertNotNull(scanner1);
    BluetoothLeScannerCompat scanner2 =
        BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(context);
    assertTrue(scanner1 == scanner2);  // Test reference equivalence with '=='
  }

  /**
   * Check "L" support.
   * <P>
   * This test will not run if the LBluetoothLeScannerCompat class isn't linked.
   */
  public void testAdvancedAndroidVersion() {
    String lCompat =
        "LBluetoothLeScannerCompat";

    try {
      Class.forName(lCompat);
    } catch (ClassNotFoundException e) {
      // Class not linked; this test cannot run.
      return;
    }

    assertEquals(lCompat, BluetoothLeScannerCompatProvider
            .getBluetoothLeScannerCompat(context, true).getClass());
  }

  /**
   * Make sure we don't try to instantiate the scanner if either manager (bt or alarms) is missing.
   */
  public void testManagersRequired() {
    final boolean canUseNativeApi = true;

    bluetoothManager = null;
    alarmManager = null;
    assertNull(BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(
        context, canUseNativeApi));

    bluetoothManager = (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);
    alarmManager = null;
    assertNull(BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(
        context, canUseNativeApi));

    bluetoothManager = null;
    alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
    assertNull(BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(
        context, canUseNativeApi));

    bluetoothManager = (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);
    alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
    assertNotNull(BluetoothLeScannerCompatProvider.getBluetoothLeScannerCompat(
        context, canUseNativeApi));
  }
}

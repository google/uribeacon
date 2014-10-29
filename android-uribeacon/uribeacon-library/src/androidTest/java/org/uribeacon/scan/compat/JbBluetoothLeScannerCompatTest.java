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
import static android.test.MoreAsserts.assertEmpty;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.BALANCED_ACTIVE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.BALANCED_IDLE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.LOW_LATENCY_ACTIVE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.LOW_LATENCY_IDLE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.LOW_POWER_ACTIVE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.LOW_POWER_IDLE_MILLIS;
import static org.uribeacon.scan.compat.JbBluetoothLeScannerCompat.SCAN_LOST_CYCLES;
import static org.uribeacon.scan.compat.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
import static org.uribeacon.scan.compat.ScanSettings.CALLBACK_TYPE_FIRST_MATCH;
import static org.uribeacon.scan.compat.ScanSettings.CALLBACK_TYPE_MATCH_LOST;
import static org.uribeacon.scan.compat.ScanSettings.SCAN_MODE_BALANCED;
import static org.uribeacon.scan.compat.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static org.uribeacon.scan.compat.ScanSettings.SCAN_MODE_LOW_POWER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.uribeacon.scan.compat.JbBluetoothLeScannerCompat;
import org.uribeacon.scan.compat.ScanCallback;
import org.uribeacon.scan.compat.ScanFilter;
import org.uribeacon.scan.compat.ScanRecord;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.compat.ScanSettings;
import org.uribeacon.scan.testing.FakeClock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.test.AndroidTestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the {@link org.uribeacon.scan.compat.JbBluetoothLeScannerCompat},
 * the core adapter class of the scan compat library.
 */
public class JbBluetoothLeScannerCompatTest extends AndroidTestCase {

  private static final ScanSettings SLOW = builder().setScanMode(SCAN_MODE_LOW_POWER).build();
  private static final ScanSettings MEDIUM = builder().setScanMode(SCAN_MODE_BALANCED).build();
  private static final ScanSettings FAST = builder().setScanMode(SCAN_MODE_LOW_LATENCY).build();
  private static final ScanSettings BATCH = builder().setReportDelayMillis(1).build();

  private static final ScanSettings FOUND = buildScanSettingsForType(CALLBACK_TYPE_FIRST_MATCH);
  private static final ScanSettings ALL = buildScanSettingsForType(CALLBACK_TYPE_ALL_MATCHES);
  private static final ScanSettings LOST = buildScanSettingsForType(CALLBACK_TYPE_MATCH_LOST);

  private static final List<ScanFilter> NO_FILTER =
      new ArrayList<ScanFilter>();
  private static final List<ScanFilter> BERT_FILTER =
      new ArrayList<ScanFilter>() {{
        add(new ScanFilter.Builder().setDeviceName("Bert").build());
      }};
  private static final List<ScanFilter> ERNIE_FILTER =
      new ArrayList<ScanFilter>() {{
        add(new ScanFilter.Builder().setDeviceName("Ernie").build());
      }};

  @Mock private AlarmManager alarmManagerMock;

  private BluetoothManager bluetoothManager;
  private AlarmManager alarmManager;
  private JbBluetoothLeScannerCompat scanner;
  private FakeClock clock;
  private TestingCallback callback;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    bluetoothManager = (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);
    alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
    clock = new FakeClock();
    scanner = new JbBluetoothLeScannerCompat(
        bluetoothManager, alarmManager, clock, null /* pending intent */);
    callback = new TestingCallback();
  }

  /**
   * Make sure the 'simple' startScan() call--the one that takes no filters or settings,
   * just a callback--works.
   */
  public void testSimpleStartScan() {
    scanner.startScan(callback);
    assertEquals(0, callback.found);

    // We see a beacon
    onScan("address", 0);

    assertEquals(1, callback.found);
  }

  /**
   * Verify that the hashsets used to track ScanSettings don't accidentally merge them.
   */
  public void testSettingsAreUnique() {
    ScanCallback callback1 = new TestingCallback();
    ScanCallback callback2 = new TestingCallback();

    assertTrue(scanner.startScan(NO_FILTER, MEDIUM, callback1));
    assertTrue(scanner.startScan(NO_FILTER, MEDIUM, callback2));
    assertEquals(2, scanner.serialClients.size());

    scanner.stopScan(callback1);
    assertEquals(1, scanner.serialClients.size());

    scanner.stopScan(callback2);
    assertEquals(0, scanner.serialClients.size());
  }

  /**
   * Verify that we default to balanced performance.  (Checks against the constants in
   * the source file, so if those constants change this test will still pass.)
   */
  public void testScanDefaults() {
    assertEquals(BALANCED_ACTIVE_MILLIS, scanner.getScanActiveMillis());
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());
  }

  /**
   * Verify that we return to defaults when there are no listeners after removing a serial listener.
   */
  public void testScanDefaultsRestored() {
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());
    scanner.startScan(NO_FILTER, FAST, callback);
    scanner.stopScan(callback);
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());
  }

  /**
   * Verify that we return to defaults when there are no listeners after removing a batch listener.
   */
  public void testScanDefaultsRestoredFromBatch() {
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());
    scanner.startScan(NO_FILTER, BATCH, callback);
    scanner.stopScan(callback);
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());
  }

  /**
   * Verify that multiple scan requests uses the most critical preset timing values.
   */
  public void testMultipleScanTiming() {
    ScanCallback slow = new TestingCallback();
    ScanCallback medium = new TestingCallback();
    ScanCallback fast = new TestingCallback();

    // Slow
    scanner.startScan(NO_FILTER, SLOW, slow);
    assertEquals(LOW_POWER_ACTIVE_MILLIS, scanner.getScanActiveMillis());
    assertEquals(LOW_POWER_IDLE_MILLIS, scanner.getScanIdleMillis());

    // Slow and Medium
    scanner.startScan(NO_FILTER, MEDIUM, medium);
    assertEquals(BALANCED_ACTIVE_MILLIS, scanner.getScanActiveMillis());
    assertEquals(BALANCED_IDLE_MILLIS, scanner.getScanIdleMillis());

    // Slow, Medium, Fast
    scanner.startScan(NO_FILTER, FAST, fast);
    assertEquals(LOW_LATENCY_ACTIVE_MILLIS, scanner.getScanActiveMillis());
    assertEquals(LOW_LATENCY_IDLE_MILLIS, scanner.getScanIdleMillis());

    // Back it off--stop Medium and Fast, keep Slow
    scanner.stopScan(medium);
    scanner.stopScan(fast);
    assertEquals(LOW_POWER_ACTIVE_MILLIS, scanner.getScanActiveMillis());
    assertEquals(LOW_POWER_IDLE_MILLIS, scanner.getScanIdleMillis());
  }

  /**
   * Verify updating the scan timing with custom values.
   */
  public void testScanTimingOverride() {
    ScanSettings settings = builder().build();

    scanner.setCustomScanTiming(123, 456, -1);  // (Third parameter is unused.)
    scanner.startScan(NO_FILTER, settings, callback);
    assertEquals(123, scanner.getScanActiveMillis());
    assertEquals(456, scanner.getScanIdleMillis());
    assertEquals(clock.currentTimeMillis() - SCAN_LOST_CYCLES * (123 + 456),
        scanner.getLostTimestampMillis());
  }

  /**
   * Verify the time values sent to the alarm scheduler instance.
   */
  public void testAlarmTiming() {
    if (true) {
        return;
    }
    ScanCallback slow = new TestingCallback();
    ScanCallback medium = new TestingCallback();
    ScanCallback fast = new TestingCallback();
    long slowCycle = LOW_POWER_ACTIVE_MILLIS + LOW_POWER_IDLE_MILLIS;
    long mediumCycle = BALANCED_ACTIVE_MILLIS + BALANCED_IDLE_MILLIS;
    long fastCycle = LOW_LATENCY_ACTIVE_MILLIS + LOW_LATENCY_IDLE_MILLIS;

    // This line enables debugging Android/mockito tests through Eclipse.
    // See https://code.google.com/p/dexmaker/issues/detail?id=2.
    System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
    MockitoAnnotations.initMocks(this);
    scanner = new JbBluetoothLeScannerCompat(
        bluetoothManager, alarmManagerMock, clock, null /* pending intent */);

    // Slow
    scanner.startScan(NO_FILTER, SLOW, slow);
    verify(alarmManagerMock).setRepeating(eq(AlarmManager.RTC_WAKEUP),
        eq(clock.currentTimeMillis()), eq(slowCycle), any(PendingIntent.class));

    // Slow and Medium
    scanner.startScan(NO_FILTER, MEDIUM, medium);
    verify(alarmManagerMock).setRepeating(eq(AlarmManager.RTC_WAKEUP),
        eq(clock.currentTimeMillis()), eq(mediumCycle), any(PendingIntent.class));

    // Slow, Medium, Fast
    scanner.startScan(NO_FILTER, FAST, fast);
    verify(alarmManagerMock).setRepeating(eq(AlarmManager.RTC_WAKEUP),
        eq(clock.currentTimeMillis()), eq(fastCycle), any(PendingIntent.class));

    // Stop Medium, should still be fast, so nothing should change
    scanner.stopScan(medium);
    verifyNoMoreInteractions(alarmManagerMock);
  }

  /**
   * Verify that the alarm is canceled when there are no listeners.
   */
  public void testCancel() {
    if (true) {
        return;
    }
    long slowCycle = LOW_POWER_ACTIVE_MILLIS + LOW_POWER_IDLE_MILLIS;

    System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
    MockitoAnnotations.initMocks(this);
    scanner = new JbBluetoothLeScannerCompat(
        bluetoothManager, alarmManagerMock, clock, null /* pending intent */);

    scanner.startScan(NO_FILTER, SLOW, callback);
    verify(alarmManagerMock).setRepeating(eq(AlarmManager.RTC_WAKEUP),
        eq(clock.currentTimeMillis()), eq(slowCycle), any(PendingIntent.class));

    scanner.stopScan(callback);
    verify(alarmManagerMock).cancel(any(PendingIntent.class));
  }

  /**
   * Test found events vs updated events.
   */
  public void testFoundEventVsUpdatedEvent() {
    TestingCallback foundCallback = new TestingCallback();
    TestingCallback updatedCallback = new TestingCallback();
    
    scanner.startScan(NO_FILTER, FOUND, foundCallback);
    scanner.startScan(NO_FILTER, ALL, updatedCallback);

    // We see a beacon; only the found callback fires.
    onScan("address", 0);

    assertEquals(1, foundCallback.found);
    assertEquals(0, updatedCallback.updated);

    // Same beacon again; only the second callback fires
    onScan("address", 0);

    assertEquals(1, foundCallback.found);
    assertEquals(1, updatedCallback.updated);
  }

  /**
   * Test lost events over time.
   */
  public void testLostEvent() {
    scanner.startScan(NO_FILTER, LOST, callback);
    onScan("address", nowMillis());

    // If a cycle completes instantly, we haven't "lost" the beacon yet.
    scanner.onScanCycleComplete();
    assertEquals(0, callback.lost);

    // Time rolls forward, but not enough time to cause the sighting to be lost
    long expectedTimeoutToLoseSightingMillis =
        clock.currentTimeMillis() - scanner.getLostTimestampMillis();
    clock.advance(expectedTimeoutToLoseSightingMillis - 5);
    scanner.onScanCycleComplete();
    assertEquals(0, callback.lost);

    // Another few milliseconds and we've lost the beacon
    clock.advance(10);
    scanner.onScanCycleComplete();
    assertEquals(1, callback.lost);
    assertEmpty(scanner.recentScanResults);
  }
  
  public void testSetScanLostOverride() {
    long scanLostOverrideMillis = 15 * 1000;
    scanner.setScanLostOverride(scanLostOverrideMillis);
    scanner.startScan(NO_FILTER, LOST, callback);
    onScan("address", nowMillis());

    // If a cycle completes instantly, we haven't "lost" the beacon yet.
    scanner.onScanCycleComplete();
    assertEquals(0, callback.lost);

    // Time rolls forward, but not enough time to cause the sighting to be lost
    long expectedTimeoutToLoseSightingMillis =
        clock.currentTimeMillis() - scanner.getLostTimestampMillis();
    // The lost timestamp includes our override.
    assertEquals(clock.currentTimeMillis() - scanLostOverrideMillis,
                 scanner.getLostTimestampMillis());
    clock.advance(expectedTimeoutToLoseSightingMillis - 5);
    scanner.onScanCycleComplete();
    assertEquals(0, callback.lost);

    // Another few milliseconds and we've lost the beacon
    clock.advance(10);
    scanner.onScanCycleComplete();
    assertEquals(1, callback.lost);
    assertEmpty(scanner.recentScanResults);
  }

  /**
   * Test lost events are deferred if we keep seeing the beacon.
   */
  public void testLostEventPostponed() {
    scanner.startScan(NO_FILTER, ALL, callback);

    // As time goes by, so long as we keep seeing the beacon it should never be lost
    long oneCycleMillis = scanner.getScanCycleMillis();
    for (int i = 0; i < SCAN_LOST_CYCLES * 10; i++) {
      onScan("address", nowMillis());
      clock.advance(oneCycleMillis);
      scanner.onScanCycleComplete();
      assertEquals(1, callback.found);
      assertEquals(i, callback.updated);
      assertEquals(0, callback.lost);
    }

    // Once enough time passes, it's lost
    int noMoreUpdates = callback.updated;
    clock.advance(SCAN_LOST_CYCLES * oneCycleMillis);
    scanner.onScanCycleComplete();
    assertEquals(1, callback.lost);
    assertEquals(noMoreUpdates, callback.updated);
  }

  /**
   * Test the mathematics of the "we've lost a beacon" timer cutoff code.
   * <P>
   * Note that this is something of a no-op test, because each of the three modes actually
   * happen to all run at the same cycle today (1.5 seconds).  This test will become
   * more relevant if that ever changes.
   */
  public void testCutoffTimestamp() {
    long now = clock.currentTimeMillis();
    ScanCallback slow = new TestingCallback();
    ScanCallback medium = new TestingCallback();
    ScanCallback fast = new TestingCallback();

    // Slow
    scanner.startScan(NO_FILTER, SLOW, slow);
    assertEquals(now - SCAN_LOST_CYCLES * (LOW_POWER_ACTIVE_MILLIS + LOW_POWER_IDLE_MILLIS),
        scanner.getLostTimestampMillis());

    // Medium
    scanner.startScan(NO_FILTER, MEDIUM, medium);
    assertEquals(now - SCAN_LOST_CYCLES * (BALANCED_ACTIVE_MILLIS + BALANCED_IDLE_MILLIS),
        scanner.getLostTimestampMillis());

    // Fast
    scanner.startScan(NO_FILTER, FAST, fast);
    assertEquals(now - SCAN_LOST_CYCLES * (LOW_LATENCY_ACTIVE_MILLIS + LOW_LATENCY_IDLE_MILLIS),
        scanner.getLostTimestampMillis());
  }

  /**
   * Test scan filters.
   * <P>
   * The filters themselves are already unit-tested pretty extensively, so it's enough here
   * to test that filters are passed through correctly.
   */
  public void testFilters() {
    List<ScanFilter> filters = new ArrayList<ScanFilter>() {{
      add(new ScanFilter.Builder().setDeviceName("foo").build());
      add(new ScanFilter.Builder().setDeviceName("bar").build());
    }};

    scanner.startScan(filters, FOUND, callback);

    onScan("not foo or bar", 0);
    assertEquals(0, callback.found);
    onScan("foo", 0);
    assertEquals(1, callback.found);
    onScan("bar", 0);
    assertEquals(2, callback.found);
    onScan("something else that isn't foo or bar", 0);
    assertEquals(2, callback.found);
  }

  /**
   * Test multiple scan listeners with filters.
   */
  public void testMultipleFilters() {
    final boolean[] bert = { false };
    final boolean[] ernie = { false };

    scanner.startScan(BERT_FILTER, FOUND, new TestingCallback() {
      @Override
      public void onScanResult(int callbackType, ScanResult result) {
        bert[0] = true;
      }
    });
    scanner.startScan(ERNIE_FILTER, FOUND, new TestingCallback() {
      @Override
      public void onScanResult(int callbackType, ScanResult result) {
        ernie[0] = true;
      }
    });

    onScan("Bert", 0);
    assertTrue(bert[0]);
    assertFalse(ernie[0]);

    bert[0] = false;
    onScan("Ernie", 0);
    assertFalse(bert[0]);
    assertTrue(ernie[0]);
  }

  /**
   * Test new registrations get past sightings.
   */
  public void testNewListenersGetPastSightings() {
    onScan("address", nowMillis());
    scanner.onScanCycleComplete();

    assertEquals(0, callback.found);
    scanner.startScan(NO_FILTER, FOUND, callback);
    assertEquals(1, callback.found);
  }

  /**
   * Test new registrations don't get past sightings from too long ago.
   */
  public void testNewListenersDontGetOldSightings() {
    onScan("address", scanner.getLostTimestampMillis() - 1);
    scanner.onScanCycleComplete();

    scanner.startScan(NO_FILTER, FOUND, callback);
    assertEquals(0, callback.found);
  }

  /**
   * Test new registrations get recent finds per their filters.
   */
  public void testNewListenersGetRecentPastSightingsCorrectlyFiltered() {
    onScan("Bert", nowMillis());
    onScan("Ernie", nowMillis());
    clock.advance(10);

    scanner.startScan(BERT_FILTER, FOUND, callback);
    assertEquals(1, callback.found);
  }

  /////////////////////////////////////////////////////////////////////////////

  private static class TestingCallback extends ScanCallback {
    
    int found = 0;
    int updated = 0;
    int lost = 0;
    int batched = 0;

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      switch (callbackType) {
        case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
          updated++;
          break;
        case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
          found++;
          break;
        case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
          lost++;
          break;
        default:
          fail("Unrecognized callback type constant received: " + callbackType);
      }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> scans) {
      batched += scans.size();
    }
  }

  private void onScan(String address, long timeMillis) {
    byte[] addressBytes = address.getBytes();
    byte[] scanRecordBytes = new byte[addressBytes.length + 2];
    scanRecordBytes[0] = (byte) (addressBytes.length + 1);
    scanRecordBytes[1] = 0x09; // Value of private ScanRecord.DATA_TYPE_LOCAL_NAME_COMPLETE;
    System.arraycopy(addressBytes, 0, scanRecordBytes, 2, addressBytes.length);

    scanner.onScanResult(address,
        new ScanResult(
            null /* BluetoothDevice */,
            ScanRecord.parseFromBytes(scanRecordBytes),
            0 /* rssi */,
            TimeUnit.MILLISECONDS.toNanos(timeMillis)));
  }

  private long nowMillis() {
    return clock.currentTimeMillis();
  }

  private static ScanSettings.Builder builder() {
    return new ScanSettings.Builder();
  }

  private static ScanSettings buildScanSettingsForType(int callbackType) {
    return builder().setCallbackType(callbackType).build();
  }
}

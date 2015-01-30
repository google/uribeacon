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
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

class TestRunner {

  private static final String TAG = TestRunner.class.getCanonicalName();

  private boolean mStopped;
  private boolean mFailed = false;
  private final TestCallback mTestCallback = new TestCallback() {
    @Override
    public void testStarted() {
      mDataCallback.dataUpdated();
    }

    @Override
    public void testCompleted(final BluetoothDevice bluetoothDevice, final BluetoothGatt gatt) {
      Log.d(TAG, "Test Completed. Failed: " + mLatestTest.isFailed());
      if (mLatestTest.isFailed()) {
        mFailed = true;
      }
      mDataCallback.dataUpdated();
      if (!mStopped) {
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            start(bluetoothDevice, gatt);
          }
        }, TimeUnit.SECONDS.toMillis(1));
      } else {
        if (gatt != null) {
          gatt.disconnect();
        }
        mHandler.removeCallbacksAndMessages(null);
      }
    }

    @Override
    public void waitingForConfigMode() {
      mDataCallback.waitingForConfigMode();
    }

    @Override
    public void connectedToBeacon() {
      mDataCallback.connectedToBeacon();
    }

    @Override
    public void multipleConfigModeBeacons(ArrayList<ScanResult> scanResults) {
      mDataCallback.multipleConfigModeBeacons(scanResults);
    }

  };
  private final ArrayList<TestHelper> mUriBeaconTests;
  private TestHelper mLatestTest;
  private final ListIterator<TestHelper> mTestIterator;
  private final DataCallback mDataCallback;
  private final Handler mHandler;

  public TestRunner(Context context, DataCallback dataCallback,
      String testType, boolean optionalImplemented) {
    mStopped = false;
    mDataCallback = dataCallback;
    if (CoreUriBeaconTests.class.getName().equals(testType)) {
      mUriBeaconTests = CoreUriBeaconTests.initializeTests(context, mTestCallback,
          optionalImplemented);
    } else {
      mUriBeaconTests = SpecUriBeaconTests
          .initializeTests(context, mTestCallback, optionalImplemented);
    }
    mTestIterator = mUriBeaconTests.listIterator();
    mHandler = new Handler(Looper.myLooper());
  }

  public void start(BluetoothDevice bluetoothDevice, BluetoothGatt gatt) {
    Log.d(TAG, "Starting tests");
    if (mTestIterator.hasNext()) {
      mLatestTest = mTestIterator.next();
      mLatestTest.run(bluetoothDevice, gatt, superBluetoothScanCallback);
    } else {
      mDataCallback.testsCompleted(mFailed);
    }
  }

  // To keep the connection to the beacon alive the same gatt object
  // must be passed around. But since gatt is attached to a callback a single super callback
  // is needed for all tests to share.
  private final BluetoothGattCallback superBluetoothScanCallback = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      mLatestTest.mGattCallback.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      mLatestTest.mGattCallback.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        int status) {
      mLatestTest.mGattCallback.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic,
        int status) {
      mLatestTest.mGattCallback.onCharacteristicWrite(gatt, characteristic, status);
    }
  };

  public ArrayList<TestHelper> getUriBeaconTests() {
    return mUriBeaconTests;
  }

  public void stop() {
    mStopped = true;
    mLatestTest.stopTest();
  }

  public void connectTo(int which) {
    mLatestTest.connectTo(which);
  }

  public interface DataCallback {

    public void dataUpdated();

    public void waitingForConfigMode();

    public void connectedToBeacon();

    public void testsCompleted(boolean failed);

    public void multipleConfigModeBeacons(ArrayList<ScanResult> scanResults);
  }
}

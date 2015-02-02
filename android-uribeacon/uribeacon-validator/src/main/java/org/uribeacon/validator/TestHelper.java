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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.ProtocolV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TestHelper {

  private static final String TAG = TestHelper.class.getCanonicalName();
  private final ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      // First time we see the beacon
      Log.d(TAG, "On scan Result");
      if (mBluetoothDevice == null) {
        mBluetoothDevice = result.getDevice();
        mBluetoothDevice.connectGatt(mContext, false, mOutSideGattCallback);
      } else {
        checkPacket(result);
      }
    }
  };
  private boolean started = false;
  private boolean failed = false;
  private boolean finished = false;
  private boolean disconnected = false;
  private BluetoothGatt mGatt;
  private final long SCAN_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private BluetoothGattService mService;
  private final String mName;
  private final Context mContext;
  private BluetoothDevice mBluetoothDevice;
  private final UUID mServiceUuid;
  private BluetoothGattCallback mOutSideGattCallback;
  public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      Log.d(TAG, "Status: " + status + "; New State: " + newState);
      mGatt = gatt;
      if (status == BluetoothGatt.GATT_SUCCESS) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
          mGatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
          if (!failed) {
            mTestActions.remove();
            disconnected = true;
            dispatch();
          }
        }
      } else {
        fail("Failed. Status: " + status + ". New State: " + newState);
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      super.onServicesDiscovered(gatt, status);
      Log.d(TAG, "On services discovered");
      mGatt = gatt;
      mService = mGatt.getService(mServiceUuid);
      mTestActions.remove();
      mTestCallback.connectedToBeacon();
      dispatch();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
      Log.d(TAG, "On characteristic read");
      mGatt = gatt;
      TestAction readTest = mTestActions.peek();
      byte[] value = characteristic.getValue();
      int actionType = readTest.actionType;
      if (readTest.expectedReturnCode != status) {
        fail("Incorrect status code: " + status + ". Expected: " + readTest.expectedReturnCode);
      } else if (actionType == TestAction.ASSERT_NOT_EQUALS
          && Arrays.equals(readTest.transmittedValue, value)) {
        fail("Values read are the same: " + Arrays.toString(value));
      } else if (actionType == TestAction.ASSERT
          && !Arrays.equals(readTest.transmittedValue, value)) {
        fail("Result not the same. Expected: " + Arrays.toString(readTest.transmittedValue)
              + ". But received: " + Arrays.toString(value));
      } else {
        mTestActions.remove();
        dispatch();
      }
    }


    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic,
        int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);
      Log.d(TAG, "On write");
      mGatt = gatt;
      TestAction writeTest = mTestActions.peek();
      if (writeTest.expectedReturnCode != status) {
        fail("Incorrect status code: " + status + ". Expected: " + writeTest.expectedReturnCode);
      } else {
        mTestActions.remove();
        dispatch();
      }
    }

  };
  private final TestCallback mTestCallback;
  private final LinkedList<TestAction> mTestActions;
  private final LinkedList<TestAction> mTestSteps;
  private final BluetoothAdapter mBluetoothAdapter;
  private final Handler mHandler;
  private boolean stopped;

  private TestHelper(
      String name, Context context, UUID serviceUuid,
      TestCallback testCallback, LinkedList<TestAction> testActions,
      LinkedList<TestAction> testSteps) {
    mName = name;
    mContext = context;
    mServiceUuid = serviceUuid;
    mTestCallback = testCallback;
    mTestActions = testActions;
    mTestSteps = testSteps;
    final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = bluetoothManager.getAdapter();
    mHandler = new Handler(Looper.myLooper());
    stopped = false;
  }

  public String getName() {
    return mName;
  }

  public LinkedList<TestAction> getTestSteps() {
    return mTestSteps;
  }

  public boolean isFailed() {
    return failed;
  }

  public boolean isStarted() {
    return started;
  }

  public void run(BluetoothDevice bluetoothDevice, BluetoothGatt gatt,
      BluetoothGattCallback outsideCallback) {
    Log.d(TAG, "Run Called for: " + getName());
    started = true;
    mBluetoothDevice = bluetoothDevice;
    mGatt = gatt;
    if (mGatt != null) {
      mService = gatt.getService(mServiceUuid);
    }
    mTestCallback.testStarted();
    mOutSideGattCallback = outsideCallback;
    dispatch();
  }

  private void connectToGatt() {
    Log.d(TAG, "Connecting");

    if (disconnected) {
      try {
        disconnected = false;
        // We have to wait before trying to connect
        // Else the connection is not successful
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    mTestCallback.waitingForConfigMode();
    // First time the device
    if (mBluetoothDevice == null) {
      scanForBeacon();
    } else {
      mBluetoothDevice.connectGatt(mContext, false, mOutSideGattCallback);
    }
  }

  private void readFromGatt() {
    Log.d(TAG, "reading");
    TestAction readTest = mTestActions.peek();
    BluetoothGattCharacteristic characteristic = mService
        .getCharacteristic(readTest.characteristicUuid);
    mGatt.readCharacteristic(characteristic);
  }

  private void writeToGatt() {
    Log.d(TAG, "Writing");
    TestAction writeTest = mTestActions.peek();
    BluetoothGattCharacteristic characteristic = mService
        .getCharacteristic(writeTest.characteristicUuid);
    // WriteType is WRITE_TYPE_NO_RESPONSE even though the one that requests a response
    // is called WRITE_TYPE_DEFAULT!
    if (characteristic.getWriteType() != BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
      Log.w(TAG, "writeCharacteristic default WriteType is being forced to WRITE_TYPE_DEFAULT");
      characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }
    characteristic.setValue(writeTest.transmittedValue);
    mGatt.writeCharacteristic(characteristic);
  }

  private void disconnectFromGatt() {
    Log.d(TAG, "Disconnecting");
    mGatt.disconnect();
  }

  private void dispatch() {
    Log.d(TAG, "Dispatching");
    int actionType = mTestActions.peek().actionType;
    // If the test is stopped and connected to the beacon
    // disconnect from the beacon
    if (stopped) {
      if (mGatt != null) {
        disconnectFromGatt();
      }
    } else if (actionType == TestAction.LAST) {
      finished = true;
      mTestCallback.testCompleted(mBluetoothDevice, mGatt);
    } else if (actionType == TestAction.CONNECT) {
      connectToGatt();
    } else if (actionType == TestAction.ASSERT || actionType == TestAction.ASSERT_NOT_EQUALS) {
      readFromGatt();
    } else if (actionType == TestAction.WRITE) {
      writeToGatt();
    } else if (actionType == TestAction.DISCONNECT) {
      disconnectFromGatt();
    } else if (actionType == TestAction.ADV_FLAGS) {
      lookForAdv();
    } else if (actionType == TestAction.ADV_TX_POWER) {
      lookForAdv();
    } else if (actionType == TestAction.ADV_URI) {
      lookForAdv();
    } else if (actionType == TestAction.ADV_PACKET) {
      lookForAdv();
    }
  }

  private void scanForBeacon() {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build();
    List<ScanFilter> filters = new ArrayList<>();

    ScanFilter filter = new ScanFilter.Builder()
        .setServiceUuid(ProtocolV2.CONFIG_SERVICE_UUID)
        .build();
    filters.add(filter);
    getLeScanner().startScan(filters, settings, mScanCallback);
    Log.d(TAG, "Looking for new beacons");
  }

  private void lookForAdv() {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build();
    List<ScanFilter> filters = new ArrayList<>();

    ScanFilter filter = new ScanFilter.Builder()
        .setDeviceAddress(mBluetoothDevice.getAddress())
        .setServiceUuid(UriBeacon.URI_SERVICE_UUID)
        .build();
    filters.add(filter);
    getLeScanner().startScan(filters, settings, mScanCallback);
    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        stopSearchingForBeacons();
        fail("Could not find adv packet");
      }
    }, SCAN_TIMEOUT);
  }

  private void stopSearchingForBeacons() {
    getLeScanner().stopScan(mScanCallback);
  }

  private void checkPacket(ScanResult result) {
    mHandler.removeCallbacksAndMessages(null);
    stopSearchingForBeacons();
    Log.d(TAG, "Found beacon");
    TestAction action = mTestActions.peek();
    if (action.actionType == TestAction.ADV_PACKET) {
      if (getAdvPacket(result).length < 2) {
        fail("Invalid Adv Packet");
      } else {
        mTestActions.remove();
        dispatch();
      }
    } else if (action.actionType == TestAction.ADV_FLAGS) {
      byte flags = getFlags(result);
      byte expectedFlags = action.transmittedValue[0];
      if (expectedFlags != flags) {
        fail("Received: " + flags + ". Expected: " + expectedFlags);
      } else {
        mTestActions.remove();
        dispatch();
      }
    } else if (action.actionType == TestAction.ADV_TX_POWER) {
      byte txPowerLevel = getTxPowerLevel(result);
      byte expectedTxPowerLevel = action.transmittedValue[0];
      if (expectedTxPowerLevel != txPowerLevel) {
        fail("Received: " + txPowerLevel + ". Expected: " + expectedTxPowerLevel);
      } else {
        mTestActions.remove();
        dispatch();
      }
    } else if (action.actionType == TestAction.ADV_URI) {
      byte[] uri = getUri(result);
      if (!Arrays.equals(action.transmittedValue, uri)) {
        fail("Received: " + Arrays.toString(uri)
            + ". Expected: " + Arrays.toString(action.transmittedValue));
      } else {
        mTestActions.remove();
        dispatch();
      }
    }
  }

  private BluetoothLeScanner getLeScanner() {
    return mBluetoothAdapter.getBluetoothLeScanner();
  }

  private byte getFlags(ScanResult result) {
    byte[] serviceData = result.getScanRecord().getServiceData(UriBeacon.URI_SERVICE_UUID);
    return serviceData[0];
  }

  private byte getTxPowerLevel(ScanResult result) {
    byte[] serviceData = result.getScanRecord().getServiceData(UriBeacon.URI_SERVICE_UUID);
    return serviceData[1];
  }

  private byte[] getUri(ScanResult result) {
    byte[] serviceData = result.getScanRecord().getServiceData(UriBeacon.URI_SERVICE_UUID);
    return Arrays.copyOfRange(serviceData, 2, serviceData.length);
  }

  private byte[] getAdvPacket(ScanResult result) {
    return result.getScanRecord().getServiceData(UriBeacon.URI_SERVICE_UUID);
  }

  private void fail(String reason) {
    Log.d(TAG, "Failing because: " + reason);
    failed = true;
    mTestActions.peek().failed = true;
    mTestActions.peek().reason = reason;
    finished = true;
    mTestCallback.testCompleted(mBluetoothDevice, mGatt);
  }

  public boolean isFinished() {
    return finished;
  }

  public void stopTest() {
    stopped = true;
    stopSearchingForBeacons();
    fail("Stopped by user");
  }

  public interface TestCallback {

    public void testStarted();

    public void testCompleted(BluetoothDevice deviceBeingTested, BluetoothGatt gatt);

    public void waitingForConfigMode();

    public void connectedToBeacon();
  }

  public static class Builder {

    private String mName;
    private Context mContext;
    private UUID mServiceUuid;
    private TestCallback mTestCallback;
    private final LinkedList<TestAction> mTestActions = new LinkedList<>();

    public Builder name(String s) {
      mName = s;
      return this;
    }

    public Builder setUp(Context context, ParcelUuid serviceUuid,
        TestCallback testCallback) {
      mContext = context;
      mServiceUuid = serviceUuid.getUuid();
      mTestCallback = testCallback;
      return this;
    }

    public Builder connect() {
      mTestActions.add(new TestAction(TestAction.CONNECT));
      return this;
    }

    public Builder write(UUID characteristicUuid, byte[] value, int expectedReturnCode) {
      mTestActions.add(
          new TestAction(TestAction.WRITE, characteristicUuid, expectedReturnCode, value));
      return this;
    }

    public Builder disconnect() {
      mTestActions.add(new TestAction(TestAction.DISCONNECT));
      return this;
    }

    public Builder assertEquals(UUID characteristicUuid, byte[] expectedValue,
        int expectedReturnCode) {
      mTestActions.add(
          new TestAction(TestAction.ASSERT, characteristicUuid, expectedReturnCode, expectedValue));
      return this;
    }

    public Builder assertNotEquals(UUID characteristicUuid, byte[] expectedValue,
        int expectedReturnCode) {
      mTestActions.add(
          new TestAction(TestAction.ASSERT_NOT_EQUALS, characteristicUuid, expectedReturnCode, expectedValue));
      return this;
    }

    public Builder assertAdvFlags(byte expectedValue) {

      mTestActions.add(new TestAction(TestAction.ADV_FLAGS, new byte[]{expectedValue}));
      return this;
    }

    public Builder assertAdvTxPower(byte expectedValue) {
      mTestActions.add(new TestAction(TestAction.ADV_TX_POWER, new byte[]{expectedValue}));
      return this;
    }

    public Builder assertAdvUri(byte[] expectedValue) {
      mTestActions.add(new TestAction(TestAction.ADV_URI, expectedValue));
      return this;
    }

    public Builder checkAdvPacket() {
      mTestActions.add(new TestAction(TestAction.ADV_PACKET));
      return this;
    }

    public Builder insertActions(Builder builder) {
      for (TestAction action : builder.mTestActions) {
        mTestActions.add(action);
      }
      return this;
    }

    public Builder writeAndRead(UUID characteristicUuid, byte[][] values) {
      for (byte[] value : values) {
        writeAndRead(characteristicUuid, value);
      }
      return this;
    }

    public Builder writeAndRead(UUID characteristicUuid, byte[] value) {
      mTestActions.add(
          new TestAction(TestAction.WRITE, characteristicUuid, BluetoothGatt.GATT_SUCCESS,
              value));
      mTestActions.add(
          new TestAction(TestAction.ASSERT, characteristicUuid, BluetoothGatt.GATT_SUCCESS,
              value));
      return this;
    }

    public TestHelper build() {
      mTestActions.add(new TestAction(TestAction.LAST));
      // Keep a copy of the steps to show in the UI
      LinkedList<TestAction> testSteps = new LinkedList<>(mTestActions);
      return new TestHelper(mName, mContext, mServiceUuid, mTestCallback,
          mTestActions, testSteps);
    }
  }
}

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
import android.os.ParcelUuid;
import android.util.Log;

import org.uribeacon.beacon.UriBeacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TestHelper {

  private static final String TAG = TestHelper.class.getCanonicalName();

  private boolean started = false;
  private boolean failed = false;
  private boolean finished = false;
  private BluetoothGattService mService;
  private String mName;
  private Context mContext;
  private BluetoothDevice mBluetoothDevice;
  private UUID mServiceUuid;
  private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      super.onConnectionStateChange(gatt, status, newState);
      Log.d(TAG, "Status: " + status + "; New State: " + newState);
      if (status == BluetoothGatt.GATT_SUCCESS) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
          gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
          if (!failed) {
            mTestActions.remove();
            dispatch(gatt);
          }
        }
      } else {
        fail(gatt, "Failed");
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      super.onServicesDiscovered(gatt, status);
      mService = gatt.getService(mServiceUuid);
      mTestActions.remove();
      mTestCallback.connectedToBeacon();
      dispatch(gatt);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        int status) {
      super.onCharacteristicRead(gatt, characteristic, status);
      TestAction readTest = mTestActions.peek();
      if (readTest.expectedReturnCode != status) {
        fail(gatt, "Incorrect status code");
      } else if (!Arrays.equals(readTest.transmittedValue, characteristic.getValue())) {
        if (readTest.transmittedValue.length != characteristic.getValue().length) {
          fail(gatt, "Wrong length. Expected: " + new String(readTest.transmittedValue) + ". But received: " + new String(characteristic.getValue()));
        } else {
          fail(gatt, "Result not the same. Expected: " + new String(readTest.transmittedValue) + ". But received: " + new String(characteristic.getValue()));
        }
      } else {
        mTestActions.remove();
        dispatch(gatt);
      }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic,
        int status) {
      super.onCharacteristicWrite(gatt, characteristic, status);
      TestAction writeTest = mTestActions.peek();
      if (writeTest.expectedReturnCode != status) {
        fail(gatt, "Incorrect status code: " + status + ". Expected: " + writeTest.expectedReturnCode);
      } else {
        mTestActions.remove();
        dispatch(gatt);
      }
    }

  };
  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      stopSearchingForBeacons();
      Log.d(TAG, "Found beacon");
      TestAction action = mTestActions.peek();

      if (action.actionType == TestAction.ADV_FLAGS) {
        byte flags = getFlags(result);
        byte expectedFlags = action.transmittedValue[0];
        if (expectedFlags != flags) {
          fail(null, "Received: " + flags + ". Expected: " + expectedFlags);
        } else {
          mTestActions.remove();
          dispatch(null);
        }
      } else if (action.actionType == TestAction.ADV_TX_POWER) {
        byte txPowerLevel = getTxPowerLevel(result);
        byte expectedTxPowerLevel = action.transmittedValue[0];
        if (expectedTxPowerLevel != txPowerLevel) {
          fail(null, "Received: " + txPowerLevel + ". Expected: " + expectedTxPowerLevel);
        } else {
          mTestActions.remove();
          dispatch(null);
        }
      } else if (action.actionType == TestAction.ADV_URI) {
        byte[] uri = getUri(result);
        if (!Arrays.equals(action.transmittedValue, uri)) {
          fail(null, "Received: " + Arrays.toString(uri)
              + ". Expected: " + Arrays.toString(action.transmittedValue));
        } else {
          mTestActions.remove();
          dispatch(null);
        }
      }
    }
  };
  private TestCallback mTestCallback;
  private LinkedList<TestAction> mTestActions;
  private LinkedList<TestAction> mTestSteps;
  private BluetoothAdapter mBluetoothAdapter;

  private TestHelper(
      String name, Context context, BluetoothDevice bluetoothDevice, UUID serviceUuid,
      TestCallback testCallback, LinkedList<TestAction> testActions,
      LinkedList<TestAction> testSteps) {
    mName = name;
    mContext = context;
    mBluetoothDevice = bluetoothDevice;
    mServiceUuid = serviceUuid;
    mTestCallback = testCallback;
    mTestActions = testActions;
    mTestSteps = testSteps;
    final BluetoothManager bluetoothManager =
        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = bluetoothManager.getAdapter();
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

  public void run() {
    Log.d(TAG, "Run Called");
    started = true;
    mTestCallback.testStarted();
    dispatch(null);
  }

  private void connectToGatt(BluetoothGatt gatt) {
    Log.d(TAG, "Connecting");
    // Gatt == null is only passed on the first connection
    if (gatt != null) {
      try {
        // We have to wait before trying to connect
        // Else the connection is not successful
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    mTestCallback.waitingForConfigMode();
    mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
  }

  private void readFromGatt(BluetoothGatt gatt) {
    Log.d(TAG, "reading");
    TestAction readTest = mTestActions.peek();
    BluetoothGattCharacteristic characteristic = mService
        .getCharacteristic(readTest.characteristicUuid);
    gatt.readCharacteristic(characteristic);
  }

  private void writeToGatt(BluetoothGatt gatt) {
    Log.d(TAG, "Writting");
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
    gatt.writeCharacteristic(characteristic);
  }

  private void disconnectFromGatt(BluetoothGatt gatt) {
    Log.d(TAG, "Disconnecting");
    gatt.disconnect();
  }

  private void dispatch(BluetoothGatt gatt) {
    Log.d(TAG, "Dispatching");
    if (mTestActions.peek().actionType == TestAction.LAST) {
      finished = true;
      mTestCallback.testCompleted();
    } else if (mTestActions.peek().actionType == TestAction.CONNECT) {
      connectToGatt(gatt);
    } else if (mTestActions.peek().actionType == TestAction.ASSERT) {
      readFromGatt(gatt);
    } else if (mTestActions.peek().actionType == TestAction.WRITE) {
      writeToGatt(gatt);
    } else if (mTestActions.peek().actionType == TestAction.DISCONNECT) {
      disconnectFromGatt(gatt);
    } else if (mTestActions.peek().actionType == TestAction.ADV_FLAGS) {
      lookForAdv();
    } else if (mTestActions.peek().actionType == TestAction.ADV_TX_POWER) {
      lookForAdv();
    } else if (mTestActions.peek().actionType == TestAction.ADV_URI) {
      lookForAdv();
    }
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
    Log.d(TAG, "Started scanning #2");
  }

  private void stopSearchingForBeacons() {
    getLeScanner().stopScan(mScanCallback);
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
  private void fail(BluetoothGatt gatt, String reason) {
    failed = true;
    mTestActions.peek().failed = true;
    mTestActions.peek().reason = reason;
    finished = true;
    mTestCallback.testCompleted();
    if (gatt != null) {
      disconnectFromGatt(gatt);
    }
  }

  public boolean isFinished() {
    return finished;
  }

  public interface TestCallback {
    public void testStarted();
    public void testCompleted();
    public void waitingForConfigMode();
    public void connectedToBeacon();
  }

  public static class Builder {

    private String mName;
    private Context mContext;
    private BluetoothDevice mBluetoothDevice;
    private UUID mServiceUuid;
    private TestCallback mTestCallback;
    private LinkedList<TestAction> mTestActions = new LinkedList<>();

    public Builder name(String s) {
      mName = s;
      return this;
    }

    public Builder setUp(Context context, BluetoothDevice bluetoothDevice, ParcelUuid serviceUuid,
        TestCallback testCallback) {
      mContext = context;
      mBluetoothDevice = bluetoothDevice;
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

    public TestHelper build() {
      mTestActions.add(new TestAction(TestAction.LAST));
      // Keep a copy of the steps to show in the UI
      LinkedList<TestAction> testSteps = new LinkedList<>(mTestActions);
      return new TestHelper(mName, mContext, mBluetoothDevice, mServiceUuid, mTestCallback,
          mTestActions, testSteps);
    }
  }
}

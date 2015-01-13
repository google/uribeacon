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
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
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
  private TestCallback mTestCallback;
  private LinkedList<TestAction> mTestActions;
  private LinkedList<TestAction> mTestSteps;


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
    }
  }

  private void fail(BluetoothGatt gatt, String reason) {
    failed = true;
    mTestActions.peek().failed = true;
    mTestActions.peek().reason = reason;
    finished = true;
    mTestCallback.testCompleted();
    disconnectFromGatt(gatt);
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

    public TestHelper build() {
      mTestActions.add(new TestAction(TestAction.LAST));
      // Keep a copy of the steps to show in the UI
      LinkedList<TestAction> testSteps = new LinkedList<>(mTestActions);
      return new TestHelper(mName, mContext, mBluetoothDevice, mServiceUuid, mTestCallback,
          mTestActions, testSteps);
    }
  }
}

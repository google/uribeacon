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

package org.uribeacon.config;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Bluetooth GATT Request Queue.
 * <p/>
 * <p>This class provides a Bluetooth GATT request queue to enable communication
 * with Bluetooth Smart or Smart Ready devices.
 * <p/>
 * The Android BLE stack only allows one active request at a time. If you issue concurrent
 * commands you will see the log entry: "E/bt-btif﹕ already has a pending command!!"
 * <p>
 * See <a href="https://code.google.com/p/android/issues/detail?id=58381">Issue 58381</a>.
 */

public class GattRequestQueue {
  private final String TAG = "GattRequestQueue";
  private final Queue<Request> mQueue = new LinkedList<Request>();
  private BluetoothGattCallback mGattCallback;

  /**
   * Queue of pending requests.
   */
  public GattRequestQueue() {
  }

  public void add(BluetoothGatt gatt, RequestType type, BluetoothGattDescriptor descriptor) {
    Request request = new Request(type, descriptor);
    add(gatt, request);
  }

  public void add(BluetoothGatt gatt, RequestType type, BluetoothGattCharacteristic characteristic) {
    Request request = new Request(type, characteristic);
    add(gatt, request);
  }

  synchronized private void add(BluetoothGatt gatt, Request request) {
    mQueue.add(request);
    if (mQueue.size() == 1) {
      mQueue.peek().start(gatt);
    }
  }

  /**
   * Create a wrapper object that calls the BluetoothGatt callbacks on the main UI Thread.
   *
   * @param callback the callback
   * @return the wrapper callback object
   */
  public BluetoothGattCallback newGattCallbackOnUiThread(BluetoothGattCallback callback) {
    return new GattCallbackOnUiThread(callback);
  }

  /**
   * Process the next request in the queue for a BluetoothGatt function (such as characteristic read).
   */
  synchronized private void processNext(BluetoothGatt bluetoothGatt) {
    // The currently executing request is kept on the head of the queue until this is called.
    if (mQueue.isEmpty())
      throw new RuntimeException("No active request in processNext()");
    mQueue.remove();
    if (!mQueue.isEmpty()) {
      mQueue.peek().start(bluetoothGatt);
    }
  }

  /**
   * BluetoothGatt request types.
   */
  public enum RequestType {
    //  CHARACTERISTIC_NOTIFICATION,
    READ_CHARACTERISTIC,
    READ_DESCRIPTOR,
    //  READ_RSSI,
    WRITE_CHARACTERISTIC,
    WRITE_DESCRIPTOR
  }

  /**
   * The object that holds a Gatt request while in the queue.
   * <br>
   * This object holds the parameters for calling BluetoothGatt methods (see start());
   */
  public class Request {
    final RequestType requestType;
    BluetoothGattCharacteristic characteristic;
    BluetoothGattDescriptor descriptor;

    public Request(RequestType requestType, BluetoothGattCharacteristic characteristic) {
      this.requestType = requestType;
      this.characteristic = characteristic;
    }

    public Request(RequestType requestType, BluetoothGattDescriptor descriptor) {
      this.requestType = requestType;
      this.descriptor = descriptor;
    }

    public void start(BluetoothGatt bluetoothGatt) {
      switch (requestType) {
        case READ_CHARACTERISTIC:
          if (!bluetoothGatt.readCharacteristic(characteristic)) {
            throw new IllegalArgumentException("Characteristic is not valid");
          }
          break;
        case READ_DESCRIPTOR:
          if (!bluetoothGatt.readDescriptor(descriptor)) {
            throw new IllegalArgumentException("Descriptor is not valid");
          }
          break;
        case WRITE_CHARACTERISTIC:
          if (!bluetoothGatt.writeCharacteristic(characteristic)) {
            throw new IllegalArgumentException("Characteristic is not valid");
          }
          break;
        case WRITE_DESCRIPTOR:
          if (!bluetoothGatt.writeDescriptor(descriptor)) {
            throw new IllegalArgumentException("Characteristic is not valid");
          }
          break;
      }
    }
  }

  /**
   * This class is used to handle BluetoothGatt callbacks occurring on the RPC Binder thread and
   * simply passes control to client's BluetoothGattCallback on the main UI thread.
   */
  private class GattCallbackOnUiThread extends BluetoothGattCallback {
    private final BluetoothGattCallback mUiThreadGattCallback;
    private final Handler mHandler;

    public GattCallbackOnUiThread(BluetoothGattCallback uiThreadCallback) {
      mUiThreadGattCallback = uiThreadCallback;
      // Defines a Handler object that's attached to the UI thread.
      mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mUiThreadGattCallback.onConnectionStateChange(gatt, status, newState);
        }
      });
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
      throw new RuntimeException("onReliableWriteCompleted not yet implemented");
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
      throw new RuntimeException("onDescriptorWrite not yet implemented");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
      throw new RuntimeException("onReliableWriteCompleted not yet implemented");
    }

    @Override
    public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mUiThreadGattCallback.onDescriptorRead(gatt, descriptor, status);
          processNext(gatt);
        }
      });
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
      throw new RuntimeException("onDescriptorWrite not yet implemented");
    }

    @Override
    public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {

      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mUiThreadGattCallback.onCharacteristicRead(gatt, characteristic, status);
          processNext(gatt);
        }
      });
    }

    @Override
    public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {

      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mUiThreadGattCallback.onCharacteristicWrite(gatt, characteristic, status);
          processNext(gatt);
        }
      });

    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mUiThreadGattCallback.onServicesDiscovered(gatt, status);
        }
      });
    }
  }
}


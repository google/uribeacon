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
import android.util.Log;

import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class TestRunner {
  private static final String TAG = TestRunner.class.getCanonicalName();

  private boolean mPause;
  private boolean mFailed = false;
  private ArrayList<TestHelper> mUriBeaconTests;
  private TestHelper mLatestTest;
  private ListIterator<TestHelper> mTestIterator;
  private DataCallback mDataCallback;
  private TestCallback mTestCallback = new TestCallback() {
    @Override
    public void testStarted() {
      mDataCallback.dataUpdated();
    }

    @Override
    public void testCompleted() {
      Log.d(TAG, "Test Completed. Failed: " + mLatestTest.isFailed());
      if (mLatestTest.isFailed()) {
        mFailed = true;
      }
      mDataCallback.dataUpdated();
      if (!mPause) {
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        start();
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
  };

  public TestRunner(Context context, BluetoothDevice bluetoothDevice, DataCallback dataCallback) {
    mDataCallback = dataCallback;
    mUriBeaconTests = UriBeaconTests.initializeTests(context, bluetoothDevice, mTestCallback);
    mTestIterator = mUriBeaconTests.listIterator();
  }

  public void start() {
    Log.d(TAG, "Starting tests");
    mPause = false;
    if (mTestIterator.hasNext()) {
      mLatestTest = mTestIterator.next();
      mLatestTest.run();
    } else {
      mDataCallback.testsCompleted(mFailed);
    }
  }

  public ArrayList<TestHelper> getUriBeaconTests() {
    return mUriBeaconTests;
  }

  public void pause() {
    mPause = true;
  }

  public interface DataCallback {
    public void dataUpdated();
    public void waitingForConfigMode();
    public void connectedToBeacon();
    public void testsCompleted(boolean failed);
  }
}

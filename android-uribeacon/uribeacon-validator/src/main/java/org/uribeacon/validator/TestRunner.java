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

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import org.uribeacon.validator.TestHelper.TestCallback;

import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class TestRunner {
  private static final String TAG = TestRunner.class.getCanonicalName();

  private boolean mPause;
  private boolean mFailed = false;
  private UriBeaconTests mUriBeaconTests;
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
      Log.d(TAG, "Test Completed");
      Log.d(TAG, "TEST RESULT: " + mLatestTest.isFailed());
      if (mLatestTest.isFailed()) {
        mFailed = true;
        Log.d(TAG, "TEST FAILED");
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

  public TestRunner(Context context, ScanResult result, DataCallback dataCallback) {
    mDataCallback = dataCallback;
    mUriBeaconTests = new UriBeaconTests(context, result, mTestCallback);
    mTestIterator = mUriBeaconTests.iterator();
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

  public UriBeaconTests getUriBeaconTests() {
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

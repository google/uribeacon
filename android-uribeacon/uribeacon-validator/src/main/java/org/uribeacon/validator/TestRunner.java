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

import android.util.Log;

import org.uribeacon.validator.UriBeaconTestHelper.TestCallback;

import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class TestRunner {
  private static final String TAG = TestRunner.class.getCanonicalName();

  private Tests mTests;
  private boolean mPause;
  private UriBeaconTestHelper mLatestTest;
  private ListIterator<UriBeaconTestHelper> mTestIterator;
  private DataCallback mDataCallback;
  private TestCallback mTestCallback = new TestCallback() {
    @Override
    public void testStarted() {
      mDataCallback.dataUpdated();
    }

    @Override
    public void testCompleted() {
      Log.d(TAG, "Test Completed");
      mDataCallback.dataUpdated();
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      start();
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
  public TestRunner(Tests tests, DataCallback dataCallback) {
    mTests = tests;
    mDataCallback = dataCallback;
    tests.setCallback(mTestCallback);
    mTestIterator = tests.iterator();
  }

  public void start() {
    Log.d(TAG, "Starting tests");
    mPause = false;
    if (mTestIterator.hasNext()) {
      mLatestTest = mTestIterator.next();
      mLatestTest.run();
    } else {
      // done
    }
  }

  public void pause() {
    mPause = true;
  }

  public interface DataCallback {
    public void dataUpdated();
    void waitingForConfigMode();
    void connectedToBeacon();
  }
}

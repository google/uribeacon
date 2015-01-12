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

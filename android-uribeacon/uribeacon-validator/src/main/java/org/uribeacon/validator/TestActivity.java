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

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.uribeacon.validator.TestRunner.DataCallback;

import java.util.ArrayList;


public class TestActivity extends Activity {

  public static final String OPTIONAL_IMPLEMENTED = "optional.implemented";
  private static final String TAG = TestActivity.class.getCanonicalName();
  private TestRunner mTestRunner;
  private ArrayList<TestHelper> mUriBeaconTests;
  private DataCallback mDataCallback = new DataCallback() {
    ProgressDialog progress;

    @Override
    public void dataUpdated() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (progress != null) {
            progress.dismiss();
          }
          mAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void waitingForConfigMode() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          progress = new ProgressDialog(TestActivity.this);
          progress.setMessage("Press Button on UriBeacon");
          progress.show();
          progress.setCancelable(false);
          progress.setCanceledOnTouchOutside(false);
        }
      });
    }

    @Override
    public void connectedToBeacon() {
      progress.dismiss();
    }

    @Override
    public void testsCompleted(final boolean failed) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          int message;
          if (failed) {
            message = R.string.test_failed;
          } else {
            message = R.string.test_success;
          }
          Toast.makeText(TestActivity.this, message, Toast.LENGTH_SHORT).show();
        }
      });
    }
  };
  // Recycle view variables
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    BluetoothDevice bluetoothDevice = getIntent().getExtras()
        .getParcelable(BluetoothDevice.class.getCanonicalName());
    boolean optionalImplemented = getIntent().getExtras().getBoolean(OPTIONAL_IMPLEMENTED);
    String testType = getIntent().getStringExtra(MainActivity.TEST_TYPE);
    mTestRunner = new TestRunner(this, bluetoothDevice, mDataCallback, testType, optionalImplemented);
    mUriBeaconTests = mTestRunner.getUriBeaconTests();
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_tests);
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new TestsAdapter(mUriBeaconTests);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mTestRunner.start();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mTestRunner.pause();
  }

}


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
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.uribeacon.validator.TestRunner.DataCallback;


public class TestActivity extends Activity {
  private static final String TAG = TestActivity.class.getCanonicalName();

  private TestRunner mTestRunner;
  private Tests mTests;
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
        }
      });
    }

    @Override
    public void connectedToBeacon() {
      progress.dismiss();
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
    ScanResult result = getIntent().getExtras().getParcelable(ScanResult.class.getCanonicalName());
    mTests = new Tests(this, result);
    mTestRunner = new TestRunner(mTests, mDataCallback);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_tests);
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new TestsAdapter(mTests);
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


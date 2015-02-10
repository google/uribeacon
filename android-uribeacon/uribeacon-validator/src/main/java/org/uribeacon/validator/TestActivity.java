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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import org.uribeacon.validator.TestRunner.DataCallback;
import org.uribeacon.validator.TestsAdapter.AdapterCallback;

import java.util.ArrayList;
import java.util.Arrays;


public class TestActivity extends Activity {

  private static final String TAG = TestActivity.class.getCanonicalName();
  private TestRunner mTestRunner;
  private final DataCallback mDataCallback = new DataCallback() {
    ProgressDialog progress;

    @Override
    public void dataUpdated() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (progress != null) {
            progress.dismiss();
            progress = null;
          }
          mAdapter.notifyDataSetChanged();
          setShareIntent();
        }
      });
    }

    @Override
    public void waitingForConfigMode() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (progress == null) {
            progress = new ProgressDialog(TestActivity.this);
            progress.setMessage(getString(R.string.put_beacon_in_config_mode));
            progress.show();
            progress.setCanceledOnTouchOutside(false);
            progress.setOnCancelListener(new OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialog) {
                mTestRunner.stop();
              }
            });
          }
        }
      });
    }

    @Override
    public void connectedToBeacon() {
      progress.dismiss();
      progress = null;
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

    @Override
    public void multipleConfigModeBeacons(final ArrayList<ScanResult> scanResults) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          progress.dismiss();
          progress = null;
          showCustomDialog(scanResults);
        }
      });
    }
  };
  private RecyclerView.Adapter mAdapter;
  private final AdapterCallback mAdapterCallback = new AdapterCallback() {
    @Override
    public void restart(int testPosition) {
      mTestRunner.restart(testPosition);
    }
  };
  private ShareActionProvider mShareActionProvider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    boolean optionalImplemented = getIntent().getBooleanExtra(MainActivity.LOCK_IMPLEMENTED, false);
    String testType = getIntent().getStringExtra(MainActivity.TEST_TYPE);
    mTestRunner = new TestRunner(this, mDataCallback, testType, optionalImplemented);
    ArrayList<TestHelper> mUriBeaconTests = mTestRunner.getUriBeaconTests();
    RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_tests);
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new TestsAdapter(mUriBeaconTests, mAdapterCallback);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.tests_activity_actions, menu);

    MenuItem item = menu.findItem(R.id.menu_item_share);

    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
    setShareIntent();
    return true;
  }

  private void setShareIntent() {
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.setType("message/rfc822");
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "UriBeacon Test Results");
    Spanned body = createTestResults();
    shareIntent.putExtra(Intent.EXTRA_TEXT, body);
    if (mShareActionProvider != null) {
      mShareActionProvider.setShareIntent(shareIntent);
    }
  }

  private Spanned createTestResults() {
    String results = "<h1>UriBeacon Results</h1>\n";
    for (TestHelper test : mTestRunner.getUriBeaconTests()) {
      results += "<h3>" + test.getName() + "</h3>";
      results += "<p>Status: ";
      if (!test.isStarted()) {
        results += "Didn't run</p>";
      } else if (!test.isFinished()) {
        results += "Running</p>";
      } else if (!test.isFailed()) {
        results += "<font color=\"#4CAF50\">Success</font></p>";
      } else {
        results += "<font color=\"#F44336\">Failed</font></p>";
        if (!test.getReference().isEmpty()) {
          results += "<p><a href=\"" + test.getReference() + "\">Reference</p>";
        }
        results += getReasonTestFailed(test);
      }
    }
    return Html.fromHtml(results);
  }

  private String getReasonTestFailed(TestHelper test) {
    String steps = "<h6>Steps to reproduce:</h6>";
    for (int i = 0; i < test.getTestSteps().size(); i++) {
      TestAction action = test.getTestSteps().get(i);
      if (action.actionType == TestAction.LAST) {
        continue;
      }
      steps += "<p>" + (i + 1) + ". ";
      if (action.failed) {
        steps += "<font color=\"#F44336\">";
      }
      switch (action.actionType) {
        case TestAction.CONNECT:
          steps += "Connect";
          break;
        case TestAction.WRITE:
          steps += "Write: " + Arrays.toString(action.transmittedValue);
          break;
        case TestAction.ASSERT:
          steps += "Assert: " + Arrays.toString(action.transmittedValue);
          break;
        case TestAction.DISCONNECT:
          steps += "Disconnect</p>";
          break;
        case TestAction.ADV_FLAGS:
          steps += "Read Adv Flags: " + Arrays.toString(action.transmittedValue);
          break;
        case TestAction.ADV_TX_POWER:
          steps += "Read Adv Tx Power: " + Arrays.toString(action.transmittedValue);
          break;
        case TestAction.ADV_URI:
          steps += "Read Adv URI: " + Arrays.toString(action.transmittedValue);
          break;
        case TestAction.ADV_PACKET:
          steps += "Scan Adv Packet";
          break;
      }
      if (action.failed) {
        steps += "</font></p><p><font color=\"#F44336\">Reason: " + action.reason + "</font></p>";
      } else {
        steps += "</p>";
      }
    }
    return steps;
  }


  @Override
  protected void onResume() {
    super.onResume();
    mTestRunner.start(null, null);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTestRunner.stop();
  }
  private void showCustomDialog(ArrayList<ScanResult> scanResults) {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(TestActivity.this)
        .setNegativeButton(R.string.cancel, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mTestRunner.stop();
            dialog.dismiss();
          }
        })
        .setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            mTestRunner.stop();
          }
        });
    LayoutInflater inflater = getLayoutInflater();
    View convertView = inflater.inflate(R.layout.multiple_beacon_dialog, null);
    alertDialog.setTitle(R.string.title_multiple_beacons);
    alertDialog.setView(convertView);
    AlertDialog dialog= alertDialog.create();
    ListView lv = (ListView) convertView.findViewById(R.id.multipleBeacons_listView);
    lv.setAdapter(new MultipleBeaconsAdapter(TestActivity.this, scanResults, mTestRunner, dialog));
    dialog.show();
  }
}


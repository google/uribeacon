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


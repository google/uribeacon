package org.uribeacon.validator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.uribeacon.validator.TestTypesAdapter.StartTestType;


public class MainActivity extends Activity {

  public static final String TEST_TYPE = "MainActivity.TestType";
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private StartTestType mStartTestType = new StartTestType() {
    @Override
    public void startTestType(String type) {
      Intent intent = new Intent(MainActivity.this, ScanActivity.class);
      intent.putExtra(MainActivity.TEST_TYPE, type);
      startActivity(intent);
    }
  };
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_types);

    mRecyclerView.setHasFixedSize(true);

    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new TestTypesAdapter(getTestsInfo(), mStartTestType);
    mRecyclerView.setAdapter(mAdapter);

  }

  private TestInfo[] getTestsInfo() {
    return new TestInfo[]{
        new TestInfo(BasicUriBeaconTests.TEST_NAME, BasicUriBeaconTests.class.getName()),
        new TestInfo(SpecUriBeaconTests.TEST_NAME, SpecUriBeaconTests.class.getName())
    };
  }

  public class TestInfo {
    public String testName;
    public String className;
    public TestInfo(String testName, String className) {
      this.testName = testName;
      this.className = className;
    }
  }

}

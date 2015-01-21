package org.uribeacon.validator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;

import org.uribeacon.validator.TestTypesAdapter.StartTestType;


public class MainActivity extends Activity {

  public static final String TEST_TYPE = "MainActivity.TestType";
  public static final String LOCK_IMPLEMENTED = "MainActivity.LockImplemented";
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private boolean lockImplemented = false;

  private StartTestType mStartTestType = new StartTestType() {
    @Override
    public void startTestType(String type) {
      Intent intent = new Intent(MainActivity.this, TestActivity.class);
      intent.putExtra(MainActivity.TEST_TYPE, type);
      intent.putExtra(MainActivity.LOCK_IMPLEMENTED, lockImplemented);
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_activity_actions, menu);
    // Set listener for switch
    MenuItem toggleLockImplemented = menu.findItem(R.id.action_lock);
    View switchLockImplemented = toggleLockImplemented.getActionView();
    Switch s = (Switch) switchLockImplemented.findViewById(R.id.switch_lock_implemented);
    s.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        lockImplemented = ((Switch) v).isChecked();
      }
    });
    return super.onCreateOptionsMenu(menu);
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

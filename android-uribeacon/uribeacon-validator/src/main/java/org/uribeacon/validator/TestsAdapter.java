package org.uribeacon.validator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.ViewHolder>{
  private ArrayList<UriBeaconTestHelper> mDataset;
  private static final String TAG = TestsAdapter.class.getCanonicalName();
  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class ViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public TextView mTestName;
    public TextView mTestResult;
    public ImageView mImageView;

    public ViewHolder(View v) {
      super(v);
      mTestName = (TextView) v.findViewById(R.id.test_name);
      mTestResult = (TextView) v.findViewById(R.id.test_reason);
      mImageView = (ImageView) v.findViewById(R.id.imageView_testIcon);

    }
  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public TestsAdapter(Tests tests){
    mDataset = tests.tests;
  }
  // Create new views (invoked by the layout manager)
  @Override
  public TestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
      int viewType) {
    // create a new view
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.test_view, parent, false);
    // set <></>he view's size, margins, paddings and layout parameters
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    UriBeaconTestHelper test = mDataset.get(position);
    setIcon(holder, test);
    setErrorMessage(holder, test);
    holder.mTestName.setText(test.getName());
  }

  private void setErrorMessage(ViewHolder holder, UriBeaconTestHelper test) {
    if (test.isFailed()) {
      for (int i = 0; i < test.getTestSteps().size(); i++) {
        TestAction action = test.getTestSteps().get(i);
        if (action.failed) {
          holder.mTestResult.setText("#" + i + 1 + ". " + action.reason);
          holder.mTestResult.setVisibility(View.VISIBLE);
          break;
        }
      }
    }
  }

  private void setIcon(ViewHolder holder, UriBeaconTestHelper test) {
    if (test.isStarted()) {
      holder.mImageView.setImageResource(R.drawable.executing);
    } else {
      holder.mImageView.setImageResource(R.drawable.not_started);
    }
    if (test.isFinished()) {
      if (test.isFailed()) {
        holder.mImageView.setImageResource(R.drawable.failed);
        int testIndex = 1;
        for (TestAction action : test.getTestSteps()) {
          if (action.failed) {
            holder.mTestResult.setText("#" + testIndex + ". " + action.reason);
            holder.mTestResult.setVisibility(View.VISIBLE);
            break;
          } else {
            testIndex++;
          }
        }
      } else {
        holder.mImageView.setImageResource(R.drawable.success);
      }
    }
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return mDataset.size();
  }
}

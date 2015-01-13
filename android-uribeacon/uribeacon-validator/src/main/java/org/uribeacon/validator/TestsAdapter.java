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

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.ViewHolder>{
  private ArrayList<TestHelper> mDataset;
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
  public TestsAdapter(UriBeaconTests uriBeaconTests){
    mDataset = uriBeaconTests.tests;
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
    TestHelper test = mDataset.get(position);
    setIcon(holder, test);
    setErrorMessage(holder, test);
    holder.mTestName.setText(test.getName());
  }

  private void setErrorMessage(ViewHolder holder, TestHelper test) {
    if (test.isFailed()) {
      for (int i = 0; i < test.getTestSteps().size(); i++) {
        TestAction action = test.getTestSteps().get(i);
        Log.d(TAG, "Type: " + action.actionType + " Number: " + i);
        if (action.failed) {
          Log.d(TAG, "Type: " + action.actionType + " Number: " + i);
          holder.mTestResult.setText("#" + (i + 1) + ". " + action.reason);
          holder.mTestResult.setVisibility(View.VISIBLE);
          break;
        }
      }
    }
  }

  private void setIcon(ViewHolder holder, TestHelper test) {
    if (test.isStarted()) {
      holder.mImageView.setImageResource(R.drawable.executing);
    } else {
      holder.mImageView.setImageResource(R.drawable.not_started);
    }
    if (test.isFinished()) {
      if (test.isFailed()) {
        holder.mImageView.setImageResource(R.drawable.failed);
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

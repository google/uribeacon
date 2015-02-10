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

import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;

public class TestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final String TAG = TestsAdapter.class.getCanonicalName();
  private static final int TYPE_HEADER = 0;
  private static final int TYPE_TEST_RESULT = 1;
  private final ArrayList<TestHelper> mDataset;
  private final AdapterCallback mAdapterCallback;
  private String mHeader;

  // Provide a suitable constructor (depends on the kind of dataset)
  public TestsAdapter(ArrayList<TestHelper> uriBeaconTests, AdapterCallback adapterCallback, String header) {
    mAdapterCallback = adapterCallback;
    mDataset = uriBeaconTests;
    mHeader = header;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
      int viewType) {
    if (viewType == TYPE_HEADER) {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.list_subheader, parent, false);
      return new HeaderViewHolder(v);
    } else if (viewType == TYPE_TEST_RESULT) {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.test_view, parent, false);
      return new TestResultViewHolder(v);
    }
    throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
    if (holder instanceof TestResultViewHolder) {
      TestResultViewHolder testResultHolder = (TestResultViewHolder) holder;
      TestHelper test = getItem(position);
      testResultHolder.mTestName.setText(test.getName());
      setIcon(testResultHolder.mImageView, test);

      holder.itemView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          mAdapterCallback.restart(position - 1);
          return true;
        }
      });
      if (test.isFailed()) {
        setErrorMessage(testResultHolder, test);
        setOnClickListener(testResultHolder);
      } else {
        testResultHolder.mTestResult.setVisibility(View.GONE);
        testResultHolder.mTestDetails.setVisibility(View.GONE);
        testResultHolder.mLayout.setOnClickListener(null);
        testResultHolder.mLayout.setClickable(false);
      }
    } else if (holder instanceof HeaderViewHolder) {
      HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
      headerViewHolder.mHeader.setText(mHeader);
    }
  }

  private void setOnClickListener(final TestResultViewHolder holder) {
    holder.mLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View item) {
        if (holder.expanded) {
          holder.expanded = false;
          holder.mTestDetails.setVisibility(View.GONE);
          holder.mTestResult.setVisibility(View.VISIBLE);
          item.getLayoutParams().height = (int) item.getResources()
              .getDimension(R.dimen.list_item_height);
        } else {
          holder.expanded = true;
          holder.mTestDetails.setVisibility(View.VISIBLE);
          holder.mTestResult.setVisibility(View.GONE);
          item.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        }
      }
    });
  }

  private void setErrorMessage(TestResultViewHolder holder, TestHelper test) {
    TextView details = holder.mTestDetails;
    String sDetails = "Steps:";
    for (int i = 0; i < test.getTestSteps().size(); i++) {
      TestAction action = test.getTestSteps().get(i);
      sDetails += "\n\t" + (i + 1) + ". ";
      switch (action.actionType) {
        case TestAction.CONNECT:
          sDetails += "Connect";
          break;
        case TestAction.WRITE:
          sDetails += "Write";
          break;
        case TestAction.ASSERT:
          sDetails += "Assert";
          break;
        case TestAction.DISCONNECT:
          sDetails += "Disconnect";
          break;
        case TestAction.ADV_FLAGS:
          sDetails += "Adv Flags";
          break;
        case TestAction.ADV_TX_POWER:
          sDetails += "Adv Tx Power";
          break;
        case TestAction.ADV_URI:
          sDetails += "Adv URI";
          break;
        case TestAction.ADV_PACKET:
          sDetails += "Adv Packet";
          break;
      }
      if (action.failed) {
        sDetails += ":\n\t\t" + action.reason + "\n\t...";
        holder.mTestResult.setText((i + 1) + ". " + action.reason);
        holder.mTestResult.setVisibility(View.VISIBLE);
        break;
      }
    }
    details.setText(sDetails);
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_HEADER;
    } else {
      return TYPE_TEST_RESULT;
    }
  }

  private TestHelper getItem(int position) {
    return mDataset.get(position -1);
  }

  private void setIcon(ImageView imageView, TestHelper test) {
    if (!test.isStarted()) {
      imageView.setImageResource(R.drawable.not_started);
    } else if (!test.isFinished()) {
      imageView.setImageResource(R.drawable.executing_animated);
      ((AnimatedVectorDrawable) imageView.getDrawable()).start();
    } else if (!test.isFailed()) {
      imageView.setImageResource(R.drawable.success);
    } else {
      imageView.setImageResource(R.drawable.failed);
    }
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return mDataset.size() + 1;
  }

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class TestResultViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public final TextView mTestName;
    public final TextView mTestResult;
    public final ImageView mImageView;
    public final TextView mTestDetails;
    public final LinearLayout mLayout;
    private boolean expanded;

    public TestResultViewHolder(View v) {
      super(v);
      mLayout = (LinearLayout) v;
      expanded = false;
      mTestName = (TextView) v.findViewById(R.id.test_name);
      mTestResult = (TextView) v.findViewById(R.id.test_reason);
      mImageView = (ImageView) v.findViewById(R.id.imageView_testIcon);
      mTestDetails = (TextView) v.findViewById(R.id.test_detailed_view);
    }
  }

  public static class HeaderViewHolder extends RecyclerView.ViewHolder {
    public final TextView mHeader;
    public HeaderViewHolder(View v) {
      super(v);
      mHeader = (TextView) v.findViewById(R.id.subheader_textView);
    }
  }
  public interface AdapterCallback {
    public void restart(int testPosition);
  }
}

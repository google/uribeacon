package org.uribeacon.validator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.uribeacon.validator.MainActivity.TestInfo;

public class TestTypesAdapter extends RecyclerView.Adapter<TestTypesAdapter.ViewHolder>{
  private TestInfo[] mDataset;
  private StartTestType mCallback;
  public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextView;
    public View mLayout;
    public ViewHolder(View v) {
      super(v);
      mLayout = v;
      mTextView = (TextView) v.findViewById(R.id.test_type);
    }
  }

  public TestTypesAdapter(TestInfo[] tests, StartTestType callback) {
    mDataset = tests;
    mCallback = callback;
  }

  // Create new views
  @Override
  public TestTypesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.type_view, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  // Replace the contest of a view
  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    holder.mTextView.setText(mDataset[position].testName);
    holder.mLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mCallback.startTestType(mDataset[position].className);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mDataset.length;
  }

  public interface StartTestType {
    public void startTestType(String type);
  }
}

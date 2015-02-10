package org.uribeacon.validator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.uribeacon.validator.MainActivity.TestInfo;

public class TestTypesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int TYPE_HEADER = 0;
  private static final int TYPE_TEST = 1;
  private final TestInfo[] mDataset;
  private final StartTestType mCallback;
  private String mHeader;

  public TestTypesAdapter(TestInfo[] tests, StartTestType callback, String header) {
    mDataset = tests;
    mCallback = callback;
    mHeader = header;
  }

  // Create new views
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == TYPE_TEST) {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.type_view, parent, false);
      return new ViewHolderTest(v);
    } else if (viewType == TYPE_HEADER) {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.list_subheader, parent, false);
      return new ViewHolderHeader(v);
    }
    throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
  }

  // Replace the contest of a view
  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
    if (holder instanceof ViewHolderTest) {
      ViewHolderTest testHolder = (ViewHolderTest) holder;
      testHolder.mTextView.setText(getItem(position).testName);
      testHolder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mCallback.startTestType(getItem(position).className);
        }
      });
    } else if (holder instanceof ViewHolderHeader) {
      ViewHolderHeader headerHolder = (ViewHolderHeader) holder;
      headerHolder.mSubheader.setText(mHeader);
    }
  }
  private TestInfo getItem(int position) {
    return mDataset[position - 1];
  }
  @Override
  public int getItemCount() {
    return mDataset.length + 1;
  }


  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_HEADER;
    } else {
      return TYPE_TEST;
    }
  }
  public static class ViewHolderHeader extends RecyclerView.ViewHolder {

    public final TextView mSubheader;

    public ViewHolderHeader(View v) {
      super(v);
      mSubheader = (TextView) v.findViewById(R.id.subheader_textView);
    }
  }
  public static class ViewHolderTest extends RecyclerView.ViewHolder {

    public final TextView mTextView;

    public ViewHolderTest(View v) {
      super(v);
      mTextView = (TextView) v.findViewById(R.id.test_type);
    }
  }

  public interface StartTestType {

    public void startTestType(String type);
  }
}

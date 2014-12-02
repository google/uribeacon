package org.uribeacon.sample;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordDialogFragment extends DialogFragment {

  private static final String TAG = PasswordDialogListener.class.getCanonicalName();

  public interface PasswordDialogListener {
    public void onDialogWriteClick(boolean reset);
  }
  private PasswordDialogListener mListener;
  private boolean mReset;
  public PasswordDialogFragment() {
  }
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (PasswordDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement "
          + PasswordDialogListener.class.getCanonicalName());
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mReset = getArguments().getBoolean("reset");
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    builder.setView(inflater.inflate(R.layout.fragment_password_dialog, null))
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            PasswordDialogFragment.this.getDialog().cancel();
          }
        })
        .setPositiveButton(R.string.write_beacon, null);
    return builder.create();
  }

  @Override
  public void onResume() {
    super.onResume();
    AlertDialog alertDialog = (AlertDialog) getDialog();
    Button writeButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    writeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onDialogWriteClick(mReset);
        dismiss();
      }
    });
  }
}

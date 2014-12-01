package org.uribeacon.sample;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordDialogFragment extends DialogFragment {

  public interface PasswordDialogListener {
    public void onDialogWriteClick(DialogFragment dialog);
  }
  private PasswordDialogListener mListener;
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

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    builder.setView(inflater.inflate(R.layout.fragment_password_dialog, null))
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            PasswordDialogFragment.this.getDialog().cancel();
          }
        })
        .setPositiveButton(R.string.write_beacon, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            mListener.onDialogWriteClick(PasswordDialogFragment.this);
          }
        });
    return builder.create();
  }
}

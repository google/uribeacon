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
import android.widget.EditText;
import android.widget.Toast;


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
        String password1 = ((EditText) getDialog().findViewById(R.id.beacon_password1)).getText()
            .toString();
        String password2 = ((EditText) getDialog().findViewById(R.id.beacon_password2)).getText()
            .toString();
        // Make sure they entered a password
        if (password1.isEmpty()) {
          Toast.makeText(getActivity(), R.string.no_password, Toast.LENGTH_SHORT).show();
        // Make sure the passwords are the same
        } else if (!password1.equals(password2)) {
          Toast.makeText(getActivity(), R.string.password_missmatch, Toast.LENGTH_SHORT).show();
        } else {
          mListener.onDialogWriteClick(PasswordDialogFragment.this);
          dismiss();
        }
      }
    });
  }
}

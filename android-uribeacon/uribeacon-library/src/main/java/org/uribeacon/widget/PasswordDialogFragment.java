/*
 * Copyright 2014 Google Inc. All rights reserved.
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
package org.uribeacon.widget;


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

import org.uribeacon.library.R;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class PasswordDialogFragment extends DialogFragment {

  private static final String TAG = PasswordDialogListener.class.getCanonicalName();
  public static final String RESET = "reset";

  public interface PasswordDialogListener {
    public void onDialogWriteClick(String password, boolean reset);
  }

  private PasswordDialogListener mListener;
  private boolean mReset;

  public PasswordDialogFragment() {
  }

  public static PasswordDialogFragment newInstance(Fragment fragment) {
    try {
      PasswordDialogListener listener = (PasswordDialogListener) fragment;
      PasswordDialogFragment dialog = new PasswordDialogFragment();
      dialog.mListener = listener;
      return dialog;
    } catch (ClassCastException e) {
      throw new ClassCastException(fragment.toString() + " must implement "
          + PasswordDialogListener.class.getCanonicalName());
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mReset = getArguments().getBoolean(RESET);

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
          mListener.onDialogWriteClick(password1, mReset);
          dismiss();
        }
      }
    });
  }
}

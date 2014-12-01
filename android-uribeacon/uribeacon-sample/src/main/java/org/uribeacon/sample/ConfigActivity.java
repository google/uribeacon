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

package org.uribeacon.sample;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV1;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.config.UriBeaconConfig;
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;
import org.uribeacon.scan.compat.ScanResult;

import java.net.URISyntaxException;
import java.util.List;

public class ConfigActivity extends Activity implements PasswordDialogFragment.PasswordDialogListener{
  private Spinner mSchema;
  private EditText mUriValue;
  private EditText mFlagsValue;
  private EditText[] mAdvertisedTxPowerLevels = new EditText[4];
  private Spinner mTxPowerMode;
  private EditText mBeaconPeriod;
  private Switch mLockState;
  private boolean mOriginalLockState;

  private ProgressDialog mConnectionDialog = null;
  private static final byte DEFAULT_TX_POWER = -63;
  private final String TAG = "ConfigActivity";
  private UriBeaconConfig mUriBeaconConfig;

  private final UriBeaconCallback mUriBeaconCallback = new UriBeaconCallback() {
    @Override
    public void onUriBeaconRead(ConfigUriBeacon configUriBeacon, int status) {
      checkRequest(status);
      updateInputFields(configUriBeacon);
    }

    @Override
    public void onUriBeaconWrite(int status) {
      checkRequest(status);
      mUriBeaconConfig.closeUriBeacon();
      finish();
    }

    private void checkRequest(int status) {
      if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(ConfigActivity.this, "Failed to update the beacon", Toast.LENGTH_SHORT)
            .show();
        finish();
      }
    }
  };

  private void blockUi() {
    mUriValue.setEnabled(false);
  }

  public void saveConfigBeacon(MenuItem menu) {
    try {
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        if (mOriginalLockState || mLockState.isChecked()) {
          showPasswordDialog();
        }
        else {
          writeUriBeaconV2();
        }
      }
      else {
        blockUi();
        ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
            .uriString(mUriValue.getText().toString())
            .txPowerLevel(DEFAULT_TX_POWER)
            .build();
        mUriBeaconConfig.writeUriBeacon(configUriBeacon);
      }
    } catch (URISyntaxException e) {
      Toast.makeText(ConfigActivity.this, "Invalid Uri", Toast.LENGTH_LONG).show();
      mUriBeaconConfig.closeUriBeacon();
      finish();
    }
  }

  private void writeUriBeaconV2() throws URISyntaxException {
    blockUi();
    ConfigUriBeacon.Builder builder = new ConfigUriBeacon.Builder()
        .uriString(mUriValue.getText().toString())
        .flags(hexStringToByte(mFlagsValue.getText().toString()))
        .beaconPeriod(Integer.parseInt(mBeaconPeriod.getText().toString()))
        .txPowerMode((byte) mTxPowerMode.getSelectedItemPosition());
    byte[] tempTxCal = new byte[4];
    for (int i = 0; i < mAdvertisedTxPowerLevels.length; i++) {
      tempTxCal[i] = Byte.parseByte(mAdvertisedTxPowerLevels[i].getText().toString());
    }
    builder.advertisedTxPowerLevels(tempTxCal);
    mUriBeaconConfig.writeUriBeacon(builder.build());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set content view
    setContentView(R.layout.activity_configbeacon);

    initializeTextFields();

    // Get the device to connect to that was passed to us by the scan
    // results Activity.
    Intent intent = getIntent();
    if (intent.getExtras() != null) {
      ScanResult scanResult = intent.getExtras().getParcelable(ScanResult.class.getCanonicalName());
      BluetoothDevice device = scanResult.getDevice();
      if (device != null) {
        // start connection progress
        mConnectionDialog = new ProgressDialog(this);
        mConnectionDialog.setIndeterminate(true);
        mConnectionDialog.setMessage("Connecting to device...");
        mConnectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            finish();
          }
        });
      }
      List<ParcelUuid> uuids = scanResult.getScanRecord().getServiceUuids();
      // Assuming the first uuid is the config uuid
      ParcelUuid uuid = uuids.get(0);
      mUriBeaconConfig = new UriBeaconConfig(this, mUriBeaconCallback, uuid);
      mUriBeaconConfig.connectUriBeacon(device);
    }
  }

  private void initializeTextFields() {
    mSchema = (Spinner) findViewById(R.id.spinner_uriProtocols);
    mUriValue = (EditText) findViewById(R.id.editText_uri);
    mFlagsValue = (EditText) findViewById(R.id.editText_flags);
    mBeaconPeriod = (EditText) findViewById(R.id.editText_beaconPeriod);
    mTxPowerMode = (Spinner) findViewById(R.id.spinner_powerMode);
    mAdvertisedTxPowerLevels[0] = (EditText) findViewById(R.id.editText_txCal0);
    mAdvertisedTxPowerLevels[1] = (EditText) findViewById(R.id.editText_txCal1);
    mAdvertisedTxPowerLevels[2] = (EditText) findViewById(R.id.editText_txCal2);
    mAdvertisedTxPowerLevels[3] = (EditText) findViewById(R.id.editText_txCal3);
    mLockState = (Switch) findViewById(R.id.switch_lock);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.config_menu, menu);
    return true;
  }
  public static void startConfigureActivity(Context context, ScanResult scanResult) {
    Intent intent = new Intent(context, ConfigActivity.class);
    intent.putExtra(ScanResult.class.getCanonicalName(), scanResult);
    context.startActivity(intent);
  }

  private void updateInputFields(ConfigUriBeacon configUriBeacon) {
    if (mUriValue != null && configUriBeacon != null) {
      mUriValue.setText(configUriBeacon.getUriString());
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        mFlagsValue.setText(byteToHexString(configUriBeacon.getFlags()));
        mBeaconPeriod.setText(Integer.toString(configUriBeacon.getBeaconPeriod()));
        mTxPowerMode.setSelection((int) configUriBeacon.getTxPowerMode());

        for (int i = 0; i < mAdvertisedTxPowerLevels.length; i++) {
          mAdvertisedTxPowerLevels[i].setText(Integer.toString(configUriBeacon.getAdvertisedTxPowerLevels()[i]));
        }
        mLockState.setChecked(configUriBeacon.getLockState());
        mOriginalLockState = configUriBeacon.getLockState();
      }
      else if (mUriBeaconConfig.getVersion().equals(ProtocolV1.CONFIG_SERVICE_UUID)) {
        hideV2Fields();
      }
    }
    else {
      Toast.makeText(this, "Beacon Contains Invalid Data", Toast.LENGTH_SHORT).show();
    }
  }

  private String byteToHexString(byte theByte) {
    return String.format("%02X", theByte);
  }

  private byte hexStringToByte(String hexString) {
    return Integer.decode("0x" + hexString).byteValue();
  }
  private void hideV2Fields(){
    findViewById(R.id.secondRow).setVisibility(View.GONE);
    findViewById(R.id.txCalRow).setVisibility(View.GONE);
    findViewById(R.id.lastRow).setVisibility(View.GONE);
  }
  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    mUriBeaconConfig.closeUriBeacon();
    Toast.makeText(this, "Disconnected from beacon", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }

  public void showPasswordDialog() {
    DialogFragment dialog = new PasswordDialogFragment();
    dialog.setArguments(new Bundle());
    dialog.show(getFragmentManager(), "PasswordDialogFragment");
  }
  @Override
  public void onDialogWriteClick(DialogFragment dialog) {
    try {
      writeUriBeaconV2();
    } catch (URISyntaxException e) {
      Toast.makeText(ConfigActivity.this, "Invalid Uri", Toast.LENGTH_LONG).show();
      mUriBeaconConfig.closeUriBeacon();
      finish();
    }
  }
}

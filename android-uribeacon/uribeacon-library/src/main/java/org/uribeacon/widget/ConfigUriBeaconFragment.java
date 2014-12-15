package org.uribeacon.widget;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.uribeacon.beacon.ConfigUriBeacon;
import org.uribeacon.config.ProtocolV1;
import org.uribeacon.config.ProtocolV2;
import org.uribeacon.config.UriBeaconConfig;
import org.uribeacon.config.UriBeaconConfig.UriBeaconCallback;
import org.uribeacon.library.R;
import org.uribeacon.scan.compat.ScanResult;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigUriBeaconFragment extends Fragment  implements
    PasswordDialogFragment.PasswordDialogListener {

  private static final int ITERATIONS = 1000;
  private final String TAG = "ConfigActivity";
  public interface ConfigUriBeaconListener {
    /**
     * Called when the beacon has been disconnected.
     */
    public void beaconDisconnected();
  }
  
  private final UriBeaconCallback mUriBeaconCallback = new UriBeaconCallback() {
    @Override
    public void onUriBeaconRead(ConfigUriBeacon configUriBeacon, int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        updateInputFields(configUriBeacon);
      } else if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(getActivity(), R.string.failed_to_read, Toast.LENGTH_SHORT).show();
        mListener.beaconDisconnected();
      } else {
        Toast.makeText(getActivity(), getString(R.string.failed_with_code) + status,
            Toast.LENGTH_SHORT).show();
        mListener.beaconDisconnected();
      }
    }

    @Override
    public void onUriBeaconWrite(int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        mUriBeaconConfig.closeUriBeacon();
        mListener.beaconDisconnected();
      } else if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(getActivity(), R.string.failed_to_write, Toast.LENGTH_SHORT).show();
      } else if (status == ConfigUriBeacon.INSUFFICIENT_AUTHORIZATION) {
        Toast.makeText(getActivity(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getActivity(), getString(R.string.failed_with_code) + status,
            Toast.LENGTH_SHORT).show();
      }
      enabledFields(true);
    }
  };
  private Spinner mUriPrefix;
  private EditText mUriSuffix;
  // V1
  private EditText mFlagsV1;
  private EditText mTxPowerLevel;
  // V2
  private Switch mLockState;
  private boolean mOriginalLockState;
  private EditText mFlagsV2;
  private Spinner mTxPowerMode;
  private EditText[] mAdvertisedTxPowerLevels = new EditText[4];
  private EditText mBeaconPeriod;
  private ProgressDialog mConnectionDialog = null;
  private UriBeaconConfig mUriBeaconConfig;
  private ConfigUriBeaconListener mListener;
  
  public ConfigUriBeaconFragment() {
    // Required empty public constructor
  }

  ///////////////////////////////
  ////// Lifecycle Methods //////
  ///////////////////////////////
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (ConfigUriBeaconListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
    }
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    setHasOptionsMenu(true);
    // set content view
    View view = inflater.inflate(R.layout.fragment_config_uri_beacon, container, false);

    initializeTextFields(view);
    view.findViewById(R.id.button_advanced_settings).setOnClickListener(onAdvancedSettingsClicked);

    // Get the device to connect to that was passed to us by the scan
    // results Activity.
    Bundle bundle = getArguments();
    if (!bundle.isEmpty()) {
      ScanResult scanResult = bundle.getParcelable(ScanResult.class.getCanonicalName());
      BluetoothDevice device = scanResult.getDevice();
      if (device != null) {
        // start connection progress
        mConnectionDialog = new ProgressDialog(getActivity());
        mConnectionDialog.setIndeterminate(true);
        mConnectionDialog.setMessage("Connecting to device...");
        mConnectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            mListener.beaconDisconnected();
          }
        });
        mConnectionDialog.show();
      }
      List<ParcelUuid> uuids = scanResult.getScanRecord().getServiceUuids();
      // Check which uuid is the config uuid
      ParcelUuid services[] = {ProtocolV2.CONFIG_SERVICE_UUID, ProtocolV1.CONFIG_SERVICE_UUID};
      for (ParcelUuid serviceUuid : services) {
        if (uuids.contains(serviceUuid)) {
          mUriBeaconConfig = new UriBeaconConfig(getActivity(), mUriBeaconCallback, serviceUuid);
          mUriBeaconConfig.connectUriBeacon(device);
          break;
        }
      }
    }
    return view;
  }

  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    mUriBeaconConfig.closeUriBeacon();
    Toast.makeText(getActivity(), R.string.disconnected_from_beacon, Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }

  ///////////////////////
  ////// On Clicks //////
  ///////////////////////
  private void onSaveClicked() {
    try {
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        if (mOriginalLockState || mLockState.isChecked()) {
          showPasswordDialog(false);
        } else {
          writeUriBeaconV2(null);
        }
      } else {
        writeUriBeaconV1();
      }
    } catch (URISyntaxException e) {
      Toast.makeText(getActivity(), R.string.invalid_uri, Toast.LENGTH_LONG).show();
      mUriBeaconConfig.closeUriBeacon();
      mListener.beaconDisconnected();
    }
  }

  public void onResetClicked() {
    if (mOriginalLockState) {
      showPasswordDialog(true);
    } else {
      resetConfigBeacon(null);
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_save) {
      onSaveClicked();
      return true;
    } else if (id == R.id.menu_reset) {
      onResetClicked();
      return true;
    } else {
      return super.onContextItemSelected(item);
    }
  }

  private View.OnClickListener onAdvancedSettingsClicked = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      view.setVisibility(View.GONE);
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        showV2Fields();
      } else if (mUriBeaconConfig.getVersion().equals(ProtocolV1.CONFIG_SERVICE_UUID)) {
        showV1Fields();
      }
    }
  };

  @Override
  public void onDialogWriteClick(String password, boolean reset) {
    try {
      byte[] key = generateKey(password);
      if (reset) {
        resetConfigBeacon(key);
      } else {
        writeUriBeaconV2(key);
      }
    } catch (URISyntaxException e) {
      Toast.makeText(getActivity(), "Invalid Uri", Toast.LENGTH_LONG).show();
      mUriBeaconConfig.closeUriBeacon();
      mListener.beaconDisconnected();
    }
  }

  /**
   * Method that generates a 128bit key
   *
   * @param password the password used to generate the key
   * @return The 128bit key in a byte[]
   */
  private static byte[] generateKey(String password) {
    try {
      byte[] salt = new byte[1];
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS,
          ConfigUriBeacon.KEY_LENGTH);
      SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
      // Return it in a byte[]
      return secretKey.getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void enabledFields(boolean block) {
    // Common
    mUriPrefix.setEnabled(block);
    mUriSuffix.setEnabled(block);
    // V1
    mFlagsV1.setEnabled(block);
    mTxPowerLevel.setEnabled(block);
    //V2
    mFlagsV2.setEnabled(block);
    for (EditText txCal : mAdvertisedTxPowerLevels) {
      txCal.setEnabled(block);
    }
    mTxPowerMode.setEnabled(block);
    mBeaconPeriod.setEnabled(block);
    mLockState.setEnabled(block);
  }

  private void resetConfigBeacon(byte[] key) {
    try {
      enabledFields(false);
      ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
          .key(key)
          .reset(true)
          .build();
      mUriBeaconConfig.writeUriBeacon(configUriBeacon);
    } catch (URISyntaxException e) {
      Toast.makeText(getActivity(), R.string.reset_failed, Toast.LENGTH_SHORT).show();
    }
  }

  private void writeUriBeaconV1() throws URISyntaxException {
    enabledFields(false);
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(getUri())
        .txPowerLevel(Byte.parseByte(mTxPowerLevel.getText().toString()))
        .flags(hexStringToByte(mFlagsV1.getText().toString()))
        .build();
    mUriBeaconConfig.writeUriBeacon(configUriBeacon);
  }

  private void writeUriBeaconV2(byte[] key) throws URISyntaxException {
    enabledFields(false);
    ConfigUriBeacon.Builder builder = new ConfigUriBeacon.Builder()
        .key(key)
        .lockState(mLockState.isChecked())
        .uriString(getUri())
        .flags(hexStringToByte(mFlagsV2.getText().toString()))
        .beaconPeriod(Integer.parseInt(mBeaconPeriod.getText().toString()))
        .txPowerMode((byte) mTxPowerMode.getSelectedItemPosition());
    byte[] tempTxCal = new byte[4];
    for (int i = 0; i < mAdvertisedTxPowerLevels.length; i++) {
      tempTxCal[i] = Byte.parseByte(mAdvertisedTxPowerLevels[i].getText().toString());
    }
    builder.advertisedTxPowerLevels(tempTxCal);
    mUriBeaconConfig.writeUriBeacon(builder.build());
  }

  private String getUri() {
    return mUriSuffix.getText().toString().isEmpty() ?
        mUriSuffix.getText().toString() : mUriPrefix.getSelectedItem() + mUriSuffix.getText().toString();
  }

  private void initializeTextFields(View view) {
    // Common
    mUriPrefix = (Spinner) view.findViewById(R.id.spinner_uriProtocols);
    mUriSuffix = (EditText) view.findViewById(R.id.editText_uri);
    // V1
    mTxPowerLevel = (EditText) view.findViewById(R.id.editText_txPowerLevel);
    mFlagsV1 = (EditText) view.findViewById(R.id.editText_flagsV1);
    // V2
    mFlagsV2 = (EditText) view.findViewById(R.id.editText_flags);
    mBeaconPeriod = (EditText) view.findViewById(R.id.editText_beaconPeriod);
    mTxPowerMode = (Spinner) view.findViewById(R.id.spinner_powerMode);
    mAdvertisedTxPowerLevels[0] = (EditText) view.findViewById(R.id.editText_txCal0);
    mAdvertisedTxPowerLevels[1] = (EditText) view.findViewById(R.id.editText_txCal1);
    mAdvertisedTxPowerLevels[2] = (EditText) view.findViewById(R.id.editText_txCal2);
    mAdvertisedTxPowerLevels[3] = (EditText) view.findViewById(R.id.editText_txCal3);
    mLockState = (Switch) view.findViewById(R.id.switch_lock);
  }
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.config_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  private void updateInputFields(ConfigUriBeacon configUriBeacon) {
    if (mUriSuffix != null && configUriBeacon != null) {
      String[] uriProtocols = getResources().getStringArray(R.array.uriProtocols);
      for (int i = 0; i < uriProtocols.length; i++) {
        if (configUriBeacon.getUriString().startsWith(uriProtocols[i])) {
          mUriPrefix.setSelection(i);
          mUriSuffix.setText(configUriBeacon.getUriString().replace(uriProtocols[i], ""));
        }
      }
      if (mUriBeaconConfig.getVersion().equals(ProtocolV2.CONFIG_SERVICE_UUID)) {
        mFlagsV2.setText(byteToHexString(configUriBeacon.getFlags()));
        mBeaconPeriod.setText(Integer.toString(configUriBeacon.getBeaconPeriod()));
        mTxPowerMode.setSelection((int) configUriBeacon.getTxPowerMode());

        for (int i = 0; i < mAdvertisedTxPowerLevels.length; i++) {
          mAdvertisedTxPowerLevels[i]
              .setText(Integer.toString(configUriBeacon.getAdvertisedTxPowerLevels()[i]));
        }
        mLockState.setChecked(configUriBeacon.getLockState());
        mOriginalLockState = configUriBeacon.getLockState();
      } else if (mUriBeaconConfig.getVersion().equals(ProtocolV1.CONFIG_SERVICE_UUID)) {
        mTxPowerLevel.setText(Integer.toString(configUriBeacon.getTxPowerLevel()));
        mFlagsV1.setText(byteToHexString(configUriBeacon.getFlags()));
      }
      showBasicFields();
      mConnectionDialog.dismiss();
    } else {
      Toast.makeText(getActivity(), R.string.invalid_data, Toast.LENGTH_SHORT).show();
    }
  }

  private String byteToHexString(byte theByte) {
    return String.format("%02X", theByte);
  }

  private byte hexStringToByte(String hexString) {
    return Integer.decode("0x" + hexString).byteValue();
  }

  private void showBasicFields() {
    getView().findViewById(R.id.uriLabel).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.urlRow).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.button_advanced_settings).setVisibility(View.VISIBLE);
  }

  private void showV1Fields() {
    getView().findViewById(R.id.secondRowV1).setVisibility(View.VISIBLE);
  }

  private void showV2Fields() {
    getView().findViewById(R.id.secondRowV2).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.txCalRow).setVisibility(View.VISIBLE);
    getView().findViewById(R.id.lastRow).setVisibility(View.VISIBLE);
  }

  private void showPasswordDialog(boolean reset) {
    DialogFragment dialog = PasswordDialogFragment.newInstance(this);
    Bundle args = new Bundle();
    args.putBoolean(PasswordDialogFragment.RESET, reset);
    dialog.setArguments(args);
    dialog.show(getFragmentManager(), PasswordDialogFragment.class.getCanonicalName());
  }
}

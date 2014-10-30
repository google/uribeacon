package org.uribeacon.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.config.GattService;
import org.uribeacon.config.Util;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class SomeActivity extends Activity {

  private static final UUID beaconService = UUID
      .fromString("b35d7da6-eed4-4d59-8f89-f6573edea967");
  private static final UUID beaconDataOneCharacteristic = UUID
      .fromString("b35d7da7-eed4-4d59-8f89-f6573edea967");
  private static final UUID beaconDataTwoCharacteristic = UUID
      .fromString("b35d7da8-eed4-4d59-8f89-f6573edea967");
  private static final UUID beaconDataSizeCharacteristic = UUID
      .fromString("b35d7da9-eed4-4d59-8f89-f6573edea967");
  private TextView beaconValue = null;
  private TextView beaconNewValue = null;
  private Button beaconUpdateValue = null;
  private ProgressDialog connectionDialog = null;
  private BluetoothDevice mDeviceToConnect = null;
  private GattService mService = null;
  private int mLength;
  private byte[] mData;
  private byte[] mDataWrite;
  private String TAG = "ConfigActivity";

  private BluetoothGattCallback mRequestCallback = new BluetoothGattCallback() {
    public void checkRequest(int status) {
      if (status == BluetoothGatt.GATT_FAILURE) {
        Toast.makeText(SomeActivity.this, "Failed to update the beacon", Toast.LENGTH_SHORT).show();
        // unblock the UI
        beaconNewValue.setEnabled(true);
        beaconUpdateValue.setEnabled(true);
        finish();
      }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      Log.d("ConfigActivity", "onCharacteristicWrite " + Thread.currentThread());
      checkRequest(status);
      byte[] value = characteristic.getValue();
      Log.d(TAG, "onCharacteristicWrite length " + value.length);
      if (!Arrays.equals(value, mDataWrite)) {
        Log.e(TAG, "onCharacteristicWrite -- response is not equal " +
            Util.byteArrayToHexString(value) + " <> " +
            Util.byteArrayToHexString(mDataWrite));
      }
      if (mDataWrite.length <= 20 || beaconDataTwoCharacteristic.equals(characteristic.getUuid())) {
        mService.disconnect();
        finish();
      }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
      checkRequest(status);
      Log.d("ConfigActivity", "onConnectionStateChange " + newState);
      if (newState == BluetoothGatt.STATE_CONNECTED) {
        gatt.discoverServices();
      } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
        Log.d("ConfigActivity", "STATE_DISCONNECTED " + newState);
        // End this activity and go back to scan results view.
        finish();
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      Log.d(TAG, "onServicesDiscovered request queue");
      checkRequest(status);
      mService.setService(beaconService);
      mService.readCharacteristic(beaconDataSizeCharacteristic);
      connectionDialog.dismiss();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
      checkRequest(status);
      UUID uuid = characteristic.getUuid();
      if (beaconDataOneCharacteristic.equals(uuid)) {
        mData = characteristic.getValue();
        if (mLength <= 20) {
          setBeaconValue();
        }
      } else if (beaconDataTwoCharacteristic.equals(uuid)) {
        mData = Util.concatenate(mData, characteristic.getValue());
        setBeaconValue();
      } else if (beaconDataSizeCharacteristic.equals(uuid)) {
        if (characteristic.getValue() == null) {
          Log.e(TAG, "Null value!");
        } else {
          mLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
          mService.readCharacteristic(beaconDataOneCharacteristic);
          if (mLength > 20) {
            mService.readCharacteristic(beaconDataTwoCharacteristic);
          }
          Log.d(TAG, "Length is " + mLength);
        }
      }
      Log.d("ConfigActivity", "onCharacteristicRead done");
    }
  };

  /**
   * Callbacks for changes to the state of the connection to BtSmartService.
   */
  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder rawBinder) {
      mService = ((GattService.LocalBinder) rawBinder).getService();
      if (mService != null) {
        // We have a connection to BtSmartService so now we can connect
        // and register the device handler.
        mService.connect(SomeActivity.this, mDeviceToConnect, mRequestCallback);
        connectionDialog.show();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName classname) {
      mService = null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set content view
    setContentView(R.layout.activity_configbeacon);

    // find the TextView to hold the current value
    beaconValue = (TextView) findViewById(R.id.beaconCurValue);
    beaconValue.setText(R.string.beacon_not_available);
    beaconNewValue = (TextView) findViewById(R.id.beaconNewValue);
    beaconUpdateValue = (Button) findViewById(R.id.writeNewValue);
    beaconUpdateValue.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // block UI
        beaconNewValue.setEnabled(false);
        beaconUpdateValue.setEnabled(false);
        String uri = beaconNewValue.getText().toString();
        try {
          UriBeacon uriBeacon = new UriBeacon.Builder().uriString(uri).build();
          mDataWrite = uriBeacon.toByteArray();
        } catch (URISyntaxException e) {
          Toast.makeText(SomeActivity.this, "Invalid Uri", Toast.LENGTH_LONG).show();
          return;
        }

        if (mDataWrite.length <= 20) {
          // write the value
          mService.writeCharacteristic(beaconDataOneCharacteristic,
              mDataWrite);
        } else {
          byte[] buff = Arrays.copyOfRange(mDataWrite, 0, 20);
          Log.d(TAG, "Buffer length is " + buff.length);
          mService.writeCharacteristic(beaconDataOneCharacteristic,
              buff);
          mService.writeCharacteristic(beaconDataTwoCharacteristic,
              Arrays.copyOfRange(mDataWrite, 20, mDataWrite.length));
        }
      }
    });

    // Get the device to connect to that was passed to us by the scan
    // results Activity.
    Intent intent = getIntent();
    if (intent.getExtras() != null) {
      mDeviceToConnect = intent.getExtras().getParcelable(
          BluetoothDevice.EXTRA_DEVICE);
      if (mDeviceToConnect != null) {
        // Make a connection to BtSmartService to enable us to use its
        // services.
        Intent bindIntent = new Intent(this, GattService.class);
        bindService(bindIntent, mServiceConnection,
            Context.BIND_AUTO_CREATE);

        // start connection progress
        connectionDialog = new ProgressDialog(this);
        connectionDialog.setIndeterminate(true);
        connectionDialog.setMessage("Connecting to device...");
        connectionDialog
            .setOnCancelListener(new DialogInterface.OnCancelListener() {

              @Override
              public void onCancel(DialogInterface dialog) {
                finish();
              }
            });
      }
    }
  }

  public static void startConfigureActivity(Context context, BluetoothDevice deviceToConnect) {
    Intent intent = new Intent(context, SomeActivity.class);
    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceToConnect);
    context.startActivity(intent);
  }

  private void setBeaconValue() {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(mData);
    if (beaconValue != null) {
      beaconValue.setText(uriBeacon.getUriString());
    }
  }

  @Override
  public void onDestroy() {
    // Close and release Bluetooth connection.
    if (mService != null) {
      mService.close();
    }
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
    Toast.makeText(this, "Disconnected from beacon", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }
}

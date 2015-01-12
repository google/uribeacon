package org.uribeacon.validator;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import org.uribeacon.config.ProtocolV2;
import org.uribeacon.validator.UriBeaconTestHelper.TestCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.UUID;

public class Tests {

  private static final String TAG = Tests.class.getCanonicalName();

  private static final UUID LOCK_STATE = UUID.fromString("ee0c2081-8786-40ba-ab96-99b91ac981d8");
  private static final UUID LOCK = UUID.fromString("ee0c2082-8786-40ba-ab96-99b91ac981d8");
  private static final UUID UNLOCK = UUID.fromString("ee0c2083-8786-40ba-ab96-99b91ac981d8");
  private static final UUID DATA = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
  private static final UUID FLAGS = UUID.fromString("ee0c2085-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_LEVELS = UUID.fromString("ee0c2086-8786-40ba-ab96-99b91ac981d8");
  private static final UUID POWER_MODE = UUID.fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
  private static final UUID PERIOD = UUID.fromString("ee0c2088-8786-40ba-ab96-99b91ac981d8");
  private static final UUID RESET = UUID.fromString("ee0c2089-8786-40ba-ab96-99b91ac981d8");

  private Context mContext;
  private ScanResult mResult;
  private TestCallback mTestCallback;

  public ArrayList<UriBeaconTestHelper> tests;

  public Tests(Context context, ScanResult result) {
    mContext = context;
    Log.d(TAG, "Initializing Tests");
    mResult = result;
  }
  public void setCallback(TestCallback testCallback) {
    mTestCallback = testCallback;
    initializeTests();
  }
  private void initializeTests() {
    tests = new ArrayList<>(Arrays.asList(
        new UriBeaconTestHelper.Builder()
            .name("Write & Read URL")
            .setUp(mContext, mResult, ProtocolV2.CONFIG_SERVICE_UUID, mTestCallback)
            .connect()
            .write(DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .connect()
            .assertEquals(DATA, "test".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build(),
        new UriBeaconTestHelper.Builder()
            .name("Test that's supposed to fail")
            .setUp(mContext, mResult, ProtocolV2.CONFIG_SERVICE_UUID, mTestCallback)
            .connect()
            .write(DATA, "01234567890123456789".getBytes(), BluetoothGatt.GATT_SUCCESS)
            .disconnect()
            .build()
    ));
  }
  public ListIterator<UriBeaconTestHelper> iterator() {
    return tests.listIterator();
  }
}

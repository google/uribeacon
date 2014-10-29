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

package org.uribeacon.scan.compat;

import org.uribeacon.scan.compat.ScanFilter;
import org.uribeacon.scan.compat.ScanRecord;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.testing.TestData;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.test.AndroidTestCase;
import android.util.SparseArray;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Unit tests for the {@link org.uribeacon.scan.compat.ScanFilter} class.
 */
public class ScanFilterTest extends AndroidTestCase {

  // This is the service data UUID in TestData.sd1
    private static final ParcelUuid SERVICE_DATA_UUID =
            ParcelUuid.fromString("000000E0-0000-1000-8000-00805F9B34FB");

    private ScanResult mScanResult;
    private ScanFilter.Builder mFilterBuilder;

    @Override
    protected void setUp() throws Exception {
        byte[] scanRecord = new byte[] {
            0x02, 0x01, 0x1a, // advertising flags
            0x05, 0x02, 0x0b, 0x11, 0x0a, 0x11, // 16 bit service uuids
            0x04, 0x09, 0x50, 0x65, 0x64, // setName
            0x02, 0x0A, (byte) 0xec, // tx power level
            0x05, 0x16, 0x0b, 0x11, 0x50, 0x64, // service data
            0x05, (byte) 0xff, (byte) 0xe0, 0x00, 0x02, 0x15, // manufacturer specific data
            0x03, 0x50, 0x01, 0x02, // an unknown data type won't cause trouble
        };

        mScanResult = new ScanResult(null /* device */, ScanRecord.parseFromBytes(scanRecord),
            -10, 1397545200000000L);
        mFilterBuilder = new ScanFilter.Builder();
    }

    // Stolen shamelessly from Android
    public void testSetNameFilter() {
        ScanFilter filter = mFilterBuilder.setDeviceName("Ped").build();
        assertTrue("setName filter fails", filter.matches(mScanResult));

        filter = mFilterBuilder.setDeviceName("Pem").build();
        assertFalse("setName filter fails", filter.matches(mScanResult));
    }

    // Stolen shamelessly from Android
    public void testSetServiceUuidFilter() {
        ScanFilter filter = mFilterBuilder.setServiceUuid(
                ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB")).build();
        assertTrue("uuid filter fails", filter.matches(mScanResult));

        filter = mFilterBuilder.setServiceUuid(
                ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB")).build();
        assertFalse("uuid filter fails", filter.matches(mScanResult));

        filter = mFilterBuilder
                .setServiceUuid(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
                        ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
                .build();
        assertTrue("uuid filter fails", filter.matches(mScanResult));
    }

    // Stolen shamelessly from Android
    public void testSetServiceDataFilter() {
        byte[] setServiceData = new byte[] {
                0x50, 0x64 };
        ParcelUuid serviceDataUuid = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
        ScanFilter filter = mFilterBuilder.setServiceData(serviceDataUuid, setServiceData).build();
        assertTrue("service data filter fails", filter.matches(mScanResult));

        byte[] emptyData = new byte[0];
        filter = mFilterBuilder.setServiceData(serviceDataUuid, emptyData).build();
        assertTrue("service data filter fails", filter.matches(mScanResult));

        byte[] prefixData = new byte[] {
                0x50 };
        filter = mFilterBuilder.setServiceData(serviceDataUuid, prefixData).build();
        assertTrue("service data filter fails", filter.matches(mScanResult));

        byte[] nonMatchData = new byte[] {
                0x51, 0x64 };
        byte[] mask = new byte[] {
                (byte) 0x00, (byte) 0xFF };
        filter = mFilterBuilder.setServiceData(serviceDataUuid, nonMatchData, mask).build();
        assertTrue("partial service data filter fails", filter.matches(mScanResult));

        filter = mFilterBuilder.setServiceData(serviceDataUuid, nonMatchData).build();
        assertFalse("service data filter fails", filter.matches(mScanResult));
    }

    // Stolen shamelessly from Android
    public void testManufacturerSpecificData() {
        byte[] setManufacturerData = new byte[] {
                0x02, 0x15 };
        int manufacturerId = 0xE0;
        ScanFilter filter =
                mFilterBuilder.setManufacturerData(manufacturerId, setManufacturerData).build();
        assertTrue("manufacturer data filter fails", filter.matches(mScanResult));

        byte[] emptyData = new byte[0];
        filter = mFilterBuilder.setManufacturerData(manufacturerId, emptyData).build();
        assertTrue("manufacturer data filter fails", filter.matches(mScanResult));

        byte[] prefixData = new byte[] {
                0x02 };
        filter = mFilterBuilder.setManufacturerData(manufacturerId, prefixData).build();
        assertTrue("manufacturer data filter fails", filter.matches(mScanResult));

        // Test data mask
        byte[] nonMatchData = new byte[] {
                0x02, 0x14 };
        filter = mFilterBuilder.setManufacturerData(manufacturerId, nonMatchData).build();
        assertFalse("manufacturer data filter fails", filter.matches(mScanResult));
        byte[] mask = new byte[] {
                (byte) 0xFF, (byte) 0x00
        };
        filter = mFilterBuilder.setManufacturerData(manufacturerId, nonMatchData, mask).build();
        assertTrue("partial setManufacturerData filter fails", filter.matches(mScanResult));
    }
    
    public void testManufacturerData() {
        byte[] scanRecord = TestData.manu_data_1;
        // Verify manufacturer with no data
        byte[] data = { (byte) 0x15 };
        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e0, data)
            .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testManufacturerDataNoMatch() {
        byte[] scanRecord = TestData.manu_data_1;
        // Verify manufacturer with no data
        byte[] data = { (byte) 0xe1, (byte) 0x00 };
        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e1, data)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testManufacturerDataNotInScanRecord() {
        byte[] scanRecord = TestData.adv_2;
        // Verify manufacturer with no data
        byte[] data = { (byte) 0xe0, (byte) 0x00 };
        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e0, data)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testManufacturerDataMask() {
        byte[] scanRecord = TestData.manu_data_1;

        // Verify matching partial manufacturer with data and mask
        byte[] data = { (byte) 0x15 };
        byte[] mask = { (byte) 0xff };

        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e0, data, mask)
            .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testManufacturerDataMaskNoMatch() {
        byte[] scanRecord = TestData.manu_data_1;

        // Verify non-matching partial manufacturer with data and mask
        byte[] data = { (byte) 0x10 };
        byte[] mask = { (byte) 0xff };
        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e0, data, mask)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testManufacturerDataMaskNotInScanRecord() {
        byte[] scanRecord = TestData.adv_2;

        // Verify matching partial manufacturer with data and mask
        byte[] data = { (byte) 0x15 };
        byte[] mask = { (byte) 0xff };

        ScanFilter filter = mFilterBuilder
            .setManufacturerData(0x00e0, data, mask)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceData() {
        byte[] scanRecord = TestData.sd1;
        byte serviceData[] = { (byte) 0x15 };

        // Verify manufacturer 2-byte UUID with no data
        ScanFilter filter = mFilterBuilder
                .setServiceData(SERVICE_DATA_UUID, serviceData)
                .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testServiceDataNoMatch() {
        byte[] scanRecord = TestData.sd1;
        byte serviceData[] = { (byte) 0xe1, (byte) 0x00 };

        // Verify manufacturer 2-byte UUID with no data
        ScanFilter filter = mFilterBuilder
                .setServiceData(SERVICE_DATA_UUID, serviceData)
                .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceDataUuidNotInScanRecord() {
        byte[] scanRecord = TestData.eir_1;
        byte serviceData[] = { (byte) 0xe0, (byte) 0x00 };

        // Verify Service Data with 2-byte UUID, no data, and NOT in scan record
        ScanFilter filter = mFilterBuilder
                .setServiceData(SERVICE_DATA_UUID, serviceData)
                .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceDataMask() {
        byte[] scanRecord = TestData.sd1;
        ScanFilter filter;

        // Verify matching partial manufacturer with data and mask
        byte[] serviceData1 = { (byte) 0x15 };
        byte[] mask1 = { (byte) 0xff };
        filter = mFilterBuilder
            .setServiceData(SERVICE_DATA_UUID, serviceData1, mask1)
            .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testServiceDataMaskNoMatch() {
        byte[] scanRecord = TestData.sd1;
        ScanFilter filter;

        // Verify non-matching partial manufacturer with data and mask
        byte[] serviceData2 = { (byte) 0xe0, (byte) 0x00, (byte) 0x10 };
        byte[] mask2 = { (byte) 0xff, (byte) 0xff, (byte) 0xff };
        filter = mFilterBuilder
            .setServiceData(SERVICE_DATA_UUID, serviceData2, mask2)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceDataMaskNotInScanRecord() {
        byte[] scanRecord = TestData.eir_1;
        ScanFilter filter;

        // Verify matching partial manufacturer with data and mask
        byte[] serviceData1 = { (byte) 0xe0, (byte) 0x00, (byte) 0x15 };
        byte[] mask1 = { (byte) 0xff, (byte) 0xff, (byte) 0xff };
        filter = mFilterBuilder
            .setServiceData(SERVICE_DATA_UUID, serviceData1, mask1)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testName() {
        // Verify the name filter matches
        byte[] scanRecord = TestData.adv_1;
        ScanFilter filter = mFilterBuilder
            .setDeviceName("Pedometer")
            .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testNameNoMatch() {
        // Verify the name filter does not match
        byte[] scanRecord = TestData.adv_1;
        ScanFilter filter = mFilterBuilder
            .setDeviceName("Foo")
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testNameNotInScanRecord() {
        // Verify the name filter does not match
        byte[] scanRecord = TestData.eir_1;
        ScanFilter filter = mFilterBuilder
            .setDeviceName("Pedometer")
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceUuid() {
        byte[] scanRecord = TestData.manu_data_1;
        ParcelUuid uuid = ParcelUuid.fromString("00001804-0000-1000-8000-00805F9B34FB");

        ScanFilter filter = mFilterBuilder
            .setServiceUuid(uuid)
            .build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testServiceUuidNoMatch() {
        // Verify the name filter does not match
        byte[] scanRecord = TestData.manu_data_1;
        ParcelUuid uuid = ParcelUuid.fromString("00001804-0000-1000-8000-000000000000");

        ScanFilter filter = mFilterBuilder
            .setServiceUuid(uuid)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceUuidNotInScanRecord() {
        // Verify the name filter does not match
        byte[] scanRecord = TestData.eir_1;
        ParcelUuid uuid = ParcelUuid.fromString("00001804-0000-1000-8000-000000000000");

        ScanFilter filter = mFilterBuilder
            .setServiceUuid(uuid)
            .build();
        assertFalse(matches(filter, null, 0, scanRecord));
    }

    public void testServiceUuidMask() {
        byte[] scanRecord = TestData.manu_data_1;
        ParcelUuid uuid = ParcelUuid.fromString("00000000-0000-0000-0000-00805F9B34FB");
        ParcelUuid mask = ParcelUuid.fromString("00000000-0000-0000-0000-FFFFFFFFFFFF");

        ScanFilter filter = mFilterBuilder
            .setServiceUuid(uuid, mask).build();
        assertMatches(filter, null, 0, scanRecord);
    }

    public void testMacAddress() {
        byte[] scanRecord = TestData.manu_data_1;
        String macAddress = "00:11:22:33:AA:BB";
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        // Can only be tested on devices
        if (adapter != null) {
          BluetoothDevice device = adapter.getRemoteDevice(macAddress);

          ScanFilter filter = mFilterBuilder
              .setDeviceAddress(macAddress).build();
          assertMatches(filter, device, 0, scanRecord);
        }
    }

    public void testMacAddressNoMatch() {
        byte[] scanRecord = TestData.manu_data_1;
        String macAddress = "00:11:22:33:AA:00";
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        // Can only be tested on devices
        if (adapter != null) {
          BluetoothDevice device = adapter.getRemoteDevice("00:11:22:33:AA:BB");
          ScanFilter filter = mFilterBuilder
              .setDeviceAddress(macAddress).build();
          assertFalse(matches(filter, device, 0, scanRecord));
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    private static boolean matches(
        ScanFilter filter, BluetoothDevice device, int rssi, byte[] scanRecord) {
      return filter.matches(new ScanResult(
          device, ScanRecord.parseFromBytes(scanRecord), rssi, 0 /* timestampNanos */));
    }

    // Inspired by the "L" implementation
    private static void assertMatches(
        ScanFilter filter, BluetoothDevice device, int rssi, byte[] scanRecordBytes) {

      // Device match.
      if (filter.getDeviceAddress() != null
          && (device == null || !filter.getDeviceAddress().equals(device.getAddress()))) {
        fail("Filter specified a device address (" + filter.getDeviceAddress()
            + ") which doesn't match the actual value: ["
            + (device == null ? "null device" : device.getAddress()) + "]");
      }

      // Scan record is null but there exist filters on it.
      ScanRecord scanRecord = ScanRecord.parseFromBytes(scanRecordBytes);
      if (scanRecord == null
          && (filter.getDeviceName() != null || filter.getServiceUuid() != null
          || filter.getManufacturerData() != null || filter.getServiceData() != null)) {
        fail("The scanRecordBytes given parsed to a null scanRecord, but the filter"
            + "has a non-null field which depends on the scan record");
      }

      // Local name match.
      if (filter.getDeviceName() != null
          && !filter.getDeviceName().equals(scanRecord.getDeviceName())) {
        fail("The filter's device name (" + filter.getDeviceName()
            + ") doesn't match the scan record device name (" + scanRecord.getDeviceName() + ")");
      }

      // UUID match.
      if (filter.getServiceUuid() != null
          && !matchesServiceUuids(filter.getServiceUuid(), filter.getServiceUuidMask(),
              scanRecord.getServiceUuids())) {
        fail("The filter specifies a service UUID but it doesn't match what's in the scan record");
      }

      // Service data match
      if (filter.getServiceDataUuid() != null && !ScanFilter.matchesPartialData(
          filter.getServiceData(),
          filter.getServiceDataMask(),
          scanRecord.getServiceData(filter.getServiceDataUuid()))) {
        fail("The filter's service data doesn't match what's in the scan record.\n"
            + "Service data: " + byteString(filter.getServiceData()) + "\n"
            + "Service data UUID: " + filter.getServiceDataUuid().toString() + "\n"
            + "Service data mask: " + byteString(filter.getServiceDataMask()) + "\n"
            + "Scan record service data: "
            + byteString(scanRecord.getServiceData(filter.getServiceDataUuid())) + "\n"
            + "Scan record data map:\n"
            + byteString(scanRecord.getServiceData()));
      }

      // Manufacturer data match.
      if (filter.getManufacturerId() >= 0 && !ScanFilter.matchesPartialData(
          filter.getManufacturerData(),
          filter.getManufacturerDataMask(),
          scanRecord.getManufacturerSpecificData(filter.getManufacturerId()))) {
        fail("The filter's manufacturer data doesn't match what's in the scan record.\n"
            + "Manufacturer ID: " + filter.getManufacturerId() + "\n"
            + "Manufacturer data: " + byteString(filter.getManufacturerData()) + "\n"
            + "Manufacturer data mask: " + byteString(filter.getManufacturerDataMask()) + "\n"
            + "Scan record manufacturer-specific data: "
            + byteString(scanRecord.getManufacturerSpecificData(filter.getManufacturerId())) + "\n"
            + "Manufacturer data array:\n"
            + byteString(scanRecord.getManufacturerSpecificData()));
      }

      // All filters match.
      assertTrue("Okay, that REALLY should have matched!    Something's broken in assertMatches().",
          matches(filter, device, rssi, scanRecordBytes));
    }

    private static String byteString(Map<ParcelUuid, byte[]> bytesMap) {
      StringBuilder builder = new StringBuilder();
      for (Entry<ParcelUuid, byte[]> entry : bytesMap.entrySet()) {
        builder.append(builder.toString().isEmpty() ? "  " : "\n  ");
        builder.append(entry.getKey().toString());
        builder.append(" --> ");
        builder.append(byteString(entry.getValue()));
      }
      return builder.toString();
    }

    private static String byteString(SparseArray<byte[]> bytesArray) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < bytesArray.size(); i++) {
        builder.append(builder.toString().isEmpty() ? "  " : "\n  ");
        builder.append(byteString(bytesArray.valueAt(i)));
      }
      return builder.toString();
    }

    private static String byteString(byte[] bytes) {
      if (bytes == null) {
        return "[null]";
      } else {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
          int v = bytes[i] & 0xFF;
          hexChars[i * 2] = hexArray[v >>> 4];
          hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
      }
    }

    // Inspired by the "L" implementation
    private static boolean matchesServiceUuids(ParcelUuid uuid, ParcelUuid parcelUuidMask,
        List<ParcelUuid> uuids) {
      if (uuid == null) {
        return true;
      }
      if (uuids == null) {
        return false;
      }

      for (ParcelUuid parcelUuid : uuids) {
        UUID uuidMask = parcelUuidMask == null ? null : parcelUuidMask.getUuid();
        if (matchesServiceUuid(uuid.getUuid(), uuidMask, parcelUuid.getUuid())) {
          return true;
        }
      }
      return false;
    }

    // Check if the uuid pattern matches the particular service uuid.
    private static boolean matchesServiceUuid(UUID uuid, UUID mask, UUID data) {
      if (mask == null) {
        return uuid.equals(data);
      }
      if ((uuid.getLeastSignificantBits() & mask.getLeastSignificantBits())
          != (data.getLeastSignificantBits() & mask.getLeastSignificantBits())) {
        return false;
      }
      return ((uuid.getMostSignificantBits() & mask.getMostSignificantBits())
          == (data.getMostSignificantBits() & mask.getMostSignificantBits()));
    }
}

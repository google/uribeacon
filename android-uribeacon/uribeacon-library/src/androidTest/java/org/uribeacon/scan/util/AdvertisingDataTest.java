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

package org.uribeacon.scan.util;

import org.uribeacon.scan.testing.TestData;
import org.uribeacon.scan.util.AdvertisingData;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for the {@link org.uribeacon.scan.util.AdvertisingData} class.
 */
public class AdvertisingDataTest extends AndroidTestCase {
  public void testGetName() {
    String name;
    name = AdvertisingData.getName(TestData.eir_1);
    assertEquals(name, "Phone");
    name = AdvertisingData.getName(TestData.adv_1);
    assertEquals(name, "Pedometer");
    name = AdvertisingData.getName(TestData.adv_2);
    assertNull(name);
  }

  public void testGetServiceUuids() {
    // PANU service class UUID
    final UUID panuUuid = UUID.fromString("00001115-0000-1000-8000-00805F9B34FB");
    final UUID handsFreeUuid = UUID.fromString("0000111F-0000-1000-8000-00805F9B34FB");
    final UUID testUuid128 = UUID.fromString("18171615-1413-1211-0807-060504030201");
    List<UUID> uuids = AdvertisingData.getServiceUuids(TestData.eir_1);
    assertEquals(3, uuids.size());
    assertEquals(panuUuid, uuids.get(0));
    assertEquals(handsFreeUuid, uuids.get(1));
    assertEquals(testUuid128, uuids.get(2));

    final UUID txPowerUuuid = UUID.fromString("00001804-0000-1000-8000-00805F9B34FB");
    uuids = AdvertisingData.getServiceUuids(TestData.manu_data_1);
    assertEquals(1, uuids.size());
    assertEquals(txPowerUuuid, uuids.get(0));
  }

  public void testGetTxPowerLevel() {
    Integer tx;
    tx = AdvertisingData.getTxPowerLevel(TestData.adv_1);
    assertNull(tx);
    tx = AdvertisingData.getTxPowerLevel(TestData.adv_2);
    assertEquals(Integer.valueOf(127), tx);
    tx = AdvertisingData.getTxPowerLevel(TestData.adv_3);
    assertEquals(Integer.valueOf(-127), tx);
  }

  public void testGetManufacturerDataCode() {
    Integer code;
    code = AdvertisingData.getManufacturerCode(TestData.ibeacon_2);
    assertEquals(0x004c, (int) code);
    code = AdvertisingData.getManufacturerCode(TestData.manu_data_2);
    assertEquals(0x0550, (int) code);
    code = AdvertisingData.getManufacturerCode(TestData.eir_1);
    assertNull(code);
  }

  public void testGetManufacturerData() {
    byte[] data;
    data = AdvertisingData.getManufacturerData(TestData.manu_data_1);
    assertTrue(Arrays.equals(TestData.manu_data_1_data, data));
    data = AdvertisingData.getManufacturerData(TestData.eir_1);
    assertNull(data);
  }

  public void testGetManufacturerDataOffset() {
    int pos1 = AdvertisingData.getManufacturerDataOffset(TestData.manu_data_1);
    // gbeacon1 manufacturing data starts at offset 11
    assertEquals(11, pos1);
    Integer pos2 = AdvertisingData.getManufacturerDataOffset(TestData.eir_1);
    assertNull(pos2);
  }

  public void testGetServiceDataId() {
    Integer code;
    code = AdvertisingData.getServiceDataId(TestData.sd1);
    assertEquals(0x00e0, (int) code);
  }

  public void testGetServiceData() {
    byte[] data;
    data = AdvertisingData.getServiceData(TestData.sd1);
    assertTrue(Arrays.equals(TestData.sd1_data, data));
  }

  public void testGetServiceDataOffset() {
    int pos1 = AdvertisingData.getServiceDataOffset(TestData.sd1);
    // sd1 manufacturing data starts at offset 11
    assertEquals(11, pos1);
  }

  public void testGetUnsignedByte() {
    // Testing
    final byte[] data = {(byte) 0x01, (byte) 0xff};
    // Test a value with no sign
    int value1 = AdvertisingData.getUnsignedByte(data, 0);
    assertEquals(0x01, value1);
    // Test a value with potential sign extend
    int value2 = AdvertisingData.getUnsignedByte(data, 1);
    assertEquals(0xff, value2);
  }

  public void testGetShort() {
    final byte[] data = {(byte) 0x01, (byte) 0x02, (byte) 0x03};
    // Test for unsigned value
    short value1 = AdvertisingData.getShort(data, 0);
    assertEquals((short) 0x0201, value1);
    short value2 = AdvertisingData.getShort(data, 1);
    assertEquals((short) 0x0302, value2);
  }

  public void testGetInt() {
    final byte[] data = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05};
    int value1 = AdvertisingData.getInt(data, 0);
    assertEquals(0x04030201, value1);
    short value2 = AdvertisingData.getShort(data, 1);
    assertNotSame(0x05040302, value2);
  }

  public void testGetLong() {
    final byte[] data = {(byte) 0x01,
        (byte) 0x02,
        (byte) 0x03,
        (byte) 0x04,
        (byte) 0x05,
        (byte) 0x06,
        (byte) 0x07,
        (byte) 0x08,
        (byte) 0x09};
    long value1 = AdvertisingData.getLong(data, 0);
    assertEquals(0x0807060504030201L, value1);
    long value2 = AdvertisingData.getLong(data, 1);
    assertNotSame(0x0908070605040302L, value2);
  }

  public void testGetBytesBigEndianAsLong() {
    final byte[] data =
        {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06};
    long value1 = AdvertisingData.getBytesBigEndianAsLong(data, 0, 5);
    assertEquals(0x0102030405L, value1);
    long value2 = AdvertisingData.getBytesBigEndianAsLong(data, 1, 5);
    assertNotSame(0x0102030405L, value2);
    long value3 = AdvertisingData.getBytesBigEndianAsLong(data, 0, 4);
    assertNotSame(0x0102030405L, value3);
  }


}

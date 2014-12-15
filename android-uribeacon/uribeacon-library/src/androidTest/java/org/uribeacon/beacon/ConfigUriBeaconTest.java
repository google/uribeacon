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

package org.uribeacon.beacon;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import junit.framework.Assert;

import java.net.URISyntaxException;

public class ConfigUriBeaconTest extends AndroidTestCase{

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public byte[] makeTxPowerLevelArray(byte value) {
    return new byte[]{value, value, value, value};
  }

  public void testNotSettingAnything() throws URISyntaxException {
    try {
      new ConfigUriBeacon.Builder().build();
      Assert.fail("Should fail");
    } catch (IllegalArgumentException e) {
      assertEquals("UriBeacon advertisements must include a URI", e.getMessage());
    }
  }

  /////////////////
  ////// URI //////
  /////////////////
  public void testWithEmptyUriString() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon =  new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .build();
    assertEquals(TestData.emptyTestString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.emptyTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testWithEmptyUriByteArray() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestByteArray)
        .build();
    assertEquals(TestData.emptyTestString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.emptyTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testInvalidUriString() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.malformedUrlString)
          .build();
      Assert.fail("Should have failed");
    } catch (URISyntaxException e) {
      assertEquals("Not a valid URI", e.getReason());
    }
  }

  public void testInvalidUriByteArray() throws URISyntaxException {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.malformedUrlByteArray)
          .build();
      Assert.fail("Should fail");
    } catch (IllegalArgumentException e) {
      assertEquals("Could not decode URI", e.getMessage());
    }
  }

  public void testValidUrlString() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.urlTestString)
        .build();
    assertEquals(TestData.urlTestString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.urlTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testValidUrlByteArray() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.urlTestByteArray)
        .build();
    assertEquals(TestData.urlTestString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.urlTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testValidLongUrlString() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.longButValidUrlString)
        .build();
      assertEquals(TestData.longButValidUrlString, configUriBeacon.getUriString());
      MoreAsserts.assertEquals(TestData.longButValidUrlByteArray, configUriBeacon.getUriBytes());
  }

  public void testValidLongUrlByteArray() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.longButValidUrlByteArray)
        .build();
    assertEquals(TestData.longButValidUrlString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.longButValidUrlByteArray, configUriBeacon.getUriBytes());
  }

  public void testLongInvalidUrlString() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.longButInvalidUrlString)
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }

  public void testLongInvalidUrlByteArray() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.longButInvalidUrlByteArray)
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }
  //////////////////
  ////// LOCK //////
  //////////////////
  public void testFalseLock() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .lockState(false)
        .build();
    assertEquals(false, configUriBeacon.getLockState());
  }

  public void testTrueLock() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .lockState(true)
        .build();
    assertEquals(true, configUriBeacon.getLockState());
  }

  /////////////////
  ////// KEY //////
  /////////////////
  public void testCorrectLengthKey() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .key(TestData.validKey)
        .build();
    MoreAsserts.assertEquals(TestData.validKey, configUriBeacon.getKey());
  }
  //TODO: add more key tests

  /////////////////////
  ////// FLAGS ////////
  /////////////////////
  public void testNoFlags() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .build();
    assertEquals(0, configUriBeacon.getFlags());
  }

  public void testOneFlag() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .flags((byte) 1)
        .build();
    assertEquals(1, configUriBeacon.getFlags());
  }

  /////////////////////////////
  ////// TX POWER LEVELS //////
  /////////////////////////////
  public void testNotSettingTxPowerLevels() throws URISyntaxException {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .beaconPeriod(TestData.validPeriod)
          .txPowerMode(TestData.validTxPowerMode)
          .build();
      Assert.fail("Should fail");
    } catch (IllegalArgumentException e) {
      assertEquals("Must include Tx AdvertisedPowerLevels", e.getMessage());
    }
  }

  public void testEmptyTxPowerLevels() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(TestData.validPeriod)
          .advertisedTxPowerLevels(new byte[0])
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Invalid length for Tx Advertised Power Levels", e.getReason());
    }
  }

  public void testShorterTxPowerLevels() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(new byte[3])
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(TestData.validPeriod)
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Invalid length for Tx Advertised Power Levels", e.getReason());
    }
  }

  public void testLongerTxPowerLevels() throws URISyntaxException {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(new byte[5])
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(TestData.validPeriod)
          .build();
      Assert.fail("Should have failed");
    } catch (URISyntaxException e) {
      assertEquals("Invalid length for Tx Advertised Power Levels", e.getReason());
    }
  }

  public void testLowTxPowerLevels() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(
              (byte) (ConfigUriBeacon.TX_POWER_LEVEL_MIN_VALUE -1)))
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(TestData.validPeriod)
          .build();
      Assert.fail("Should have failed");
    } catch (URISyntaxException e) {
      assertEquals("Invalid TxPower Level", e.getReason());
    }
  }

  public void testHighTxPowerLevels() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(
              (byte) (ConfigUriBeacon.TX_POWER_LEVEL_MAX_VALUE + 1)))
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(TestData.validPeriod)
          .build();
      Assert.fail("Should have failed");
    } catch (URISyntaxException e) {
      assertEquals("Invalid TxPower Level", e.getReason());
    }
  }

  public void testCorrectPowerLevelsOnly() throws URISyntaxException {
    try {
      for (byte i = ConfigUriBeacon.TX_POWER_LEVEL_MIN_VALUE;
           i <= ConfigUriBeacon.TX_POWER_LEVEL_MAX_VALUE; i++) {
        new ConfigUriBeacon.Builder()
            .uriString(TestData.emptyTestString)
            .advertisedTxPowerLevels(makeTxPowerLevelArray(i))
            .build();
        Assert.fail("Should fail");
      }
    } catch (IllegalArgumentException e) {
      assertEquals("Must include Tx Power Mode", e.getMessage());
    }
  }

  public void testCorrectPowerLevels() throws URISyntaxException {
    for (byte i = ConfigUriBeacon.TX_POWER_LEVEL_MIN_VALUE;
         i <= ConfigUriBeacon.TX_POWER_LEVEL_MAX_VALUE; i++) {
      byte[] testTxPowerLevels = makeTxPowerLevelArray(i);
      ConfigUriBeacon beacon = new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(testTxPowerLevels)
          .beaconPeriod(TestData.validPeriod)
          .txPowerMode(TestData.validTxPowerMode)
          .build();
      MoreAsserts.assertEquals(testTxPowerLevels, beacon.getAdvertisedTxPowerLevels());
    }
  }
  ////////////////////////
  ////// POWER MODE //////
  ////////////////////////

  public void testNotSetPowerMode() throws URISyntaxException {
    try {
      ConfigUriBeacon beacon =  new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .beaconPeriod(TestData.validPeriod)
          .build();
      Assert.fail("Should fail");
    } catch (IllegalArgumentException e) {
      assertEquals("Must include Tx Power Mode", e.getMessage());
    }
  }

  public void testSetLowInvalidPowerMode() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .beaconPeriod(TestData.validPeriod)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode((byte) (ConfigUriBeacon.POWER_MODE_NONE - 1))
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Unknown power mode", e.getReason());
    }
  }

  public void testSetHighInvalidPowerMode() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .beaconPeriod(TestData.validPeriod)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode((byte) (ConfigUriBeacon.POWER_MODE_HIGH + 1))
          .build();
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Unknown power mode", e.getReason());
    }
  }

  public void testSetValidPowerMode() throws URISyntaxException {
    for (byte i = ConfigUriBeacon.POWER_MODE_ULTRA_LOW; i <= ConfigUriBeacon.POWER_MODE_HIGH;
        i++) {
      ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .beaconPeriod(TestData.validPeriod)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode(i)
          .build();
      assertEquals(i, configUriBeacon.getTxPowerMode());
    }
  }

  ///////////////////////////
  ////// Beacon Period //////
  ///////////////////////////
  public void testNotSetPeriod() throws URISyntaxException {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode(TestData.validTxPowerMode)
          .build();
      Assert.fail("Should fail");
    } catch (IllegalArgumentException e) {
      assertEquals("Need Broadcasting Period", e.getMessage());
    }
  }

  public void testInvalidLowPeriod() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(ConfigUriBeacon.PERIOD_NONE - 1)
          .build();
    } catch (URISyntaxException e) {
      assertEquals("Invalid broadcasting period", e.getReason());
    }
  }

  public void testInvalidHighPeriod() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(ConfigUriBeacon.UINT16_MAX_VALUE)
          .build();
    } catch (URISyntaxException e) {
      assertEquals("Invalid broadcasting period", e.getReason());
    }
  }

  public void testValidPeriod() throws URISyntaxException {
    for (int i = ConfigUriBeacon.UINT16_MIN_VALUE; i <= ConfigUriBeacon.UINT16_MAX_VALUE;
        i++) {
      ConfigUriBeacon beacon =  new ConfigUriBeacon.Builder()
          .uriString(TestData.emptyTestString)
          .advertisedTxPowerLevels(makeTxPowerLevelArray(TestData.validTxPowerLevels))
          .txPowerMode(TestData.validTxPowerMode)
          .beaconPeriod(i)
          .build();
      assertEquals(i, beacon.getBeaconPeriod());
    }
  }

  ///////////////////
  ////// Reset //////
  ///////////////////
  public void testOnlyTrueReset() throws URISyntaxException {
    ConfigUriBeacon beacon = new ConfigUriBeacon.Builder()
        .reset(true)
        .build();
    assertTrue(beacon.getReset());
  }
  public void testOnlyFalseReset() throws URISyntaxException {
    ConfigUriBeacon beacon = new ConfigUriBeacon.Builder()
        .reset(false)
        .uriString(TestData.emptyTestString)
        .build();
    assertFalse(beacon.getReset());
  }

  //////////////////////////////////
  ////// Create Config Beacon //////
  //////////////////////////////////
  public void testEmptyArrayCreateConfigUriBeacon() throws URISyntaxException {
    ConfigUriBeacon beacon = ConfigUriBeacon.createConfigUriBeacon(TestData.emptyTestByteArray);
    assertEquals(ConfigUriBeacon.NO_URI, beacon.getUriString());
    assertEquals(ConfigUriBeacon.NO_TX_POWER_LEVEL, beacon.getTxPowerLevel());
    assertEquals(ConfigUriBeacon.NO_FLAGS, beacon.getFlags());
  }
  public void testWithNoUriCreateConfigUriBeacon() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = ConfigUriBeacon.createConfigUriBeacon(TestData.emptyAdbPacketBytes);
    assertEquals(ConfigUriBeacon.NO_URI, configUriBeacon.getUriString());
    assertEquals(TestData.noTxPowerLevel, configUriBeacon.getTxPowerLevel());
    assertEquals(TestData.noFlags, configUriBeacon.getFlags());
    MoreAsserts.assertEquals(TestData.emptyTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testInvalidUriStringCreateConfigUriBeacon() throws URISyntaxException {
    try {
      ConfigUriBeacon.createConfigUriBeacon(TestData.invalidUrlAdbPacketBytes);
      Assert.fail("Should have failed");
    } catch (IllegalArgumentException e) {
      assertEquals("Could not decode URI", e.getMessage());
    }
  }

  public void testValidUrlCreateConfigUriBeacon() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = ConfigUriBeacon.createConfigUriBeacon(TestData.validUrlAdbPacketBytes);
    assertEquals(TestData.urlTestString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.urlTestByteArray, configUriBeacon.getUriBytes());
  }

  public void testValidLongUrlCreateConfigUriBeacon() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = ConfigUriBeacon.createConfigUriBeacon(TestData.longValidUrlAdbPacketBytes);
    assertEquals(TestData.longButValidUrlString, configUriBeacon.getUriString());
    MoreAsserts.assertEquals(TestData.longButValidUrlByteArray, configUriBeacon.getUriBytes());
  }

  public void testLongInvalidLongUrlCreateConfigUriBeacon() {
    try {
      ConfigUriBeacon.createConfigUriBeacon(TestData.longInvalidUrlAdbPacketBytes);
      Assert.fail("Should fail");
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }

  ////////////////////////
  ////// Uri Length //////
  ////////////////////////
  public void testEmptyStringLength() throws URISyntaxException {
    assertEquals(TestData.emptyTestString.length(),
        ConfigUriBeacon.uriLength(TestData.emptyTestString));
  }
  //TODO: add test for null
  public void testInvalidUriStringLength() {
    assertEquals(-1, ConfigUriBeacon.uriLength(TestData.malformedUrlString));
  }

  public void testValidUrlStringLength(){
    assertEquals(TestData.urlTestByteArray.length,
        ConfigUriBeacon.uriLength(TestData.urlTestString));
  }

  public void testValidLongUrlStringLength() {
    assertEquals(TestData.longButValidUrlByteArray.length,
        ConfigUriBeacon.uriLength(TestData.longButValidUrlString));
  }

  public void testLongInvalidLongUrlLength() {
    assertEquals(TestData.longButInvalidUrlByteArray.length,
        ConfigUriBeacon.uriLength(TestData.longButInvalidUrlString));
  }
}

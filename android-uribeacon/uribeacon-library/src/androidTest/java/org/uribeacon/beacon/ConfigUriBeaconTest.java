package org.uribeacon.beacon;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import junit.framework.Assert;

import java.net.URISyntaxException;

public class ConfigUriBeaconTest extends AndroidTestCase{

  private static final String SHOULD_NOT_FAIL = "Should not fail: ";
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void setContext(Context context) {
    super.setContext(context);
  }

  public void testNotSettingAnything() {
    try {
      new ConfigUriBeacon.Builder().build();
    } catch (URISyntaxException e) {
      Assert.fail(SHOULD_NOT_FAIL + e.getReason());
    } catch (IllegalArgumentException e) {
      assertEquals("UriBeacon advertisements must include a URI", e.getMessage());
    }
  }
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
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }

  public void testLongInvalidUrlByteArray() {
    try {
      new ConfigUriBeacon.Builder()
          .uriString(TestData.longButInvalidUrlByteArray)
          .build();
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }

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

  public void testShortKey() throws URISyntaxException {
    ConfigUriBeacon configUriBeacon = new ConfigUriBeacon.Builder()
        .uriString(TestData.emptyTestString)
        .key(TestData.shortKey)
        .build();
  }
}

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

import android.content.Context;
import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import junit.framework.Assert;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * Unit tests for the {@link UriBeacon} class.
 */
public class UriBeaconTest extends AndroidTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  public void testEncodeUriWithEmptyString() {
    MoreAsserts.assertEquals(new byte[]{}, UriBeacon.encodeUri(""));
  }
  public void testEncodeUriWithUrlString() {
    MoreAsserts.assertEquals(TestData.urlTestByteArray, UriBeacon.encodeUri(TestData.urlTestString));
  }
  public void testEncodeUriWithUuidString() {
    MoreAsserts.assertEquals(TestData.uuidTestByteArray, UriBeacon.encodeUri(TestData.uuidTestString));
  }
  public void testEncodeUriWithInvalidUrl() {
    assertEquals(null, UriBeacon.encodeUri(TestData.malformedUrlString));
  }
  public void testBuildWithNoUri() {
    try {
      new UriBeacon.Builder().build();
      Assert.fail("Should have failed");
    } catch (IllegalArgumentException e) {

    } catch (URISyntaxException e) {
      Assert.fail("Should have thrown illegal argument exception");
    }
  }
  public void testBuilderWithEmptyStringUri() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString("").build();
      MoreAsserts.assertEquals(new byte[]{}, beacon.getUriBytes());
      assertEquals("", beacon.getUriString());
    } catch (URISyntaxException e) {
      Assert.fail("Should not raise any errors");
    }
  }
  public void testBuilderWithEmptyArrayUri() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(new byte[]{}).build();
      MoreAsserts.assertEquals(new byte[]{}, beacon.getUriBytes());
      assertEquals("", beacon.getUriString());
    } catch (URISyntaxException e) {
      Assert.fail("Should not raise any errors");
    }
  }
  public void testBuilderWithUriString() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.urlTestString).build();
      MoreAsserts.assertEquals(TestData.urlTestByteArray, beacon.getUriBytes());
      assertEquals(TestData.urlTestString, beacon.getUriString());
    } catch (URISyntaxException e) {
      Assert.fail("Should not raise any errors");
    }
  }
  public void testBuilderWithUriByteArray() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.urlTestByteArray).build();
      MoreAsserts.assertEquals(TestData.urlTestByteArray, beacon.getUriBytes());
      assertEquals(TestData.urlTestString, beacon.getUriString());
    } catch (URISyntaxException e) {
      Assert.fail("Should not raise any errors");
    }
  }
  public void testBuilderWithMalformedUriString() {
    try {
      new UriBeacon.Builder().uriString(TestData.malformedUrlString).build();
    } catch (URISyntaxException e) {
      assertEquals("Not a valid URI", e.getReason());
    }
  }
  public void testBuilderWithMalformedUriByteArray() {
    try {
      new UriBeacon.Builder().uriString(TestData.malformedUrlByteArray).build();
    } catch (IllegalArgumentException e) {
      assertEquals("Could not decode URI", e.getMessage());
    } catch (URISyntaxException e) {
      Assert.fail("Should not raise URISyntaxException");
    }
  }
  public void testBuilderWithLongValidUriString() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.longButValidUrlString).build();
      assertEquals(TestData.longButValidUrlString, beacon.getUriString());
      MoreAsserts.assertEquals(TestData.longButValidUrlByteArray, beacon.getUriBytes());
    } catch (URISyntaxException e) {
      Assert.fail("Should not throw: " + e.getClass().getName());
    }
  }
  public void testBuilderWithLongValidUriByteArray() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.longButValidUrlByteArray).build();
      assertEquals(TestData.longButValidUrlString, beacon.getUriString());
      MoreAsserts.assertEquals(TestData.longButValidUrlByteArray, beacon.getUriBytes());
    } catch (URISyntaxException e) {
      Assert.fail("Should not throw: " + e.getClass().getName());
    }
  }
  public void testBuilderWithLongInvalidUriString() {
    try {
      new UriBeacon.Builder().uriString(TestData.longButInvalidUrlString).build();
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }
  public void testBuilderWithLongInvalidUriByteArray() {
    try {
      new UriBeacon.Builder().uriString(TestData.longButInvalidUrlByteArray).build();
    } catch (URISyntaxException e) {
      assertEquals("Uri size is larger than 18 bytes", e.getReason());
    }
  }
  public void testToByteArrayWithEmptyUriString() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString("").build();
      MoreAsserts.assertEquals(TestData.emptyAdbPacketBytes, beacon.toByteArray());
    } catch (URISyntaxException e) {
      Assert.fail("Should not throw: " + e.getClass().getName());
    }
  }
  public void testToByteArrayWithUriString() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.urlTestString).build();
      MoreAsserts.assertEquals(TestData.urlAdbPacketBytes, beacon.toByteArray());
    } catch (URISyntaxException e) {
      Assert.fail("Should not throw: " + e.getClass().getName());
    }
  }
  public void testToByteArrayWithUriByteArray() {
    try {
      UriBeacon beacon = new UriBeacon.Builder().uriString(TestData.urlTestByteArray).build();
      MoreAsserts.assertEquals(TestData.urlAdbPacketBytes, beacon.toByteArray());
    } catch (URISyntaxException e) {
      Assert.fail("Should not throw: " + e.getClass().getName());
    }
  }
  // TODO: Add more tests for toByteArray using parseFromBytes instead of builder
  public void testParseFromBytes() {
    // Check that it disallows this scanRecord
    UriBeacon beacon = UriBeacon.parseFromBytes(TestData.adv_1);
    assertNull(beacon);
    beacon = UriBeacon.parseFromBytes(TestData.adv_2);
    assertEquals(0x20, beacon.getTxPowerLevel());
    assertEquals(0, beacon.getFlags());
    assertEquals("http://www.uribeacon.org", beacon.getUriString());
  }

  // Convert a json array containing bytes and quoted strings into a byte array
  private byte[] jsonToByteArray(JSONArray jsonArray) throws JSONException {
    ByteArrayBuffer bb = new ByteArrayBuffer(31);
    for (int i = 0; i < jsonArray.length(); i++) {
      Object item = jsonArray.get(i);
      if (item instanceof Integer) {
        bb.append(jsonArray.getInt(i));
      } else if (item instanceof String) {
        byte[] itemBytes = ((String) item).getBytes();
        bb.append(itemBytes, 0, itemBytes.length);
      } else {
        throw new JSONException("Unknown type in array");
      }
    }
    return bb.toByteArray();
  }

  private JSONObject inputJson(InputStream is) throws IOException, JSONException {
    BufferedReader streamReader = new BufferedReader(new InputStreamReader(is));
    StringBuilder jsonString = new StringBuilder();
    String line;
    while ((line = streamReader.readLine()) != null) {
      jsonString.append(line);
    }
    return new JSONObject(jsonString.toString());
  }

  public void testUriBeaconTestData() throws JSONException, IOException {
    Context context = getContext();
    AssetManager am = context.getAssets();
    JSONObject testObject = inputJson(am.open("testdata.json"));
    JSONArray testData = testObject.getJSONArray("test-data");
    for (int i = 0; i < testData.length(); i++) {
      JSONObject encodingTest = testData.getJSONObject(i);

      String uri = encodingTest.getString("url");
      assertNotNull(uri);
      Integer txPowerLevel = encodingTest.optInt("tx", 20);
      Integer flags = encodingTest.optInt("flags", 0);
      UriBeacon uriBeacon;
      try {
        uriBeacon = new UriBeacon.Builder()
            .uriString(uri)
            .txPowerLevel(txPowerLevel.byteValue())
            .flags(flags.byteValue())
            .build();
      } catch (URISyntaxException e) {
        uriBeacon = null;
      }
      JSONArray scanRecordJson = encodingTest.optJSONArray("scanRecord");
      if (scanRecordJson == null || uriBeacon == null) {
        assertNull("Null assert failed for uriBeacon", scanRecordJson);
        assertNull("Null assert failed for " + uriBeacon, uriBeacon);
      } else {
        byte[] scanRecord = jsonToByteArray(scanRecordJson);
        byte[] uriBeaconScanRecord = uriBeacon.toByteArray();
        MoreAsserts.assertEquals(scanRecord, uriBeaconScanRecord);
      }
    }
  }
}
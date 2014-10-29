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

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Unit tests for the {@link UriBeacon} class.
 */
public class UriBeaconTest extends AndroidTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }


  public void testParseFromBytes() {
    // Check that it disallows this scanRecord
    UriBeacon beacon = UriBeacon.parseFromBytes(TestData.adv_1);
    assertNull(beacon);
    beacon = UriBeacon.parseFromBytes(TestData.adv_2);
    assertEquals(0x20, beacon.getTxPowerLevel());
    assertEquals(0, beacon.getFlags());
    assertEquals("http://www.uribeacon.org", beacon.getUriString());
  }

  public void testEncodeUri() {
    byte[] encodedUri_1 = {
        0x00,
        // URI
        'u', 'r', 'i', 'b', 'e', 'a', 'c', 'o', 'n',
        // Expansion code for .org
        0x08
    };
    byte[] result = UriBeacon.encodeUri("http://www.uribeacon.org");
    MoreAsserts.assertEquals(encodedUri_1, result);
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
        assertTrue("Assert failed for " + uriBeacon, Arrays.equals(scanRecord, uriBeaconScanRecord));
      }
    }
  }
}
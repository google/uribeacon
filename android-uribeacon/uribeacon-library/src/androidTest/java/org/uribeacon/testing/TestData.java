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
package org.uribeacon.scan.testing;

/**
 * See: Supplement to the Bluetooth Core Specification, Version 4,
 * Adoption Date: 03 December 2013
 * <P>
 * Sample test data of various species of beacons.
 */
public class TestData {
  // This is an example extended inquiry response for a phone with PANU
  // and Hands-free Audio Gateway
  public static final byte[] eir_1 = {
    0x06, // Length of this Data
    0x09, // <<Complete Local Name>>
    'P', 'h', 'o', 'n', 'e', 0x05, // Length of this Data
    0x03, // <<Complete list of 16-bit Service UUIDs>>
    0x15, 0x11, // PANU service class UUID
    0x1F, 0x11, // Hands-free Audio Gateway service class UUID

    0x01, // Length of this data
    0x05, // <<Complete list of 32-bit Service UUIDs>>

    0x11, // Length of this data
    0x07, // <<Complete list of 128-bit Service UUIDs>>
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, // Made up UUID
    0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, //
    0x00 // End of Data (Not transmitted over the air
  };

  // This is an example of advertising data with AD types
  public static final byte[] adv_1 = {
    0x02, // Length of this Data
    0x01, // <<Flags>>
    0x01, // LE Limited Discoverable Mode
    0x0A, // Length of this Data
    0x09, // <<Complete local name>>
    'P', 'e', 'd', 'o', 'm', 'e', 't', 'e', 'r'
  };

  // This is an example of advertising data with positive TX Power
  // Level.
  public static final byte[] adv_2 = {
    0x02, // Length of this Data
    0x0a, // <<TX Power Level>>
    127 // Level = 127
  };

  // This is an example of advertising data with negative TX Power
  // Level.
  public static final byte[] adv_3 = {
    0x02, // Length of this Data
    0x0a, // <<TX Power Level>>
    -127 // Level = -127
  };

  // Manufacturer data packet 1
  public static final byte[] manu_data_1 = {
    0x02, // Length of this Data
    0x01, // <<Flags>>
    0x04, // BR/EDR Not Supported.
    0x03, // Length of this Data
    0x02, // <<Incomplete List of 16-bit Service UUIDs>>
    0x04, 0x18, // TX Power Service UUID

    0x1e, // Length of this Data
    (byte) 0xff, // <<Manufacturer Specific Data>>
    // Company Identifier Code = Google
    (byte) 0xe0, 0x00,
    // Header
    0x15,
    // Running time ENCRYPT
    (byte) 0xd2, 0x77, 0x01, 0x00,
    // Scan Freq ENCRYPT
    0x32, 0x05,
    // Time in slow mode
    0x00, 0x00,
    // Time in fast mode
    0x7f, 0x17,
    // Subset of UID
    0x56, 0x00,
    // ID Mask
    (byte) 0xd4, 0x7c, 0x18,
    // RFU (reserved)
    0x00,
    // GUID = decimal 1297482358
    0x76, 0x02, 0x56, 0x4d, 0x00,
    // Ranging Payload Header
    0x24,
    // MAC of scanning address
    (byte) 0xa4, (byte) 0xbb,
    // NORM RX RSSI -67dBm
    (byte) 0xb0,
    // NORM TX POWER -77dBm, so actual TX POWER = -36dBm
    (byte) 0xb3,
    // Note based on the values aboves PATH LOSS = (-36) - (-67) = 31dBm
    // Below zero padding added to test it is handled correctly
    0x00
  };

  // Manufactuer data region
  public static final byte[] manu_data_1_data = {
    // manufacturer data
    (byte) 0x15, (byte) 0xd2, (byte) 0x77, (byte) 0x01, //
    (byte) 0x00, (byte) 0x32, (byte) 0x05, (byte) 0x00, //
    (byte) 0x00, (byte) 0x7f, (byte) 0x17, (byte) 0x56, //
    (byte) 0x00, (byte) 0xd4, (byte) 0x7c, (byte) 0x18, //
    (byte) 0x00, (byte) 0x76, (byte) 0x02, (byte) 0x56, //
    (byte) 0x4d, (byte) 0x00, (byte) 0x24, (byte) 0xa4, //
    (byte) 0xbb, (byte) 0xb0, (byte) 0xb3
  };

    // Manufacturer data packet 2
  public static final byte[] manu_data_2 = {
    0x02, // Length of this Data
    0x01, // <<Flags>>
    0x04, // BR/EDR Not Supported.
    0x03, // Length of this Data
    0x02, // <<Incomplete List of 16-bit Service UUIDs>>
    // TX Power Service UUID
    0x04, 0x08,

    // Google Specific Data
    30, // length of data that follows
    (byte) 0xff, // <<Manufacturer Specific Data>>
    // Company Identifier Code = Google MTV
    0x50, 0x05,
    // Header
    0x15,
    // Running time ENCRYPT
    (byte) 0xd2, 0x77, 0x01, 0x00,
    // Scan Freq ENCRYPT
    0x32, 0x05,
    // Time in slow mode
    0x00, 0x00,
    // Time in fast mode
    0x7f, 0x17,
    // Subset of UID
    0x56, 0x00,
    // ID Mask
    (byte) 0xd4, 0x7c, 0x18,
    // RFU (reserved)
    0x00,
    // GUID = decimal 1297482358
    0x76, 0x02, 0x56, 0x4d, 0x00,
    // Ranging Payload Header
    0x24,
    // MAC of scanning address
    (byte) 0xa4, (byte) 0xbb,
    // RX RSSI -67dBm
    (byte) 0xb0,
    // NORM TX POWER -77dBm, so actual TX POWER = -36dBm
    (byte) 0xb3
    // Note based on the values above PATH LOSS = (-36) - (-67) = 31dBm
  };

  private static final String PROXIMITY_NOW = "14e4fd9f-6667-4ccb-a61b-24d09ab17e93";

  // iBeacon Test Packet with UUID
  public static final byte[] ibeacon_2 = {
    (byte) 0x1a, // length of param data that follows (21 dec)
    (byte) 0xff, // <<Manufacturer Specific Data>>
    // Company Identifier Code = Apple, Inc.
    0x4c, 0x00,
    // Apple subtype and length
    0x02, 0x15,
    // UUID = PROXIMITY_NOW
    // IEEE 128-bit UUID represented as UUID[15]: lsb To UUID[0]: msb
    (byte) 0x14, (byte) 0xe4, (byte) 0xfd, (byte) 0x9f, // UUID[15] - UUID[12]
    (byte) 0x66, (byte) 0x67, (byte) 0x4c, (byte) 0xcb, // UUID[11] - UUID[08]
    (byte) 0xa6, (byte) 0x1b, (byte) 0x24, (byte) 0xd0, // UUID[07] - UUID[04]
    (byte) 0x9a, (byte) 0xb1, (byte) 0x7e, (byte) 0x93, // UUID[03] - UUID[00]
    // Major ID = 0
    0x00, 0x00,
    // Minor ID = 1
    0x00, 0x01,
    // TX Power = 2
    0x02,
    // Random extra byte to test parsing works ok
    0x00
  };

  // Example data including a service data block
  public static final byte[] sd1 = {
    0x02, // Length of this Data
    0x01, // <<Flags>>
    0x04, // BR/EDR Not Supported.

    0x03, // Length of this Data
    0x02, // <<Incomplete List of 16-bit Service UUIDs>>
    0x04, 0x18, // TX Power Service UUID

    0x1e, // Length of this Data
    (byte) 0x16, // <<Service Specific Data>>
    // Service UUID
    (byte) 0xe0, 0x00,
    // Header
    0x15,
    // Running time ENCRYPT
    (byte) 0xd2, 0x77, 0x01, 0x00,
    // Scan Freq ENCRYPT
    0x32, 0x05,
    // Time in slow mode
    0x00, 0x00,
    // Time in fast mode
    0x7f, 0x17,
    // Subset of UID
    0x56, 0x00,
    // ID Mask
    (byte) 0xd4, 0x7c, 0x18,
    // RFU (reserved)
    0x00,
    // GUID = decimal 1297482358
    0x76, 0x02, 0x56, 0x4d, 0x00,
    // Ranging Payload Header
    0x24,
    // MAC of scanning address
    (byte) 0xa4, (byte) 0xbb,
    // NORM RX RSSI -67dBm
    (byte) 0xb0,
    // NORM TX POWER -77dBm, so actual TX POWER = -36dBm
    (byte) 0xb3,
    // Note based on the values aboves PATH LOSS = (-36) - (-67) = 31dBm
    // Below zero padding added to test it is handled correctly
    0x00
  };

  // Service data region of sd1
  public static final byte[] sd1_data = {
      // manufacturer data
      (byte) 0x15, (byte) 0xd2, (byte) 0x77, (byte) 0x01, //
      (byte) 0x00, (byte) 0x32, (byte) 0x05, (byte) 0x00, //
      (byte) 0x00, (byte) 0x7f, (byte) 0x17, (byte) 0x56, //
      (byte) 0x00, (byte) 0xd4, (byte) 0x7c, (byte) 0x18, //
      (byte) 0x00, (byte) 0x76, (byte) 0x02, (byte) 0x56, //
      (byte) 0x4d, (byte) 0x00, (byte) 0x24, (byte) 0xa4, //
      (byte) 0xbb, (byte) 0xb0, (byte) 0xb3
  };
}

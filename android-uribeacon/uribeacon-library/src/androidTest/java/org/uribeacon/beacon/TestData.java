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

/**
 * See: Supplement to the Bluetooth Core Specification, Version 4,
 * Adoption Date: 03 December 2013
 * <p/>
 * Sample test data of various species of beacons.
 */
public class TestData {
    // This is an example of advertising data with AD types
    public static final byte[] adv_1 = {
            // Length of this Data
            0x02,
            // <<Flags>>
            0x01,
            // LE Limited Discoverable Mode
            0x01,
            // Length of this Data
            0x0A,
            // <<Complete local name>>
            0x09,
            'P', 'e', 'd', 'o', 'm', 'e', 't', 'e', 'r'
    };

    // Example UriBeacon data
    public static final byte[] adv_2 = {
            // Length of this Data
            0x02,
            // <<Flags>>
            0x01,
            // BR/EDR Not Supported.
            0x04,
            // Length of this Data
            0x03,
            //     Complete List of 16-bit Service Class UUIDs»
            0x03,
            // URI Service class UUID (16-bit)
            (byte) 0xD8, (byte) 0xFE,
            // Length of this Data
            0x10,
            // Service Data
            0x16,
            // URI Service class UUID (16-bit)
            (byte) 0xD8, (byte) 0xFE,
            // Flags
            0x00,
            // Calibrated Tx Power
            0x20,
            // Expansion code for http://www.
            0x00,
            // URI
            'u', 'r', 'i', 'b', 'e', 'a', 'c', 'o', 'n',
            // Expansion code for .org
            0x08
    };
}

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

import android.test.AndroidTestCase;

/**
 * Unit tests for the {@link org.uribeacon.scan.util.RangingUtils} class.
 */
public class RangingUtilsTest extends AndroidTestCase {

  // relative error to be used in comparing doubles
  private static final double DELTA = 1e-5; 
  
  public void testDistanceForRssi() {
    // Distance expected to be 1.0 meters based on an RSSI/TxPower of -41dBm
    // Using params: int rssi (dBm), int calibratedTxPower (dBm)
    double distance = RangingUtils.distanceFromRssi(-41, 0);
    assertEquals(1.0, distance, DELTA);
    
    double distance2 = RangingUtils.distanceFromRssi(-70, -9);
    assertEquals(10.0, distance2, DELTA);
    
    // testing that the double values are not casted to integers
    double distance3 = RangingUtils.distanceFromRssi(-67, -36);
    assertEquals(0.31622776601683794, distance3, DELTA);
    
    double distance4 = RangingUtils.distanceFromRssi(-50, -29);
    assertEquals(0.1, distance4, DELTA);
   }

  public void testRssiFromDistance() {
    // RSSI expected at 1 meter based on the calibrated tx field of -41dBm
    // Using params: distance (m), int calibratedTxPower (dBm),
    int rssi = RangingUtils.rssiFromDistance(1.0, 0);
    assertEquals(0, rssi);
  }

}

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

import java.net.URISyntaxException;

public class ConfigUriBeacon extends UriBeacon {
  public static final int PERIOD_NONE = -1;
  public static final int POWER_MODE_NONE = -1;
  public static final int POWER_MODE_ULTRA_LOW = 0;
  public static final int POWER_MODE_LOW = 1;
  public static final int POWER_MODE_MEDIUM = 2;
  public static final int POWER_MODE_HIGH = 3;
  private static final int MIN_UINT = 0;
  private static final int MAX_UINT8 = 255;
  private static final int MAX_UINT16 = 65535;
  private static final int MIN_INT8 = -128;
  private static final int MAX_INT8 = 127;

  boolean mLocked;
  int[] mPowerLevels;
  private int mPowerMode = POWER_MODE_NONE;
  int mPeriod;

  private ConfigUriBeacon(UriBeacon uriBeacon, boolean locked, int[] powerLevels, int powerMode, int period) {
    super(uriBeacon);
    mLocked = locked;
    mPowerLevels = powerLevels;
    mPowerMode = powerMode;
    mPeriod = period;
  }
  /**
   * Parse scan record bytes to {@link ConfigUriBeacon}. <p/> The format is defined in UriBeacon
   * Definition.
   *
   * @param scanRecordBytes The scan record of Bluetooth LE advertisement and/or scan response.
   */
  public static ConfigUriBeacon parseFromBytes(byte[] scanRecordBytes) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecordBytes);
    return new ConfigUriBeacon(uriBeacon, false, null, POWER_MODE_NONE, -1);
  }

  /**
   * @return Whether or not the beacon is locked
   */
  public boolean getLocked() {
    return mLocked;
  }

  /**
   * @return An array containing the Tx Power for each HIGH, MEDIUM, LOW and LOWEST
   */
  public int[] getPowerLevels() {
    return mPowerLevels;
  }

  /**
   * @return The power mode the beacon is on. HIGH, MEDIUM, LOW, LOWEST (3, 2, 1, 0 respectively)
   */
  public int getPowerMode() {
    return mPowerMode;
  }

  /**
   * @return The period in milliseconds.
   */
  public int getPeriod() {
    return mPeriod;
  }
  public static final class Builder extends UriBeacon.Builder {
    boolean mLocked;
    int[] mPowerLevels;
    int mPowerMode = POWER_MODE_NONE;
    int mPeriod;

    /**
     * Sets whether or not the beacon is locked.
     * @param locked True if the beacon is locked false otherwise.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder locked(boolean locked) {
      mLocked = locked;
      return this;
    }

    /** {@inheritDoc} */
    public Builder uriString(String uriString){
      super.uriString(uriString);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /** {@inheritDoc} */
    public Builder uriString(byte[] uriBytes){
      super.uriString(uriBytes);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /** {@inheritDoc} */
    public Builder flags(byte flags) {
      super.flags(flags);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /**
     * Set the tx power for High, Medium, Low, Lowest
     * @param levels The array containing the tx powers for the levels
     * @return The ConfigUriBeacon Builder.
     */
    public Builder powerLevels(int[] levels) {
      mPowerLevels = levels;
      return this;
    }

    /**
     * Add a Uri to the UriBeacon advertised data.
     *
     * @param powerMode The power mode to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder powerMode(int powerMode) {
      mPowerMode = powerMode;
      return this;
    }

    /**
     * Set the broadcasting period for the beacon.
     * @param period The broadcasting period
     * @return The ConfigUriBeacon Builder.
     */
    public Builder period(int period) {
      mPeriod = period;
      return this;
    }

    /** {@inheritDoc} */
    public Builder txPowerLevel(byte txPowerLevel) {
      super.txPowerLevel(txPowerLevel);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }


    /**
     * Build ConfigUriBeacon.
     *
     * @return The ConfigUriBeacon
     */
    public ConfigUriBeacon build() throws URISyntaxException {
      UriBeacon uriBeacon = super.build();
      checkPowerMode();
      checkPowerLevels();
      checkPeriod();
      return new ConfigUriBeacon(uriBeacon, mLocked, mPowerLevels, mPowerMode, mPeriod);
    }

    private void checkPowerMode() throws URISyntaxException {
      if (mPowerMode < POWER_MODE_NONE || mPowerMode > POWER_MODE_HIGH) {
        throw new URISyntaxException(Integer.toString(mPowerMode), "Unknown power mode");
      }
    }

    private void checkPowerLevels() {
      for (int i = 0; i < mPowerLevels.length; i++) {
        if (mPowerLevels[i] < MIN_INT8) {
          throw new IllegalArgumentException("Tx Power at position " + i + " less than " + MIN_INT8);
        }
        else if (mPowerLevels[i] > MAX_INT8) {
          throw new IllegalArgumentException("Tx Power at position " + i + " greater than " + MAX_INT8);
        }
      }
    }

    private void checkPeriod() {
      if (mPeriod < MIN_UINT) {
        throw new IllegalArgumentException("Period must be greater or equal to 0. Currently: " + mPeriod );
      }
      else if (mPeriod > MAX_UINT16) {
        throw new IllegalArgumentException("Period too long. Currently: " + mPeriod);
      }
    }
  }
}

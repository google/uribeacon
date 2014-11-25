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
  private static final String TAG = ConfigUriBeacon.class.getCanonicalName();
  public static final int PERIOD_NONE = -1;
  public static final byte POWER_MODE_NONE = -1;
  public static final byte POWER_MODE_ULTRA_LOW = 0;
  public static final byte POWER_MODE_LOW = 1;
  public static final byte POWER_MODE_MEDIUM = 2;
  public static final byte POWER_MODE_HIGH = 3;
  private static final int UINT16_MIN_VALUE = 0;
  private static final int UINT16_MAX_VALUE = 65535;


  private static final byte TX_POWER_LEVEL_MAX_VALUE = 20;
  private static final byte TX_POWER_LEVEL_MIN_VALUE = -100;

  private boolean mLockState;
  private byte[] mAdvertisedTxPowerLevels;
  private byte mTxPowerMode = POWER_MODE_NONE;
  private int mBeaconPeriod;

  private ConfigUriBeacon(UriBeacon uriBeacon, boolean lockState, byte[] advertisedTxPowerLevels, byte txPowerMode, int beaconPeriod) {
    super(uriBeacon);
    mLockState = lockState;
    mAdvertisedTxPowerLevels = advertisedTxPowerLevels;
    mTxPowerMode = txPowerMode;
    mBeaconPeriod = beaconPeriod;
  }
  /**
   * Parse scan record bytes to {@link ConfigUriBeacon}. <p/> The format is defined in UriBeacon
   * Definition.
   *
   * @param scanRecordBytes The scan record of Bluetooth LE advertisement and/or scan response.
   */
  public static ConfigUriBeacon parseFromBytes(byte[] scanRecordBytes) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecordBytes);
    return new ConfigUriBeacon(uriBeacon, false, null, POWER_MODE_NONE, PERIOD_NONE);
  }

  /**
   * @return Whether or not the beacon is locked
   */
  public boolean getLockState() {
    return mLockState;
  }

  /**
   * @return An array containing the Tx Power for each HIGH, MEDIUM, LOW and LOWEST
   */
  public byte[] getAdvertisedTxPowerLevels() {
    return mAdvertisedTxPowerLevels;
  }

  /**
   * @return The power mode the beacon is on. HIGH, MEDIUM, LOW, LOWEST (3, 2, 1, 0 respectively)
   */
  public byte getTxPowerMode() {
    return mTxPowerMode;
  }

  /**
   * @return The period in milliseconds.
   */
  public int getBeaconPeriod() {
    return mBeaconPeriod;
  }
  public static final class Builder extends UriBeacon.Builder {

    boolean mLockState;
    byte[] mAdvertisedTxPowerLevels;
    byte mTxPowerMode = POWER_MODE_NONE;
    int mBeaconPeriod = PERIOD_NONE;

    /**
     * Sets whether or not the beacon is locked.
     *
     * @param lockState True if the beacon is lockState false otherwise.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder lockState(boolean lockState) {
      mLockState = lockState;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    public Builder uriString(String uriString) {
      super.uriString(uriString);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /**
     * {@inheritDoc}
     */
    public Builder uriString(byte[] uriBytes) {
      super.uriString(uriBytes);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /**
     * {@inheritDoc}
     */
    public Builder flags(byte flags) {
      super.flags(flags);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /**
     * Set the tx power for High, Medium, Low, Lowest
     *
     * @param levels The array containing the tx powers for the levels
     * @return The ConfigUriBeacon Builder.
     */
    public Builder advertisedTxPowerLevels(byte[] levels) {
      mAdvertisedTxPowerLevels = levels;
      return this;
    }

    /**
     * Add a Uri to the UriBeacon advertised data.
     *
     * @param txPowerMode The power mode to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder txPowerMode(byte txPowerMode) {
      mTxPowerMode = txPowerMode;
      return this;
    }

    /**
     * Set the broadcasting period for the beacon.
     *
     * @param beaconPeriod The broadcasting period
     * @return The ConfigUriBeacon Builder.
     */
    public Builder beaconPeriod(int beaconPeriod) {
      mBeaconPeriod = beaconPeriod;
      return this;
    }

    /**
     * Only used in V1 {@inheritDoc}
     */
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
      if (mTxPowerMode != POWER_MODE_NONE || mBeaconPeriod != PERIOD_NONE ||
          mAdvertisedTxPowerLevels != null) {
        checkTxPowerMode();
        checkAdvertisedTxPowerLevels();
        checkBeaconPeriod();
      }
      return new ConfigUriBeacon(uriBeacon, mLockState, mAdvertisedTxPowerLevels, mTxPowerMode,
          mBeaconPeriod);
    }

    private void checkTxPowerMode() throws URISyntaxException {
      if (mTxPowerMode < POWER_MODE_NONE || mTxPowerMode > POWER_MODE_HIGH) {
        throw new URISyntaxException(Integer.toString(mTxPowerMode), "Unknown power mode");
      }
    }

    private void checkAdvertisedTxPowerLevels() throws URISyntaxException {
      if (mAdvertisedTxPowerLevels == null) {
        throw new IllegalArgumentException("Must include Tx AdvertisedPowerLevels");
      }
      if (mAdvertisedTxPowerLevels.length != 4) {
        throw new URISyntaxException("Invalid length for Tx Advertised Power Levels",
            Integer.toString(mAdvertisedTxPowerLevels.length));
      }
      for (int i = 0; i < mAdvertisedTxPowerLevels.length; i++) {
        if (mAdvertisedTxPowerLevels[i] < TX_POWER_LEVEL_MIN_VALUE
            || mAdvertisedTxPowerLevels[i] > TX_POWER_LEVEL_MAX_VALUE) {
          throw new URISyntaxException("Invalid TxPower Level",
              Byte.toString(mAdvertisedTxPowerLevels[i]), i);
        }
      }
    }
    private void checkBeaconPeriod() throws URISyntaxException {
      if (mBeaconPeriod == PERIOD_NONE) {
        throw new IllegalArgumentException("Need Broadcasting Period");
      }
      if (mBeaconPeriod < UINT16_MIN_VALUE || mBeaconPeriod > UINT16_MAX_VALUE) {
        throw new URISyntaxException("Invalid broadcasting period", Integer.toString(mBeaconPeriod));
      }
    }
  }
}
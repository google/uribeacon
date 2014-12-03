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
  // This error should be defined in the BluetoothGatt library but it isn't.
  public static final int INSUFFICIENT_AUTHORIZATION = 8;
  public static final int PERIOD_NONE = -1;
  public static final byte POWER_MODE_NONE = -1;
  public static final byte POWER_MODE_ULTRA_LOW = 0;
  public static final byte POWER_MODE_LOW = 1;
  public static final byte POWER_MODE_MEDIUM = 2;
  public static final byte POWER_MODE_HIGH = 3;
  public static final int KEY_LENGTH = 128;
  private static final int UINT16_MIN_VALUE = 0;
  private static final int UINT16_MAX_VALUE = 65535;

  private static final byte TX_POWER_LEVEL_MAX_VALUE = 20;
  private static final byte TX_POWER_LEVEL_MIN_VALUE = -100;

  private byte[] mKey;
  private boolean mLockState;
  private byte[] mAdvertisedTxPowerLevels;
  private byte mTxPowerMode = POWER_MODE_NONE;
  private int mBeaconPeriod;
  private boolean mReset;

  private ConfigUriBeacon(UriBeacon uriBeacon, byte[] key, boolean lockState, byte[] advertisedTxPowerLevels, byte txPowerMode, int beaconPeriod, boolean reset) {
    super(uriBeacon);
    mKey = key;
    mLockState = lockState;
    mAdvertisedTxPowerLevels = advertisedTxPowerLevels;
    mTxPowerMode = txPowerMode;
    mBeaconPeriod = beaconPeriod;
    mReset = reset;
  }

  /**
   * Parse scan record bytes to {@link ConfigUriBeacon}. <p/> The format is defined in UriBeacon
   * Definition.
   *
   * @param scanRecordBytes The scan record of Bluetooth LE advertisement and/or scan response.
   */
  public static ConfigUriBeacon parseFromBytes(byte[] scanRecordBytes) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecordBytes);
    return new ConfigUriBeacon(uriBeacon, null, false, null, POWER_MODE_NONE, PERIOD_NONE, false);
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

  /**
   * @return If the beacon should be reset.
   */
  public boolean getReset() {
    return mReset;
  }

  /**
   * @return The key to (un)lock the beacon.
   */
  public byte[] getKey() {
    return mKey;
  }

  public static final class Builder extends UriBeacon.Builder {
    byte[] mKey;
    boolean mLockState;
    byte[] mAdvertisedTxPowerLevels;
    byte mTxPowerMode = POWER_MODE_NONE;
    int mBeaconPeriod = PERIOD_NONE;
    boolean mReset;


    public Builder key(byte[] key) {
      mKey = key;
      return this;
    }
    /**
     * Sets whether or not the beacon is locked.
     * @param lockState True if the beacon is locked false otherwise.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder lockState(boolean lockState) {
      mLockState = lockState;
      return this;
    }

    /** {@inheritDoc} */
    public Builder uriString(String uriString) {
      super.uriString(uriString);
      // Allow chaining on ConfigUriBeacon by returning this
      return this;
    }

    /** {@inheritDoc} */
    public Builder uriString(byte[] uriBytes) {
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
     * @param advertisedTxPowerLevels The array containing the tx powers for the levels
     * @return The ConfigUriBeacon Builder.
     */
    public Builder advertisedTxPowerLevels(byte[] advertisedTxPowerLevels) {
      mAdvertisedTxPowerLevels = advertisedTxPowerLevels;
      return this;
    }

    /**
     * Add a Uri to the UriBeacon advertised data.
     * @param txPowerMode The power mode to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder txPowerMode(byte txPowerMode) {
      mTxPowerMode = txPowerMode;
      return this;
    }

    /**
     * Set the broadcasting period for the beacon.
     * @param beaconPeriod The broadcasting period
     * @return The ConfigUriBeacon Builder.
     */
    public Builder beaconPeriod(int beaconPeriod) {
      mBeaconPeriod = beaconPeriod;
      return this;
    }

    /**
     * Indicates if the beacon should be reset or not.
     * @param reset If the beacon should be reset
     * @return The ConfigUriBeacon Builder
     */
    public Builder reset(boolean reset) {
      mReset = reset;
      return this;
    }

    /**
     * Only used in V1
     * {@inheritDoc}
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
      if (mReset) {
        UriBeacon uriBeacon = new UriBeacon.Builder().uriString("").build();
        return new ConfigUriBeacon(uriBeacon, mKey, false, null, POWER_MODE_NONE, PERIOD_NONE, mReset);
      }
      UriBeacon uriBeacon = super.build();
      if (mTxPowerMode != POWER_MODE_NONE || mBeaconPeriod != PERIOD_NONE ||
          mAdvertisedTxPowerLevels != null) {
        checkTxPowerMode();
        checkAdvertisedTxPowerLevels();
        checkBeaconPeriod();
      }
      return new ConfigUriBeacon(uriBeacon, mKey, mLockState, mAdvertisedTxPowerLevels, mTxPowerMode,
          mBeaconPeriod, false);
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
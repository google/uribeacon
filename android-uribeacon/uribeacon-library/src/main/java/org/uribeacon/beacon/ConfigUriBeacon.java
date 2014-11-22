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


public class ConfigUriBeacon {

  private final UriBeacon mUriBeacon;
  private final boolean mLockState;
  private final byte[] mKey;
  private final byte[] mAdvertisedTxPowerLevel;
  private final byte mTxPowerMode;
  private final int mPeriod;
  private final boolean mReset;

  private ConfigUriBeacon(UriBeacon uriBeacon, boolean lockState, byte[] key,
      byte[] advertisedTxPowerLevel, byte txPowerMode, int period) {
    mUriBeacon = uriBeacon;
    mLockState = lockState;
    mKey = key;
    mAdvertisedTxPowerLevel = advertisedTxPowerLevel;
    mTxPowerMode = txPowerMode;
    mPeriod = period;
    mReset = false;
  }

  private ConfigUriBeacon(byte[] key) {
    mUriBeacon = null;
    mLockState = false;
    mKey = key;
    mAdvertisedTxPowerLevel = null;
    mTxPowerMode = 0;
    mPeriod = 0;
    mReset = true;
  }

  private ConfigUriBeacon(UriBeacon uriBeacon) {
    mUriBeacon = uriBeacon;
    mKey = null;
    mLockState = false;
    mAdvertisedTxPowerLevel = null;
    mTxPowerMode = 0;
    mPeriod = 0;
    mReset = false;
  }

  /**
   * @return The state of the lock
   */
  public boolean getLockState() {
    return mLockState;
  }

  /**
   * @return The key used to (un)lock the UriBeacon
   */
  public byte[] getKey() {
    return mKey;
  }

  /**
   * @return The Uri of the UriBeacon
   */
  public String getUriString() {
    return mUriBeacon.getUriString();
  }

  /**
   * @return The Uri of the UriBeacon
   */
  public byte[] getUriBytes() {
    return mUriBeacon.getUriBytes();
  }

  /**
   * @return The flags used in the UriBeacon
   */
  public byte getFlags() {
    return mUriBeacon.getFlags();
  }

  /**
   * @return The Advertised Tx Power Levels
   */
  public byte[] getAdvertisedTxPowerLevel() {
    return mAdvertisedTxPowerLevel;
  }

  /**
   * @return The Tx Power Mode the UriBeacon is using
   */
  public byte getTxPowerMode() {
    return mTxPowerMode;
  }

  /**
   * @return The period at which the beacon is transmitting
   */
  public int getPeriod() {
    return mPeriod;
  }

  /**
   * @return If the beacon should be reset or not
   */
  public boolean getReset() {
    return mReset;
  }

  /**
   * @return Tx Power Level at which the beacon is transmitting
   */
  public byte getTxPowerLevel() {
    return mUriBeacon.getTxPowerLevel();
  }

  /**
   * Generates the adv packet to be used in the V1 UriBeacon.
   * @return A byte representation of the adv packet
   * @throws URISyntaxException If there is a problem with the URI
   */
  public byte[] toByteArray() throws URISyntaxException {
    return mUriBeacon.toByteArray();
  }

  /**
   *
   * @param scanRecordBytes
   * @return
   */
  public static ConfigUriBeacon parseFromBytes(byte[] scanRecordBytes) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecordBytes);
    return new ConfigUriBeacon(uriBeacon);
  }

  public static final class Builder {

    private UriBeacon.Builder mBuilder = new UriBeacon.Builder();
    private boolean mLockState;
    private byte[] mKey;
    private byte[] mAdvertisedTxPowerLevel;
    private byte mTxPowerMode;
    private int mPeriod;
    private boolean mReset;

    /**
     * Set the beacon's lock state
     * @param lockState If the beacon is locked or not
     * @return The Builder
     */
    public Builder lockState(boolean lockState) {
      mLockState = lockState;
      return this;
    }

    /**
     * Set the key to (un)lock the UriBeacon with
     * @param key The key used to (un)lock the UriBeacon
     * @return The Builder
     */
    public Builder key(byte[] key) {
      mKey = key;
      return this;
    }

    /**
     * Add a Uri to the ConfigUriBeacon advertised data.
     * @param uriString The Uri to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder uriString(String uriString) {
      mBuilder.uriString(uriString);
      return this;
    }

    /**
     * Add the bytes of a URI to the ConfigUriBeacon.
     * @param uriString The Uri to be advertised
     * @return The ConfigUriBeacon Builder
     */
    public Builder uriString(byte[] uriString) {
      mBuilder.uriString(uriString);
      return this;
    }

    /**
     * Add flags to the ConfigUriBeacon advertised data.
     * @param flags The flags to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder flags(byte flags) {
      mBuilder.flags(flags);
      return this;
    }

    /**
     * Add the Tx Power table to the UriBeacon
     * @param advertisedTxPowerLevel Array corresponding to the tx power level of each tx power mode
     * @return
     */
    public Builder advertisedTxPower(byte[] advertisedTxPowerLevel) {
      mAdvertisedTxPowerLevel = advertisedTxPowerLevel;
      return this;
    }

    /**
     * Set the Tx Power Mode that the UriBeacon will use to advertise the data
     * @param txPowerMode The tx power mode
     * @return
     */
    public Builder txPowerMode(byte txPowerMode) {
      mTxPowerMode = txPowerMode;
      return this;
    }

    /**
     * Set the broadcasting period
     * @param period
     * @return
     */
    public Builder period(int period) {
      mPeriod = period;
      return this;
    }

    /**
     * Set if the beacon should be reset or not
     * @param reset
     * @return
     */
    public Builder reset(boolean reset) {
      mReset = reset;
      return this;
    }

    /**
     * Add a Tx Power Level to the UriBeacon advertised data. (Use for Beacon V1)
     * @param txPowerLevel The TX Power Level to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder txPowerLevel(byte txPowerLevel) {
      mBuilder.txPowerLevel(txPowerLevel);
      return this;
    }


    /**
     * Build ConfigUriBeacon.
     * @return The ConfigUriBeacon
     */
    public ConfigUriBeacon build() throws URISyntaxException {
      if (mReset) {
        return new ConfigUriBeacon(mKey);
      }
      UriBeacon uriBeacon = mBuilder.build();
      return new ConfigUriBeacon(uriBeacon, mLockState, mKey, mAdvertisedTxPowerLevel, mTxPowerMode, mPeriod);
    }
  }
}

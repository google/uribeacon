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

import android.net.Uri;

import java.net.URISyntaxException;

public class ConfigUriBeacon extends UriBeacon {
  private int mPowerMode = POWER_MODE_NONE;

  public static final int POWER_MODE_NONE = -1;
  public static final int POWER_MODE_ULTRA_LOW = 0;
  public static final int POWER_MODE_LOW = 1;
  public static final int POWER_MODE_MEDIUM = 2;
  public static final int POWER_MODE_HIGH = 3;

  private ConfigUriBeacon(UriBeacon uriBeacon, int powerMode) {
    super(uriBeacon);
    mPowerMode = powerMode;
  }

  public static ConfigUriBeacon parseFromBytes(byte[] scanRecordBytes) {
    UriBeacon uriBeacon = UriBeacon.parseFromBytes(scanRecordBytes);
    return new ConfigUriBeacon(uriBeacon, POWER_MODE_NONE);
  }

  public static final class Builder extends UriBeacon.Builder {
    int mPowerMode = POWER_MODE_NONE;

    /**
     * Add a Uri to the UriBeacon advertised data.
     *
     * @param powerMode The power mode to be advertised.
     * @return The ConfigUriBeacon Builder.
     */
    public Builder powerMode(byte powerMode) {
      mPowerMode = powerMode;
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
    public Builder txPowerLevel(byte txPowerLevel) {
      super.txPowerLevel(txPowerLevel);
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
     * Build ConfigUriBeacon.
     *
     * @return The ConfigUriBeacon
     */
    public ConfigUriBeacon build() throws URISyntaxException {
      UriBeacon uriBeacon = super.build();
      if (mPowerMode < POWER_MODE_NONE || mPowerMode > POWER_MODE_HIGH) {
        throw new URISyntaxException(Integer.toString(mPowerMode), "Unknown power mode");
      }
      return new ConfigUriBeacon(uriBeacon, mPowerMode);
    }

  }
}

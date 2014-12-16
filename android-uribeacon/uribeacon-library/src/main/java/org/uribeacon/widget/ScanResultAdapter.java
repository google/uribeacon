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
package org.uribeacon.widget;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.util.RegionResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Adapter for holding and sorting devices found through scanning that can be used in a ListView.
 */
public abstract class ScanResultAdapter extends BaseAdapter {
  private final Map<String /* device address */, DeviceSighting> mLeScanResults;
  private List<DeviceSighting> mSortedSightings;
  public final LayoutInflater mInflater;
  public final RegionResolver mRegionResolver;
  private final Handler mHandler;

  // Timeout to remove a ScanResult after lifetime expires.
  private class Timeout implements Runnable {
    final String mAddress;

    Timeout(String address) {
      mAddress = address;
    }

    public void run() {
      remove(mAddress);
    }
  }

  private final HashMap<String, Timeout> mTimeouts = new HashMap<>();

  public ScanResultAdapter(LayoutInflater inflater) {
    mLeScanResults = new HashMap<>();
    mRegionResolver = new RegionResolver();
    mSortedSightings = null;
    mInflater = inflater;
    mHandler = new Handler();
  }
  public void setSmoothFactor(double smoothFactor) {
    mRegionResolver.setSmoothFactor(smoothFactor);
  }
  /**
   * Adds the ScanResult to the adapter with an auto-removal after lifetimeSeconds.
   * <p/>
   * Note: The lifetime param is useful when a device stops matching a filter
   * (e.g. advertising the config service) but is not reported lost by the scanner
   * because it is still advertising a UriBeacon.
   *
   * @param scanResult        The ScanResult to add to the adapter.
   * @param calibratedTxPower The Transmit Power used for the ScanResult.
   * @param lifetimeSeconds   The number of seconds before the ScanResult is removed.
   */
  public void add(ScanResult scanResult, int calibratedTxPower, int lifetimeSeconds) {
    final String address = scanResult.getDevice().getAddress();
    // Create or replace the timeout runnable
    Timeout timer = mTimeouts.get(address);
    if (timer != null) {
      mHandler.removeCallbacks(timer);
    } else {
      timer = new Timeout(address);
      mTimeouts.put(address, timer);
    }
    mHandler.postDelayed(timer, TimeUnit.SECONDS.toMillis(lifetimeSeconds));
    add(scanResult, calibratedTxPower);
  }

  /**
   * Adds the scanResult to the adapter.
   *
   * @param scanResult The ScanResult to add to the adapter.
   * @param txPower    The Transmit Power used for the ScanResult.
   */
  public void add(ScanResult scanResult, int txPower) {
    final String address = scanResult.getDevice().getAddress();
    mRegionResolver.onUpdate(address, scanResult.getRssi(), txPower);
    double distance = mRegionResolver.getDistance(address);
    DeviceSighting sightings = mLeScanResults.get(address);
    if (sightings == null) {
      mLeScanResults.put(scanResult.getDevice().getAddress(),
          new DeviceSighting(scanResult, distance));
    } else {
      sightings.updateSighting(scanResult, distance);
    }
    notifyDataSetChanged();
  }

  /**
   * Remove ScanResult associated with the device from the adapter.
   *
   * @param device The device to remove from the adapter.
   */
  public void remove(BluetoothDevice device) {
    String address = device.getAddress();
    remove(address);
  }

  private void remove(String address) {
    mRegionResolver.onLost(address);
    mLeScanResults.remove(address);

    // Clean out the timeout runnable
    Timeout timer = mTimeouts.get(address);
    if (timer != null) {
      mHandler.removeCallbacks(timer);
      mTimeouts.remove(address);
    }

    notifyDataSetChanged();
  }

  /**
   * Remove all elements from the collection.
   */
  public void clear() {
    mLeScanResults.clear();
    notifyDataSetChanged();
  }

  @Override
  public void notifyDataSetChanged() {
    mSortedSightings = null;
    super.notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mLeScanResults.size();
  }

  @Override
  public DeviceSighting getItem(int i) {
    if (mSortedSightings == null) {
      mSortedSightings = new ArrayList<>(mLeScanResults.values());
      Collections.sort(mSortedSightings);
    }
    return mSortedSightings.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  /**
   * Hold the ScanResult and distance information.
   */
  public class DeviceSighting implements Comparable<DeviceSighting> {
    public ScanResult scanResult;
    public double latestDistance;
    public long period;

    public DeviceSighting(ScanResult scanResult, double distance) {
      this.scanResult = scanResult;
      this.latestDistance = distance;
    }

    public void updateSighting(ScanResult scanResult, double distance) {
      long currentPeriod = TimeUnit.NANOSECONDS.toMillis(scanResult.getTimestampNanos()
          - this.scanResult.getTimestampNanos());
      this.period = this.period != 0 ?
          (this.period + currentPeriod)/2
          : currentPeriod;
      this.scanResult = scanResult;
      this.latestDistance = distance;
    }

    @Override
    public int compareTo(@NonNull DeviceSighting other) {
      final String address = scanResult.getDevice().getAddress();
      final String otherAddress = other.scanResult.getDevice().getAddress();
      // Sort by the stabilized region of the device, unless
      // they are the same, in which case sort by distance.
      final String nearest = mRegionResolver.getNearestAddress();
      if (address.equals(nearest)) {
        return -1;
      }
      if (otherAddress.equals(nearest)) {
        return 1;
      }
      int r1 = mRegionResolver.getRegion(address);
      int r2 = mRegionResolver.getRegion(otherAddress);
      if (r1 != r2) {
        return ((Integer) r1).compareTo(r2);
      }
      // The two devices are in the same region, sort by device address.
      return address.compareTo(other.scanResult.getDevice().getAddress());
    }
  }
}


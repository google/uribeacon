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
package org.uribeacon.sample;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.scan.compat.ScanResult;
import org.uribeacon.scan.util.RangingUtils;
import org.uribeacon.widget.ScanResultAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for formatting, holding and sorting devices found through scanning that can be used in a ListView.
 */
class DeviceListAdapter extends ScanResultAdapter {
  private static final SimpleDateFormat TIMESTAMP_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS", Locale.US);

  // Adapter for holding devices found through scanning.
  public DeviceListAdapter(LayoutInflater layoutInflater) {
    super(layoutInflater);
  }

  private static String formatMillis(long millis) {
    return TIMESTAMP_FORMAT.format(new Date(millis));
  }

  @SuppressLint("InflateParams")
  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    ViewHolder viewHolder;
    // General ListView optimization code.
    if (view == null) {
      view = mInflater.inflate(R.layout.listitem_device, null);
      viewHolder = new ViewHolder();
      viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
      viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }

    StringBuilder deviceData = new StringBuilder();
    String nearest = mRegionResolver.getNearestAddress();
    DeviceSighting deviceSighting = getItem(i);
    ScanResult scanResult = deviceSighting.scanResult;
    UriBeacon beacon;
    byte txPowerLevel;
    beacon = UriBeacon.parseFromBytes(scanResult.getScanRecord().getBytes());

    String displayName = null;
    if (beacon != null) {
      displayName = beacon.getUriString();
      txPowerLevel = beacon.getTxPowerLevel();
      // Check if null to avoid null pointer exception.
      // Check if name is empty if it's empty show other value for displayName
      if (displayName != null && displayName.isEmpty()) {
        displayName = null;
      }
    }
    else {
      txPowerLevel = (byte) scanResult.getScanRecord().getTxPowerLevel();
    }
    if (displayName == null) {
      displayName = scanResult.getDevice().getName();
    }
    String deviceAddress = scanResult.getDevice().getAddress();
    if (displayName == null) {
      displayName = deviceAddress;
    } else {
      deviceData
          .append(deviceAddress)
          .append("\n");
    }

    long tsMillis = TimeUnit.NANOSECONDS.toMillis(scanResult.getTimestampNanos());
    String distance = String.format(Locale.US, "%.1f",
        mRegionResolver.getDistance(deviceAddress));

    deviceData
        .append("Timestamp: ")
        .append(formatMillis(tsMillis))
        .append(" Tx: ")
        .append(txPowerLevel)
        .append(" RSSI: ")
        .append(scanResult.getRssi())
        .append(" Avg RSSI: ")
        .append(mRegionResolver.getSmoothedRssi(deviceAddress))
        .append(" Distance: ")
        .append(distance);
    if (deviceSighting.period != 0) {
      deviceData
          .append(" Period(ms): ")
          .append(deviceSighting.period);
    }
    // The stabilized region computed from the hysteresis.
    int region = mRegionResolver.getRegion(deviceAddress);

    if (scanResult.getDevice().getAddress().equals(nearest)) {
      deviceData.append(" Region: NEAREST");
      viewHolder.deviceName.setTextColor(0xff008800);
    } else if (region == RangingUtils.Region.NEAR) {
      deviceData.append(" Region: NEAR");
      viewHolder.deviceName.setTextColor(0xaa004400);
    } else if (region == RangingUtils.Region.MID) {
      deviceData.append(" Region: MID");
      viewHolder.deviceName.setTextColor(0xffaa6600);
    } else if (region == RangingUtils.Region.FAR) {
      deviceData.append(" Region: FAR");
      viewHolder.deviceName.setTextColor(0xff880000);
    } else {
      viewHolder.deviceName.setTextColor(0xff000000);
    }

    viewHolder.deviceName.setText(displayName
        + "  (" + String.format("%.1f", deviceSighting.latestDistance) + "m)");
    viewHolder.deviceAddress.setText(deviceData.toString());
    return view;
  }

  static class ViewHolder {
    TextView deviceName;
    TextView deviceAddress;
  }
}

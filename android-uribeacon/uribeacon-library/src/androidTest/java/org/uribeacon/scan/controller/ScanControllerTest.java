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

package org.uribeacon.scan.controller;

import org.uribeacon.scan.compat.ScanCallback;
import org.uribeacon.scan.compat.ScanFilter;
import org.uribeacon.scan.compat.ScanSettings;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link org.uribeacon.scan.controller.ScanController} class.
 */
public class ScanControllerTest extends AndroidTestCase {
  MockContext mMockContext;
  ScanCallback mScanCallback;

  public void testStartStopScan() {
    mMockContext = new MockContext(mContext);
    ScanController scanController = new ScanController(mMockContext,
        ScanController.ScreenOffMode.NO_SCAN);
    List<ScanFilter> filters = new ArrayList<ScanFilter>();
    
    mScanCallback = new ScanCallback() { };
    
    ScanSettings.Builder builder = new ScanSettings.Builder();
    builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
    ScanSettings settings = builder.build();

    scanController.startScan(settings, filters, mScanCallback);
    assertEquals(1, scanController.getNumScanners());

    // start again with the same settings 
    scanController.startScan(settings, filters, mScanCallback);
    assertEquals(1, scanController.getNumScanners());

    scanController.stopScan(settings);
    assertEquals(0, scanController.getNumScanners());

    scanController.unregister();
  }

  public void testScreenOnOff() {
    mMockContext = new MockContext(mContext);
    ScanController scanController = new ScanController(mMockContext, 
        ScanController.ScreenOffMode.NO_SCAN);
    List<ScanFilter> filters = new ArrayList<ScanFilter>();

    ScanSettings.Builder builder = new ScanSettings.Builder();
    builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
    ScanSettings settings = builder.build();

    scanController.startScan(settings, filters, mScanCallback);

    mMockContext.sendScreenOnEvent();
    assertEquals(ScanController.ScanState.FAST_SCAN, scanController.getScanState());

    mMockContext.sendScreenOffEvent();
    assertEquals(ScanController.ScanState.NO_SCAN, scanController.getScanState());

    scanController.unregister();
  }

  public void testMotion() {
    mMockContext = new MockContext(mContext);
    ScanController scanController = new ScanController(mMockContext, 
        ScanController.ScreenOffMode.NO_SCAN);
    List<ScanFilter> filters = new ArrayList<ScanFilter>();

    ScanSettings.Builder builder = new ScanSettings.Builder();
    builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
    ScanSettings settings = builder.build();

    scanController.startScan(settings, filters, mScanCallback);

    mMockContext.sendScreenOnEvent();
    assertEquals(ScanController.ScanState.FAST_SCAN, scanController.getScanState());

    scanController.onMotionTimeout();
    assertEquals(ScanController.ScanState.SLOW_SCAN, scanController.getScanState());

    scanController.onMotion();
    assertEquals(ScanController.ScanState.FAST_SCAN, scanController.getScanState());

    mMockContext.sendScreenOffEvent();
    assertEquals(ScanController.ScanState.NO_SCAN, scanController.getScanState());

    scanController.onMotionTimeout();
    assertEquals(ScanController.ScanState.NO_SCAN, scanController.getScanState());

    scanController.onMotion();
    assertEquals(ScanController.ScanState.NO_SCAN, scanController.getScanState());

    scanController.unregister();
  }
}


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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

class MockContext extends android.test.mock.MockContext {
  Context mContext;
  BroadcastReceiver mReceiver;

  MockContext(Context context) {
    mContext = context;
  }

  @Override
  public Context getApplicationContext() {
    return mContext;
  }

  @Override
  public String getPackageName() {
    return mContext.getPackageName();
  }

  @Override
  public ContentResolver getContentResolver() {
    return mContext.getContentResolver();
  }

  @Override
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    mReceiver = receiver;
    return mContext.registerReceiver(receiver, filter);
  }

  @Override
  public void unregisterReceiver(BroadcastReceiver receiver) {
    mReceiver = null;
    mContext.unregisterReceiver(receiver);
  }

  @Override
  public Object getSystemService(String name) {
    return mContext.getSystemService(name);
  }

  public void sendScreenOnEvent() {
    mReceiver.onReceive(this, new Intent(Intent.ACTION_SCREEN_ON));
  }

  public void sendScreenOffEvent() {
    mReceiver.onReceive(this, new Intent(Intent.ACTION_SCREEN_OFF));
  }
}
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

import android.app.Activity;
import android.os.Bundle;

import org.uribeacon.widget.ConfigUriBeaconFragment;

public class ConfigActivity extends Activity implements ConfigUriBeaconFragment.ConfigUriBeaconListener {

  private final String TAG = ConfigActivity.class.getCanonicalName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    setContentView(R.layout.activity_configbeacon);
    if (findViewById(R.id.fragment_container) != null) {

      // However, if we're being restored from a previous state,
      // then we don't need to do anything and should return or else
      // we could end up with overlapping fragments.
      if (savedInstanceState != null) {
        return;
      }

      // Create a new Fragment to be placed in the activity layout
      ConfigUriBeaconFragment fragment = new ConfigUriBeaconFragment();

      // In case this activity was started with special instructions from an
      // Intent, pass the Intent's extras to the fragment as arguments
      fragment.setArguments(getIntent().getExtras());

      // Add the fragment to the 'fragment_container' FrameLayout
      getFragmentManager()
          .beginTransaction()
          .add(R.id.fragment_container, fragment).commit();
    }
  }

  @Override
  public void beaconDisconnected() {
    finish();
  }
}

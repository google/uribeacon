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

import org.uribeacon.scan.compat.BluetoothLeScannerCompatProvider;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class SettingsActivity extends Activity {

  public static class SettingsFragment extends PreferenceFragment implements
      OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_general);
      // Establish listeners for scan mode list preference change
      setSummaryChangeListener(R.string.pref_key_serial_scan_mode);
    }

    // Change the summary of the preference. Allow null new value for initialization.
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      if (preference instanceof SwitchPreference) {
        SwitchPreference s = (SwitchPreference) preference;

        if (newValue != null) {
          s.setChecked(((Boolean) newValue));
        }
      } else if (preference instanceof EditTextPreference) {
        EditTextPreference e = (EditTextPreference) preference;
        if (newValue != null) {
          e.setText(newValue.toString());
        }
        preference.setSummary(e.getText());
      } else if (preference instanceof ListPreference) {
        ListPreference l = (ListPreference) preference;
        if (newValue != null) {
          l.setValue(newValue.toString());
        }
        preference.setSummary(l.getEntry());
      } else if (preference instanceof CheckBoxPreference) {
        CheckBoxPreference c = (CheckBoxPreference) preference;
        if (newValue != null) {
          c.setChecked(((Boolean) newValue));
        }
      }

      return true;
    }

    // Initialize the preference summary and register a change listener.
    private void setSummaryChangeListener(int resId) {
      Preference preference = findPreference(getString(resId));
      onPreferenceChange(preference, null);
      preference.setOnPreferenceChangeListener(this);
    }

    private Preference getPreference(int id) {
      return findPreference(getString(id));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment())
        .commit();
  }
}

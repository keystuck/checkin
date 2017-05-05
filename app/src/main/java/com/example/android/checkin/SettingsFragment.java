package com.example.android.checkin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Created by emilystuckey on 3/7/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_checkin);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        //go through preferences (other than checkbox) and set up pref summary
        for (int i = 0; i < count; i++){
            Preference p = preferenceScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }

    }

    /**
     * Update summary for preference
     *
     * @param preference The pref to be updated
     * @param value     The value to update it to
     */
    private void setPreferenceSummary(Preference preference, String value){
        if (preference instanceof EditTextPreference)
            preference.setSummary(value);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //Figure out which one changed
        Preference preference = findPreference(s);
        if (null != preference){
            //update summary
            if (!(preference instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(preference.getKey(), "");
                if (preference.getKey().equals(getString(R.string.contact_num_key))) {
                    setPreferenceSummary(preference, value);
                }
                else {
                    Toast.makeText(getContext(), "This value cannot be changed by the user at this time", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

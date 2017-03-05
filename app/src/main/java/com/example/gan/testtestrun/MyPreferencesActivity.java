package com.example.gan.testtestrun;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;


/**
 * Created by gan on 2/28/17.
 */

//public class MyPreferencesActivity extends PreferenceActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
public class MyPreferencesActivity extends PreferenceActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 100;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 200;
    private static ListPreference wifiConnections;
    public MyPreferencesActivity() {
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferencesFragment()).commit();
    }

    public static class MyPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            wifiConnections = (ListPreference)findPreference("wifiConnections");
            if(wifiConnections != null)
            {
                wifiConnections.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), WifiSettingActivity.class);
                        startActivity(intent);
                        return false;
                    }
                });
            }
        }
    }

}

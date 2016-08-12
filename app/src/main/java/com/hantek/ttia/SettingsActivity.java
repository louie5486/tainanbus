package com.hantek.ttia;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hantek.ttia.module.SharedPreferencesHelper;

import java.io.File;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements View.OnClickListener {
    private Button settingButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        Log.d("SettingsActivity", "onBuildHeaders");
        loadHeadersFromResource(R.xml.pref_headers, target);

        setContentView(R.layout.settings_pref_layout);

        settingButton = (Button) findViewById(R.id.settingButton);
        settingButton.setOnClickListener(this);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference, Preference.OnPreferenceChangeListener onPreferenceChangeListener) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        // Trigger the listener immediately with the preference's current value.
        onPreferenceChangeListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // return super.isValidFragment(fragmentName);
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onClick(View v) {
        String id = SharedPreferencesHelper.getInstance(this).getCustomerID();
        String cID = SharedPreferencesHelper.getInstance(this).getCarID();

        if (id.equalsIgnoreCase("") || id.equalsIgnoreCase("0")) {
            return;
        }

        if (cID.equalsIgnoreCase("")) {
            return;
        }

        if (SharedPreferencesHelper.getInstance(this).setConfig(1)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle("請確認是否刪除「路線檔」及「音效檔」");

            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };

            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Road");
                    delete(file);
                    file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/advert");
                    delete(file);
                    finish();
                }
            };

            dialog.setNegativeButton(R.string.no, negative);
            dialog.setPositiveButton(R.string.yes, positive);
            dialog.create().show();
        }
    }

    private void delete(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                delete(new File(dir, children[i]));
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_car_data);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            try {
                bindPreferenceSummaryToValue(findPreference("customer_id"), this);
                bindPreferenceSummaryToValue(findPreference("car_id"), this);
                bindPreferenceSummaryToValue(findPreference("imei"), this);
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_key_car_id))) {
                try {
                    int id = Integer.parseInt(stringValue);
                    if (id > 65535)
                        return false;
                } catch (Exception e) {
                    return false;
                }
            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_key_customer_id))) {
                try {
                    int id = Integer.parseInt(stringValue);
                    if (id > 65535)
                        return false;
                } catch (Exception e) {
                    return false;
                }
            } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_key_imei))) {
                if (stringValue.length() > 15)
                    return false;
            }

            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
            return true;
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"), this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    }
}

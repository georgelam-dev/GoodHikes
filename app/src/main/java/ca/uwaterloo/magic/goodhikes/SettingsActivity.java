package ca.uwaterloo.magic.goodhikes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import ca.uwaterloo.magic.goodhikes.data.UserManager;

/**
 * Created by GeorgeLam on 2/19/2016.
 */

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private boolean setup_interval, setup_logout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private UserManager userManager;
    protected static final String LOG_TAG = "SettingsActivity";
    private PreferenceScreen screen;
    private Preference login_pref;
    private Preference profile_pref;
    private Preference logout_pref;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        setup_interval = false;
        setup_logout = false;

        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        screen = getPreferenceScreen();
        login_pref = getPreferenceScreen().findPreference("Login");
        profile_pref = getPreferenceScreen().findPreference("User Profile");
        logout_pref = getPreferenceScreen().findPreference("Logout");

        userManager = new UserManager(getApplicationContext());
        if (userManager.checkLogin()) {
            screen.removePreference(login_pref);
            screen.addPreference(profile_pref);
            screen.addPreference(logout_pref);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.logout)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.profile)));
        } else {
            screen.addPreference(login_pref);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.login)));
            screen.removePreference(profile_pref);
            screen.removePreference(logout_pref);
        }

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.interval_pref)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.map_pref)));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        userManager = new UserManager(getApplicationContext());
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        if (preference instanceof ListPreference) {
            preference.setOnPreferenceChangeListener(this);
        } else {
            preference.setOnPreferenceClickListener(this);
        }

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            if (listPreference.getKey().equals("Interval")) {
                if (setup_interval == true) {
                    AlertDialog.Builder warning_prompt = new AlertDialog.Builder(this);
                    warning_prompt.setMessage("A tracking restart is required for this setting to take effect");
                    warning_prompt.setCancelable(false);
                    warning_prompt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
                    AlertDialog interval_warning = warning_prompt.create();
                    interval_warning.show();
                }
                setup_interval = true;
            }
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("Logout")) {
            AlertDialog.Builder logout_prompt = new AlertDialog.Builder(this);
            logout_prompt.setMessage("Are you sure?");
            logout_prompt.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    userManager.clearUser();
                    Log.d(LOG_TAG, "logout");
                    Intent restart = new Intent(getApplicationContext(), LoginActivity.class);
                    restart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    sendBroadcast(new Intent("logout"));
                    startActivity(restart);
                    finish();
                }
            });
            logout_prompt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {}
            });
            AlertDialog logout_message = logout_prompt.create();
            logout_message.show();
        }
        else if(preference.getKey().equals("Login")) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        else if (preference.getKey().equals("User Profile")) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Settings Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ca.uwaterloo.magic.goodhikes/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Settings Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://ca.uwaterloo.magic.goodhikes/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

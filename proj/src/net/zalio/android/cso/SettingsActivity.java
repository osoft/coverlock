package net.zalio.android.cso;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
	protected static final String TAG = "SettingsActivity";
	private static final int REQ_CODE = Settings.class.hashCode();
	private static Context mContext;
	private DevicePolicyManager mPolicyManager;


    @SuppressWarnings("deprecation")
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mContext = getBaseContext();
        setupSimplePreferencesScreen();
        PreferenceManager prefManager = this.getPreferenceManager();
        final SharedPreferences pref = prefManager.getSharedPreferences();
        
        Settings.init(this);
       
        findPreference("pref_enable_service").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        findPreference("pref_timeout_list").setDependency("pref_enable_service");
        findPreference("pref_enable_auto_screen_on").setDependency("pref_enable_service");
        findPreference("pref_enable_auto_screen_off").setDependency("pref_enable_service");
        findPreference("pref_enable_auto_screen_on").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        findPreference("pref_enable_auto_screen_off").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        
        mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean active = mPolicyManager
				.isAdminActive(new ComponentName(this,
						CSODeviceAdminReceiver.class));
        if(active){
            if(Settings.enable_Service){        	
            	Intent i = new Intent(this.getApplicationContext(), CoverDetectService.class);
            	startService(i);
            }
        	return;
        }
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.prompt_acquiring_device_admin));
        builder.setPositiveButton(android.R.string.ok, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent acquireAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				acquireAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, 
						new ComponentName(SettingsActivity.this, CSODeviceAdminReceiver.class));
				acquireAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.prompt_acquiring_device_admin));
				startActivityForResult(acquireAdmin, REQ_CODE);
				dialog.dismiss();
			}
        	
        });
        builder.setNegativeButton(android.R.string.no, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				pref.edit().putBoolean("pref_enable_service", false).commit();
				if(CoverDetectService.getInstance() != null){
					CoverDetectService.getInstance().stopSelf();
				}
				dialog.cancel();
				finish();
			}
        	
        });
        builder.setCancelable(false);
        builder.create().show();
    }
    
    @Override
	protected
    void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode == REQ_CODE){
    		if(resultCode == Activity.RESULT_OK){
    			
    		}else{
    			finish();
    		}
    	}
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference("pref_timeout_list"));

    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
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
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                MyLog.i("cso", listPreference.getKey() + " changed to: " + listPreference.getEntryValues()[index]);
                if(listPreference.getKey().equals("pref_timeout_list")){
                	Settings.timeout_Screen_Off = Integer.parseInt((String) listPreference.getEntryValues()[index]);
                	
                }
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                //preference.setSummary(stringValue);
            	MyLog.i("cso", preference.getKey() + " changed to: " + stringValue);
            	if(preference.getKey().equals("pref_enable_service")){
            		if(stringValue.equals("true")){
            			MyLog.i(TAG, "Starting service");
            			Settings.enable_Service = true;
                    	Intent i = new Intent(mContext, CoverDetectService.class);
                    	mContext.startService(i);
                    	AliveKeeper keeper = new AliveKeeper(mContext);
                    	keeper.startKeeping();
            		}else{
            			MyLog.i(TAG, "Stopping Service");
            			AliveKeeper keeper = new AliveKeeper(mContext);
            			keeper.stopKeeping();
            			Settings.enable_Service = false;
            			CoverDetectService.getInstance().stopSelf();
            		}
            	}else if(preference.getKey().equals("pref_enable_auto_screen_on")){
            		if(stringValue.equals("true")){
            			Settings.enable_Auto_Screen_On = true;
            			MyLog.i(TAG, "enable auto screen on");
            		}else{
            			Settings.enable_Auto_Screen_On = false;
            			MyLog.i(TAG, "disabled auto screen on");
            		}          		
            	}else if(preference.getKey().equals("pref_enable_auto_screen_off")){
            		if(stringValue.equals("true")){
            			Settings.enable_Auto_Screen_Off = true;
            			MyLog.i(TAG, "enable auto screen off");
            		}else{
            			Settings.enable_Auto_Screen_Off = false;
            			MyLog.i(TAG, "disabled auto screen off");
            		}          		
            	}
            }
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
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_enable_service"));
            bindPreferenceSummaryToValue(findPreference("pref_timeout_list"));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
}

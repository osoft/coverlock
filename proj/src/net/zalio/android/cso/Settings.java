package net.zalio.android.cso;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	private static final String TAG = "Settings";
	public static final int DEFAULT_TIMEOUT_SCREEN_OFF = 3000;
	public static final boolean DEFAULT_ENABLE_SERVICE = false; 
	public static final boolean DEFAULT_ENABLE_AUTO_SCREEN_ON = true; 
	public static final boolean DEFAULT_ENABLE_AUTO_SCREEN_OFF = true;
	public static final boolean DEFAULT_ENABLE_DISABLE_AUTO_SCREEN_OFF_IN_LANDSCAPE = false;
	
	public static boolean enable_Service = DEFAULT_ENABLE_SERVICE;
	public static boolean enable_Auto_Screen_On = DEFAULT_ENABLE_AUTO_SCREEN_ON;
	public static boolean enable_Auto_Screen_Off = DEFAULT_ENABLE_AUTO_SCREEN_OFF;
	public static boolean enable_Disable_Auto_Screen_Off_In_Landscape = DEFAULT_ENABLE_DISABLE_AUTO_SCREEN_OFF_IN_LANDSCAPE;
	public static int timeout_Screen_Off = DEFAULT_TIMEOUT_SCREEN_OFF;
	
	
	public static void init(Context context){
		if(context == null){
			MyLog.w(TAG, "Invalid context!");
		}
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Settings.enable_Service = 
        		pref.getBoolean("pref_enable_service", DEFAULT_ENABLE_SERVICE);
        Settings.enable_Auto_Screen_On = 
        		pref.getBoolean("pref_enable_auto_screen_on", DEFAULT_ENABLE_AUTO_SCREEN_ON);
        Settings.enable_Auto_Screen_Off = 
        		pref.getBoolean("pref_enable_auto_screen_off", DEFAULT_ENABLE_AUTO_SCREEN_OFF);
        Settings.enable_Disable_Auto_Screen_Off_In_Landscape = 
        		pref.getBoolean("pref_enable_disable_auto_screen_off_in_landscape", DEFAULT_ENABLE_DISABLE_AUTO_SCREEN_OFF_IN_LANDSCAPE);
        Settings.timeout_Screen_Off = Integer.parseInt(
        		pref.getString("pref_timeout_list", String.valueOf(DEFAULT_TIMEOUT_SCREEN_OFF)));
	}
}

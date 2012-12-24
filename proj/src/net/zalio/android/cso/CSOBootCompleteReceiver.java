package net.zalio.android.cso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CSOBootCompleteReceiver extends BroadcastReceiver {
	final static String TAG = "CSOBootCompleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent){
		MyLog.i(TAG, "OnBoot");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Settings.enable_Service = pref.getBoolean("pref_enable_service", false);
        Settings.enable_Auto_Screen_On = pref.getBoolean("pref_enable_auto_screen_on", false);
        Settings.timeout_Screen_Off = pref.getInt("pref_timeout_list", 1000);
        
        if(Settings.enable_Service){
        	MyLog.i(TAG, "CSO enabled!");
        	Intent i = new Intent(context, CoverDetectService.class);
        	context.startService(i);
        }else{
        	MyLog.i(TAG, "CSO not enabled!");
        }
	}

}

package net.zalio.android.cso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CSOBootCompleteReceiver extends BroadcastReceiver {
	final static String TAG = "CSOBootCompleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent){
		MyLog.i(TAG, "OnBoot");
		
        Settings.init(context.getApplicationContext());
        
        if(Settings.enable_Service){
        	MyLog.i(TAG, "CSO enabled!");
        	Intent i = new Intent(context, CoverDetectService.class);
        	context.startService(i);
        	AliveKeeper keeper = new AliveKeeper(context);
        	keeper.startKeeping();
        }else{
        	MyLog.i(TAG, "CSO not enabled!");
        }
	}

}

package net.zalio.android.cso;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AliveKeeper {

	private Context mContext;
	AlarmManager mAlarmManager;
	PendingIntent mPi;

	public AliveKeeper(Context context){
		mContext = context;
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}
	public void startKeeping(){
		if(mContext == null){
			return;
		}
		Intent i = new Intent(mContext, CoverDetectService.class);
		mPi = PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.cancel(mPi);
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 5, mPi);
	}
	
	public void stopKeeping(){
		Intent i = new Intent(mContext, CoverDetectService.class);
		mPi = PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.cancel(mPi);
	}
}

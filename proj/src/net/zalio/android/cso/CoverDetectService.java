package net.zalio.android.cso;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class CoverDetectService extends Service implements SensorEventListener {
	private final static String TAG = "cso";
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private DevicePolicyManager mPolicyManager;

	private final static int MSG_SCREEN_OFF = 10001;
	protected static final int MSG_SCREEN_ON = 10002;
	
	static private CoverDetectService instance = null;

	private Handler mHandler = new Handler() {
		@SuppressLint("Wakelock")
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SCREEN_OFF:
				boolean active = mPolicyManager
						.isAdminActive(new ComponentName(
								CoverDetectService.this,
								CSODeviceAdminReceiver.class));
				if (active) {
					MyLog.i(TAG, "Locking!");
					mPolicyManager.lockNow();
				}
				break;
			case MSG_SCREEN_ON:
				PowerManager pm = (PowerManager) CoverDetectService.this
						.getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(
						PowerManager.FULL_WAKE_LOCK
								| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
				wl.acquire(5000);
				
				break;
			}
		}
	};
	
	BroadcastReceiver mScreenOnOffReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				MyLog.i(TAG, "Screen is ON");
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				MyLog.i(TAG, "Screen is OFF");
			}
			
		}
		
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

		MyLog.i("cso", "Received start id " + startId + ": " + intent);
		
		CoverDetectService.setInstance(this);

		// Add a SCREEN_ON SCREEN_OFF listener to save battery.
		//IntentFilter iF = new IntentFilter();
		//iF.addAction(Intent.ACTION_SCREEN_OFF);
		//iF.addAction(Intent.ACTION_SCREEN_ON);
		//this.registerReceiver(mScreenOnOffReceiver, iF);
		
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}
	
    @Override
    public void onDestroy() {
    	mSensorManager.unregisterListener(this);
    	//this.unregisterReceiver(mScreenOnOffReceiver);
        // Tell the user we stopped.
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		MyLog.i(TAG, "Proximity: " + event.values[0]);
		float prox = event.values[0];
		if (prox == 0.0f) {
			scheduleScreenOff();
		} else {
			cancelScreenOff();
			if(Settings.enable_Auto_Screen_On){
			    turnScreenOn();
			}
		}

	}

	private void scheduleScreenOff() {
		if (mHandler.hasMessages(MSG_SCREEN_OFF)) {
			return;
		}
		
		mHandler.sendEmptyMessageDelayed(MSG_SCREEN_OFF, Settings.timeout_Screen_Off);
		MyLog.i(TAG, "Scheduled Screen Off");
	}

	private void cancelScreenOff() {
		if(mHandler.hasMessages(MSG_SCREEN_OFF)){
			mHandler.removeMessages(MSG_SCREEN_OFF);
			MyLog.i(TAG, "Cancelled Screen Off");			
		}
	}
	private void turnScreenOn() {
		
		mHandler.sendEmptyMessage(MSG_SCREEN_ON);
		MyLog.i(TAG, "Turn Screen On");
	}

	public static CoverDetectService getInstance() {
		return instance;
	}

	public static void setInstance(CoverDetectService instance) {
		CoverDetectService.instance = instance;
	}
}

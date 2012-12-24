package net.zalio.android.cso;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CSODeviceAdminReceiver extends DeviceAdminReceiver {
	
	@Override
	public void onEnabled(Context context, Intent intent){
		MyLog.i("CSODeviceAdminReceiver", "CSO Enabled!");
		Toast.makeText(context, "ENABLED", Toast.LENGTH_SHORT).show();
        
        super.onEnabled(context, intent);
	}
	
	@Override
	public void onDisabled(Context context, Intent intent){
		MyLog.i("CSODeviceAdminReceiver", "CSO Disabled!");
		Toast.makeText(context, "DISABLED", Toast.LENGTH_SHORT).show();
        
        super.onDisabled(context, intent);
	}
}

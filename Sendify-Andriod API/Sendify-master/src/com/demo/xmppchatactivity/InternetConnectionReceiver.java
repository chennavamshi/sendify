package com.demo.xmppchatactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;


public class InternetConnectionReceiver extends BroadcastReceiver {
	
	private String LOGTAG = "Service"; 
	
	@Override    
	public void onReceive(final Context context, final Intent intent) {
 
		int status = NetworkUtil.getConnectivityStatus(context);
		if(status==0){
			Log.i("XMPPChatActivityDemo","Network Status is : "+status+" and stopping the service." );
			Intent myIntent = new Intent(context, XMPPChatDemoService.class);
	        context.stopService(myIntent);
		} else{
			Log.i("XMPPChatActivityDemo","Network Status is : "+status );
		}
    }
} 
package com.sendify.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;


public class InternetConnectionReceiver extends BroadcastReceiver {
	
	private static final String LOGTAG = SendifyLogUtil.makeLogTag(InternetConnectionReceiver.class); 

	private SendifyService sendifyService;

    public InternetConnectionReceiver(SendifyService sendifyService) {
        this.sendifyService = sendifyService;
    }

	@Override    
	public void onReceive(final Context context, final Intent intent) {
		Log.d(LOGTAG, "ConnectivityReceiver.onReceive()...");
        String action = intent.getAction();
        Log.d(LOGTAG, "action=" + action);

		int status = NetworkUtil.getConnectivityStatus(context);
		if(status==0){
			Log.e(LOGTAG, "Network unavailable");
            sendifyService.disconnect();
		} else{
			Log.i(LOGTAG,"Network Status is : "+status );
			Log.i(LOGTAG, "Network connected");
            sendifyService.connect();
		}
    }
} 
package com.sendify.service;

import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public final class SendifyServiceManager {
	
	private static final String LOGTAG = SendifyLogUtil.makeLogTag(SendifyServiceManager.class);

    private Context context;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private String xmppService;

    private int xmppPort;
    
    private String callbackActivityPackageName;

    private String callbackActivityClassName;

    public SendifyServiceManager(Context context) {
    	Log.i(LOGTAG, "SendifyServiceManager()........START");
    	this.context = context;

        if (context instanceof Activity) {
            Log.i(LOGTAG, "Callback Activity...");
            Activity callbackActivity = (Activity) context;
            callbackActivityPackageName = callbackActivity.getPackageName();
            callbackActivityClassName = callbackActivity.getClass().getName();
        }
        
        xmppHost = SendifyConstant.getHOST();
        xmppPort = SendifyConstant.getPORT();
        xmppService = SendifyConstant.getSERVICE();
        Log.i(LOGTAG, "xmppHost=" + xmppHost);
        Log.i(LOGTAG, "xmppPort=" + xmppPort);
        Log.i(LOGTAG, "xmppService=" + xmppService);
        
        sharedPrefs = context.getSharedPreferences(SendifyConstant.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(SendifyConstant.getHOST(), xmppHost);
        editor.putInt(""+SendifyConstant.getPORT(), xmppPort);
        //editor.putString(SendifyConstant.CALLBACK_ACTIVITY_PACKAGE_NAME,callbackActivityPackageName);
        //editor.putString(SendifyConstant.CALLBACK_ACTIVITY_CLASS_NAME,callbackActivityClassName);
        editor.commit();
        Log.i(LOGTAG, "SendifyServiceManager()........END");
    }

    public void startService() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = SendifyService.getIntent();
                context.startService(intent);
            }
        });
        serviceThread.start();
    }

    public void stopService() {
        Intent intent = SendifyService.getIntent();
        context.stopService(intent);
    }
  
//    public void setNotificationIcon(int iconId) {
//        Editor editor = sharedPrefs.edit();
//        editor.putInt(SendifyConstant.NOTIFICATION_ICON, iconId);
//        editor.commit();
//    }
//
//    public static void viewNotificationSettings(Context context) {
//        Intent intent = new Intent().setClass(context,
//                NotificationSettingsActivity.class);
//        context.startActivity(intent);
//    }

}

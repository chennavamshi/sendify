package com.sendify.service;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SendifyService extends Service {

	private static final String LOGTAG = SendifyLogUtil.makeLogTag(SendifyService.class);

    public static final String SERVICE_NAME = "com.sendify.service.SendifyService";

    private SharedPreferences sharedPrefs;
    
    private String deviceId;

	private TelephonyManager telephonyManager;
    
	private TaskTracker taskTracker;
	
	private ExecutorService executorService;

    private TaskSubmitter taskSubmitter;  
    
    private SendifyManager sendifyManager;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onCreate() {
        Log.d(LOGTAG, "SendifyServiceManager.onCreate()...");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        sharedPrefs = getSharedPreferences(SendifyConstant.SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);

        // Get deviceId
        deviceId = telephonyManager.getDeviceId();
        Editor editor = sharedPrefs.edit();
        editor.putString(SendifyConstant.DEVICE_ID, deviceId);
        editor.commit();

        // If running on an emulator
        if (deviceId == null || deviceId.trim().length() == 0
                || deviceId.matches("0+")) {
            if (sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
                deviceId = sharedPrefs.getString(SendifyConstant.EMULATOR_DEVICE_ID,
                        "");
            } else {
                deviceId = (new StringBuilder("EMU")).append(
                        (new Random(System.currentTimeMillis())).nextLong())
                        .toString();
                editor.putString(SendifyConstant.EMULATOR_DEVICE_ID, deviceId);
                editor.commit();
            }
        }
        Log.d(LOGTAG, "deviceId=" + deviceId);

        sendifyManager = new SendifyManager(this);

//        taskSubmitter.submit(new Runnable() {
//            public void run() {
//                NotificationService.this.start();
//            }
//        });
    }
	
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOGTAG, "onStart()...");
    }

    @Override
    public void onDestroy() {
        Log.d(LOGTAG, "onDestroy()...");
        stop();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOGTAG, "onRebind()...");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOGTAG, "onUnbind()...");
        return true;
    }

    public static Intent getIntent() {
        return new Intent(SERVICE_NAME);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }

    public TaskTracker getTaskTracker() {
        return taskTracker;
    }

    public SendifyManager getSendifyManager() {
        return sendifyManager;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void connect() {
        Log.d(LOGTAG, "connect()...");
        taskSubmitter.submit(new Runnable() {
            public void run() {
                SendifyService.this.getSendifyManager().connect();
            }
        });
    }

    public void disconnect() {
        Log.d(LOGTAG, "disconnect()...");
        taskSubmitter.submit(new Runnable() {
            public void run() {
                SendifyService.this.getSendifyManager().disconnect();
            }
        });
    }

//    private void registerNotificationReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constants.ACTION_SHOW_NOTIFICATION);
//        filter.addAction(Constants.ACTION_NOTIFICATION_CLICKED);
//        filter.addAction(Constants.ACTION_NOTIFICATION_CLEARED);
//        registerReceiver(notificationReceiver, filter);
//    }
//
//    private void unregisterNotificationReceiver() {
//        unregisterReceiver(notificationReceiver);
//    }

//    private void registerConnectivityReceiver() {
//        Log.d(LOGTAG, "registerConnectivityReceiver()...");
//        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_DATA_CONNECTION_STATE); 
//        IntentFilter filter = new IntentFilter();
//        // filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(connectivityReceiver, filter);
//    }

//    private void unregisterConnectivityReceiver() {
//        Log.d(LOGTAG, "unregisterConnectivityReceiver()...");
//        telephonyManager.listen(phoneStateListener,
//                PhoneStateListener.LISTEN_NONE);
//        unregisterReceiver(connectivityReceiver);
//    }

    private void start() {
        Log.d(LOGTAG, "start()...");
//        registerNotificationReceiver();
//        registerConnectivityReceiver();
        Intent intent = getIntent();
        startService(intent);
        //sendifyManager.connect();
    }

    private void stop() {
        Log.d(LOGTAG, "stop()...");
//        unregisterNotificationReceiver();
//        unregisterConnectivityReceiver();
//        xmppManager.disconnect(); 
        executorService.shutdown();
    }

    public class TaskSubmitter {

        final SendifyService sendifyService;

        public TaskSubmitter(SendifyService sendifyService) {
            this.sendifyService = sendifyService;
        }

        @SuppressWarnings("unchecked")
        public Future submit(Runnable task) {
            Future result = null;
            if (!sendifyService.getExecutorService().isTerminated() && !sendifyService.getExecutorService().isShutdown()
                    && task != null) {
                result = sendifyService.getExecutorService().submit(task);
            }
            return result;
        }

    }

    public class TaskTracker {

        final SendifyService sendifyService;

        public int count;

        public TaskTracker(SendifyService sendifyService) {
            this.sendifyService = sendifyService;
            this.count = 0;
        }

        public void increase() {
            synchronized (sendifyService.getTaskTracker()) {
                sendifyService.getTaskTracker().count++;
                Log.d(LOGTAG, "Incremented task count to " + count);
            }
        }

        public void decrease() {
            synchronized (sendifyService.getTaskTracker()) {
                sendifyService.getTaskTracker().count--;
                Log.d(LOGTAG, "Decremented task count to " + count);
            }
        }

    }

}

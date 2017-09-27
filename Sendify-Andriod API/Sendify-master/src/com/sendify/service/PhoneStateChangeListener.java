package com.sendify.service;


import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateChangeListener extends PhoneStateListener {

	private static final String LOGTAG = SendifyLogUtil
            .makeLogTag(PhoneStateChangeListener.class);

    private final SendifyService sendifyService;

    public PhoneStateChangeListener(SendifyService sendifyService) {
        this.sendifyService = sendifyService;
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        super.onDataConnectionStateChanged(state);
        Log.d(LOGTAG, "onDataConnectionStateChanged()...");
        Log.d(LOGTAG, "Data Connection State = " + getState(state));
        
        if (state == TelephonyManager.DATA_CONNECTED) {
            sendifyService.connect();  
        }
    }

    private String getState(int state) {
        switch (state) {
        case 0: // '\0'
            return "DATA_DISCONNECTED";
        case 1: // '\001'
            return "DATA_CONNECTING";
        case 2: // '\002'
            return "DATA_CONNECTED";
        case 3: // '\003'
            return "DATA_SUSPENDED";
        }
        return "DATA_<UNKNOWN>";
    }
}

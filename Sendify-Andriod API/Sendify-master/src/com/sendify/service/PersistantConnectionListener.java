package com.sendify.service;

import org.jivesoftware.smack.ConnectionListener;

import android.util.Log;

public class PersistantConnectionListener implements ConnectionListener{

	private static final String LOGTAG = SendifyLogUtil.makeLogTag(PersistantConnectionListener.class);

    private final SendifyManager sendifyManager;

    public PersistantConnectionListener(SendifyManager sendifyManager) {
        this.sendifyManager = sendifyManager;
    }

    public void connectionClosed() {
        Log.d(LOGTAG, "connectionClosed()...");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(LOGTAG, "connectionClosedOnError()...");
        if (sendifyManager.getConnection() != null
                && sendifyManager.getConnection().isConnected()) {
            sendifyManager.getConnection().disconnect();
        }
        sendifyManager.startReconnectionThread();
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d(LOGTAG, "reconnectingIn()...");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(LOGTAG, "reconnectionFailed()...");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(LOGTAG, "reconnectionSuccessful()...");
    }

}

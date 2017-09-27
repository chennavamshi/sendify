
package com.sendify.service;

import android.util.Log;

public class ReconnectionThread extends Thread {

    private static final String LOGTAG = SendifyLogUtil.makeLogTag(ReconnectionThread.class);

    private final SendifyManager sendifyManager;

    private int waiting;

    ReconnectionThread(SendifyManager sendifyManager) {
        this.sendifyManager = sendifyManager;
        this.waiting = 0;
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                Log.d(LOGTAG, "Trying to reconnect in " + waiting()
                        + " seconds");
                Thread.sleep((long) waiting() * 1000L);
                sendifyManager.connect();
                waiting++;
            }
        } catch (final InterruptedException e) {
            sendifyManager.getHandler().post(new Runnable() {
                public void run() {
                    sendifyManager.getConnectionListener().reconnectionFailed(e);
                }
            });
        }
    }

    private int waiting() {
        if (waiting > 20) {
            return 600;
        }
        if (waiting > 13) {
            return 300;
        }
        return waiting <= 7 ? 10 : 60;
    }
}

package com.demo.xmppchatactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {   

    @Override
    public void onReceive(Context context, Intent intent) {

    	intent = new Intent(context, XMPPChatDemoService.class);
		context.startService(intent);
    }
}
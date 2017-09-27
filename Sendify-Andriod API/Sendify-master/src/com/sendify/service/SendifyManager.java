package com.sendify.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

public class SendifyManager {

	private static final String LOGTAG = SendifyLogUtil.makeLogTag(SendifyManager.class);

    private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

    private Context context;

    private SendifyService.TaskSubmitter taskSubmitter;

    private SendifyService.TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private String xmppService;
    
    private XMPPConnection connection;

    private String username;

    private String password;

    private ConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;
    
    private List<Runnable> taskList;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;

    public SendifyManager(SendifyService sendifyService) {
        context = sendifyService;
        taskSubmitter = sendifyService.getTaskSubmitter();
        taskTracker = sendifyService.getTaskTracker();
        sharedPrefs = sendifyService.getSharedPreferences();

        xmppHost = sharedPrefs.getString(SendifyConstant.getHOST(), "localhost");
        xmppPort = sharedPrefs.getInt(""+SendifyConstant.getPORT(), 5222);
        username = sharedPrefs.getString(SendifyConstant.XMPP_USERNAME,"");
        password = sharedPrefs.getString(SendifyConstant.XMPP_PASSWORD,"");

        connectionListener = new PersistantConnectionListener(this);
//        notificationPacketListener = new NotificationPacketListener(this);

//        handler = new Handler();
//        taskList = new ArrayList<Runnable>();
        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        Log.d(LOGTAG, "connect()...");
        submitLoginTask();
    }

    public void disconnect() {
        Log.d(LOGTAG, "disconnect()...");
        terminatePersistentConnection();
    }
    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final SendifyManager sendifyManager = SendifyManager.this;

            public void run() {
                if (sendifyManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    //sendifyManager.getConnection().removePacketListener(sendifyManager.getNotificationPacketListener());  
                    sendifyManager.getConnection().disconnect();
                }
                sendifyManager.runTask();
            }

        };
        addTask(runnable);
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

//    public PacketListener getNotificationPacketListener() {
//        return notificationPacketListener;
//    }

    public void startReconnectionThread() {
        synchronized (reconnection) {
            if (!reconnection.isAlive()) {
                reconnection.setName("Xmpp Reconnection Thread");
                reconnection.start();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount() {
        removeAccount();
        submitLoginTask();
        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }
    
    public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }
    
    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }
    
    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered() {
        return sharedPrefs.contains(SendifyConstant.XMPP_USERNAME) && sharedPrefs.contains(SendifyConstant.XMPP_PASSWORD);
    }

    private void submitConnectTask() {
        Log.d(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
    }

    private void submitRegisterTask() {
        Log.d(LOGTAG, "submitRegisterTask()...");
        submitConnectTask();
        addTask(new RegisterTask());
    }

    private void submitLoginTask() {
        Log.d(LOGTAG, "submitLoginTask()...");
        submitRegisterTask();
        addTask(new LoginTask());
    }
    
    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)... running: "+running);
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
            	Log.d(LOGTAG, "addTask(runnable)... taskList.isEmpty() && !running");
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }
    
    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(SendifyConstant.XMPP_USERNAME);
        editor.remove(SendifyConstant.XMPP_PASSWORD);
        editor.commit();
    }
    
    private class ConnectTask implements Runnable {

        final SendifyManager sendifyManager;

        private ConnectTask() {
            this.sendifyManager = SendifyManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "ConnectTask.run()...");

            if (!sendifyManager.isConnected()) {
                // Create the configuration for this new connection
            	ConnectionConfiguration connConfig = new ConnectionConfiguration(SendifyConstant.getHOST(), SendifyConstant.getPORT(),SendifyConstant.getSERVICE());
                // connConfig.setSecurityMode(SecurityMode.disabled);
                //connConfig.setSecurityMode(SecurityMode.required);
                //connConfig.setSASLAuthenticationEnabled(false);
                //connConfig.setCompressionEnabled(false);

                XMPPConnection connection = new XMPPConnection(connConfig);
                sendifyManager.setConnection(connection);

                try {
                    // Connect to the server
                    connection.connect();
                    Log.i(LOGTAG, "XMPP connected successfully");

                    // packet provider
                    //ProviderManager.getInstance().addIQProvider("notification","androidpn:iq:notification",new NotificationIQProvider());

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed", e);
                    running = false;
                }
                sendifyManager.runTask();

            } else {
                Log.i(LOGTAG, "XMPP connected already");
                sendifyManager.runTask();
            }
        }
    }
    
    private class RegisterTask implements Runnable {

        final SendifyManager sendifyManager;

        private RegisterTask() {
            sendifyManager = SendifyManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "RegisterTask.run()...");

            if (!sendifyManager.isRegistered()) {
                final String newUsername = sharedPrefs.getString(SendifyConstant.DEVICE_ID, "");
                final String newPassword = "password";
                
                Registration registration = new Registration();

                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(registration.getPacketID()), new PacketTypeFilter(IQ.class));

                PacketListener packetListener = new PacketListener() {

                    public void processPacket(Packet packet) {
                        Log.d("RegisterTask.PacketListener",
                                "processPacket().....");
                        Log.d("RegisterTask.PacketListener", "packet="
                                + packet.toXML());
                        
                        if (packet instanceof IQ) {
                            IQ response = (IQ) packet;
                            if (response.getType() == IQ.Type.ERROR) {		
                                if (!response.getError().toString().contains(
                                        "409")) {
                                    Log.e(LOGTAG,
                                            "Unknown error while registering XMPP account! "
                                                    + response.getError()
                                                            .getCondition());
                                }
                            } else if (response.getType() == IQ.Type.RESULT) {  
                                sendifyManager.setUsername(newUsername);
                                sendifyManager.setPassword(newPassword);
                                Log.d(LOGTAG, "username=" + newUsername);
                                Log.d(LOGTAG, "password=" + newPassword);
                                Editor editor = sharedPrefs.edit();
                                editor.putString(SendifyConstant.XMPP_USERNAME,newUsername);
                                editor.putString(SendifyConstant.XMPP_PASSWORD,newPassword);
                                editor.commit();
                                Log.i(LOGTAG,
                                		"Account registered successfully");
                                sendifyManager.runTask();
                            }
                        }
                    }
                };
                connection.addPacketListener(packetListener, packetFilter);

                registration.setType(IQ.Type.SET);
                // registration.setTo(xmppHost);
                // Map<String, String> attributes = new HashMap<String, String>();
                // attributes.put("username", rUsername);
                // attributes.put("password", rPassword);
                // registration.setAttributes(attributes);
                registration.addAttribute("username", newUsername);
                registration.addAttribute("password", newPassword);
                
                registration.addAttribute("imsi", sharedPrefs.getString(SendifyConstant.DEVICE_ID, ""));
                registration.addAttribute("imei", "324234343434434");
                
                connection.sendPacket(registration);

            } else {
                Log.i(LOGTAG, "Account registered already");
                sendifyManager.runTask();
            }
        }
    }
    private class LoginTask implements Runnable {

        final SendifyManager sendifyManager;

        private LoginTask() {
            this.sendifyManager = SendifyManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "LoginTask.run()...");
            
            if (!sendifyManager.isAuthenticated()) {
                Log.d(LOGTAG, "username=" + username);
                Log.d(LOGTAG, "password=" + password);

                try {
                    sendifyManager.getConnection().login(username,password);
                    Log.d(LOGTAG, "Loggedn in successfully");

                    // connection listener
                    if (sendifyManager.getConnectionListener() != null) {
                        sendifyManager.getConnection().addConnectionListener(
                                sendifyManager.getConnectionListener());
                    }

                    // packet filter
                    //PacketFilter packetFilter = new PacketTypeFilter(NotificationIQ.class);
                    // packet listener
                    //PacketListener packetListener = sendifyManager.getNotificationPacketListener();
                    //connection.addPacketListener(packetListener, packetFilter);

                    sendifyManager.runTask();

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    
                    if (errorMessage != null
                            && errorMessage
                                    .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        sendifyManager.reregisterAccount();
                        return;
                    }
                    sendifyManager.startReconnectionThread();

                } catch (Exception e) {	  
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    sendifyManager.startReconnectionThread();  
                }

            } else {			
                Log.i(LOGTAG, "Logged in already");
                sendifyManager.runTask();
            }

        }
    }
}

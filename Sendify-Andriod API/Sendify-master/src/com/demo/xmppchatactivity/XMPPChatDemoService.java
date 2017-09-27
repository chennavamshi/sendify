package com.demo.xmppchatactivity;

import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class XMPPChatDemoService extends Service {

	public static final String HOST = "";
	public static final int PORT = 5222;
	public static final String SERVICE = "";
	public static final String USERNAME = "";
	public static final String PASSWORD = "";
	
	public static Context con;
	private XMPPConnection connection;
	ResultReceiver resultReceiver;
	private Intent intent;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return null;
	}
	
	
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.intent = intent;
		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
		Log.d("XMPPChatActivityDemo", "Service Started.");
		connect();
	    return START_STICKY;
	}
	
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
		
	    if (connection != null) {
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseBareAddress(message .getFrom());
						Log.i("XMPPChatActivityDemo", "Text Recieved " + message.getBody() + " from " + fromName );
						// intent.putExtra("Text Received", message.getBody() + " from " + fromName);
					}
				}
			}, filter);
		}
		
	}
	
	public void connect() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// Create a connection
				SmackAndroid.init(getApplicationContext());
				Log.d("XMPPChatActivityDemo", "Init successful.");
				AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration(HOST, PORT, SERVICE);
				 //ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
				XMPPConnection connection = new XMPPConnection(connConfig);

				try {
					connection.connect();
					Log.i("XMPPChatActivityDemo", "Connected to " + connection.getHost());
				} catch (XMPPException ex) {
					Log.e("XMPPChatActivityDemo", "Failed to connect to " + connection.getHost());
					Log.e("XMPPChatActivityDemo", ex.toString());
					setConnection(null);
				}
				
				try {
					// SASLAuthentication.supportSASLMechanism("PLAIN", 0);
					connection.login(USERNAME, PASSWORD);
					Log.i("XMPPChatActivityDemo",
							"Logged in as " + connection.getUser());

					// Set the status to available
					Presence presence = new Presence(Presence.Type.available);
					connection.sendPacket(presence);
					setConnection(connection);

					Roster roster = connection.getRoster();
					Collection<RosterEntry> entries = roster.getEntries();
					for (RosterEntry entry : entries) {
						Log.d("XMPPChatActivityDemo", "--------------------------------------");
						Log.d("XMPPChatActivityDemo", "RosterEntry " + entry);
						Log.d("XMPPChatActivityDemo", "User: " + entry.getUser());
						Log.d("XMPPChatActivityDemo", "Name: " + entry.getName());
						Log.d("XMPPChatActivityDemo", "Status: " + entry.getStatus());
						Log.d("XMPPChatActivityDemo", "Type: " + entry.getType());
						
						Presence entryPresence = roster.getPresence(entry.getUser());

						Log.d("XMPPChatActivityDemo", "Presence Status: " + entryPresence.getStatus());
						Log.d("XMPPChatActivityDemo", "Presence Type: " + entryPresence.getType());
						
						Presence.Type type = entryPresence.getType();
						
						if (type == Presence.Type.available) {
							Log.d("XMPPChatActivityDemo", "Presence AVIALABLE");
						}
						
						Log.d("XMPPChatActivityDemo", "Presence : " + entryPresence);
					}
					//createAccount();
					//createNode();
					//getNode();
					//subscribe();
					//publish();					
					//subscribe();
					//getNode();
				} catch (XMPPException ex) {
					Log.e("XMPPChatActivityDemo", "Failed to log in as " + USERNAME);
					Log.e("XMPPChatActivityDemo", ex.toString());
					setConnection(null);
				}
			}
		});
		
		t.start();
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTaskRemoved(Intent rootIntent) {
	    Log.e("FLAGX : ", ServiceInfo.FLAG_STOP_WITH_TASK + "");
	    Intent restartServiceIntent = new Intent(getApplicationContext(),
	            this.getClass());
	    restartServiceIntent.setPackage(getPackageName());

	    PendingIntent restartServicePendingIntent = PendingIntent.getService(
	            getApplicationContext(), 1, restartServiceIntent,
	            PendingIntent.FLAG_ONE_SHOT);
	    AlarmManager alarmService = (AlarmManager) getApplicationContext()
	            .getSystemService(Context.ALARM_SERVICE);
	    alarmService.set(AlarmManager.ELAPSED_REALTIME,
	            SystemClock.elapsedRealtime() + 1000,
	            restartServicePendingIntent);

	    super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onDestroy() {
		/*super.onDestroy();
		try {
			if (connection != null)
				connection.disconnect();
		} catch (Exception e) {}
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
		Log.d("XMPPChatActivityDemo", "Service Stopped.");*/
		Log.d("XMPPChatActivityDemo", "Service Stopped.");
		sendBroadcast(new Intent("StartKill"));
	}
	public void createAccount() {
		
		AccountManager accountManager=new AccountManager(connection);
		HashMap<String, String> attr = new HashMap<String, String>(); 
		attr.put("user", "sub"); 
		attr.put("password", "sub");
		
		try {
		    accountManager.createAccount("shashank", "password", attr);
		} catch (XMPPException ex) {
		    Log.d("XMPPChatActivityDemo", ex.toString());
		}
    }
    
    public void createNode() {
		
		PubSubManager pubSubManager = new PubSubManager(this.connection);
		
        ConfigureForm form = new ConfigureForm(FormType.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);

        LeafNode leafNode;
        
		try {
			leafNode = pubSubManager.createNode("TestNode0");
			leafNode.sendConfigurationForm(form);
			Log.d("XMPPChatActivityDemo", "Node created succesfully.");
		} catch (XMPPException ex) {
			// TODO Auto-generated catch block
			Log.d("XMPPChatActivityDemo", ex.toString());
		}
    }
    
    public void getNode() {
		
		// Create a pubsub manager using an existing Connection
        PubSubManager pubSubManager = new PubSubManager(this.connection);
		LeafNode node;
		
		try {
			// Get the node
			node = pubSubManager.getNode("TestNode0");
			Log.d("XMPPChatActivityDemo", "Node retrieved succesfully.");
		} catch (XMPPException ex) {
			// TODO Auto-generated catch block
			Log.d("XMPPChatActivityDemo", ex.toString());
		}
		
    }

    public void publish() {
		
		// Create a pubsub manager using an existing Connection
        PubSubManager pubSubManager = new PubSubManager(this.connection);
        
        SimplePayload payload = new SimplePayload(
                "elementname",
                "pubsub:testnode:elementname",
                "<elementname>Hello World!</elementname>");
        
        PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(
                 null, payload);        
        
        LeafNode node;
		
        try {
			// Get the node
			node = (LeafNode) pubSubManager.getNode("TestNode0");
			Log.d("XMPPChatActivityDemo", node.toString() + " retrieved succesfully.");
			// Publish an Item with payload
			node.publish(item);
			Log.d("XMPPChatActivityDemo", item.toString() + " published succesfully.");
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			Log.d("XMPPChatActivityDemo", ex.toString());
		}        
    }
    
    public void subscribe() {
		
		// Create a pubsub manager using an existing Connection
        PubSubManager pubSubManager = new PubSubManager(this.connection);

        // Get the node
        LeafNode node;
		try {
			node = pubSubManager.getNode("TestNode0");
			Log.d("XMPPChatActivityDemo", node.toString() + " retrieved succesfully.");
			node.addItemEventListener(new ItemEventCoordinator());
			Log.d("XMPPChatActivityDemo", "ItemEventListener added succesfully.");
			node.subscribe("admin@localhost");
			Log.d("XMPPChatActivityDemo", "Node subscribed succesfully.");
		} catch (XMPPException ex) {
			// TODO Auto-generated catch block
			Log.d("XMPPChatActivityDemo", ex.toString());
		}
    }
    
    class ItemEventCoordinator implements ItemEventListener<Item>
    {
		@Override
		public void handlePublishedItems(ItemPublishEvent items) {
			// TODO Auto-generated method stub
			System.out.println("Item: " + items.getItems().toString());
            System.out.println(items);
			
		}
    } 
}

//package com.demo.xmppchatactivity;
//
//import java.util.Collection;
//import java.util.HashMap;
//
//import org.jivesoftware.smack.AccountManager;
//import org.jivesoftware.smack.AndroidConnectionConfiguration;
//import org.jivesoftware.smack.BOSHConfiguration;
//import org.jivesoftware.smack.BOSHConnection;
//import org.jivesoftware.smack.PacketListener;
//import org.jivesoftware.smack.Roster;
//import org.jivesoftware.smack.RosterEntry;
//import org.jivesoftware.smack.SmackAndroid;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.filter.MessageTypeFilter;
//import org.jivesoftware.smack.filter.PacketFilter;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Packet;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.util.StringUtils;
//
//import android.annotation.SuppressLint;
//import android.app.AlarmManager;
//import android.app.AlertDialog;
//import android.app.Notification;
//import android.app.PendingIntent;
//import android.app.ProgressDialog;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.ServiceInfo;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.ResultReceiver;
//import android.os.SystemClock;
//import android.util.Log;
//import android.widget.Toast;
//
//public class XMPPChatDemoService extends Service {
//
//	public static final String HOST = "162.242.243.182";
//	public static final int PORT = 5222;
//	public static final String SERVICE = "localhost";
//	public static final String USERNAME = "admin";
//	public static final String PASSWORD = "password";
//	public static Context con;
//	public XMPPConnection connection;
//	ResultReceiver resultReceiver;
//	private Intent intent;
//	private Context context;
//	
//	private BroadcastReceiver connectivityReceiver;
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		
//		return null;
//	}
//	
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		// Let it continue running until it is stopped.
//		//resultReceiver = intent.getParcelableExtra("receiver");
//		//Log.d("XMPPChatDemoService", "resultReceiver: " + resultReceiver.toString());
//		this.intent = intent;
//		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
//		Log.i("MyService", "Service Started.");
//		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
//		Boolean isInternetPresent = cd.isConnectingToInternet();
//		if(isInternetPresent){
//			Log.i("Service","connecting()......");
//			connect();
//		} else{
//			Log.i("Service","Internet is Not enabled");
//		}	
//						
//	    return START_STICKY;
//	}
//	
//	public void setConnection(XMPPConnection connection) {
//		this.connection = connection;
//		
//	    if (connection != null) {
//			// Add a packet listener to get messages sent to us
//			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
//			connection.addPacketListener(new PacketListener() {
//				@Override
//				public void processPacket(Packet packet) {
//					Message message = (Message) packet;
//					if (message.getBody() != null) {
//						String fromName = StringUtils.parseBareAddress(message .getFrom());
//						Log.i("MyService", "Text Recieved " + message.getBody() + " from " + fromName );
//						// intent.putExtra("Text Received", message.getBody() + " from " + fromName);
//					}
//				}
//			}, filter);
//		}
//		
//	}
//	
//	public void connect() {
//
//		Thread t = new Thread(new Runnable() {
//			
//
//			@Override
//			public void run() {
//				// Create a connection
//				context = getApplicationContext();
//				SmackAndroid.init(getApplicationContext());
//				Log.d("XMPPChatActivityDemo", "Init successful.");
//				AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration(HOST, PORT, SERVICE);
//				//BOSHConfiguration connConfig = new BOSHConfiguration(true, HOST, 80, "/http-bind/", SERVICE); 
//				//ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
//				Log.i("MyService","Connection : "+connConfig.getPort());
//				//BOSHConnection connection = new BOSHConnection(connConfig);
//				XMPPConnection connection = new XMPPConnection(connConfig);
//				try {
//					connection.connect();
//					Log.i("MyService", "Connected to " + connection.getHost());
//				} catch (XMPPException ex) {
//					Log.e("MyService", "Failed to connect to " + connection.getHost());
//					Log.e("MyService", ex.toString());
//					setConnection(null);
//				}
//				try {
//					// SASLAuthentication.supportSASLMechanism("PLAIN", 0);
//					connection.login(USERNAME, PASSWORD);
//					Log.i("MyService",
//							"Logged in as " + connection.getUser());
//
//					// Set the status to available
//					Presence presence = new Presence(Presence.Type.available);
//					connection.sendPacket(presence);
//					setConnection(connection);
//
//					Roster roster = connection.getRoster();
//					Collection<RosterEntry> entries = roster.getEntries();
//					for (RosterEntry entry : entries) {
//						Log.d("MyService", "--------------------------------------");
//						Log.d("MyService", "RosterEntry " + entry);
//						Log.d("MyService", "User: " + entry.getUser());
//						Log.d("MyService", "Name: " + entry.getName());
//						Log.d("MyService", "Status: " + entry.getStatus());
//						Log.d("MyService", "Type: " + entry.getType());
//						
//						Presence entryPresence = roster.getPresence(entry.getUser());
//
//						Log.d("MyService", "Presence Status: " + entryPresence.getStatus());
//						Log.d("MyService", "Presence Type: " + entryPresence.getType());
//						
//						Presence.Type type = entryPresence.getType();
//						
//						if (type == Presence.Type.available) {
//							Log.d("MyService", "Presence AVIALABLE");
//						}
//						
//						Log.d("MyService", "Presence : " + entryPresence);
//					}
//					
//				} catch (XMPPException ex) {
//					Log.e("MyService", "Failed to log in as " + USERNAME);
//					Log.e("MyService", ex.toString());
//					setConnection(null);
//				}
//			}
//		});
//		
//		t.start();
//	}
//	
//	@SuppressLint("NewApi")
//	@Override
//	public void onTaskRemoved(Intent rootIntent) {
//	    Log.e("FLAGX : ", ServiceInfo.FLAG_STOP_WITH_TASK + "");
//	    Intent restartServiceIntent = new Intent(getApplicationContext(),
//	            this.getClass());
//	    restartServiceIntent.setPackage(getPackageName());
//
//	    PendingIntent restartServicePendingIntent = PendingIntent.getService(
//	            getApplicationContext(), 1, restartServiceIntent,
//	            PendingIntent.FLAG_ONE_SHOT);
//	    AlarmManager alarmService = (AlarmManager) getApplicationContext()
//	            .getSystemService(Context.ALARM_SERVICE);
//	    alarmService.set(AlarmManager.ELAPSED_REALTIME,
//	            SystemClock.elapsedRealtime() + 1000,
//	            restartServicePendingIntent);
//
//	    super.onTaskRemoved(rootIntent);
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		try {
//			if (connection != null){
//				connection.disconnect();
//			}
//		} catch (Exception e) {			
//			Log.d("MyService", "Service Stopped.");
//			sendBroadcast(new Intent("StartKill"));
//		}
//	}
//}

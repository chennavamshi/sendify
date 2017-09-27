package com.sendify.service;

public class SendifyConstant {

	private static String HOST = "";
	private static String SERVICE = "";
	private static int PORT = 5222;
	
	public static String DEVICE_ID;
	
	public static String EMULATOR_DEVICE_ID;
	
	public static String XMPP_USERNAME = "";
	
	public static String XMPP_PASSWORD = "";
	
	public static final String SHARED_PREFERENCE_NAME = "client_preferences";
		
	public static String getHOST() {
		return HOST;
	}
	
	public void setHOST(String hOST) {
		HOST = hOST;
	}
	
	public static String getSERVICE() {
		return SERVICE;
	}
	
	public void setSERVICE(String sERVICE) {
		SERVICE = sERVICE;
	}
	
	public static int getPORT() {
		return PORT;
	}
	
	public void setPORT(int pORT) {
		PORT = pORT;
	}
	
}
package com.finn.bot.core;
/*
JWTItems.java - Class to serialize the items needed to sign JWT token
Created by Lokesh H K, August 21, 2019.
Released into the repository BoT-Java-SDK.
*/

public class JWTItems extends Object{
	private String actionID;
	private String deviceID;
	private String queueID;
	
	public JWTItems(final String actID, final String devID, final String qID){
		this.actionID = actID;
		this.deviceID = devID;
		this.queueID = qID;
	}
	
	public String getActionID(){
		return this.actionID;
	}
	
	public String getDeviceID(){
		return this.deviceID;
	}
	
	public String getQueueID(){
		return this.queueID;
	}
}

package com.finn.bot.store;

/*
ActionInfo.java - Class and Methods to represent actions information between memory and storage
Created by Lokesh H K, August 09, 2019.
Released into the repository BoT-Java-SDK.
*/

public class ActionInfo {
	private String actionId;
	private String frequency;
	private String lastTriggerTime;
	
	public ActionInfo(){}
	
	public ActionInfo(final String id, final String freq, final String ltt){
		actionId = id;
		frequency = freq;
		lastTriggerTime = ltt;
	}
	
	public String getActionId(){
		return actionId;
	}
	
	public void setActionId(final String id){
		actionId = id;
	}
	
	public String getFrequency(){
		return frequency;
	}
	
	public void setFrequency(final String freq){
		frequency = freq;
	}
	
	public String getLastTriggerTime(){
		return lastTriggerTime;
	}
	
	public void setLastTriggerTime(final String ltt){
		lastTriggerTime = ltt;
	}
}

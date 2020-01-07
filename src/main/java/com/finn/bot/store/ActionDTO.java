package com.finn.bot.store;
/*
ActionDTO.java - Class and Methods to represent action in JSON Data to Java Object
Created by Lokesh H K, September 09, 2019.
Released into the repository BoT-Java-SDK.
*/

public class ActionDTO {
	private String makerID;
	private String actionID;
	private String actionName;
	private String type;
	private String info;
	private String frequency;
	private String dateCreated;
	
	public void setMakerID(final String mID){
		makerID = mID;
	}
	
	public String getMakerID(){
		return makerID;
	}
	
	public void setActionID(final String aID){
		actionID = aID;
	}
	
	public String getActionID(){
		return actionID;
	}
	
	public void setActionName(final String aName){
		actionName = aName;
	}
	
	public String getActionName(){
		return actionName;
	}
	
	public void setType(final String aType){
		type = aType;
	}
	
	public String getType(){
		return type;
	}
	
	public void setInfo(final String aInfo){
		info = aInfo;
	}
	
	public String getInfo(){
		return info;
	}
	
	public void setFrequency(final String aFrequency){
		frequency = aFrequency;
	}
	
	public String getFrequency(){
		return frequency;
	}
	
	public void setDateCreated(final String aDate){
		dateCreated = aDate;
	}
	
	public String getDateCreated(){
		return dateCreated;
	}
}

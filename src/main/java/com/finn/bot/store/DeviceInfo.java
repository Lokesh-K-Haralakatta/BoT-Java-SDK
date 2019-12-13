package com.finn.bot.store;

/*
DeviceInfo.java - Acts as wrapper between JSON Serialization/De-serialization for Device Info
Created by Lokesh H K, August 12, 2019.
Released into the repository BoT-Java-SDK.
*/

public class DeviceInfo {
	private String deviceID;
	private String name;
	private String makerID;
	private String publicKey;
	private Integer multipair;
	private String aid;
	
	public void setDeviceId(final String dID){
		this.deviceID = dID;
	}
	
	public void setDeviceName(final String dName){
		this.name = dName;
	}
	
	public void setMakerID(final String mkrId){
		this.makerID = mkrId;
	}
	
	public void setPublicKey(final String pubKey){
		this.publicKey = pubKey;
	}
	
	public void setMultipair(final Integer mpValue){
		this.multipair = mpValue;
	}
	
	public void setAlternateId(final String altId){
		this.aid = altId;
	}
	
	public String getDeviceId(){
		return this.deviceID;
	}
	
	public String getDeviceName(){
		return this.name;
	}
	
	public String getMakerID(){
		return this.makerID;
	}
	
	public String getPublicKey(){
		return this.publicKey;
	}
	
	public Integer getMultipair(){
		return this.multipair;
	}
	
	public String getAlternateId(){
		return this.aid;
	}
	
}


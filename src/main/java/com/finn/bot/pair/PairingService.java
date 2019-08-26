package com.finn.bot.pair;
/*
PairingService.java - Class and Methods to check device pairing status with the help of underlying BoT Service
Created by Lokesh H K, August 26, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.core.BoTService;
import com.finn.bot.store.KeyStore;

public class PairingService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(PairingService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Instance for BoTService
	private static BoTService bot = BoTService.getBoTServiceInstance();
	
	//Instance for PairingService Class as it's designed to follow singleton pattern
	private static PairingService instance = new PairingService();
		
	//Pairing Service related constants
	private final static Integer POLLING_INTERVAL_IN_MILLISECONDS = 10000;
	private final static Integer MAXIMUM_TRIES = 10;
	private final static String BoT_EndPoint = "/pair";
	
	//Make constructor as Private
	private PairingService() {}
	
	//Public method to return reference to single PairingService instance always
	public static PairingService getPairingServiceInstance(){
		return instance;
	}
	
	//Method to check whether the device is multi pair enabled or not
	private Boolean isDeviceMaultipair(){
		return keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR;
	}
	
	//Method to check whether the device is pairable or not
	private Boolean isDevicePairable(){
		if(isDeviceMaultipair()){
			LOGGER.fine("Devicve is MultiPair Enabled");
			return true;
		}
		
		return keyStore.getDeviceState() == KeyStore.DEVICE_NEW;
	}
	
	//Method to check whether the device has pair status as true or not with BoT Service
	private Boolean isDevicePaired(){
		Boolean pairingStatus = false;
		try {
				String response = bot.get(PairingService.BoT_EndPoint);
				pairingStatus = (response != null && response.contains("true"))? true : false;
		}
		catch(Exception e){
			LOGGER.severe("Exception caught duirng retrieving pairing status from BoT Service");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			pairingStatus = false;
		}
		
		return pairingStatus;
	}
	
	//Method to check whether the device can be pair able or not
	//Poll the pairing status max number of tries from BoT Service and
	//Set Device state to paired and pass on the control to activate the device
	public void pairDeviceWithBoTService() throws InterruptedException{
		if(this.isDevicePairable() == false)
			return;
		
		if(this.isDeviceMaultipair())
			return;
		
		int tries = 0;
		Boolean devicePaired = false;
		do {
			tries++;
			LOGGER.info("Polling Device Pair Status - Attempt#" + tries + " ...");
			if(isDevicePaired()) {
				devicePaired = true;
				break;
			}
			Thread.sleep(PairingService.POLLING_INTERVAL_IN_MILLISECONDS);
		}while(tries < PairingService.MAXIMUM_TRIES);
		
		if(devicePaired){
			keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
			LOGGER.info("Device successfully paired, Activating Device ...");
			//Call ActivationService.activateDevice Method
		}
		else {
			LOGGER.warning("Device Not Paired with in given max number tries");
		}
	}
	
}

package com.finn.bot.service;
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
	private static final Logger LOGGER = Logger.getLogger(PairingService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Instance for BoTService
	private static BoTService bot = BoTService.getBoTServiceInstance();
	
	//Instance for PairingService Class as it's designed to follow singleton pattern
	private static PairingService instance = new PairingService();
		
	//Pairing Service related constants
	private static final Integer POLLING_INTERVAL_IN_MILLISECONDS = 10000;
	private static final Integer MAXIMUM_TRIES = 10;
	private static final String ENDPOINT = "/pair";
	
	//Make constructor as Private
	private PairingService() {}
	
	//Public method to return reference to single PairingService instance always
	public static PairingService getPairingServiceInstance(){
		return instance;
	}
	
	//Method to check whether the device has pair status as true or not with BoT Service
	public Boolean isDevicePaired(){
		Boolean pairingStatus = false;
		try {
				String response = bot.get(PairingService.ENDPOINT);
				pairingStatus = (response != null && response.contains("true"));
		}
		catch(Exception e){
			LOGGER.severe("Exception caught duirng retrieving pairing status from BoT Service");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			pairingStatus = false;
		}
		
		return pairingStatus;
	}
	
	//Method to poll pairing status from Back End Service from Max number of tries 
	private Boolean pollPairingStatus() throws InterruptedException{
		int tries = 0;
		do {
			tries++;
			LOGGER.config(String.format("Polling Device Pair Status - Attempt#%d ...", tries));
			if(isDevicePaired()) {
				return true;
			}
			Thread.sleep(PairingService.POLLING_INTERVAL_IN_MILLISECONDS);
		}while(tries < PairingService.MAXIMUM_TRIES);
		LOGGER.config(String.format("Device not paired in max attempts#%d , try again!!!", tries));
		return false;
	}
		
	//Method to check whether the device can be pair able or not
	//Poll the pairing status max number of tries from BoT Service and
	//Set Device state to paired and pass on the control to activate the device
	public synchronized void pairDevice() throws InterruptedException {
		if(keyStore.getDeviceState() != KeyStore.DEVICE_INVALID &&
				keyStore.getDeviceState() >= KeyStore.DEVICE_ACTIVE &&
				this.isDevicePaired()){
			LOGGER.config("Device is already paired and activated OR Device is Multipair");
			return;
		}
		
		if(pollPairingStatus()){
			if(keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR) {
				LOGGER.config("Device is multi-pair enabled Device ...");
			}
			else {
			   keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
			   LOGGER.config("Device successfully paired, Activating Device ...");
			   ActivationService.getActivationServiceInstance().activateDevice();
			}
		}
		else {
			LOGGER.config("Device Not Paired with in given max number tries");
		}
	}
	
}

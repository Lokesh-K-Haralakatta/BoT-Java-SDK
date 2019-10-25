package com.finn.bot.service;
/*
ActivationService.java - Class and Methods to check device activation status with the help of underlying BoT Service
Created by Lokesh H K, September 6, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.core.BoTService;
import com.finn.bot.store.KeyStore;

public class ActivationService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(ActivationService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Instance for BoTService
	private static BoTService bot = BoTService.getBoTServiceInstance();
	
	//Instance for ActivationService Class as it's designed to follow singleton pattern
	private static ActivationService instance = new ActivationService();
	
	//Activation Service related constants
	private final static Integer POLLING_INTERVAL_IN_MILLISECONDS = 10000;
	private final static Integer MAXIMUM_TRIES = 3;
	private final static String BoT_EndPoint = "/status";	
	
	//Make constructor as Private
	private ActivationService(){}
	
	//Public method to return reference to single ActivationService instance always
	public static ActivationService getActivationServiceInstance(){
		return instance;
	}
	
	//Method to make activation request to BoT Module and check device is activated or not
	private Boolean isDeviceActivated(){
		Boolean activationStatus = false;
		try {
				String response = bot.get(ActivationService.BoT_EndPoint);
				activationStatus = (response != null && response.equals(""))? true : false;
		}
		catch(Exception e){
			LOGGER.severe("Exception caught duirng retrieving pairing status from BoT Service");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			activationStatus = false;
		}
		
		return activationStatus;		
	}
	
	//Method to poll for device activation status for max number of tries
	private Boolean pollActivationStatus() throws InterruptedException{
		int tries = 0;
		do {
			tries++;
			LOGGER.config("Polling Device Activation Status - Attempt#" + tries + " ...");
			if(isDeviceActivated()) {
				return true;
			}
			Thread.sleep(ActivationService.POLLING_INTERVAL_IN_MILLISECONDS);
		}while(tries < ActivationService.MAXIMUM_TRIES);
		LOGGER.severe("Device not activated in max attempts# "+tries+" , try again!!!");
		return false;		
	}
	
	//Method to activate the device by polling activation status for max number of tries 
	//Set Device State to Active on successful activation
	public synchronized void activateDevice() throws InterruptedException{
		if(pollActivationStatus()){
			keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
			LOGGER.config("Device is activated for payments...");
		}
		else {
			LOGGER.severe("Device could not be activated for payments, try again!!!");
		}
	}
}

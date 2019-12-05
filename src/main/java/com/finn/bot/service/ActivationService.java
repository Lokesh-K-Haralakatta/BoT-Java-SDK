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
				String response = bot.post(ActivationService.BoT_EndPoint,null);
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
		if(keyStore.getDeviceState() != KeyStore.DEVICE_INVALID &&
			keyStore.getDeviceState() >= KeyStore.DEVICE_ACTIVE &&
			PairingService.getPairingServiceInstance().isDevicePaired()){
			LOGGER.config("Device is already paired and activated OR Device is Multipair");
			return;
		}
		
		if(pollActivationStatus()){
			if(keyStore.getDeviceState() != KeyStore.DEVICE_MULTIPAIR)
				keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
			LOGGER.config("Device is activated for payments, DeviceState set to " +
					             keyStore.getDeviceState(keyStore.getDeviceState()));
		}
		else {
			LOGGER.severe("Device could not be activated for payments, try again!!!");
		}
	}
}

package com.finn.bot.api;
/*
SDKWrapper.java - Class and Methods to provide an api interface for end user to consume
                  Java SDK functionality without Webserver as a library.
Created by Lokesh H K, Oct 14, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Logger;

import com.finn.bot.service.ActionService;
import com.finn.bot.service.BLEService;
import com.finn.bot.service.ConfigurationService;
import com.finn.bot.service.PairingService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.KeyStore;
import com.google.zxing.WriterException;

public class SDKWrapper {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(SDKWrapper.class.getName());
	
	//KeyStore Instance
	private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Configuration Service Instance
	private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//Pairing Service Instance
	private static PairingService pairingService = PairingService.getPairingServiceInstance();
	
	//Activation Service Instance
	private static ActionService actionService = ActionService.getActionServiceInstance();
	
	//BLEService Instance
	private static BLEService bleService = BLEService.getBLEServiceInstance();
	
	//Private constructor to block user instantiation
	private SDKWrapper(){}
	
	//Static Method to retrieve actions from BoT Server
	public static List<ActionDTO> getActions(){
		return actionService.getActions(); 
	}
	
	//Static Method to pair and activate the device
	public static boolean pairAndActivateDevice(final String makerID, final String deviceName, 
		final Boolean generateDeviceID, final Boolean isMultiPair, final String alternateID) throws InterruptedException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException, WriterException{
		Boolean devicePaired = false;
		if(( devicePaired = pairingService.isDevicePaired())){
			LOGGER.info("Device is already paired, proceeding with configuring the device for payments");
			configService.configureDevice();
		}
		else {
			LOGGER.info("Device is not paired yet, proceeding with device initialization and configuration");
			configService.resetDeviceConfiguration(generateDeviceID, deviceName!=null);
			configService.initializeDeviceConfiguration(makerID, deviceName, generateDeviceID, isMultiPair, alternateID);
			configService.configureDevice();
			//Call executeBLENOService method to start bleno-service.js
			bleService.executeBLENOService();
			devicePaired = pairingService.isDevicePaired();
		}
		
		return devicePaired;
	}
	
	//Static Method to verify the given action exists with the server
	private static Boolean actionExistsWithServer(final String actionId){
		List<ActionDTO> actions = actionService.getActions();
		for(ActionDTO action : actions){
			String serverActionId = action.getActionID();
			LOGGER.fine("Server Action ID: " + serverActionId);
			if(serverActionId.compareToIgnoreCase(actionId) == 0){
				LOGGER.fine("Given actionId: " + actionId + " found with actions retrieved from server");
				return true;
			}
		}
		LOGGER.warning("Given actionId: " + actionId + " not found with actions retrieved from server");
		return false;
	}
	
	//Static Method to trigger an action
	public static boolean triggerAction(final String actionId, final Double value){
		if( actionId == null || actionId.length() == 0){
			LOGGER.severe("Action ID cannot be null or empty");
			return false;
		}
		
		if(keyStore.getDeviceState() < KeyStore.DEVICE_ACTIVE){
			LOGGER.severe("Invalid Device state to trigger action");
			return false;
		}
		else if(keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR && 
				( keyStore.getDeviceAltId() == null || keyStore.getDeviceAltId().length() == 0)){
			LOGGER.severe("Multipair Device, Missing alternate device Id");
			return false;
		}
		else if(!actionExistsWithServer(actionId)){
			LOGGER.severe("ActionId: "+ actionId + " does not exists with Server Actions");
			return false;
		}
		else {
			LOGGER.info("Triggering the action with actionID: " + actionId);
			String response = actionService.triggerAction(actionId);
			if(response != null && response.contains("status\":\"OK")){
				LOGGER.info("Action trigger successfull for " + actionId);
				return true;
			}
			else {
				LOGGER.severe("Action trigger failed with response: " + response);
				return false;
			}
			
		}
	}
}

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
	private static final Logger LOGGER = Logger.getLogger(SDKWrapper.class.getName());
	
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
	public static boolean pairAndActivateDevice(final String makerId, final String deviceName, 
		final boolean generateDeviceId, final boolean deviceMultiPair, final String alternateDeviceId) 
		throws InterruptedException, NoSuchProviderException, NoSuchAlgorithmException, 
		InvalidKeySpecException, CertificateException, IOException, WriterException{
		
		Boolean devicePaired = pairingService.isDevicePaired();
		if(keyStore.getDeviceState() != KeyStore.DEVICE_INVALID && devicePaired){
			LOGGER.info("Device is already paired, proceeding with configuring the device for payments");
			configService.configureDevice();
		}
		else {
			LOGGER.info("Device is not paired yet, proceeding with device pairing and configuration");
			configService.initializeDeviceConfiguration(makerId, deviceName, generateDeviceId, deviceMultiPair, alternateDeviceId);
			//Call executeBLENOService method to start bleno-service.js
			bleService.executeBLENOService();
			devicePaired = pairingService.isDevicePaired();
			configService.configureDevice();
		}
		
		return devicePaired;
	}
	
	//Static Method to verify the given action exists with the server
	private static Boolean actionExistsWithServer(final String actionId){
		List<ActionDTO> actions = actionService.getActions();
		String searchResult = String.format("Given actionId: %s not found with actions retrieved from server",actionId);
		for(ActionDTO action : actions){
			String serverActionId = action.getActionID();
			if(serverActionId.compareToIgnoreCase(actionId) == 0){
				searchResult = String.format("Given actionId: %s found with actions retrieved from server",actionId);
				LOGGER.config(searchResult);
				return true;
			}
		}
		LOGGER.warning(searchResult);
		return false;
	}
	
	//Static Method to trigger an action
	public static boolean triggerAction(final String actionId){
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
			LOGGER.severe(String.format("ActionId: %s does not exists with Server Actions", actionId));
			return false;
		}
		else {
			String response = actionService.triggerAction(actionId);
			if(response != null && response.contains("status\":\"OK")){
				LOGGER.info(String.format("Action trigger successfull for %s", actionId));
				return true;
			}
			else {
				LOGGER.severe(String.format("Action trigger failed with response: %s", response));
				return false;
			}
			
		}
	}
	
	//Static Method to call underlying Configuration Service to reset device configuration
	public static void resetDeviceConfiguration(final boolean resetDeviceID, final boolean resetDeviceName){
		configService.resetDeviceConfiguration(resetDeviceID, resetDeviceName);
	}
}

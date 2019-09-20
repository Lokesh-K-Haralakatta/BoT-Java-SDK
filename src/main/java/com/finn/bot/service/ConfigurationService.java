package com.finn.bot.service;
/*
ConfigurationService.java - Class and Methods to initialize and configure the device to enable for autonomous payments
Created by Lokesh H K, September 18, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

import com.finn.bot.store.KeyStore;
import com.google.zxing.WriterException;

public class ConfigurationService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(ConfigurationService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	//PairingService Instance
	private final PairingService pairingService = PairingService.getPairingServiceInstance();
	//ActivationService Instance
	private final ActivationService activationService = ActivationService.getActivationServiceInstance();
	
	//Instance for ConfigurationService Class as it's designed to follow singleton pattern
	private static ConfigurationService instance = new ConfigurationService();	
	
	//Make constructor as Private
	private ConfigurationService() {}	
	
	//Public method to return reference to single ConfigurationService instance always
	public static ConfigurationService getConfigurationServiceInstance(){
		return instance;
	}
	
	//Method to initialize the device with the provided initialization details
	public void initializeDeviceConfiguration(final String makerID, final String deviceName, 
		final boolean generateDeviceID, final boolean isMultiPair, final String alternateID) throws NoSuchProviderException, NoSuchAlgorithmException,
	    InvalidKeySpecException, CertificateException, IOException, WriterException{
		
		LOGGER.info("Initializing the device with makerID: "+makerID);
		keyStore.setMakerId(makerID);
		
		if(!keyStore.isKeyPairGenerated()){
			LOGGER.info("Generating the key-pair for the device");
			keyStore.generateAndStoreKeyPair(true);
		}
		
		//Handle deviceID for the device
		if(generateDeviceID){
			LOGGER.info("Generating the fresh deviceID for the device");
			keyStore.setDeviceId(keyStore.generateUUID4());
		}
		else if(keyStore.getDeviceId() == null || keyStore.getDeviceId() == ""){
			LOGGER.info("DeviceID does not exist in KeyStore, generating the fresh deviceID for the device");
			keyStore.setDeviceId(keyStore.generateUUID4());
		}
		else
			LOGGER.info("Reusing the existing deviceID present in KeyStore for the device: " +keyStore.getDeviceId());
		
		//Handle deviceName for the device
		if(deviceName != null)
			keyStore.setDeviceName(deviceName);
		else
			keyStore.setDeviceName("BoT-Java-SDK-Device");
		LOGGER.info("DeviceName set to: " +keyStore.getDeviceName());
		
		//Handle device pair type and set device state accordingly
		if(isMultiPair){
			keyStore.setDeviceState(KeyStore.DEVICE_MULTIPAIR);
			if(alternateID != null)
				keyStore.setDeviceAltId(alternateID);
			else
				keyStore.setDeviceAltId("altID");
			LOGGER.info("Device enabled for multipair with alternate deviceID: " + keyStore.getDeviceAltId());
		}
		else
			keyStore.setDeviceState(KeyStore.DEVICE_NEW);
		LOGGER.info("Device State set to " + keyStore.getDeviceState(keyStore.getDeviceState()));
		
		//Generate QRCode for the device if needed
		if(!keyStore.isQRCodeGenerated()){
			LOGGER.info("Generating the fresh QRCode for the device");
			keyStore.generateQRCode();
		}
	}
	
	//Method to reset the device configuration
	public void resetDeviceConfiguration(final Boolean resetDeviceID, final Boolean resetDeviceName){
		keyStore.setMakerId("");
		keyStore.setDeviceState(KeyStore.DEVICE_INVALID);
		keyStore.setDeviceAltId("");
		keyStore.clearKeyPair();
		keyStore.clearQrCode();
		
		if(resetDeviceID)
			keyStore.setDeviceId("");
		
		if(resetDeviceName)
			keyStore.setDeviceName("");
	}
	
	//Method to configure the device status based on the present state
	public void configureDevice() throws InterruptedException{
		switch(keyStore.getDeviceState()){
			case KeyStore.DEVICE_NEW: LOGGER.info("Device not paired yet, Initializing pairing...");
									  pairingService.pairDevice();
									  break;
			case KeyStore.DEVICE_PAIRED: LOGGER.info("Device paired but not activated, Initializing activation process...");
										 activationService.activateDevice();
										 break;
			case KeyStore.DEVICE_ACTIVE: LOGGER.info("Device is already active, enbaled for autonomous payments");
										 break;
            default:
            	     LOGGER.warning("Device state is invalid, reset device configuration and try again!!!");
		}
	}
}

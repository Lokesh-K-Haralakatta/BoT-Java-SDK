package com.finn.bot;

/*
Main.java - Testing Java SDK various classes methods as Integration tests
Created by Lokesh H K, August 09, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;

import com.finn.bot.core.BoTService;
import com.finn.bot.store.ActionInfo;
import com.finn.bot.store.KeyStore;

public class Main {
    private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
    private static BoTService botService = BoTService.getBoTServiceInstance();
    
	private static void testActionsStore(){
		KeyStore keyStore = KeyStore.getKeyStoreInstance();
		ActionInfo action = new ActionInfo();
		
		String actionId = keyStore.generateUUID4();
		action.setActionId(actionId);
		action.setFrequency("Minutely");
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		
		action.setActionId(keyStore.generateUUID4());
		action.setFrequency("Hourly");
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		
		action.setActionId(keyStore.generateUUID4());
		action.setFrequency("Daily");
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		System.out.println("Total 3 actions placed into Store");
		
		action = keyStore.getAction(actionId);
		System.out.println("Action ID: " + action.getActionId() + "\nAction Frequency: " + action.getFrequency());
		
		action = keyStore.getAction(null);
		if(action != null)
			System.out.println("Error: action details found for null actionId");
		else
			System.out.println("No action found for actionId value null");
		
		action = keyStore.getAction("actionId-3");
		if(action == null)
			System.out.println("No action details found for non existing actionId value actionId-3");
		else
		    System.out.println("Error: action details found for non existing actionId value actionId-3");
		
		Set<ActionInfo> allActions = keyStore.getAllActions();
		System.out.println("Total actions in Store: " +allActions.size());
		for(ActionInfo actionItem : allActions){
			System.out.println("Action ID: " + actionItem.getActionId() + "\nAction Frequency: " + actionItem.getFrequency());
		}
	}
	
	private static void testKeyPairsFunctionality() throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
		if(keyStore.isKeyPairGenerated()){
			System.out.println("Key Pairs already generated and stored for the device");
		}
		else {
			System.out.println("Key Pairs are not generated for the device, doing it now...");
			keyStore.generateAndStoreKeyPair();
		}
	}
	
	private static void testGetKeys(){
		String key = keyStore.getKey(KeyStore.API_KEY);
		if(key != null){
			System.out.println("API Key Retrieved from Key Store:\n ====================== \n");
			System.out.println(key);
			System.out.println("\n ====================== \n");
		}
		else
			System.out.println("Failed to get API Key Contents from Key Store");
		
		key = keyStore.getKey(KeyStore.PRIVATE_KEY);
		if(key != null){
			System.out.println("Private Key Retrieved from Key Store:\n ====================== \n");
			System.out.println(key);
			System.out.println("\n ====================== \n");
		}
		else
			System.out.println("Failed to get Private Key Contents from Key Store");
		
		key = keyStore.getKey(KeyStore.PUBLIC_KEY);
		if(key != null){
			System.out.println("Public Key Retrieved from Key Store:\n ====================== \n");
			System.out.println(key);
			System.out.println("\n ====================== \n");
		}
		else
			System.out.println("Failed to get Public Key Contents from Key Store");
		
	}
	
	private static void testDeviceInfoStore(){
		
		//Set deviceInfo Items 
		keyStore.setDeviceId(keyStore.generateUUID4());
		keyStore.setMakerId(keyStore.generateUUID4());
		keyStore.setDeviceAltId(keyStore.generateUUID4());
		
		//Get deviceInfo Items
		String deviceID = keyStore.getDeviceId();
		if(deviceID == null)
			System.out.println("DeviceID is NULL");
		else
			System.out.println("DeviceID: " +deviceID);
		
		String makerID = keyStore.getMakerId();
		if(makerID == null)
			System.out.println("MakerID is NULL");
		else
			System.out.println("MakerID: " +makerID);
		
		String deviceAltID = keyStore.getDeviceAltId();
		if(deviceAltID == null)
			System.out.println("DeviceAltID is NULL");
		else
			System.out.println("DeviceAltID: " +deviceAltID);
	
	}
	
	private static void testDeviceState(){
		int state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));
		
		//Set Device State to NEW
		System.out.println("Setting device state to NEW");
		keyStore.setDeviceState(KeyStore.DEVICE_NEW);
		state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));
		
		//Set Device State to PAIRED
		System.out.println("Setting device state to PAIRED");
		keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
		state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));
		
		//Set Device State to ACTIVE
		System.out.println("Setting device state to ACTIVE");
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));	
		
		//Set Device State to MULTIPAIR
		System.out.println("Setting device state to MULTIPAIR");
		keyStore.setDeviceState(KeyStore.DEVICE_MULTIPAIR);
		state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));
		
		//Set Device State to INVALID VALUE
		System.out.println("Setting device state to INVALID VALUE");
		keyStore.setDeviceState(34);
		state = keyStore.getDeviceState();
		System.out.println("Device State: " + state);
		System.out.println("Device State Msg: " + keyStore.getDeviceState(state));		
	}
	
	private static void testDeviceInfoJSON(){
		String deviceInfoJson = keyStore.getDeviceInfo();
		System.out.println("DeviceInfo: " + deviceInfoJson);
	}
	
	private static void testQRCodeGenerationAndRetrieveal(){
		byte[] qrCodeBytes = keyStore.getQRCode();
		System.out.println("Number of bytes in QRCode: " +qrCodeBytes.length);
	}
	
	private static void testBoTHTTPGet(){
		try {
			String response = botService.get("/pair");
			System.out.print("Device Pair Status: " + response);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void testEncodeDecodeJWT() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException{
		String subject = "Finn Banking of Things";
		String encodedToken = botService.encodeJWT(subject);
		System.out.println("Encoded JWT Token: " +encodedToken);
		String decodedText = botService.decodeJWT(encodedToken);
		System.out.print("Decoded Text: " +decodedText);
	}
	
	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
		//testActionsStore();
		//testKeyPairsFunctionality();
		//testGetKeys();
		//testDeviceInfoStore();
		//testDeviceState();
		//testDeviceInfoJSON();
		//testQRCodeGenerationAndRetrieveal();
		testBoTHTTPGet();
		//testEncodeDecodeJWT();
	}

}

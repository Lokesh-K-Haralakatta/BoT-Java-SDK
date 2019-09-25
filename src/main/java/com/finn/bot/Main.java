package com.finn.bot;
/*
Main.java - Testing Java SDK various classes methods as Integration tests
Created by Lokesh H K, August 09, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Set;

import com.finn.bot.core.BoTService;
import com.finn.bot.service.ActionService;
import com.finn.bot.service.ActivationService;
import com.finn.bot.service.BLEService;
import com.finn.bot.service.ConfigurationService;
import com.finn.bot.service.PairingService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.ActionInfo;
import com.finn.bot.store.KeyStore;
import com.google.zxing.WriterException;

public class Main {
    private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
    private static BoTService botService = BoTService.getBoTServiceInstance();
    private static PairingService pairService = PairingService.getPairingServiceInstance();
    private static ActivationService activationService = ActivationService.getActivationServiceInstance();
    private static ActionService actionService = ActionService.getActionServiceInstance();
    private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
    private static BLEService bleService = BLEService.getBLEServiceInstance();
    
	private static void testActionsStore(){
		KeyStore keyStore = KeyStore.getKeyStoreInstance();
		ActionInfo action = new ActionInfo();
		
		String actionId = keyStore.generateUUID4();
		action.setActionId(actionId);
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		
		action.setActionId(keyStore.generateUUID4());
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		
		action.setActionId(keyStore.generateUUID4());
		action.setLastTriggerTime(""+12345678);
		keyStore.storeAction(action);
		System.out.println("Total 3 actions placed into Store");
		
		action = keyStore.getAction(actionId);
		System.out.println("Action ID: " + action.getActionId() + "\nAction ltt: " + action.getLastTriggerTime());
		
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
			System.out.println("Action ID: " + actionItem.getActionId() + "\nAction ltt: " + actionItem.getLastTriggerTime());
		}
	}
	
	private static void testKeyPairsFunctionality() throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
		if(keyStore.isKeyPairGenerated()){
			System.out.println("Key Pairs already generated and stored for the device");
		}
		else {
			System.out.println("Key Pairs are not generated for the device, doing it now...");
			keyStore.generateAndStoreKeyPair(true);
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
	
	/*private static void testEncodeDecodeJWT() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException{
		String subject = "Finn Banking of Things";
		String encodedToken = botService.signToken(subject);
		System.out.println("Encoded JWT Token: " +encodedToken);
		String decodedText = botService.decodeJWT(encodedToken);
		System.out.print("Decoded Text: " +decodedText);
	}*/

	private static void testBoTHTTPGet(){
		//botService.setHTTPS(false);
		try {
			String response = botService.get("/pair");
			System.out.print("Device Pair Status: " + response);
			response = botService.get("/actions");
			System.out.println("Actions from Server: \n"+response+"\n");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
		
	private static void testBoTHttpPost() throws KeyManagementException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		String actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
		//botService.setHTTPS(false);
		System.out.println("Trigger action for " +actionId);
		System.out.println("Response from HTTP Post Execution: " + botService.post("/actions", actionId));
	}
	
	private static void testDevicePairing() throws InterruptedException{
		pairService.pairDevice();
	}
	
	private static void testDeviceActivation() throws InterruptedException{
		activationService.activateDevice();
	}
	
	private static void testGetActions(){
	   List<ActionDTO> actions = actionService.getActions();
	   System.out.println("Number of Actions Retrieved: " + actions.size());
	   for(ActionDTO action:actions){
		   System.out.println(action.getActionID()+":"+action.getActionName()+":"+action.getFrequency());
	   }
	}
	
	private static void testTriggerAction(){
		//Set deviceID to eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc since all actions are added as services
		keyStore.setDeviceId("eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc");
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		
		String actionId = "E6509B49-5048-4151-B965-BB7B2DBC7905";
		System.out.println("Response from postAction for always valid action: " +actionService.triggerAction(actionId));

		actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
		System.out.println("Response from postAction for minutely action: " +actionService.triggerAction(actionId));

		actionId = "749081B8-664D-4A15-908E-1C3F6590930D";
		System.out.println("Response from postAction for hourly action: " +actionService.triggerAction(actionId));

		actionId = "81F6011A-9AF0-45AE-91CD-9A0CDA81FA1F";
		System.out.println("Response from postAction for daily action: " +actionService.triggerAction(actionId));

		actionId = "0BF5E8D2-9062-467E-BB19-88CB76D06F8E";
		System.out.println("Response from postAction for weekly action: " +actionService.triggerAction(actionId));
		
		actionId = "C257DB70-AE57-4409-B94E-678CB1567FA6";
		System.out.println("Response from postAction for monthly action: " +actionService.triggerAction(actionId));
		
		actionId = "D93F99E1-011B-4609-B04E-AEDBA98A7C5F";
		System.out.println("Response from postAction for half-yearly action: " +actionService.triggerAction(actionId));
		
		actionId = "0097430C-FA78-4087-9B78-3AC7FEEF2245";
		System.out.println("Response from postAction for yearly action: " +actionService.triggerAction(actionId));
		
		actionId = "A42ABD19-3226-47AB-8045-8129DBDF117F";
		System.out.println("Response from postAction for invalid action: " +actionService.triggerAction(actionId));
	}
	
	private static void testDeviceConfiguration() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		System.out.println("Device Configuration after reseting existing configuration: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Exists: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Exists: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Initialize device with new deviceID and single pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, false, null);
		System.out.println("Device Configuration after initializing for Single Pair: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Generated: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Generated: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Call configureDevice with device state as NEW
		System.out.println("Configuring the device with state: " +keyStore.getDeviceState(keyStore.getDeviceState()));
		configService.configureDevice();
		
		//Set device state as paired and call configure device
		keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
		System.out.println("Configuring the device with state: " +keyStore.getDeviceState(keyStore.getDeviceState()));
		configService.configureDevice();

		//Set device state as active and call configure device
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		System.out.println("Configuring the device with state: " +keyStore.getDeviceState(keyStore.getDeviceState()));
		configService.configureDevice();
		
		//Reset device configuration without resetting the device ID
		configService.resetDeviceConfiguration(false, false);
		System.out.println("Device Configuration after reseting Single Pair with retianing deviceID and deviceName: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Exists: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Exists: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Initialize device with existing deviceID and multi pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", "MP-Device", true, true, "RPI-Java-MLP");
		System.out.println("Device Configuration after initializing for Multi Pair: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Generated: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Generated: " + (keyStore.isQRCodeGenerated()?"yes":"no"));

		//Call configureDevice with device state as MULTIPAIR
		System.out.println("Configuring the device with state: " +keyStore.getDeviceState(keyStore.getDeviceState()));
		configService.configureDevice();
		
		//Reset device configuration with resetting the device ID
		configService.resetDeviceConfiguration(true,false);
		System.out.println("Device Configuration after reseting Multi Pair with reseting deviceID: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Exists: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Exists: " + (keyStore.isQRCodeGenerated()?"yes":"no"));		
	}
	
	private static void testBlenoService() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		System.out.println("Device Configuration after reseting existing configuration: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Exists: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Exists: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Initialize device with new deviceID and single pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, false, null);
		System.out.println("Device Configuration after initializing for Single Pair: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Generated: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Generated: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}

	private static void testBlenoServiceMultiPair() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		System.out.println("Device Configuration after reseting existing configuration: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Exists: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Exists: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Initialize device with new deviceID and multi pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, true, "RPI-Java-MLP");
		System.out.println("Device Configuration after initializing for Single Pair: ");
		System.out.println("MakerID: "+keyStore.getMakerId());
		System.out.println("DeviceID: "+keyStore.getDeviceId());
		System.out.println("DeviceName: "+keyStore.getDeviceName());
		System.out.println("DeviceState: "+keyStore.getDeviceState(keyStore.getDeviceState()));
		System.out.println("DeviceAlternateID: "+keyStore.getDeviceAltId());
		System.out.println("KeyPair Generated: " + (keyStore.isKeyPairGenerated()?"yes":"no"));
		System.out.println("QrCode Generated: " + (keyStore.isQRCodeGenerated()?"yes":"no"));
		
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}
	
	private static void testBlenoServiceWithPairedDevice() throws InterruptedException{
		//Set Device ID to already paired device id
		keyStore.setDeviceId("eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc");
		
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}
	
	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, 
	                   IOException, InvalidKeySpecException, CertificateException, KeyManagementException, InterruptedException, WriterException {
		
		//Command to execute the main method
		//java -Dbleno.service.path=/home/pi -cp BoT-Java-SDK-0.0.1-SNAPSHOT.jar com.finn.bot.Main
		
		//testActionsStore();
		//testKeyPairsFunctionality();
		//testGetKeys();
		//testDeviceInfoStore();
		//testDeviceState();
		//testDeviceInfoJSON();
		//testQRCodeGenerationAndRetrieveal();
		//testEncodeDecodeJWT();
		//testBoTHTTPGet();
		//testBoTHttpPost();
		//testDevicePairing();
		//testDeviceActivation();
		//testGetActions();
		//testTriggerAction();
		//testDeviceConfiguration();
		//testBlenoService();
		//testBlenoServiceMultiPair();
		testBlenoServiceWithPairedDevice();
	}

}

package com.finn.bot.tests;
/*
IntegrationTests.java - Testing Java SDK various classes methods as Integration tests
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
import java.util.logging.Logger;

import com.finn.bot.api.SDKWrapper;
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

public class IntegrationTests {
	//Class Logger Instance
	private static final Logger LOGGER = Logger.getLogger(IntegrationTests.class.getName());
	//Private constructor
	private IntegrationTests() {}
	//Service Instances needed for tests
    private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
    private static BoTService botService = BoTService.getBoTServiceInstance();
    private static PairingService pairService = PairingService.getPairingServiceInstance();
    private static ActivationService activationService = ActivationService.getActivationServiceInstance();
    private static ActionService actionService = ActionService.getActionServiceInstance();
    private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
    private static BLEService bleService = BLEService.getBLEServiceInstance();
    //Independent test modules
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
		LOGGER.info("Total 3 actions placed into Store");
		action = keyStore.getAction(actionId);
		String actionDetails = String.format("Action ID: %s \t Action ltt: %s" ,  action.getActionId(), action.getLastTriggerTime());
		LOGGER.info(actionDetails);
		action = keyStore.getAction(null);
		
		if(action != null)
			LOGGER.info("Error: action details found for null actionId");
		else
			LOGGER.info("No action found for actionId value null");
		action = keyStore.getAction("actionId-3");
		if(action == null)
			LOGGER.info("No action details found for non existing actionId value actionId-3");
		else
		    LOGGER.info("Error: action details found for non existing actionId value actionId-3");
		
		Set<ActionInfo> allActions = keyStore.getAllActions();
		if(allActions != null) {
			String allActionsInfo = String.format("Total actions in Store: %d" , allActions.size());
			LOGGER.info(allActionsInfo);
			for(ActionInfo actionItem : allActions){
				actionDetails = String.format("Action ID: %s \t Action ltt: %s" ,  actionItem.getActionId(), actionItem.getLastTriggerTime());
				LOGGER.info(actionDetails);
			}
		}
		else
			LOGGER.warning("No actions available in the KeyStore");
	}
	private static void testKeyPairsFunctionality() throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
		if(keyStore.isKeyPairGenerated()){
			LOGGER.info("Key Pairs already generated and stored for the device");
		}
		else {
			LOGGER.warning("Key Pairs are not generated for the device, doing it now...");
			keyStore.generateAndStoreKeyPair(true);
		}
	}
	private static void testGetKeys(){
		String key = keyStore.getKey(KeyStore.API_KEY);
		if(key != null){
			String apiKeyData = String.format("API Key Retrieved from Key Store: %s", key);
			LOGGER.info(apiKeyData);
		}
		else
			LOGGER.warning("Failed to get API Key Contents from Key Store");
		
		key = keyStore.getKey(KeyStore.PRIVATE_KEY);
		if(key != null){
			String privateKeyData = String.format("Private Key Retrieved from Key Store: %s", key);
			LOGGER.info(privateKeyData);
		}
		else
			LOGGER.warning("Failed to get Private Key Contents from Key Store");
		
		key = keyStore.getKey(KeyStore.PUBLIC_KEY);
		if(key != null){
			String publicKeyData = String.format("Public Key Retrieved from Key Store: %s", key);
			LOGGER.info(publicKeyData);
		}
		else
			LOGGER.warning("Failed to get Public Key Contents from Key Store");
		
	}
	private static void testDeviceInfoStore(){
		//Set deviceInfo Items 
		keyStore.setDeviceId(keyStore.generateUUID4());
		keyStore.setMakerId(keyStore.generateUUID4());
		keyStore.setDeviceAltId(keyStore.generateUUID4());
		printDeviceConfigInfo();
	}
	private static void printDeviceStateInfo(final int state) {
		if(state >=0 && state <=4) {
			String deviceStateStr = String.format("Device State: %d" , state);
			LOGGER.info(deviceStateStr);
			String deviceStateMsgStr = String.format("Device State Msg: %s", keyStore.getDeviceState(state));
			LOGGER.info(deviceStateMsgStr);
		}
		else
			LOGGER.warning("Inavlid device state...");
	}
	private static void testDeviceState(){
		int state = keyStore.getDeviceState();
		printDeviceStateInfo(state);
		//Set Device State to NEW
		LOGGER.info("Setting device state to NEW");
		keyStore.setDeviceState(KeyStore.DEVICE_NEW);
		state = keyStore.getDeviceState();
		printDeviceStateInfo(state);
		//Set Device State to PAIRED
		LOGGER.info("Setting device state to PAIRED");
		keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
		state = keyStore.getDeviceState();
		printDeviceStateInfo(state);
		//Set Device State to ACTIVE
		LOGGER.info("Setting device state to ACTIVE");
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		state = keyStore.getDeviceState();
		printDeviceStateInfo(state);	
		//Set Device State to MULTIPAIR
		LOGGER.info("Setting device state to MULTIPAIR");
		keyStore.setDeviceState(KeyStore.DEVICE_MULTIPAIR);
		state = keyStore.getDeviceState();
		printDeviceStateInfo(state);
		//Set Device State to INVALID VALUE
		LOGGER.info("Setting device state to INVALID VALUE");
		keyStore.setDeviceState(34);
		state = keyStore.getDeviceState();
		printDeviceStateInfo(state);		
	}
	private static void testDeviceInfoJSON(){
		String deviceInfoJson = keyStore.getDeviceInfo();
		if(deviceInfoJson != null) {
			String deviceInfoStr = String.format("DeviceInfo: %s", deviceInfoJson);
			LOGGER.info(deviceInfoStr);
		}
		else
			LOGGER.warning("DeviceInfoJason is NULL");
	}
	private static void testQRCodeGenerationAndRetrieveal(){
		byte[] qrCodeBytes = keyStore.getQRCode();
		if(qrCodeBytes != null) {
			String qrcodeBytesLength = String.format("Number of bytes in QRCode: %d", qrCodeBytes.length);
			LOGGER.info(qrcodeBytesLength);
		}
		else
			LOGGER.warning("QRCode bytes are empty");
	}
	private static void testBoTHTTPGet(){
		try {
			String response = botService.get("/pair");
			if(response != null) {
				String pairStatusMsg = String.format("Device Pair Status: %s", response);
				LOGGER.info(pairStatusMsg);
			}
			else
				LOGGER.warning("BotService GET response is NULL for /pair");
			
			response = botService.get("/actions");
			if(response != null) {
				String actionsData = String.format("Actions from Server: %s", response);
				LOGGER.info(actionsData);
			}
			else
				LOGGER.warning("BotService GET response is NULL for /actions");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void testBoTHttpPost() throws KeyManagementException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		String actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
		String response = botService.post("/actions", actionId);
		if(response != null) {
			String postResponseData = String.format("Response from HTTP Post Execution: %s", response);
			LOGGER.info(postResponseData);
		}
		else
			LOGGER.warning("Response from HTTP Post is NULL for /actions");
	}
	private static void testDevicePairing() throws InterruptedException{
		pairService.pairDevice();
	}
	private static void testDeviceActivation() throws InterruptedException{
		activationService.activateDevice();
	}
	private static void testGetActions(){
	   List<ActionDTO> actions = actionService.getActions();
	   if(actions != null) {
		   String actionsCountStr = String.format("Number of Actions Retrieved: %d", actions.size());
		   LOGGER.info(actionsCountStr);
		   for(ActionDTO action:actions){
			   LOGGER.info(String.format("%s:%s:%s",action.getActionID(),action.getActionName(),action.getFrequency()));
		   }
	   }
	   else
		   LOGGER.warning("actionService.getActions retunred NULL");
	}
	private static void testTriggerAction(){
		//Set deviceID to eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc since all actions are added as services
		keyStore.setDeviceId("eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc");
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		
		String actionId = "E6509B49-5048-4151-B965-BB7B2DBC7905";
		LOGGER.info(String.format("Response from postAction for always valid action: %s",
				                                 actionService.triggerAction(actionId)));
	}
	private static void printDeviceConfigInfo() {
		LOGGER.info(String.format("MakerID: %s", keyStore.getMakerId()));
		LOGGER.info(String.format("DeviceID: %s", keyStore.getDeviceId()));
		LOGGER.info(String.format("DeviceName: %s", keyStore.getDeviceName()));
		LOGGER.info(String.format("DeviceState: %s", keyStore.getDeviceState(keyStore.getDeviceState())));
		LOGGER.info(String.format("DeviceAlternateID: %s", keyStore.getDeviceAltId()));
		LOGGER.info(String.format("KeyPair Exists: %s", Boolean.toString(keyStore.isKeyPairGenerated())));
		LOGGER.info(String.format("QrCode Exists: %s", Boolean.toString(keyStore.isQRCodeGenerated())));
	}
	private static void testDeviceConfiguration() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		printDeviceConfigInfo();
		//Initialize device with new deviceID and single pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, false, null);
		printDeviceConfigInfo();
		//Call configureDevice with device state as NEW
		configService.configureDevice();
		//Set device state as paired and call configure device
		keyStore.setDeviceState(KeyStore.DEVICE_PAIRED);
		configService.configureDevice();
		//Set device state as active and call configure device
		keyStore.setDeviceState(KeyStore.DEVICE_ACTIVE);
		configService.configureDevice();
		//Reset device configuration without resetting the device ID
		configService.resetDeviceConfiguration(false, false);
		printDeviceConfigInfo();
		//Initialize device with existing deviceID and multi pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", "MP-Device", true, true, "RPI-Java-MLP");
		printDeviceConfigInfo();
		//Call configureDevice with device state as MULTIPAIR
		configService.configureDevice();
		//Reset device configuration with resetting the device ID
		configService.resetDeviceConfiguration(true,false);
		printDeviceConfigInfo();		
	}
	private static void testBlenoService() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		LOGGER.info("Device Configuration after reseting existing configuration: ");
		printDeviceConfigInfo();
		//Initialize device with new deviceID and single pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, false, null);
		LOGGER.info("Device Configuration after initializing for Single Pair: ");
		printDeviceConfigInfo();
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}
	private static void testBlenoServiceMultiPair() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, WriterException, InterruptedException{
		//Reset existing device configuration
		configService.resetDeviceConfiguration(true,true);
		LOGGER.info("Device Configuration after reseting existing configuration: ");
		printDeviceConfigInfo();
		//Initialize device with new deviceID and multi pair
		configService.initializeDeviceConfiguration("469908A3-8F6C-46AC-84FA-4CF1570E564B", null, true, true, "RPI-Java-MLP");
		LOGGER.info("Device Configuration after initializing for Single Pair: ");
		printDeviceConfigInfo();
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}
	private static void testBlenoServiceWithPairedDevice() throws InterruptedException{
		//Set Device ID to already paired device id
		keyStore.setDeviceId("eb25d0ba-2dcd-4db2-8f96-a4fbe54dbffc");
		//Call executeBLENOService method to start bleno-service.js
		bleService.executeBLENOService();
	}
	private static void testSDKWrapperPairActivate() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InterruptedException, IOException, WriterException{
		//Retrieving actions from server
		LOGGER.info(String.format("Total actions retrieved: %d", SDKWrapper.getActions().size()));
		//Reset device configuration if required
		//SDKWrapper.resetDeviceConfiguration(true, true);
		//Testing device pair and activation for payments
		if(SDKWrapper.pairAndActivateDevice("469908A3-8F6C-46AC-84FA-4CF1570E564B", 
                "RPI-Zero-Java", true, false, null))
			LOGGER.info("SDKWrapper.pairAndActivateDevice Success");
		else
			LOGGER.info("SDKWrapper.pairAndActivateDevice Failed!!!");
	}
	private static void testSDKWrapperPairActivateMultiPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InterruptedException, IOException, WriterException{
		//Reset device configuration if required
		//SDKWrapper.resetDeviceConfiguration(true, true);
		//Testing device pair and activation for payments of Multipair device
		if(SDKWrapper.pairAndActivateDevice("469908A3-8F6C-46AC-84FA-4CF1570E564B", 
                "RPI-Zero-Java-MP", true, true, "Java-MPD"))
			LOGGER.info("SDKWrapper.pairAndActivateDevice Success");
		else
			LOGGER.info("SDKWrapper.pairAndActivateDevice Failed!!!");
	}	
	private static void testSDKWrapperTriggerAction(){
		//Try to call triggerAction with actionId as null or zero length
		SDKWrapper.triggerAction("", 0.0);
		//Try to call triggerAction with device state as new
		int deviceState = keyStore.getDeviceState();
		keyStore.setDeviceState(KeyStore.DEVICE_NEW);
		SDKWrapper.triggerAction("unknownAction", 0.0);
		//Try to call triggerAction with empty alternate device id for multipair device
		keyStore.setDeviceState(KeyStore.DEVICE_MULTIPAIR);
		String altId = keyStore.getDeviceAltId();
		keyStore.setDeviceAltId("");
		SDKWrapper.triggerAction("unknownAction", 0.0);
		//Resotre the device state
		keyStore.setDeviceAltId(altId);
		keyStore.setDeviceState(deviceState);
		//Trigger the unknown action with the original device state
		SDKWrapper.triggerAction("unknownAction", 0.0);
		//Trigger the valid action with the original device state
		String actionId = "E6509B49-5048-4151-B965-BB7B2DBC7905";
		if(SDKWrapper.triggerAction(actionId, 0.0))
			LOGGER.info("Triggering valid action successful");
		else
			LOGGER.info("Triggering valid action failed!!!");
	}
	public static void runTests() throws NoSuchProviderException, NoSuchAlgorithmException, 
	                   IOException, InvalidKeySpecException, CertificateException, KeyManagementException, InterruptedException, WriterException {
		
		//Command to execute the runTests method 
		//java -Dbleno.service.path=/home/pi -jar BoT-Java-SDK.jar tests
		//java -Dbleno.service.path=/home/pi -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar tests
		
		testActionsStore();
		testKeyPairsFunctionality();
		testGetKeys();
		testDeviceInfoStore();
		testDeviceState();
		testDeviceInfoJSON();
		testQRCodeGenerationAndRetrieveal();
		testBoTHTTPGet();
		testBoTHttpPost();
		testDevicePairing();
		testDeviceActivation();
		testGetActions();
		testTriggerAction();
		testDeviceConfiguration();
		testBlenoService();
		testBlenoServiceMultiPair();
		testBlenoServiceWithPairedDevice();
		testSDKWrapperPairActivate();
		testSDKWrapperPairActivateMultiPair();
		testSDKWrapperTriggerAction();
	}
}
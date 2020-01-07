package com.finn.bot.controller;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.finn.bot.service.ActionService;
import com.finn.bot.service.ActivationService;
import com.finn.bot.service.PairingService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.KeyStore;

/*
SDKController.java - Rest Controller with end points for end user to consume and perform required operation
Created by Lokesh H K, October 03, 2019.
Released into the repository BoT-Java-SDK.
*/

@RestController
public class SDKController {
	//Class Logger Instance
	private static final Logger LOGGER = Logger.getLogger(SDKController.class.getName());
			
	private static Boolean actionExistsWithServer(final String actionId){
		List<ActionDTO> actions = ActionService.getActionServiceInstance().getActions();
		String searchResult = String.format("Given actionId: %s not found with actions retrieved from server", actionId);
		for(ActionDTO action : actions){
			String serverActionId = action.getActionID();
			if(serverActionId.compareToIgnoreCase(actionId) == 0){
				searchResult = String.format("Given actionId: %s found with actions retrieved from server", actionId);
				LOGGER.config(searchResult);
				return true;
			}
		}
		LOGGER.severe(searchResult);
		return false;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getSupportedEndPoints() {

      return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("BoT-Java-SDK Webserver: \n Supported End Points: /qrcode \t /actions \t /pairing \t /activate");
    }
    
    @RequestMapping(value = "/qrcode", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCodeBytes() {

      return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(KeyStore.getKeyStoreInstance().getQRCode());
    }
    
    @RequestMapping(value = "/actions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<ActionDTO>> getActions(){
    	return ResponseEntity
    			.ok()
    			.contentType(MediaType.APPLICATION_JSON)
    			.body(ActionService.getActionServiceInstance().getActions());
    			
    }
    
    @RequestMapping(value = "/pairing", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> pairDevice(){
    	if(KeyStore.getKeyStoreInstance().getDeviceState() == KeyStore.DEVICE_INVALID){
    		return ResponseEntity
    				.badRequest()
    				.contentType(MediaType.TEXT_PLAIN)
    				.body("Device is not configured, configure device first!!!");
    	}
    	else if(KeyStore.getKeyStoreInstance().getDeviceState() > KeyStore.DEVICE_NEW &&
    			PairingService.getPairingServiceInstance().isDevicePaired()){
    		return ResponseEntity
    				.badRequest()
    				.contentType(MediaType.TEXT_PLAIN)
    				.body("Device is already paired OR Device is Multipair");
    	}
    	else {
    		try {
    			PairingService.getPairingServiceInstance().pairDevice();
    			if(KeyStore.getKeyStoreInstance().getDeviceState() != KeyStore.DEVICE_NEW){
    				return ResponseEntity
    						.ok()
    						.contentType(MediaType.TEXT_PLAIN)
    						.body("Device pairing successful and activated for payments");
    			}
    			else {
    				return ResponseEntity
    						.badRequest()
    						.contentType(MediaType.TEXT_PLAIN)
    						.body("Unable to pair device");
    			}
    		}
    		catch(Exception e){
    			return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body(ExceptionUtils.getStackTrace(e));
    		}
    	}
    }
    
    @RequestMapping(value = "/activate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> activateDevice(){
    	if(KeyStore.getKeyStoreInstance().getDeviceState() == KeyStore.DEVICE_INVALID){
    		return ResponseEntity
    				.badRequest()
    				.contentType(MediaType.TEXT_PLAIN)
    				.body("Device is not configured, configure device first!!!");
    	}
    	else if(KeyStore.getKeyStoreInstance().getDeviceState() >= KeyStore.DEVICE_ACTIVE
    			&& PairingService.getPairingServiceInstance().isDevicePaired()){
    		return ResponseEntity
    				.badRequest()
    				.contentType(MediaType.TEXT_PLAIN)
    				.body("Device is already activated OR Device is Multipair");
    	}
    	else if(KeyStore.getKeyStoreInstance().getDeviceState() < KeyStore.DEVICE_PAIRED){
    		return ResponseEntity
    				.badRequest()
    				.contentType(MediaType.TEXT_PLAIN)
    				.body("Device is not paired yet, try pairing the device first, then activate");
    	}
    	else {
    		try {
    			ActivationService.getActivationServiceInstance().activateDevice();
    			if(KeyStore.getKeyStoreInstance().getDeviceState() > KeyStore.DEVICE_PAIRED){
    				return ResponseEntity
    						.ok()
    						.contentType(MediaType.TEXT_PLAIN)
    						.body("Device activation for autonomous payments successful");
    			}
    			else {
    				return ResponseEntity
    						.badRequest()
    						.contentType(MediaType.TEXT_PLAIN)
    						.body("Unable to activate device for autonomous payments");
    			}
    		}
    		catch(Exception e){
    			return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body(ExceptionUtils.getStackTrace(e));
    		}
    	}
    }
    
    @RequestMapping(value = "/actions", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> triggerAction(@RequestBody String jsonActionString){
    	
    	String bodyFormat = "{\" actionID \" : \"UUID4-actionId-String\" } ";
		String bodyContents = String.format("Required JSON Body Format: %s", bodyFormat);	
    	try {
    		if(!jsonActionString.contains("actionID"))
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body(bodyContents);
    		
    		String parsedActionId = (jsonActionString.split(":")[1]).split("}")[0].trim();
    		
    		if(parsedActionId == null)
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body(bodyContents);
    		
    		String actionId = parsedActionId.substring(1, parsedActionId.length()-1);
    		LOGGER.config(String.format("Given Action ID: %s", actionId));
    		if(KeyStore.getKeyStoreInstance().getDeviceState() == KeyStore.DEVICE_INVALID
    				|| KeyStore.getKeyStoreInstance().getDeviceState() < KeyStore.DEVICE_ACTIVE)
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("Invalid Device State: "+KeyStore.getKeyStoreInstance().getDeviceState(KeyStore.getKeyStoreInstance().getDeviceState()));
    		else if(KeyStore.getKeyStoreInstance().getDeviceState() == KeyStore.DEVICE_MULTIPAIR &&
    				( KeyStore.getKeyStoreInstance().getDeviceAltId() == null || 
    				  KeyStore.getKeyStoreInstance().getDeviceAltId().length() == 0)){
    			LOGGER.severe("Device is Multipair Enabled and Device Alternate ID is Missing");
    				return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("Multipair Device, Missing Alternate Id for the device");
    		}
    		else if(!actionExistsWithServer(actionId))
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("ActionId: "+ actionId + " does not exists with Server Actions"); 
    		else {
    			LOGGER.config(String.format("Triggering the action with actionID: ", actionId));
    			String response = ActionService.getActionServiceInstance().triggerAction(actionId);
    			if(response != null && response.contains("status\":\"OK"))
    				return ResponseEntity
        					.ok()
        					.contentType(MediaType.TEXT_PLAIN)
        					.body("Action trigger successful for "+actionId);
    			else
    				return ResponseEntity
        					.badRequest()
        					.contentType(MediaType.TEXT_PLAIN)
        					.body("Action trigger failed: " + response);
    		}
    	}
    	catch(Exception e){
    		return ResponseEntity
					.badRequest()
					.contentType(MediaType.TEXT_PLAIN)
					.body(bodyContents); 		
    	}
    	
    }
}

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
	private final static Logger LOGGER = Logger.getLogger(SDKController.class.getName());
			
	private static Boolean actionExistsWithServer(final String actionId){
		List<ActionDTO> actions = ActionService.getActionServiceInstance().getActions();
		for(ActionDTO action : actions){
			String serverActionId = action.getActionID();
			LOGGER.info("Server Action ID: " + serverActionId);
			if(serverActionId.compareToIgnoreCase(actionId) == 0){
				LOGGER.info("Given actionId: " + actionId + " found with actions retrieved from server");
				return true;
			}
		}
		LOGGER.warning("Given actionId: " + actionId + " not found with actions retrieved from server");
		return false;
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
    	if(KeyStore.getKeyStoreInstance().getDeviceState() > KeyStore.DEVICE_NEW){
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
    						.body("Device pairing successful");
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
    
    @RequestMapping(value = "/actions", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> triggerAction(@RequestBody String jsonActionString){
    	
    	String bodyFormat = "{\" actionID \" : \"UUID4-actionId-String\" } ";
			
    	try {
    		if(!jsonActionString.contains("actionID"))
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("Required JSON Body Format: "+bodyFormat);
    		
    		String parsedActionId = (jsonActionString.split(":")[1]).split("}")[0].trim();
    		
    		if(parsedActionId == null)
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("Required JSON Body Format: "+bodyFormat);
    		
    		String actionId = parsedActionId.substring(1, parsedActionId.length()-1);
    		LOGGER.info("Given Action ID: " + actionId);
    		if(KeyStore.getKeyStoreInstance().getDeviceState() < KeyStore.DEVICE_ACTIVE)
        		return ResponseEntity
    					.badRequest()
    					.contentType(MediaType.TEXT_PLAIN)
    					.body("Invalid Device State: "+KeyStore.getKeyStoreInstance().getDeviceState(KeyStore.getKeyStoreInstance().getDeviceState()));
    		else if(KeyStore.getKeyStoreInstance().getDeviceState() == KeyStore.DEVICE_MULTIPAIR &&
    				( KeyStore.getKeyStoreInstance().getDeviceAltId() == null || 
    				  KeyStore.getKeyStoreInstance().getDeviceAltId().length() == 0)){
    			LOGGER.info("Device is Multipair Enabled and Device Alternate ID is Missing");
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
    			LOGGER.info("Triggering the action with actionID: " + actionId);
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
					.body("Required JSON Body Format: "+bodyFormat); 		
    	}
    	
    }
}

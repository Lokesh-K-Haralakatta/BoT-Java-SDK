package com.finn.bot.examples;
/*
SDKWrapperLibSample - Java Sample Application to show case the usage of BoT-Java-SDK as a library for single pair
Created by Lokesh H K, October 21, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.api.SDKWrapper;
import com.finn.bot.store.ActionDTO;

/*
 * The below given steps describes the flow:
 * 		- Reset Device Configuration only if needed
 * 		- Pair and activate the device using BLE with the FINN Mobile Application
 * 		- Retrieve actions from the BoT Server
 * 		- Check and validate the given actionId is present in retrieved actions
 *      - Trigger the given action for every 5 minutes indefinitely till application exited
 */

/*
 * Command to execute the SDKWrapperLibSample run method 
 * 		java -Dbleno.service.path=/home/pi -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample
*/

public class SDKWrapperLibSample {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(SDKWrapperLibSample.class.getName());
	
	//Default SDK Log file path
	private final static String logFile = "/tmp/java-sdk.log.*";
	
	//Static constants
	private final static String makerId = "469908A3-8F6C-46AC-84FA-4CF1570E564B";
	private final static String deviceName = "SDKWrapperLibSample-"+getRandomIntegerBetweenRange(1,100);
	private final static String actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
	private final static Boolean generateDeviceId = true;
	private final static Boolean deviceMultiPair = false;
	private final static String alternateDeviceId = null;
	private final static Integer actionTriggerInterval = 5 * 60 * 1000;
	
	//Payments counters
	private static Integer actionTriggerSucceeded = 0;
	private static Integer actionTriggerFailed = 0;
	
	public static void run() {
		try {
			
			//Reset Device Configuration only if needed otherwise comment below 4 lines of code
			boolean resetDeviceID = true;
			boolean resetDeviceName = true;
			SDKWrapper.resetDeviceConfiguration(resetDeviceID, resetDeviceName);
			LOGGER.info("Device Configuration reset done");
			
			//Pair the device using BLE with the FINN Mobile Application
			LOGGER.info("Pairing and Activating the device through BLE, if not already done");
			if(SDKWrapper.pairAndActivateDevice(makerId, 
	                  deviceName, generateDeviceId, deviceMultiPair, alternateDeviceId)){
				LOGGER.info("Device Successfully paired and activated for autonomous payments");
				LOGGER.info("Triggering the action with actionId: " + actionId);
				List<ActionDTO> actions = SDKWrapper.getActions();
				if(isActionDefinedWithServer(actions,actionId)){
					do {
							if(SDKWrapper.triggerAction(actionId, 0.0))
								actionTriggerSucceeded++;
							else
								actionTriggerFailed++;
						
							LOGGER.info("Action triggers success in this session: " + actionTriggerSucceeded);
							LOGGER.info("Action triggers failed in this session: " + actionTriggerFailed);
							LOGGER.info("Press Ctrl + C to quit the sample");
						
							Thread.sleep(actionTriggerInterval);
					}while(true);
				}
				else
					LOGGER.warning("Given actionId: " + actionId + " not defined with the Server");
			}
			else
				LOGGER.warning("Device Pairing Failed, check the log for details: " + logFile);
		}
		catch(Exception e){
			LOGGER.severe("Exception while running SDKWrapperLibSample: \n" + ExceptionUtils.getStackTrace(e));
		}
	}

	//Method to provide a random number in the given range
	private static int getRandomIntegerBetweenRange(double min, double max){
	    int x = (int) ((Math.random()*((max-min)+1))+min);
	    return x;
	}
	
	//Method to validate the given action is present in the actions defined with the server
	private static boolean isActionDefinedWithServer(final List<ActionDTO> actions, final String actionId){
		for ( ActionDTO action : actions)
			if(action.getActionID().compareToIgnoreCase(actionId) == 0)
				return true;
			
		return false;
	}
}

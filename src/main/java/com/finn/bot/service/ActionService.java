package com.finn.bot.service;
/*
ActionService.java - Class and Methods for end user to interact with actions to/from service
Created by Lokesh H K, September 09, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.List;
import java.util.logging.Logger;
import java.lang.reflect.Type;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.core.BoTService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.KeyStore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActionService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(ActionService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Instance for BoTService
	private static BoTService bot = BoTService.getBoTServiceInstance();
	
	//Instance for ActionService Class as it's designed to follow singleton pattern
	private static ActionService instance = new ActionService();
	
	//Static final variables for ActionService
	private final static String ACTIONS_END_POINT = "/actions";
	
	//Make constructor as Private
	private ActionService(){}
	
	//Public method to return reference to single ActionService instance always
	public static ActionService getActionServiceInstance(){
		return instance;
	}
	
	//Method to retrieve actions from the back-end server and return as array of ActionDTOs
	//If retrieving actions from server is failed then locally stored actions are returned
	public synchronized List<ActionDTO> getActions(){
		List<ActionDTO> actions = null;
		String botResponse = null;
		Type listType = new TypeToken<List<ActionDTO>>() {}.getType();
		try {
			botResponse = bot.get(ACTIONS_END_POINT);
			if(botResponse != null && !botResponse.contains("Failed with StatusCode:")){
				actions = new Gson().fromJson(botResponse, listType);
				LOGGER.info("Total number of actions retrieved from server: " +actions.size());
				keyStore.saveActions(botResponse);
			}
			else {
				LOGGER.warning("Failed to retrieve actions from Server, getting local actions...");
				botResponse = keyStore.getActions();
				if(botResponse != null){
					actions = new Gson().fromJson(botResponse, listType);
					LOGGER.info("Total number of actions retrieved from local store: " +actions.size());
				}
			}
		}
		catch(Exception e){
			LOGGER.severe("Exception Caught while retrieveing actions from server: ");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			LOGGER.warning("Getting local stored actions...");
			botResponse = keyStore.getActions();
			if(botResponse != null){
				actions = new Gson().fromJson(botResponse, listType);
				LOGGER.info("Total number of actions retrieved from local store: " +actions.size());
			}
		}
		return actions;
	}
	
	//Method to initiate action with the back end server and return the response
	public synchronized String postAction(final String actionID){
		if(actionID == null){
			LOGGER.warning("ActionID can not be NULL");
			return null;
		}
		
		try {
			return(bot.post(ACTIONS_END_POINT, actionID));
		}
		catch(Exception e){
			LOGGER.severe("Exception caught during performing postAction using BoT Service Instance");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			return null;
		}	
	}
}

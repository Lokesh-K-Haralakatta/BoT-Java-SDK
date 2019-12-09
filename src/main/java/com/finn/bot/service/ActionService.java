package com.finn.bot.service;
/*
ActionService.java - Class and Methods for end user to interact with actions to/from service
Created by Lokesh H K, September 09, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.List;
import java.util.logging.Logger;
import java.lang.reflect.Type;
import java.time.Instant;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.core.BoTService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.ActionInfo;
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
				LOGGER.config("Total number of actions retrieved from server: " +actions.size());
				keyStore.saveActions(botResponse);
			}
			else {
				LOGGER.warning("Failed to retrieve actions from Server, getting local actions...");
				botResponse = keyStore.getActions();
				if(botResponse != null){
					actions = new Gson().fromJson(botResponse, listType);
					LOGGER.config("Total number of actions retrieved from local store: " +actions.size());
				}
			}
		}
		catch(Exception e){
			LOGGER.severe("Exception Caught while retrieveing actions from server: ");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			LOGGER.config("Getting local stored actions...");
			botResponse = keyStore.getActions();
			if(botResponse != null){
				actions = new Gson().fromJson(botResponse, listType);
				LOGGER.config("Total number of actions retrieved from local store: " +actions.size());
			}
		}
		return actions;
	}
	
	//Method to initiate action with the back end server and return the response
	private synchronized String postAction(final String actionID){
		if(actionID == null){
			LOGGER.severe("ActionID can not be NULL");
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
	
	//Method for end user to trigger an action
	public String triggerAction(final String actionID){
		if(actionID == null){
			LOGGER.severe("ActionID can not be NULL");
			return "ActionID can not be NULL";
		}
		else if(!isActionValid(actionID)){
			LOGGER.severe("Invalid ActionID - " +actionID);
			return "Invalid ActionID - " + actionID + " Not present in retrieved actions OR "
					+ "Action Frequency is not valid";
			
		}
		else {
			Instant triggerTime = Instant.now();
			String triggerResponse = postAction(actionID);
			if(triggerResponse != null && !triggerResponse.contains("Not-OK") && triggerResponse.contains("OK")){
				LOGGER.config("Action trigger successful for " + actionID + " , saving trigger time...");
				ActionInfo action = new ActionInfo(actionID,Long.toString(triggerTime.getEpochSecond()));
				keyStore.storeAction(action);
				LOGGER.config("Trigger Time saved for action - " +actionID+ " : "+triggerTime.toString());
			}
			return triggerResponse;
		}
	}
	
	//Method to validate the provided action 
	private Boolean isActionValid(final String actionID){
		List<ActionDTO> actions = getActions();
		if(isActionPresent(actions,actionID) && isValidActionFrequency(actions,actionID)){
			return true;
		}
		else {
			LOGGER.severe("Given actionID: "+actionID+" - Not present in retrieved actions OR "
					+ "Action Frequency is not valid");
			return false;
		}	
	}
	
	//Method to check and confirm the given action is present in actions list or not
	private Boolean isActionPresent(final List<ActionDTO> actions, final String actionID){
		for(ActionDTO action : actions)
			if(action.getActionID().equalsIgnoreCase(actionID))
				return true;
		
		return false;
	}
	
	//Method to check and confirm the frequency associated with the action is correct
	private Boolean isValidActionFrequency(final List<ActionDTO> actions, final String actionID){
		for(ActionDTO action : actions)
			if(action.getActionID().equalsIgnoreCase(actionID)){
				ActionInfo savedAction = keyStore.getAction(actionID);
				Instant triggerTime = Instant.now();
				Long lastTriggerTimeInSeconds = null;
				if(savedAction != null){
					lastTriggerTimeInSeconds = Long.parseLong(savedAction.getLastTriggerTime());
					long elapsedSeconds = triggerTime.getEpochSecond() - lastTriggerTimeInSeconds;
					LOGGER.config("Action Frequency for " + actionID + " : " + action.getFrequency());
					LOGGER.config("Number of seconds since from last trigger for action - " + actionID + " : "+elapsedSeconds);
					switch(action.getFrequency()){
						case "always" : return true;
						case "minutely": return elapsedSeconds > 60;
						case "hourly": return elapsedSeconds > 60*60;
						case "daily": return elapsedSeconds > 24*60*60;
						case "weekly": return elapsedSeconds > 7*24*60*60;
						case "monthly": return elapsedSeconds > 28*24*60*60;
						case "half_yearly": return elapsedSeconds > 6*28*24*60*60;
						case "yearly": return elapsedSeconds > 12*28*24*60*60;
						default: return false;
					}
				}
				else
					return true;
			}
		return false;
	}	
}

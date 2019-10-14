package com.finn.bot.api;
/*
SDKWrapper.java - Class and Methods to provide an api interface for end user to consume
                  Java SDK functionality without Webserver as a library.
Created by Lokesh H K, Oct 14, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.List;
import java.util.logging.Logger;

import com.finn.bot.service.ActionService;
import com.finn.bot.service.ConfigurationService;
import com.finn.bot.service.PairingService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.KeyStore;

public class SDKWrapper {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(SDKWrapper.class.getName());
	
	//KeyStore Instance
	private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Configuration Service Instance
	private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//Pairing Service Instance
	private static PairingService pairingService = PairingService.getPairingServiceInstance();
	
	//Activation Service Instance
	private static ActionService actionService = ActionService.getActionServiceInstance();
	
	//Private constructor to block user instantiation
	private SDKWrapper(){}
	
	//Static Method to retrieve actions from BoT Server
	public static List<ActionDTO> getActions(){
		return actionService.getActions(); 
	}
	
}

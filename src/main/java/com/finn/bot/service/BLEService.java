package com.finn.bot.service;
/*
BLEService.java - Class and Methods to interact with bleno-service.js using Java Runtime
Created by Lokesh H K, September 24, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.finn.bot.store.KeyStore;

public class BLEService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(BLEService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//PairingService Instance
	private final PairingService pairingService = PairingService.getPairingServiceInstance();
	
	//Instance for BLEService Class as it's designed to follow singleton pattern
	private static BLEService instance = new BLEService();
	
	//Default path to bleno-service.js script
	private static final String BLENO_SERVICE_JS_FILE = "bleno-service.js";
	
	//Make constructor as Private
	private BLEService(){}
	
	//Public method to return reference to single BLEService instance always
	public static BLEService getBLEServiceInstance(){
		return instance;
	}

	//Private class to use as thread instances to dump logs from executing process
	private class PrintBLENOServiceOutput extends Thread {
		//Class Logger Instance
		private final Logger LOGGER = Logger.getLogger(PrintBLENOServiceOutput.class.getName());		
		InputStream is = null;
 
		PrintBLENOServiceOutput(InputStream is, String type) {
			this.is = is;
		}
 
		public void run() {
			String s = null;
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				while ((s = br.readLine()) != null) {
					LOGGER.config(s);
				}
			} catch (IOException e) {
				LOGGER.severe("Exception caught duirng dumping messages from bleno-service.js");
				LOGGER.severe(ExceptionUtils.getStackTrace(e));
			}
		}
	}
	
	//Method to return reference to new thread instance to dump logs
	private PrintBLENOServiceOutput getStreamWrapper(InputStream is, String type) {
		return new PrintBLENOServiceOutput(is, type);
	}
	
	//Method to start BLENO Service as process
	public void executeBLENOService() throws InterruptedException{
		Runtime rt = Runtime.getRuntime();
		PrintBLENOServiceOutput errorMessageThread, outputMessageThread;
		//Extract / prepare the bleno service java script to be executed
		String blenoServicePath = System.getProperty("bleno.service.path", BLENO_SERVICE_JS_FILE);
		if(blenoServicePath != null && !blenoServicePath.contains(BLENO_SERVICE_JS_FILE)){
			blenoServicePath += File.separator + BLEService.BLENO_SERVICE_JS_FILE;
		}
		
		//Check the existence of the bleno service file at prepared path
		if(!Files.exists(Paths.get(blenoServicePath))){
			LOGGER.severe("Invalid BLENO SERVICE Path: " + blenoServicePath);
			LOGGER.severe("Quitting Now... Please check and run again");
			System.exit(-1);
		}
		
		//Build the required arguments for bleno service
		String blenoArgs = new String(keyStore.getMakerId() + " " + keyStore.getDeviceId() + " " + keyStore.getDeviceName() 
		                                         + " " + keyStore.getKey(KeyStore.PUBLIC_KEY) + " ");
		if(keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR){
			blenoArgs += "1" + " " + keyStore.getDeviceAltId();
		}
		
		String blenoCmdString = "node" + " " + blenoServicePath + " " + blenoArgs;
		LOGGER.config("BLENO Service Command String: " + blenoCmdString);
		try {
			Process proc = rt.exec(blenoCmdString);
			errorMessageThread = getStreamWrapper(proc.getErrorStream(), "ERROR");
			outputMessageThread = getStreamWrapper(proc.getInputStream(), "OUTPUT");
			errorMessageThread.start();
			outputMessageThread.start();
			do {
				Thread.sleep(5000);
				if(pairingService.isDevicePaired()){
					LOGGER.config("Device paired, stopping bleno-service.js");
					proc.destroyForcibly();
					break;
				}
				else {
					LOGGER.info("Waiting for device to be paired through BLE Service");
				}
			}while(proc.isAlive());
			LOGGER.config(blenoServicePath + " Execution Completed");
		} catch (IOException e) {
			LOGGER.severe("Exception caught duirng execution of bleno-service.js");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}
}

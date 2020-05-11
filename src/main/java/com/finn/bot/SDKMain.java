package com.finn.bot;
/*
SDKMain.java - Java SDK Starting Point to bootstrap the Webserver end points for end user to consume the provided end points
and also to execute the built-in java samples to use Java-SDK as library as well as through embed webserver end points.
Created by Lokesh H K, October 03, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.finn.bot.api.SDKWrapper;
import com.finn.bot.examples.SDKWebServerMultiPairSample;
import com.finn.bot.examples.SDKWebServerSample;
import com.finn.bot.examples.SDKWrapperLibMultiPairSample;
import com.finn.bot.examples.SDKWrapperLibSample;
import com.finn.bot.service.ConfigurationService;
import com.finn.bot.store.KeyStore;
import com.finn.bot.tests.IntegrationTests;
import com.google.zxing.WriterException;

/*
 * This file is the starting point for BoT-Java-SDK.
 * 
 * BoT-Java-SDK can be used in dual modes - Webserver Mode and Library Mode
 * 
 * To use BoT-Java-SDK in Webserver Mode,execute the below command:
 * 				java -Dbleno.service.path=/home/pi -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar server
 * 
 * To use BoT-Java-SDK as library for single pair device, execute the below command:
 * 				java -Dbleno.service.path=/home/pi -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libSample
 * 
 * To use BoT-Java-SDK as library for multi pair device, execute the below command:
 * 				java -Dbleno.service.path=/home/pi -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar libMultiPairSample
 * 
 * To consume embed WebServer EndPoints of BoT-Java-SDK for single pair device, execute the below command:
 * 				java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverSample
 * 
 * * To consume embed WebServer EndPoints of BoT-Java-SDK for multi pair device, execute the below command:
 * 				java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverMultiPairSample
 */

@SpringBootApplication
public class SDKMain {
	//Class Logger Instance
	private static final Logger LOGGER = Logger.getLogger(SDKMain.class.getName());
	//Required Java SDK Class Instances
	private static final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	private static final ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//Method to print usage format for Java SDK Main
	private static void printUsage() {
		LOGGER.info("Default Usage: java -jar BoT-Java-SDK-0.0.1-SNAPSHOT.jar [ config | server | tests | libSample | libMultiPairSample | serverSample | serverMultiPairSample ]");
		LOGGER.info("Usage with JVM properties: java -Dbleno.service.path=bleo-service-path "
				+ " -Djava.util.logging.config.file=logging-properties-file "
				+ " -jar BoT-Java-SDK-0.0.1-SNAPSHOT.jar [server | tests | libSample | serverSample]");		
	}
	
	//Method to provide a random number in the given range
	private static int getRandomIntegerBetweenRange(double min, double max){
	    int x = (int) ((Math.random()*((max-min)+1))+min);
	    return x;
	}
	
	//Method to return supported end points as single string message
	private static String getEndpointsString(final String [] endPoints) {
		StringBuilder epString = new StringBuilder();
		for ( String ep : endPoints)
			epString.append(ep);
		return epString.toString();
	}
	
	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException, WriterException {
		final String [] endPoints = { "	/qrcode ", " /actions ", " /pairing ", " /activate " };
		final String falseStr = "false";
		String makerId = null;
		String deviceName = "BoT-Device-"+getRandomIntegerBetweenRange(1,100);
		Boolean generateDeviceId = true;
		Boolean multiPair = false;
		Boolean resetId = false;
		Boolean resetName = false;
		String altId = "";
		
		if(args.length == 1)
			switch (args[0]){
				case "config" : LOGGER.info("Configuring the Device with the given details: ");
								makerId = System.getProperty("maker.id");
								deviceName = System.getProperty("device.name", deviceName);
								generateDeviceId = Boolean.valueOf(System.getProperty("generate.id", "true"));
								multiPair = Boolean.valueOf(System.getProperty("multi.pair", falseStr));
								altId = System.getProperty("alternate.id", "");
								String configStr = String.format("MakerId: %s DeviceName: %s Generate ID: %s  Multipair: %s AlternateId: %s",
										                                              makerId, deviceName, generateDeviceId, multiPair,altId);
								LOGGER.info(configStr);
								try {
									if(SDKWrapper.pairAndActivateDevice(makerId, 
							                  deviceName, generateDeviceId, multiPair, altId)){
										LOGGER.info("Device Successfully paired and activated for autonomous payments");
									}
									else {
										LOGGER.severe("Device pairing and activation failed, check log for details and try again");
									}
									System.exit(0);
								}
								catch(Exception e) {
									LOGGER.severe("Exception caught during initializing and configuring the device");
									LOGGER.severe(ExceptionUtils.getStackTrace(e));
									System.exit(1);
								}
								break;
				 
				case "reset" : LOGGER.info("Reseting the device configuration with the given details: ");
							   resetId = Boolean.valueOf(System.getProperty("reset.id", falseStr));
							   resetName = Boolean.valueOf(System.getProperty("reset.name", falseStr));
							   String resetStr = String.format("Reset Device ID: %s Reset Device Name: %s", resetId,resetName);
							   LOGGER.info(resetStr);
							   SDKWrapper.resetDeviceConfiguration(resetId, resetName);
							   LOGGER.info("Device Configuration Reset Done");
							   System.exit(0);
							   break;
							   
				case "server": if(keyStore.getDeviceState() == KeyStore.DEVICE_INVALID) {
									makerId = System.getProperty("maker.id");
									if(makerId != null) {
										LOGGER.info("Configuring the Device with the given details: ");
										deviceName = System.getProperty("device.name", deviceName);
										generateDeviceId = Boolean.valueOf(System.getProperty("generate.id", "true"));
										multiPair = Boolean.valueOf(System.getProperty("multi.pair", falseStr));
										altId = System.getProperty("alternate.id", "");
										configStr = String.format("MakerId: %s DeviceName: %s Generate ID: %s  Multipair: %s AlternateId: %s",
												                                              makerId, deviceName, generateDeviceId, multiPair,altId);
										LOGGER.info(configStr);
										configService.initializeDeviceConfiguration(makerId, deviceName, generateDeviceId, multiPair, altId);
									} else {
										LOGGER.info("Missing some of required SDK specific values, rerun as per below given usage");
										LOGGER.info("Usage: java -Dmaker.id=makerID/productID -Ddevice.name=DeviceName [ -Dgenerate.id=true/false \\\r\n" + 
												"       -Dmulti.pair=true/false -Dalternate.id=AlternateDeviceId ]  -jar BoT-Java-SDK.jar server");
										System.exit(1);
									}
								}
								
								LOGGER.info("Starting BoT-Java-SDK-Webserver with the end points: " + getEndpointsString(endPoints));
								SpringApplication.run(SDKMain.class, args);
			 
								break;
				case "tests": try {
								IntegrationTests.runTests();
								System.exit(0);
						  	  }
						  	  catch(Exception e){
						  		LOGGER.severe("Exception while running Integration Tests: \n" + ExceptionUtils.getStackTrace(e));
							  	System.exit(1);
						  	  }
						  	  break;
				case "libSample": SDKWrapperLibSample.run(); break;
				case "libMultiPairSample" : SDKWrapperLibMultiPairSample.run(); break;
				case "serverSample": SDKWebServerSample.run(); break;
				case "serverMultiPairSample": SDKWebServerMultiPairSample.run(); break;
				default: printUsage(); System.exit(1);
			}
		else {
			printUsage();
			System.exit(1);
		}
	}
}

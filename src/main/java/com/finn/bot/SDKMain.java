package com.finn.bot;
/*
SDKMain.java - Java SDK Starting Point to bootstrap the Webserver end points for end user to consume
Created by Lokesh H K, October 03, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.finn.bot.examples.SDKWrapperLibMultiPairSample;
import com.finn.bot.examples.SDKWrapperLibSample;
import com.finn.bot.tests.IntegrationTests;

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
 */
@SpringBootApplication
public class SDKMain {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(SDKMain.class.getName());
	
	public static void main(String[] args) {
		if(args.length == 1)
			switch (args[0]){
				case "server": LOGGER.info("Starting BoT-Java-SDK-Webserver with the end points: "
											+ "	/qrcode   /actions   /pairing ");
				               SpringApplication.run(SDKMain.class, args); 
				               break;
				case "tests": try {
								IntegrationTests.runTests();
						  	  }
						  	  catch(Exception e){
							  	e.printStackTrace();
						  	  }
						  	  break;
				case "libSample": SDKWrapperLibSample.run(); break;
				case "libMultiPairSample" : SDKWrapperLibMultiPairSample.run(); break;
				case "serverSample": System.out.println("Given Option is to run Server Sample"); break;
			}
		else {
			System.out.println("Default Usage: java -jar BoT-Java-SDK-0.0.1-SNAPSHOT.jar [server | tests | libSample | serverSample]");
			System.out.println("Usage with JVM properties: java -Dbleno.service.path=bleo-service-path "
					+ " -Djava.util.logging.config.file=logging-properties-file "
					+ " -jar BoT-Java-SDK-0.0.1-SNAPSHOT.jar [server | tests | libSample | serverSample]");
		}
	}
}

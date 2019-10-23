package com.finn.bot;
/*
SDKMain.java - Java SDK Starting Point to bootstrap the Webserver end points for end user to consume
Created by Lokesh H K, October 03, 2019.
Released into the repository BoT-Java-SDK.
*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.finn.bot.examples.SDKWrapperLibMultiPairSample;
import com.finn.bot.examples.SDKWrapperLibSample;
import com.finn.bot.tests.IntegrationTests;

@SpringBootApplication
public class SDKMain {
	public static void main(String[] args) {
		if(args.length == 1)
			switch (args[0]){
				case "server": SpringApplication.run(SDKMain.class, args); break;
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

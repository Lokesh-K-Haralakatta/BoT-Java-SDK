package com.finn.bot;
/*
SDKMain.java - Java SDK Starting Point to bootstrap the Webserver end points for end user to consume
Created by Lokesh H K, October 03, 2019.
Released into the repository BoT-Java-SDK.
*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SDKMain {
	public static void main(String[] args) {
		SpringApplication.run(SDKMain.class, args);
	}
}

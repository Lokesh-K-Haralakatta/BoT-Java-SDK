package com.finn.bot.examples;
/*
SDKWebServerMultiPairSample - Java Sample Application to show case the consumption of embed WebServer end points for multi pair device
Created by Lokesh H K, November 13, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.finn.bot.service.ConfigurationService;
import com.finn.bot.store.ActionDTO;
import com.finn.bot.store.KeyStore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.WriterException;

/*
 * The below given steps describes the flow:
 * 		- Reset Device Configuration only if needed
 * 		- Pair and activate the device through QRCode by accessing /qrcode endpoint with the FINN Mobile Application
 * 		- Retrieve actions from the BoT Server using /actions end point
 *      - Trigger the given action for every 5 minutes indefinitely till application exited by posting to /actions end point
 */

/*
 * Command to execute the SDKWebServerMultiPairSample run method 
 * 		java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverMultiPairSample
*/

public class SDKWebServerMultiPairSample {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(SDKWebServerMultiPairSample.class.getName());
	
	//Default SDK Log file path
	private final static String logFile = "/tmp/java-sdk.log.*";
	
	//Static constants
	private final static String makerId = "469908A3-8F6C-46AC-84FA-4CF1570E564B";
	private final static String deviceName = "SDKWebServerMultiPairSample-1";
	private final static String actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
	private final static Boolean generateDeviceId = true;
	private final static Boolean deviceMultiPair = true;
	private final static String alternateDeviceId = "SDKWebServerMP-1";
	private final static Integer actionTriggerInterval = 5 * 60 * 1000;
	private final static String actionsEndpoint = "/actions";
	private final static String pairingEndpoint = "/pairing";
	private final static String qrcodeEndpoint = "/qrcode";
	
	//Base URL for Embed WebServer
	private static String baseUrl = "http://10.26.16.25:3001";
	
	//Payments counters
	private static Integer actionTriggerSucceeded = 0;
	private static Integer actionTriggerFailed = 0;	
	
	//Configuration Service Instance
	private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//KeyStore Instance
	private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Method to retrieve defined actions from the Server through embed WebServer EndPoint /actions
	private static List<ActionDTO> getActions() throws IOException{
		String responseBody = null;
		String url = baseUrl+actionsEndpoint;
		CloseableHttpClient httpclient = HttpClients.createDefault();;
		Type listType = new TypeToken<List<ActionDTO>>() {}.getType();
		List<ActionDTO> actions = null;
		
		try {
			//Instantiate HTTP Get
            HttpGet httpget = new HttpGet(url);
            
            //Execute HTTP GET
            LOGGER.config("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	LOGGER.config("GET Response Body Contents: \n" +responseBody + "\n");
            	actions = new Gson().fromJson(responseBody, listType);
            }
		}
		catch(Exception e){
			LOGGER.severe("Exception caught during performing GET Call with URL: " + url);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		
		return actions;
	}

	//Method to wait for device pairing status from the Server through embed WebServer EndPoint /pairing
	private static String getPairingStatus() throws IOException{
		String responseBody = null;
		String url = baseUrl+pairingEndpoint;
		CloseableHttpClient httpclient = HttpClients.createDefault();;

		try {
			//Instantiate HTTP Get
            HttpGet httpget = new HttpGet(url);
            
            //Execute HTTP GET
            LOGGER.config("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	LOGGER.config("GET Response Body Contents: \n" +responseBody + "\n");
            }
		}
		catch(Exception e){
			LOGGER.severe("Exception caught during performing GET Call with URL: " + url);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		
		return responseBody;
	}
	
	//Static Method to pair and activate the device
	private static boolean pairAndActivateDevice(final String makerId, final String deviceName, 
		final boolean generateDeviceId, final boolean deviceMultiPair, final String alternateDeviceId) 
		throws InterruptedException, NoSuchProviderException, NoSuchAlgorithmException, 
		InvalidKeySpecException, CertificateException, IOException, WriterException{
		
		String url = baseUrl+qrcodeEndpoint;
		Boolean devicePaired = false;
		if(keyStore.getDeviceState() != KeyStore.DEVICE_INVALID && keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR){
			LOGGER.info("Device is Multipair device");
			devicePaired = true;
		}
		else {
			LOGGER.info("Device is not paired yet, proceeding with device initialization, pairing and configuration");
			configService.initializeDeviceConfiguration(makerId, deviceName, generateDeviceId, deviceMultiPair, alternateDeviceId);
			LOGGER.info("Access QrCode for the device from URL: " + url + " and Pair using FINN Mobile Application");
			String pairingResponse = getPairingStatus();
			devicePaired = pairingResponse.contains("Device pairing successful") || pairingResponse.contains("Device is Multipair");
		}
		
		return devicePaired;
	}
	
	
	//Static Method to trigger action using /actions end point
	private static Boolean triggerAction(final String actionString) throws IOException{
		String responseBody = null;
		String url = baseUrl+actionsEndpoint;
		CloseableHttpClient httpclient = HttpClients.createDefault();;
		Boolean triggerResult = false;
		try {
			//Instantiate HTTP Post
            HttpPost httpPost = new HttpPost(url);
            
            //Prepare Post Body
            StringEntity entity = new StringEntity(actionString);
			httpPost.setEntity(entity);
			
			//Add required HTTP headers
            httpPost.addHeader("Content-Type", "application/json");
            
            //Execute HTTP Post
            LOGGER.config("Executing request " + httpPost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpPost);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	LOGGER.config("Post Response Body Contents: \n" +responseBody + "\n");
            	triggerResult = response.getStatusLine().getStatusCode() == 200 ? true : false;
            }
		}
		catch(Exception e){
			LOGGER.severe("Exception caught during performing GET Call with URL: " + url);
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		
		return triggerResult;
	}
	public static void run() {
		LOGGER.info("Running SDKWebServerMultipairSample...");
		try {
			
			//Reset Device Configuration only if needed otherwise comment below 4 lines of code
			boolean resetDeviceID = true;
			boolean resetDeviceName = true;
			configService.resetDeviceConfiguration(resetDeviceID, resetDeviceName);
			LOGGER.info("Device Configuration reset done");
			
			if(pairAndActivateDevice(makerId, deviceName, generateDeviceId, deviceMultiPair, alternateDeviceId)) {
				LOGGER.info("Device pair and activation successful, proceeding with payments...");
				LOGGER.info("Number of actions retrieved from Server: " + getActions().size());
				String actionStr = "{\" actionID \" : \"" + actionId + "\" } ";
				do {
					if(triggerAction(actionStr))
						actionTriggerSucceeded++;
					else
						actionTriggerFailed++;
					
					LOGGER.info("Action triggers success in this session: " + actionTriggerSucceeded);
					LOGGER.info("Action triggers failed in this session: " + actionTriggerFailed);
					LOGGER.info("Press Ctrl + C to quit the sample");
				
					Thread.sleep(actionTriggerInterval);

				}while(true);
			}
			else {
				LOGGER.warning("Device Pairing Failed, check the log for details: " + logFile);
			}
			
		}
		catch(Exception e) {
			LOGGER.severe("Exception caught while try to consume embed WebServer end points");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}
			
}

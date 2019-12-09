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
 * Note: Make sure to update the Webserver ipAddress before building the sample
 * 
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
	private static final Logger LOGGER = Logger.getLogger(SDKWebServerMultiPairSample.class.getName());
	
	//Default SDK Log file path
	private static final String logFile = "/tmp/java-sdk.log.*";
	
	//Static constants
	private static final String makerId = "469908A3-8F6C-46AC-84FA-4CF1570E564B";
	private static final String deviceName = "SDKWebServerMultiPairSample-1";
	private static final String actionId = "A42ABD19-3226-47AB-8045-8129DBDF117E";
	private static final Boolean generateDeviceId = true;
	private static final Boolean deviceMultiPair = true;
	private static final String alternateDeviceId = "SDKWebServerMP-1";
	private static final Integer actionTriggerInterval = 5 * 60 * 1000;
	private static final String actionsEndpoint = "/actions";
	private static final String pairingEndpoint = "/pairing";
	private static final String qrcodeEndpoint = "/qrcode";
	
	//Base URL for Embed WebServer
	private static final String ipAddress = "";
	private static final int port = 3001;
	private static String baseUrl = "http://" + ipAddress + ":" + port;
	
	//Payments counters
	private static Integer actionTriggerSucceeded = 0;
	private static Integer actionTriggerFailed = 0;	
	
	//Configuration Service Instance
	private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//KeyStore Instance
	private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Adding private constructor to avoid creation of instance
	private SDKWebServerMultiPairSample() {}
	
	//Method to put get request to the Server through embed WebServer EndPoint /pairing
	private static String getRequest(final String endPoint) throws IOException{
		String responseBody = null;
		String url = baseUrl+endPoint;
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			//Instantiate HTTP Get
            HttpGet httpget = new HttpGet(url);
            
            //Execute HTTP GET
            String requestStr = String.format("Executing request %s" , httpget.getRequestLine());
            LOGGER.config(requestStr);
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	String resBodyContents = String.format("GET Response Body Contents: \n %s \n", responseBody);
            	LOGGER.config(resBodyContents);
            }
		}
		catch(Exception e){
			String exceptionMsg = String.format("Exception caught during performing GET Call with URL: %s " , url);
			LOGGER.severe(exceptionMsg);
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
			String pairMsg = String.format("Access QrCode for the device from URL: %s and " + 
			                                        "Pair using FINN Mobile Application", url);
			LOGGER.info(pairMsg);
			String pairingResponse = getRequest(pairingEndpoint);
			devicePaired = pairingResponse != null && ( pairingResponse.contains("Device pairing successful")
					                                    || pairingResponse.contains("Device is Multipair"));
		}
		
		return devicePaired;
	}
	
	
	//Static Method to trigger action using /actions end point
	private static Boolean triggerAction(final String actionString) throws IOException{
		String responseBody = null;
		String url = baseUrl+actionsEndpoint;
		CloseableHttpClient httpclient = HttpClients.createDefault();
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
            String requestStr = String.format("Executing request %s" , httpPost.getRequestLine());
            LOGGER.config(requestStr);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	String resBodyContents = String.format("Post Response Body Contents: \n %s \n", responseBody);
            	LOGGER.config(resBodyContents);
            	triggerResult = response.getStatusLine().getStatusCode() == 200 ? true : false;
            }
		}
		catch(Exception e){
			String exceptionMsg = String.format("Exception caught during performing POST Call with URL: %s " , url);
			LOGGER.severe(exceptionMsg);
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
				Type listType = new TypeToken<List<ActionDTO>>() {}.getType();
				List<ActionDTO> actions = new Gson().fromJson(getRequest(actionsEndpoint), listType);
				String actionsCount = String.format("Number of actions retrieved from Server: %s" , actions.size());
				LOGGER.info(actionsCount);
				String actionStr = "{\" actionID \" : \"" + actionId + "\" } ";
				do {
					if(triggerAction(actionStr))
						actionTriggerSucceeded++;
					else
						actionTriggerFailed++;
					
					String successActions = String.format("Action triggers success in this session: %s" , actionTriggerSucceeded);
					String failedActions = String.format("Action triggers failed in this session: %s" , actionTriggerFailed);
					LOGGER.info(successActions);
					LOGGER.info(failedActions);
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

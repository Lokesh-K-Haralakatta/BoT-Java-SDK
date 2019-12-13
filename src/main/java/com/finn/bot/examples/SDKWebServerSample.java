package com.finn.bot.examples;
/*
SDKWebServerSample - Java Sample Application to show case the consumption of embed WebServer end points for single pair
Created by Lokesh H K, November 12, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Random;
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
 * Command to execute the SDKWebServerSample run method 
 * 		java -Djava.util.logging.config.file=logging.properties -jar BoT-Java-SDK.jar serverSample
*/
public class SDKWebServerSample {
	//Class Logger Instance
	private static final Logger LOGGER = Logger.getLogger(SDKWebServerSample.class.getName());
	
	//Default SDK Log file path
	private static final String LOG_FILE = "/tmp/java-sdk.log.*";
	
	//Static constants
	private static final String MAKER_ID = "469908A3-8F6C-46AC-84FA-4CF1570E564B";
	private static final String DEVICE_NAME = "SDKWebServerSample-"+getRandomInteger();
	private static final String ACTION_ID = "A42ABD19-3226-47AB-8045-8129DBDF117E";
	private static final Boolean GEN_DEVICE_ID = true;
	private static final Boolean DEVICE_MP = false;
	private static final String ALT_DEVICE_ID = null;
	private static final Integer TRIGGER_INTERVAL = 5 * 60 * 1000;
	
	//Webserver endpoints to consume
	private static final String ACTIONS_ENDPOINT = "/actions";
	private static final String PAIRING_ENDPOINT = "/pairing";
	private static final String QRCODE_ENDPOINT = "/qrcode";
	
	//Base URL for Embed WebServer
	private static final String IP_ADDRESS = "";
	private static final int PORT_NUMBER = 3001;
	private static String baseUrl = "http://" + IP_ADDRESS + ":" + PORT_NUMBER;
	
	//Payments counters
	private static Integer actionTriggerSucceeded = 0;
	private static Integer actionTriggerFailed = 0;
	
	//Configuration Service Instance
	private static ConfigurationService configService = ConfigurationService.getConfigurationServiceInstance();
	
	//KeyStore Instance
	private static KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Adding private constructor to avoid creation of instance
    private SDKWebServerSample() {}
		
	//Method to place get request to the Server through embed WebServer EndPoint
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
            	String resBodyContents = String.format("GET Response Body Contents: %s", responseBody);
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

	//Method to provide a random integer
	private static int getRandomInteger(){
		return new Random().nextInt();
	}
	
	//Static Method to pair and activate the device
	private static boolean pairAndActivateDevice(final String makerId, final String deviceName, 
		final boolean generateDeviceId, final boolean deviceMultiPair, final String alternateDeviceId) 
		throws InterruptedException, NoSuchProviderException, NoSuchAlgorithmException, 
		InvalidKeySpecException, CertificateException, IOException, WriterException{
		
		String url = baseUrl+QRCODE_ENDPOINT;
		Boolean devicePaired = false;
		if(keyStore.getDeviceState() != KeyStore.DEVICE_INVALID && keyStore.getDeviceState() >= KeyStore.DEVICE_ACTIVE){
			LOGGER.info("Device is already paired/activated OR Multipair device");
			devicePaired = true;
		}
		else {
			LOGGER.info("Device is not paired yet, proceeding with device initialization, pairing and configuration");
			configService.initializeDeviceConfiguration(makerId, deviceName, generateDeviceId, deviceMultiPair, alternateDeviceId);
			String pairMsg = String.format("Access QrCode for the device from URL: %s and " + 
                    "Pair using FINN Mobile Application", url);
			LOGGER.info(pairMsg);
			String pairingResponse = getRequest(PAIRING_ENDPOINT);
			devicePaired = pairingResponse != null && pairingResponse.contains("Device pairing successful");
		}
		
		return devicePaired;
	}

	//Static Method to trigger action using /actions end point
	private static Boolean triggerAction(final String actionString) throws IOException{
		String responseBody = null;
		String url = baseUrl+ACTIONS_ENDPOINT;
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
            	String resBodyContents = String.format("Post Response Body Contents: %s", responseBody);
            	LOGGER.config(resBodyContents);
            	triggerResult = response.getStatusLine().getStatusCode() == 200;
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
		LOGGER.info("Running SDKWebServerSample...");
		try {
			
			//Reset Device Configuration only if needed otherwise comment below 4 lines of code
			boolean resetDeviceID = true;
			boolean resetDeviceName = true;
			configService.resetDeviceConfiguration(resetDeviceID, resetDeviceName);
			LOGGER.info("Device Configuration reset done");
			
			if(pairAndActivateDevice(MAKER_ID, DEVICE_NAME, GEN_DEVICE_ID, DEVICE_MP, ALT_DEVICE_ID)) {
				LOGGER.info("Device pair and activation successful, proceeding with payments...");
				Type listType = new TypeToken<List<ActionDTO>>() {}.getType();
				List<ActionDTO> actions = new Gson().fromJson(getRequest(ACTIONS_ENDPOINT), listType);
				String actionsCount = String.format("Number of actions retrieved from Server: %s" , actions.size());
				LOGGER.info(actionsCount);
				String actionStr = "{\" actionID \" : \"" + ACTION_ID + "\" } ";
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
				
					Thread.sleep(TRIGGER_INTERVAL);

				}while(true);
			}
			else {
				LOGGER.warning("Device Pairing Failed, check the log for details: " + LOG_FILE);
			}
			
		}
		catch(Exception e) {
			LOGGER.severe("Exception caught while try to consume embed WebServer end points");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}
}

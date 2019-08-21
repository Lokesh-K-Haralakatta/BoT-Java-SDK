package com.finn.bot.core;
/*
BoTService.java - Class and Methods to interact with BoT Core Service End Points using HTTP Calls
Created by Lokesh H K, August 16, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.finn.bot.store.KeyStore;

public class BoTService {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(BoTService.class.getName());
	
	//KeyStore Instance 
	private final KeyStore keyStore = KeyStore.getKeyStoreInstance();
	
	//Instance for BoTService Class as it's designed to follow singleton pattern
	private static BoTService instance = new BoTService();
	
	//BoT Service related constants
	private final static String HOST = "api-dev.bankingofthings.io";
	private final static String URI = "/bot_iot";
	private final static Integer HTTPS_PORT = 443;
	
	//Flag to enable or disable HTTP Secure Communication
	private static Boolean https = false;
	
	//Make constructor as Private
	private BoTService() {}
	
	//Gson Instances to handle JSON Data
	private Gson jsonObject = new GsonBuilder().disableHtmlEscaping().create();
	
	//Public method to return reference to single BoTService instance always
	public static BoTService getBoTServiceInstance(){
		return instance;
	}
	
	//Method to set HTTPS Flag to given value
	public void setHTTPS(final Boolean https){
		BoTService.https = https;
	}
	
	//Method to return HTTPS Flag value
	public Boolean getHTTPS(){
		return BoTService.https;
	}
	
	//Private method to build complete URL and return based on value in flag - https
	private String buildCompleteURL(final String endPoint){
		String url = null;
		
		if(https)
			url = "https://"+HOST+":"+HTTPS_PORT+URI+endPoint;
		else 
			url = "http://"+HOST+":"+URI+endPoint;
			
		return url;
	}
	
	//Private method to build HTTP Client instance based on value in flag - https
	private CloseableHttpClient getHTTPClient() throws KeyManagementException, NoSuchAlgorithmException{
		CloseableHttpClient httpclient = null;
		
		if(https){
			//Prepare SSL Context
			SSLContext sslcontext = SSLContexts.custom().build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
															sslcontext,
	                										new String[] { "TLSv1" , "TLSv2" },
	                										null,
	                										SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			//Instantiate HTTP Client associated with SSL Context
			httpclient = HttpClients.custom()
	                				.setSSLSocketFactory(sslsf)
	                				.build();
			LOGGER.fine("Instantiated HTTPClient instance with SSL Context");
		}
		else {
			//Instantiate default HTTP Client
			httpclient = HttpClients.createDefault();
			LOGGER.fine("Instantiated default HTTPClient instance");
		}
		
		return httpclient;
	}
	
	//Method to execute HTTP Get call on provided end point
	public String get(final String endPoint) throws KeyManagementException, NoSuchAlgorithmException, 
	                   IOException, CertificateException {
		String responseBody = null;
		
		String completeURL = buildCompleteURL(endPoint);
		LOGGER.fine("Constructed complete URL: " +completeURL);
		
		CloseableHttpClient httpclient = getHTTPClient();
		LOGGER.fine("Got HTTP Client Instance");
		try {
            HttpGet httpget = new HttpGet(completeURL);

            LOGGER.fine("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    LOGGER.fine("Handling GET Response for status code: " + status);
                    if (status >= 200 && status < 300) {
                    	LOGGER.fine("HTTP GET Succeeded with status code: "+status);
                    	String decodedResponse = null;
                    	try {
                    		decodedResponse = entity != null ? decodeJWT(EntityUtils.toString(entity)):null;
                    		LOGGER.fine("Decoded Response: " +decodedResponse);
                    	}
                    	catch(Exception e){
                    		LOGGER.severe("Exception caught duirng decoding response from GET Call");
                			LOGGER.severe(ExceptionUtils.getStackTrace(e));
                    	}
                        return  decodedResponse;
                    } else {
                    	LOGGER.severe("HTTP GET Failed with status code: "+status);
                    	return entity != null ? EntityUtils.toString(entity) : null;
                    }
                }

            };
            
            //Add required HTTP headers
            httpget.addHeader("makerID", keyStore.getMakerId());
            httpget.addHeader("deviceID", keyStore.getDeviceId());
            
            //Execute HTTP GET
            responseBody = httpclient.execute(httpget, responseHandler);
            LOGGER.info("GET Response Body Contents: " + responseBody);
            
        }
		catch(Exception e){
			LOGGER.severe("Exception caught duirng performing GET Call with BoT Service");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		finally {
            httpclient.close();
        }
		
		return responseBody;
	}
	
	//Method to execute HTTP Post call on provided end point
	public String post(final String endPoint, final String actionId) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyManagementException, IOException{
		String responseBody = null;

		String completeURL = buildCompleteURL(endPoint);
		LOGGER.fine("Constructed complete URL: " +completeURL);
		
		CloseableHttpClient httpclient = getHTTPClient();
		LOGGER.fine("Got HTTP Client Instance");

		try {
			//Get instance for HTTP Post
			HttpPost httpPost = new HttpPost(completeURL);
			
			//Prepare body for POST Call
			String signedToken = signPayload(actionId);
			PostBodyItem body = new PostBodyItem(signedToken);
			String bodyStr = jsonObject.toJson(body);
			LOGGER.info("Prepared body contents for POST Call: "+bodyStr);
			StringEntity entity = new StringEntity(bodyStr);
			httpPost.setEntity(entity);

            //Add required HTTP headers
            httpPost.addHeader("makerID", keyStore.getMakerId());
            httpPost.addHeader("deviceID", keyStore.getDeviceId());
            httpPost.addHeader("Content-Type", "application/json");
            
            //Execute HTTP POST
            LOGGER.fine("Executing request " + httpPost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpPost);
            
            //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	LOGGER.fine("POST Response Body Contents: " +responseBody);
            }
            
            //Check the response code and respond back
            int statusCode = response.getStatusLine().getStatusCode();
            if( statusCode == 200){
            	LOGGER.info("HTTP POST Call Succeeded...");
            	responseBody = decodeJWT(responseBody);
            	LOGGER.info("HTTP Post Call BoT Value: "+responseBody);
            }
            else {
            	if(responseBody == null)
            		responseBody = "HTTP POST Call with URL: " +completeURL+" Failed with StatusCode: " +statusCode;
            	LOGGER.severe(responseBody);
            }
		}
		catch(Exception e){
			LOGGER.severe("Exception caught duirng performing POST Call with BoT Service");
			LOGGER.severe(ExceptionUtils.getStackTrace(e));			
		}
		finally {
			httpclient.close();
		}
		return responseBody;
	}
	
	//Method to sign given payload using Key Pair
	private String signPayload(final String actionId) throws NoSuchAlgorithmException, InvalidKeySpecException{
		String encodedToken = null;
		
		if(actionId != null){
			// Get private key from keysStore and load using PKCS8 Spec
			byte[] keyBytes = Base64.getDecoder().decode(keyStore.getKey(KeyStore.PRIVATE_KEY));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec pkcsKeySpec = new PKCS8EncodedKeySpec(keyBytes);
			PrivateKey privateKey = kf.generatePrivate(pkcsKeySpec);
            LOGGER.fine("Got private key from keysStore and loaded using PKCS8 Spec");
            
			// Get public key from keysStore and load using X509 Spec
			keyBytes = Base64.getDecoder().decode(keyStore.getKey(KeyStore.PUBLIC_KEY));
			kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);
			LOGGER.fine("Got public key from keysStore and loaded using X509 Spec");
			
			// Prepare JWT Header
			Map<String,String> jwtHeader = new HashMap<String,String>();
			jwtHeader.put("typ", "JWT");
			jwtHeader.put("alg", "RS256");
			String jwtHdrStr = jsonObject.toJson(jwtHeader);
			LOGGER.fine("JWT Header String: "+jwtHdrStr);
			
			// Prepare JWT Data
			Map<String,JWTItems> payloadHash = new HashMap<String,JWTItems>();
			JWTItems items = new JWTItems(actionId,keyStore.getDeviceId(),keyStore.generateUUID4());
			payloadHash.put("bot", items);
			String payloadStr = jsonObject.toJson(payloadHash);
			LOGGER.fine("JWT Data String: "+payloadStr);
			
			// Encode JWT Header and JWT Data in Base64
			String encHeader = Base64.getEncoder().encodeToString(jwtHdrStr.getBytes(StandardCharsets.UTF_8));
	        String encPayload = Base64.getEncoder().encodeToString(payloadStr.getBytes(StandardCharsets.UTF_8));
	        LOGGER.fine("Encoded JWT Header and JWT Data in Base64");
	        
	        // Sign the encoded parts using RSA256 Algorithm
	        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey)publicKey, (RSAPrivateKey)privateKey);
	        byte[] signatureBytes = algorithm.sign(encHeader.getBytes(StandardCharsets.UTF_8), encPayload.getBytes(StandardCharsets.UTF_8));
	        String signature = Base64.getEncoder().encodeToString(signatureBytes);
	        LOGGER.fine("Signed the encoded parts using RSA256 Algorithm");
	        
	        // Get encoded token in JWT format
	        encodedToken = String.format("%s.%s.%s", encHeader, encPayload, signature);
			LOGGER.fine("Encoded Token: " +encodedToken);
		}
		
		return encodedToken;
	}
	
	//Method to decode given JWT token using API Key and return bot value
	private String decodeJWT(final String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String botValue = null;
		
		if(token != null){
			// Get API key from keysStore and load using X509 Spec
			byte[] keyBytes = Base64.getDecoder().decode(keyStore.getKey(KeyStore.API_KEY));
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);
			LOGGER.fine("Got API key from keysStore and loaded using X509 Spec");
			
			// Generate JWT verifier and verify the token
			JWTVerifier verifier = JWT.require(Algorithm.RSA256((RSAPublicKey)publicKey)).build();
			try {
				DecodedJWT jwt = verifier.verify(token);
				botValue = jwt.getClaim("bot").asString();
				LOGGER.fine("BoT Value: " +botValue);
			}
			catch(JWTVerificationException e){
				LOGGER.severe("Exception caught while verifying JWT Token");
				LOGGER.severe(ExceptionUtils.getStackTrace(e));
			}
		}
		
		return botValue; 
	}
}

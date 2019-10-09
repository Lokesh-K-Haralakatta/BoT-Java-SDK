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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.finn.bot.store.KeyStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	//Root CA SSL Finger Print Values
	private final static String ROOT_CA_FP_SHA1 = "3ea22bbffb38a6769a30d6951bf0a9bb9a847dd6";
	private final static String ROOT_CA_FP_SHA256 = "85763f1dfffde3791e52ce50776b7b50a15ae0f06a804819eca97ab22ce349b5";
	
	//Flag to enable or disable HTTP Secure Communication
	private static Boolean https = true;
	
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
			url = "https://"+HOST+URI+endPoint;
		else 
			url = "http://"+HOST+URI+endPoint;
			
		return url;
	}
	
	//Private method to build HTTP Client instance based on value in flag - https
	private CloseableHttpClient getHTTPClient() throws KeyManagementException, NoSuchAlgorithmException{
		CloseableHttpClient httpclient = null;
		
		if(https){
			 // create http response certificate intercepter
	        HttpResponseInterceptor certificateInterceptor = (httpResponse, context) -> {
	            ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection)context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
	            SSLSession sslSession = routedConnection.getSSLSession();
	            if (sslSession != null) {

	                // get the server certificates from the SSLSession
	                Certificate[] certificates = sslSession.getPeerCertificates();
	                LOGGER.fine("Count of collected certificates from SSLSession: " +certificates.length);
	                // add the certificates to the context, where we can later grab it from
	                context.setAttribute("PEER_CERTIFICATES", certificates);
	                LOGGER.fine("Added collected PEER_CERTIFICATES to context");
	            }
	        };

	        // create closable http client and assign the certificate intercepter
	        httpclient = HttpClients.custom()
	                				.addInterceptorLast(certificateInterceptor)
	                				.build();
			
		}
		else {
			//Instantiate default HTTP Client
			httpclient = HttpClients.createDefault();
			LOGGER.fine("Instantiated default HTTPClient instance");
		}

		return httpclient;
	}
	
	//Method to execute HTTP Get call on provided end point
	public synchronized String get(final String endPoint) throws KeyManagementException, NoSuchAlgorithmException, 
	                   IOException, CertificateException {
		
		String responseBody = null;
		Boolean fingerPrintStatus = false;
		
		String completeURL = buildCompleteURL(endPoint);
		LOGGER.fine("Constructed complete URL: " +completeURL);
		
		CloseableHttpClient httpclient = getHTTPClient();
		LOGGER.fine("Got HTTP Client Instance");
		try {
            HttpGet httpget = new HttpGet(completeURL);

            //Add required HTTP headers
            httpget.addHeader("makerID", keyStore.getMakerId());
            httpget.addHeader("deviceID", keyStore.getDeviceId());
            
            //Execute HTTP GET
            LOGGER.info("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = null;
            if(https){
            	// create http context where the certificate will be added
                HttpContext context = new BasicHttpContext();
                response = httpclient.execute(httpget,context);
                fingerPrintStatus = verifyFingerPrint(context);
                if(fingerPrintStatus)
                	LOGGER.info("SSL Finger Print Verification Succeeded for HTTP GET");
                else {
                	LOGGER.severe("SSL Finger Print Verification Failed for HTTP GET");
                	return("SSL Finger Print Verification Failed for HTTP GET");
                }
            }
            else
            	response = httpclient.execute(httpget);
            
          //Extract Response Contents
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
            	responseBody = EntityUtils.toString(responseEntity);
            	LOGGER.fine("GET Response Body Contents: \n" +responseBody + "\n");
            }
            
            //Check the response code and respond back
            int statusCode = response.getStatusLine().getStatusCode();
            if( statusCode == 200){
            	LOGGER.info("HTTP GET Call Succeeded...");
            	responseBody = decodeJWT(responseBody);
            	LOGGER.fine("HTTP GET Call BoT Value: "+responseBody);
            }
            else {
            	if(responseBody == null)
            		responseBody = "HTTP GET Call with URL: " +completeURL+" Failed with StatusCode: " +statusCode;
            	else{
            		responseBody = "HTTP GET Call with URL: " +completeURL+" Failed with StatusCode: " 
            	                     +statusCode + " with response: " +decodeJWT(responseBody);
            	}	
            	LOGGER.severe(responseBody);
            }
            
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
	
	//Method to verify the SSL Finger Print from the received Peer Certificate
	private Boolean verifyFingerPrint(HttpContext context) throws CertificateEncodingException, NoSuchAlgorithmException{
		Boolean status = false;
		
		// obtain the server certificates from the context
        Certificate[] peerCertificates = (Certificate[])context.getAttribute("PEER_CERTIFICATES");

        // loop over certificates and print meta-data
        String fpSha1 = null;
        String fpSha256 = null;
        for (Certificate certificate : peerCertificates){
            fpSha1 = getSSLFingerPrint((X509Certificate)certificate,"SHA-1");
            fpSha256 = getSSLFingerPrint((X509Certificate)certificate,"SHA-256");
            LOGGER.fine("SHA-1: "+fpSha1);
            LOGGER.fine("SHA-256: "+fpSha256);
            if(fpSha1.equals(ROOT_CA_FP_SHA1) && fpSha256.equals(ROOT_CA_FP_SHA256)){
            	return true;
            }
        }
        
		return status;
	}
	
	//Method extract requested Certificate ThumbPrint and return as Hex String
    private String getSSLFingerPrint(final X509Certificate cert, final String mdType) 
    		                        throws NoSuchAlgorithmException, CertificateEncodingException {
    	String fingerPrint = null;
    	     
    	MessageDigest md = null;
    	switch(mdType){
    	  case "SHA-1" : md = MessageDigest.getInstance("SHA-1"); break;
    	  case "SHA-256" : md = MessageDigest.getInstance("SHA-256"); break;
    	  default: md = MessageDigest.getInstance("SHA-1"); break;
    	}
    	     
    	byte[] der = cert.getEncoded();
    	md.update(der);
    	byte[] digest = md.digest();
    	fingerPrint = bytesToHexString(digest);

    	return fingerPrint;
    }

    //Method to convert bytes to Hex String
    private String bytesToHexString(byte[] bytes){ 
    	StringBuilder sb = new StringBuilder();
    	
    	for(byte b : bytes){ 
    		sb.append(String.format("%02x", b&0xff)); 
    	}
    	return sb.toString(); 
    } 
    
	//Method to execute HTTP Post call on provided end point
	public synchronized String post(final String endPoint, final String actionId) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyManagementException, IOException{
		
		String responseBody = null;
		Boolean fingerPrintStatus = false;
		
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
			LOGGER.fine("Prepared body contents for POST Call: "+bodyStr);
			StringEntity entity = new StringEntity(bodyStr);
			httpPost.setEntity(entity);

            //Add required HTTP headers
            httpPost.addHeader("makerID", keyStore.getMakerId());
            httpPost.addHeader("deviceID", keyStore.getDeviceId());
            httpPost.addHeader("Content-Type", "application/json");
            
            //Execute HTTP POST
            LOGGER.info("Executing request " + httpPost.getRequestLine());
            CloseableHttpResponse response = null;
            if(https){
            	// create http context where the certificate will be added
                HttpContext context = new BasicHttpContext();
                response = httpclient.execute(httpPost,context);
                fingerPrintStatus = verifyFingerPrint(context);
                if(fingerPrintStatus)
                	LOGGER.info("SSL Finger Print Verification Succeeded for HTTP POST");
                else {
                	LOGGER.severe("SSL Finger Print Verification Failed for HTTP POST");
                	return ("SSL Finger Print Verification Failed for HTTP POST");
                }
            }
            else
            	response = httpclient.execute(httpPost);
            
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
            	LOGGER.fine("HTTP Post Call BoT Value: "+responseBody);
            }
            else {
            	if(responseBody == null)
            		responseBody = "HTTP POST Call with URL: " +completeURL+" Failed with StatusCode: " +statusCode;
            	else{
            		responseBody = "HTTP POST Call with URL: " +completeURL+" Failed with StatusCode: " 
            	                     +statusCode + " with response: " +decodeJWT(responseBody);
            	}	
            	LOGGER.severe(responseBody);
            }
		}
		catch(Exception e){
			LOGGER.severe("Exception caught during performing POST Call with BoT Service");
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
			JWTItems items = null;
			if(keyStore.getDeviceState() == KeyStore.DEVICE_MULTIPAIR)
				items = new JWTItems(actionId,keyStore.getDeviceId(),keyStore.generateUUID4(), keyStore.getDeviceAltId());
			else
				items = new JWTItems(actionId,keyStore.getDeviceId(),keyStore.generateUUID4());
			
			payloadHash.put("bot", items);
			String payloadStr = jsonObject.toJson(payloadHash);
			LOGGER.info("Payload Data String: "+payloadStr);
			
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

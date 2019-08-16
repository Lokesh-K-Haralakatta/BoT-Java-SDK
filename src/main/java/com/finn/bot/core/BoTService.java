package com.finn.bot.core;
/*
BoTService.java - Class and Methods to interact with BoT Core Service End Points using HTTP Calls
Created by Lokesh H K, August 16, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.net.URISyntaxException;
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

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
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
	private final static Integer HTTP_PORT = 80;
	private final static Integer HTTPS_PORT = 8443;
	
	//Flag to enable or disable HTTP Secure Communication
	private static Boolean https = false;
	
	//Make constructor as Private
	private BoTService() {}
	
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
			url = "http://"+HOST+URI+endPoint;
			
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
			LOGGER.info("Instantiated HTTPClient instance with SSL Context");
		}
		else {
			//Instantiate default HTTP Client
			httpclient = HttpClients.createDefault();
			LOGGER.info("Instantiated default HTTPClient instance");
		}
		
		return httpclient;
	}
	
	//Method to execute HTTP Get call on provided end point
	public String get(final String endPoint) throws KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException, CertificateException, InvalidKeySpecException, URISyntaxException{
		String responseBody = null;
		
		String completeURL = buildCompleteURL(endPoint);
		LOGGER.info("Constructed complete URL: " +completeURL);
		
		CloseableHttpClient httpclient = getHTTPClient();
		LOGGER.info("Got HTTP Client Instance");
		try {
            HttpGet httpget = new HttpGet(completeURL);

            LOGGER.info("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    LOGGER.info("Handling GET Response for status code: " + status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            
            //Add required HTTP headers
            httpget.addHeader("makerID", keyStore.getMakerId());
            httpget.addHeader("deviceID", keyStore.getDeviceId());
            
            //Execute HTTP GET
            responseBody = httpclient.execute(httpget, responseHandler);
            LOGGER.info("GET Response Body Contents: " + responseBody);
        } finally {
            httpclient.close();
        }
		
		return decodeJWT(responseBody);
	}
	
	//Method to encode given token using API Key
	public String encodeJWT(final String token) throws NoSuchAlgorithmException, InvalidKeySpecException{
		// get private key from keysStore
		byte[] keyBytes = Base64.getDecoder().decode(keyStore.getKey(KeyStore.PRIVATE_KEY));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcsKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = kf.generatePrivate(pkcsKeySpec);
        
        Algorithm algorithm = Algorithm.RSA256((RSAPrivateKey)privateKey);
		String encodedToken = JWT.create().withSubject(token).sign(algorithm);
				
		return encodedToken;
	}
	
	//Method to decode given JWT token using API Key and return bot value
	public String decodeJWT(final String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// get API key from keysStore
		byte[] keyBytes = Base64.getDecoder().decode(keyStore.getKey(KeyStore.API_KEY));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);
        JWTVerifier verifier = JWT.require(Algorithm.RSA256((RSAPublicKey)publicKey)).build();
        DecodedJWT jwt = verifier.verify(token);
					
		return jwt.getClaim("bot").asString();
	}
}

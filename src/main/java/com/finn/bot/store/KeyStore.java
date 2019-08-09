package com.finn.bot.store;

/*
KeyStore.java - Class and Methods to handle the storage facility required for Java SDK using Redis
Created by Lokesh H K, August 09, 2019.
Released into the repository BoT-Java-SDK.
*/

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;

public class KeyStore {
	//Class Logger Instance
	private final static Logger LOGGER = Logger.getLogger(KeyStore.class.getName());
	
	//Instance for KeyStore Class as it's designed to follow singleton pattern
	private static KeyStore instance = new KeyStore();
	
	//Instance for Redis Client
	private Jedis jedisClient = null;
	
	//Define required final constants at class level
	private static final String ACTION_STORE_PREFIX = "action#"; // Used as prefix with action store
	private static final String KEYPAIR_FLAG = "keysGenerated"; // Flag to determine keys generation status
	private static final String KEYPAIR_STORE = "keysStore"; // Key Store name in Redis for all keys
	private static final String API_KEY_FILE = "/api.pem"; // Finn API Key Path
	private static final String DEVICE_STATE_KEY = "deviceState"; // Key name in Redis for device state
	
	private static final String DEVICE_INFO_STORE = "deviceInfo"; // Key Store name in Redis for device specific info
	private static final String DEVICE_ID = "deviceId"; // Key name for Device ID within deviceInfo
	private static final String MAKER_ID = "makerId"; // Key name for Maker ID within deviceInfo
	private static final String DEVICE_ALTERNATE_ID = "deviceAltId"; // Key name for Device Alternate ID
			
	public static final String API_KEY = "finnApiKey"; // Key name for API Key within keysStore
	public static final String PRIVATE_KEY = "privateKey"; // Key name for Private Key within keysStore
	public static final String PUBLIC_KEY = "publicKey"; // Key name for Public Key within keysStore
	
	public static final int DEVICE_NEW = 0; // value for DEVICE_STATE_NEW
	public static final int DEVICE_PAIRED = 1; // value for DEVICE_STATE_PAIRED
	public static final int DEVICE_ACTIVE = 2; // value for DEVICE_STATE_ACTIVE
	public static final int DEVICE_MULTIPAIR = 3; //value for DEVICE_STATE_MULTIPAIR
	public static final int DEVICE_INVALID = 4; //value for INVALID
	
	//Make constructor as Private
	private KeyStore(){
		jedisClient = new Jedis();
	}
	
	//Public method to return reference to single KeyStore instance always
	public static KeyStore getKeyStoreInstance(){
		return instance;
	}
	
	//Method to set device state based on the value provided
	public void setDeviceState(final int state){
		switch(state){
			case DEVICE_NEW: jedisClient.set(DEVICE_STATE_KEY,Integer.toString(DEVICE_NEW)); break;
			case DEVICE_PAIRED: jedisClient.set(DEVICE_STATE_KEY,Integer.toString(DEVICE_PAIRED)); break;
			case DEVICE_ACTIVE: jedisClient.set(DEVICE_STATE_KEY,Integer.toString(DEVICE_ACTIVE)); break;
			case DEVICE_MULTIPAIR: jedisClient.set(DEVICE_STATE_KEY,Integer.toString(DEVICE_MULTIPAIR)); break;
			default: jedisClient.set(DEVICE_STATE_KEY,Integer.toString(DEVICE_INVALID)); break;
		}
	}
	
	//Method to retrieve device state value from Redis and return it's integer value
	public int getDeviceState(){
		String state = null;
		if((state = jedisClient.get(DEVICE_STATE_KEY)) != null)
			return Integer.parseInt(state);
		else
			return -1;
	}
	
	//Method to return device state string based on value provided
	public String getDeviceState(final int state){
		switch(state){
		case DEVICE_NEW: return "NEW";
		case DEVICE_PAIRED: return "PAIRED";
		case DEVICE_ACTIVE: return "ACTIVE";
		case DEVICE_MULTIPAIR: return "MULTIPAIR";
		default: return "INVALID";
		}
	}
	
	//Method to set deviceID to given string in deviceInfo
	public void setDeviceId(final String deviceId){
		jedisClient.hset(DEVICE_INFO_STORE,DEVICE_ID,deviceId);
	}
	
	//Method to get deviceID from deviceInfo store
	public String getDeviceId(){
		return jedisClient.hget(DEVICE_INFO_STORE,DEVICE_ID);
	}

	//Method to set makerID to given string in deviceInfo
	public void setMakerId(final String makerId){
		jedisClient.hset(DEVICE_INFO_STORE,MAKER_ID,makerId);
	}
	
	//Method to get makerID from deviceInfo store
	public String getMakerId(){
		return jedisClient.hget(DEVICE_INFO_STORE,MAKER_ID);
	}

	//Method to set deviceAletrnateID to given string in deviceInfo
	public void setDeviceAltId(final String deviceAltId){
		jedisClient.hset(DEVICE_INFO_STORE,DEVICE_ALTERNATE_ID,deviceAltId);
	}
	
	//Method to get deviceAlternateID from deviceInfo store
	public String getDeviceAltId(){
		return jedisClient.hget(DEVICE_INFO_STORE,DEVICE_ALTERNATE_ID);
	}
	
	//Method to return requested key contents as bytes retrieved from Redis keys store
	public byte[] getKey(final String keyType){
		byte[] keyBytes = null;
		if (jedisClient.exists(KEYPAIR_FLAG)){
			String keyPairFlag = jedisClient.get(KEYPAIR_FLAG);
			if(keyPairFlag != null && keyPairFlag.equals("true")){
				LOGGER.info("KeyPairs already generated and present in Key Store");
				switch(keyType){
				case API_KEY: keyBytes = jedisClient.hget(KEYPAIR_STORE, API_KEY).getBytes();
                			  LOGGER.info("Length of API Key: " + keyBytes.length + " bytes");
                			  break;
				case PRIVATE_KEY: keyBytes = jedisClient.hget(KEYPAIR_STORE, PRIVATE_KEY).getBytes();
  			  					  LOGGER.info("Length of Private Key: " + keyBytes.length + " bytes");
  			  					  break;
				case PUBLIC_KEY: keyBytes = jedisClient.hget(KEYPAIR_STORE, PUBLIC_KEY).getBytes();
					  			 LOGGER.info("Length of Public Key: " + keyBytes.length + " bytes");
					  			 break;  			  					  
				}	
		    }
			else 
				LOGGER.warning("KeyPairs are not available in Key Store");
		}
		else
			LOGGER.warning("KeyPairs are not yet generated");
		
		return keyBytes;
	}
	
	//Method to read Finn API Key Contents from file and return as bytes
	public byte[] readApiKeyContents() throws IOException{
		byte[] apiKeyBytes = null;
		LOGGER.info("Loading contents from " + API_KEY_FILE);
        InputStream in = getClass().getResourceAsStream(API_KEY_FILE);
        final int apiKeyBytesLength = in.available();
        LOGGER.info("API Key Bytes Length: " + apiKeyBytesLength);
        apiKeyBytes = new byte[apiKeyBytesLength];
		in.read(apiKeyBytes);
        in.close();
        LOGGER.info("API Key bytes loaded into apiKeyBytes array");
        
		return apiKeyBytes;
	}
	
	//Method to check whether KEYPAIR_FLAG initialized and set, returns true / false
	public Boolean isKeyPairGenerated(){
		LOGGER.info("Checking KeyPairs generated for the device or not");
		if (jedisClient.exists(KEYPAIR_FLAG)){
			String keyPairFlag = jedisClient.get(KEYPAIR_FLAG);
			if(keyPairFlag != null && keyPairFlag.equals("true")){
				LOGGER.info("KeyPairs already generated and present in Key Store");
				return true;
			}
			else{
				LOGGER.warning("KEYPAIR_FLAG exists in Redis but it's associated value is either NULL or FALSE");
				return false;
			}
		}
		else{
			LOGGER.warning("KEYPAIR_FLAG not exists in Redis");
			return false;
		}
	}
	
	//Method to generate RSA Key Pairs of length 1024, gets API Key Contents and stores in Redis 
	public void generateAndStoreKeyPair()throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		if(!isKeyPairGenerated()){
			LOGGER.info("Generating and Storing keyPairs for the device");
			
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();
			PrivateKey priv = kp.getPrivate();
			PublicKey pub = kp.getPublic();
			
			LOGGER.info("Key Pair Generated");
			LOGGER.info("Private key format: " + priv.getFormat());
			LOGGER.info("Public key format: " + pub.getFormat());
			
			/* Generate private key. */
			byte[] privateKeyBytes = priv.getEncoded();
			PKCS8EncodedKeySpec pkcsKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey pvtKey = kf.generatePrivate(pkcsKeySpec);
			
			/* Generate public key. */
			byte[] publicKeyBytes = pub.getEncoded();
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			kf = KeyFactory.getInstance("RSA");
			PublicKey pubKey = kf.generatePublic(x509KeySpec);
			
			Map<String,String> keyPairMap = new HashMap<String,String>();
			keyPairMap.put(PRIVATE_KEY,Base64.getEncoder().encodeToString(pvtKey.getEncoded()));
			keyPairMap.put(PUBLIC_KEY, Base64.getEncoder().encodeToString(pubKey.getEncoded()));
			
			byte[] apiKeyBytes = readApiKeyContents();
			LOGGER.info("API Key Bytes Length: " + apiKeyBytes.length);
			keyPairMap.put(API_KEY, new String(apiKeyBytes));
			
			jedisClient.hset(KEYPAIR_STORE,keyPairMap);
			jedisClient.set(KEYPAIR_FLAG, "true");
			
			LOGGER.info("keyPairs generated and stored, KEYPAIR_FLAG set to true for the device");
		}
	}
	
	//Method to generate and return UUID4 as String
	public String generateUUID4(){
		return UUID.randomUUID().toString();
	}
	
	//Method to store given action into Redis actions store
	public Long storeAction(final ActionInfo action){
		Long result = 0l;
		if(action != null){
			final String actionId = action.getActionId();
			final String KEY = ACTION_STORE_PREFIX+actionId;
			Map<String,String> actionMap = new HashMap<String,String>();
			actionMap.put("actionId", actionId);
			actionMap.put("frequency", action.getFrequency());
			actionMap.put("ltt", action.getLastTriggerTime());
			result = jedisClient.hset(KEY,actionMap);
			LOGGER.info("Added action with id - " + actionId + " to Actions Store");
		}
		
		return result;
	}
	
	//Method to search and return the requested ation from Redis actions store
	public ActionInfo getAction(final String actionId){
		final String KEY = ACTION_STORE_PREFIX+actionId;
		Map<String,String> actionMap = jedisClient.hgetAll(KEY);
		ActionInfo action = null;
		if(actionMap.size() > 0){
			LOGGER.info("Retrieved action with id - " + actionId + " from Actions Store");
			action = new ActionInfo(actionMap.get("actionId"),actionMap.get("frequency"),actionMap.get("ltt"));
		}
		return action;
	}
	
	//Method to return all actions available in Redis actions store
	public Set<ActionInfo> getAllActions(){
		final String keyPattern = ACTION_STORE_PREFIX+"*";
		LOGGER.info("Prepared Key Pattern to get all actions: " +keyPattern);
		Set<String> actionKeys = jedisClient.keys(keyPattern);
		Set<ActionInfo> allActions = new HashSet<ActionInfo>();
		LOGGER.info("Total actions keys retrieved from Actions Store: " +actionKeys.size());
		String actionId;
		for(String actionKey : actionKeys){
			actionId = actionKey.split("#")[1];
			LOGGER.info("Retrieving action details for actionId: " +actionId);
			allActions.add(getAction(actionId));
		}
		return allActions;
	}
}

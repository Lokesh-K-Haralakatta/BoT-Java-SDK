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
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

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
	private static final String ACTIONS_STORE = "actions"; // Store to save retrieved actions from server
	private static final String KEYPAIR_FLAG = "keysGenerated"; // Flag to determine keys generation status
	private static final String KEYPAIR_STORE = "keysStore"; // Key Store name in Redis for all keys
	private static final String PRIVATE_KEY_FILE = "/private-pkcs8.der"; // Private Key File in PKCS8 format
	private static final String PUBLIC_KEY_FILE = "/public.der"; // Public Key File
	private static final String API_KEY_FILE = "/api.pem"; // Finn API Key File
	private static final String DEVICE_STATE_KEY = "deviceState"; // Key name in Redis for device state
	
	private static final String DEVICE_INFO_STORE = "deviceInfo"; // Key Store name in Redis for device specific info
	private static final String DEVICE_ID = "deviceId"; // Key name for Device ID within deviceInfo
	private static final String DEVICE_NAME = "deviceName"; // Key name for Device Name within deviceInfo
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
	
	private static final String QR_CODE_FLAG = "qrcodeGenerated"; // Flag to determine qrcode generation status
	private static final String QR_CODE_KEY  = "qrCode"; // File name to store generated QRCode as PNG Image
	private static final String QR_CODE_FILE_TYPE = "png"; // QRCode Image Type
	private static final Integer QR_CODE_SIZE = 200; //QRCode Image Size
	
	//Make constructor as Private
	private KeyStore(){
		jedisClient = new Jedis();
	}
	
	//Public method to return reference to single KeyStore instance always
	public static KeyStore getKeyStoreInstance(){
		return instance;
	}
	
	//Method to return generated QRCode in bytes
	public byte[] getQRCode(){
		byte[] qrCodeBytes = null;
		//Check QRCode generation status, if not generated, generate freshly
		if(!isQRCodeGenerated()){
			LOGGER.fine("QRCode is not yet generated, generating now...");
			try {
				generateQRCode();
				LOGGER.fine("QRCode generated, QR_CODE_FLAG set to true");
			}
			catch(Exception e){
				LOGGER.severe("Exception Caught while generating QRCode!!!");
				LOGGER.severe(ExceptionUtils.getStackTrace(e));
			}
		}
		
		if(isQRCodeGenerated()){
			LOGGER.fine("QRCode generated, reading QRCode bytes to return");
			qrCodeBytes = Base64.getDecoder().decode(jedisClient.get(QR_CODE_KEY));
		}
		return qrCodeBytes;
	}
	
	//Method to generate QRCode and save it into Key Store
	public void generateQRCode()throws WriterException, IOException {
		if(!isQRCodeGenerated()){
			// Create the ByteMatrix for the QR-Code that encodes the given String
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(this.getDeviceInfo(), BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hintMap);
			
			// Make the BufferedImage that are to hold the QRCode
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
					
			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (byteMatrix.get(i, j))
						graphics.fillRect(i, j, 1, 1);
				}
			}
			
			//Save QRCode PNG Image into Key Store
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, QR_CODE_FILE_TYPE, bos);
			jedisClient.set(QR_CODE_KEY, Base64.getEncoder().encodeToString(bos.toByteArray()));
			jedisClient.set(QR_CODE_FLAG, "true");
			
			LOGGER.fine("QRCode successfully generated and saved to KeyStore");
		}
	}
	
	//Method to check whether QR_CODE_FLAG initialized and set, returns true / false
	public Boolean isQRCodeGenerated(){
		LOGGER.fine("Checking QRCode generated for the device or not");
		if (jedisClient.exists(QR_CODE_FLAG)){
			String qrcodeFlag = jedisClient.get(QR_CODE_FLAG);
			if(qrcodeFlag != null && qrcodeFlag.equals("true")){
				LOGGER.fine("QRCode already generated and present in Key Store");
				return true;
			}
			else{
				LOGGER.fine("QR_CODE_FLAG exists in Redis but it's associated value is either NULL or FALSE");
				return false;
			}
		}
		else{
			LOGGER.fine("QR_CODE_FLAG not exists in Redis");
			return false;
		}
	}
	
	//Method to clear generated QrCode
	public void clearQrCode(){
		jedisClient.set(QR_CODE_FLAG,"false");
		jedisClient.set(QR_CODE_KEY,"");
	}
	
	//Method to prepare and return DeviceInfo JSON Object in string format
	public String getDeviceInfo(){
		//Fill in DeviceInfo Instance
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDeviceId(this.getDeviceId());
		deviceInfo.setDeviceName(this.getDeviceName());
		deviceInfo.setMakerID(this.getMakerId());
		deviceInfo.setPublicKey(this.getKey(PUBLIC_KEY));
		deviceInfo.setMultipair(0);
		deviceInfo.setAlternateId(null);
		if(this.getDeviceState() == KeyStore.DEVICE_MULTIPAIR){
			deviceInfo.setMultipair(1);
			deviceInfo.setAlternateId(this.getDeviceAltId());
		}
		
		//Convert DeviceInfo Instance to JSON String
		Gson gson = new Gson();
		String jsonDeviceInfo = gson.toJson(deviceInfo);
		LOGGER.fine("Device Info in JSON String: " + jsonDeviceInfo);
		
		return jsonDeviceInfo;
	}
	
	//Method to set device state based on the value provided
	public synchronized void setDeviceState(final int state){
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
	public synchronized void setDeviceId(final String deviceId){
		jedisClient.hset(DEVICE_INFO_STORE,DEVICE_ID,deviceId);
	}
	
	//Method to get deviceID from deviceInfo store
	public String getDeviceId(){
		return jedisClient.hget(DEVICE_INFO_STORE,DEVICE_ID);
	}

	//Method to set deviceName to given string in deviceInfo
	public synchronized void setDeviceName(final String dName){
		jedisClient.hset(DEVICE_INFO_STORE,DEVICE_NAME,dName);
	}
	
	//Method to get deviceName from deviceInfo store
	public String getDeviceName(){
		return jedisClient.hget(DEVICE_INFO_STORE,DEVICE_NAME);
	}
	
	//Method to set makerID to given string in deviceInfo
	public synchronized void setMakerId(final String makerId){
		jedisClient.hset(DEVICE_INFO_STORE,MAKER_ID,makerId);
	}
	
	//Method to get makerID from deviceInfo store
	public String getMakerId(){
		return jedisClient.hget(DEVICE_INFO_STORE,MAKER_ID);
	}

	//Method to set deviceAletrnateID to given string in deviceInfo
	public synchronized void setDeviceAltId(final String deviceAltId){
		jedisClient.hset(DEVICE_INFO_STORE,DEVICE_ALTERNATE_ID,deviceAltId);
	}
	
	//Method to get deviceAlternateID from deviceInfo store
	public String getDeviceAltId(){
		return jedisClient.hget(DEVICE_INFO_STORE,DEVICE_ALTERNATE_ID);
	}
	
	//Method to return requested key contents as String retrieved from keys store
	public String getKey(final String keyType){
		String key = null;
		if (jedisClient.exists(KEYPAIR_FLAG)){
			String keyPairFlag = jedisClient.get(KEYPAIR_FLAG);
			if(keyPairFlag != null && keyPairFlag.equals("true")){
				LOGGER.fine("KeyPairs already generated and present in Key Store");
				switch(keyType){
				case API_KEY: key = jedisClient.hget(KEYPAIR_STORE, API_KEY);
                			  LOGGER.fine("Length of API Key: " + key.length());
                			  break;
				case PRIVATE_KEY: key = jedisClient.hget(KEYPAIR_STORE, PRIVATE_KEY);
  			  					  LOGGER.fine("Length of Private Key: " + key.length());
  			  					  break;
				case PUBLIC_KEY: key = jedisClient.hget(KEYPAIR_STORE, PUBLIC_KEY);
					  			 LOGGER.fine("Length of Public Key: " + key.length());
					  			 break;  			  					  
				}	
		    }
			else 
				LOGGER.fine("KeyPairs are not available in Key Store");
		}
		else
			LOGGER.fine("KeyPairs are not yet generated");
		
		return key;
	}
	
	//Method to read Key Contents from file and return as bytes
	private byte[] readKeyContentsFromFile(final String keyFilePath) throws IOException {
		
		//Read key contents from provided key file
		LOGGER.fine("Loading contents from " + keyFilePath);
		InputStream in = getClass().getResourceAsStream(keyFilePath);
		DataInputStream dis = new DataInputStream(in);
		final int keyBytesLength = in.available();
		LOGGER.fine("Available bytes : " + keyBytesLength);
		byte[] keyBytes = new byte[keyBytesLength];
		dis.readFully(keyBytes);
		in.close();
        
		return keyBytes;
	}
	
	//Method to check whether KEYPAIR_FLAG initialized and set, returns true / false
	public Boolean isKeyPairGenerated(){
		LOGGER.fine("Checking KeyPairs generated for the device or not");
		if (jedisClient.exists(KEYPAIR_FLAG)){
			String keyPairFlag = jedisClient.get(KEYPAIR_FLAG);
			if(keyPairFlag != null && keyPairFlag.equals("true")){
				LOGGER.info("KeyPairs already generated and present in Key Store");
				return true;
			}
			else{
				LOGGER.fine("KEYPAIR_FLAG exists in Redis but it's associated value is either NULL or FALSE");
				return false;
			}
		}
		else{
			LOGGER.fine("KEYPAIR_FLAG not exists in Redis");
			return false;
		}
	}
	
	//Method to generate RSA Key Pairs of length 1024, gets API Key Contents and stores in Redis 
	public void generateAndStoreKeyPair(final Boolean generateFreshKeyPair)throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
		if(!isKeyPairGenerated()){
			byte[] privateKeyBytes = null;
			byte[] publicKeyBytes = null;
			if(generateFreshKeyPair){
				LOGGER.fine("Generating Fresh keyPair for the device");
			
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(1024);
				KeyPair kp = kpg.generateKeyPair();
				PrivateKey priv = kp.getPrivate();
				PublicKey pub = kp.getPublic();
			
				LOGGER.fine("Key Pair Generated");
				LOGGER.fine("Private key format: " + priv.getFormat());
				LOGGER.fine("Public key format: " + pub.getFormat());
			
				privateKeyBytes = priv.getEncoded();
				publicKeyBytes = pub.getEncoded();
				
				LOGGER.fine("Generated fresh key pair for the device");
			}
			else {
				privateKeyBytes = readKeyContentsFromFile(PRIVATE_KEY_FILE);
				publicKeyBytes = readKeyContentsFromFile(PUBLIC_KEY_FILE);
				LOGGER.fine("Loaded key pair contents from files");
			}
			
			LOGGER.fine("Bytes in Private Key: " +privateKeyBytes.length);
			LOGGER.fine("Bytes in Public Key: " +publicKeyBytes.length);
			
			/* Generate private key.*/ 
			PKCS8EncodedKeySpec pkcsKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey pvtKey = kf.generatePrivate(pkcsKeySpec);
			LOGGER.fine("Private Key successfully loaded...");
			
			/* Generate public key.*/
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			kf = KeyFactory.getInstance("RSA");
			PublicKey pubKey = kf.generatePublic(x509KeySpec);
			LOGGER.fine("Public Key successfully loaded..."); 
			
			Map<String,String> keyPairMap = new HashMap<String,String>();
			keyPairMap.put(PRIVATE_KEY,Base64.getEncoder().encodeToString(pvtKey.getEncoded()));
			keyPairMap.put(PUBLIC_KEY, Base64.getEncoder().encodeToString(pubKey.getEncoded()));
			
			//Retrieve Finn API Key Contents and save to key store
			byte[] apiKeyBytes = readKeyContentsFromFile(API_KEY_FILE);
			keyPairMap.put(API_KEY, new String(apiKeyBytes));
			
			jedisClient.hset(KEYPAIR_STORE,keyPairMap);
			jedisClient.set(KEYPAIR_FLAG, "true");
			
			LOGGER.fine("keyPairs generated and stored, KEYPAIR_FLAG set to true for the device");
		}
	}
	
	//Method to clear generated QrCode
	public void clearKeyPair(){
		jedisClient.set(KEYPAIR_FLAG,"false");
		Map<String,String> keyPairMap = new HashMap<String,String>();
		keyPairMap.put(PRIVATE_KEY,"");
		keyPairMap.put(PUBLIC_KEY, "");
		jedisClient.hset(KEYPAIR_STORE,keyPairMap);
	}
	
	//Method to generate and return UUID4 as String
	public String generateUUID4(){
		return UUID.randomUUID().toString();
	}
	
	//Method to save actions given as string into Actions Store
	public void saveActions(final String actions){
		jedisClient.set(ACTIONS_STORE, actions);
	}
	
	//Method to return actions saved in Actions Store as String
	public String getActions(){
		return jedisClient.get(ACTIONS_STORE);
	}
	
	//Method to store given action into Redis actions store
	public Long storeAction(final ActionInfo action){
		Long result = 0l;
		if(action != null){
			final String actionId = action.getActionId();
			final String KEY = ACTION_STORE_PREFIX+actionId;
			Map<String,String> actionMap = new HashMap<String,String>();
			actionMap.put("actionId", actionId);
			actionMap.put("ltt", action.getLastTriggerTime());
			result = jedisClient.hset(KEY,actionMap);
			LOGGER.fine("Added action with id - " + actionId + " to Actions Store");
		}
		
		return result;
	}
	
	//Method to search and return the requested ation from Redis actions store
	public ActionInfo getAction(final String actionId){
		final String KEY = ACTION_STORE_PREFIX+actionId;
		Map<String,String> actionMap = jedisClient.hgetAll(KEY);
		ActionInfo action = null;
		if(actionMap.size() > 0){
			LOGGER.fine("Retrieved action with id - " + actionId + " from Actions Store");
			action = new ActionInfo(actionMap.get("actionId"),actionMap.get("ltt"));
		}
		return action;
	}
	
	//Method to return all actions available in Redis actions store
	public Set<ActionInfo> getAllActions(){
		final String keyPattern = ACTION_STORE_PREFIX+"*";
		LOGGER.fine("Prepared Key Pattern to get all actions: " +keyPattern);
		Set<String> actionKeys = jedisClient.keys(keyPattern);
		Set<ActionInfo> allActions = new HashSet<ActionInfo>();
		LOGGER.info("Total actions keys retrieved from Actions Store: " +actionKeys.size());
		String actionId;
		for(String actionKey : actionKeys){
			actionId = actionKey.split("#")[1];
			LOGGER.fine("Retrieving action details for actionId: " +actionId);
			allActions.add(getAction(actionId));
		}
		return allActions;
	}
}

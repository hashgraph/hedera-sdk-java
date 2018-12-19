package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.node.HederaNode;
import com.hedera.sdk.query.HederaQuery;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.query.HederaQuery.QueryType;
import com.hedera.sdk.query.HederaQueryHeader.QueryResponseType;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse;
import com.hederahashgraph.api.proto.java.GetByKeyQuery;
import com.hederahashgraph.api.proto.java.GetByKeyResponse;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.Signature;
/**
 * This class is a helper for managing keys and signatures in tandem. Each instance of the object can store a {@link HederaKeyPair} and its corresponding {@link HederaSignature}
 */
public class HederaKeySignature implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaKeySignature.class);
	private static final long serialVersionUID = 1;

	private static String JSON_DESCRIPTION = "description";
	private static String JSON_UUID = "uuid";
	private static String JSON_TYPE = "type";
	private static String JSON_KEY = "key";
	private static String JSON_KEYS = "keys";
	private static String JSON_SIGNATURE = "signature";
	private static String JSON_SIGNATURE_TYPE = "signatureType";
	private byte[] publicKey = null;
	private byte[] signature = null;
	private KeyType keyType = KeyType.NOTSET;
	private HederaKeySignatureList keySigList = null;
	private HederaKeySignatureThreshold keySigThreshold = null;
	private HederaContractID contractIDKey = null;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private long cost = 0;
	private byte[] stateProof = null;
	private HederaNode node = null;

	/**
	 * A description for the key 
	 */
	public String keyDescription = "";
	/**
	 * Automatically generated UUID for the key, this can be overwritten
	 */
	public String uuid = UUID.randomUUID().toString();
	/**
	 * The list of entities related to the key in this object
	 */
	public List<HederaEntityID> entityIDs = new ArrayList<HederaEntityID>();
	/**
	 * sets the {@link HederaNode} object to use for communication with a node
	 * @param node the {@link HederaNode} object
	 */
	public void setNode (HederaNode node) {
		this.node = node;
	}
	/**
	 * gets the {@link HederaNode} object to use for communication with a node
	 * @return node the {@link HederaNode} object
	 */
	public HederaNode getNode () {
		return this.node;
	}
	/**
	 * returns the cost of a query
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * returns the state proof following a query
	 * @return byte[]
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}
	/**
	 * Default constructor
	 */
	public HederaKeySignature() {


	}
	/**
	 * Constructor from key type, key, signature and description
	 * @param keyType the type of key 
	 * @param publicKey the key as a byte array (byte[])
	 * @param signature the signature as a byte array (byte[])
	 * @param keyDescription the description of the key
	 */
	public HederaKeySignature(KeyType keyType, byte[] publicKey, byte[] signature, String keyDescription) {

		this.publicKey = publicKey.clone();
		if (signature != null) {
			this.signature = signature.clone();
		} else {
			this.signature = null;
		}
		this.keyType = keyType;
		this.keyDescription = keyDescription;

	}
	/**
	 * Constructor from key type, key and signature 
	 * @param keyType the type of key 
	 * @param publicKey the key as a byte array (byte[])
	 * @param signature the signature as a byte array (byte[])
	 */
	public HederaKeySignature(KeyType keyType, byte[] publicKey, byte[] signature) {
		this(keyType, publicKey, signature, "");
	}
	/**
	 * Constructor from a {@link HederaContractID} and description
	 * Note: The signature will be set to null
	 * @param contractKey the {@link HederaContractID} key
	 * @param keyDescription the description for the key
	 */
	public HederaKeySignature(HederaContractID contractKey, String keyDescription) {

		this.keyType = KeyType.CONTRACT;
		this.contractIDKey = contractKey;
		// contract signatures are 0 byte by default
		this.signature = null;
		this.keyDescription = keyDescription;

	}
	/**
	 * Constructor from a {@link HederaContractID} 
	 * Note: The signature will be set to null
	 * @param contractKey the {@link HederaContractID} key
	 */
	public HederaKeySignature(HederaContractID contractKey) {
		this(contractKey, "");
	}
	/**
	 * Constructor from a {@link HederaKeySignatureThreshold} object and description
	 * @param thresholdKeySigPair a {@link HederaKeySignatureThreshold}
	 * @param keyDescription the description for the key
	 */
	public HederaKeySignature(HederaKeySignatureThreshold thresholdKeySigPair, String keyDescription) {

		this.keyType = KeyType.THRESHOLD;
		this.keySigThreshold = thresholdKeySigPair;
		this.keyDescription = keyDescription;

	}
	/**
	 * Constructor from a {@link HederaKeySignatureThreshold} object 
	 * @param thresholdKeySigPair a {@link HederaKeySignatureThreshold}
	 */
	public HederaKeySignature(HederaKeySignatureThreshold thresholdKeySigPair) {
		this(thresholdKeySigPair, "");
	}
	/**
	 * Constructor from a {@link HederaKeySignatureList} and description
	 * @param keySigList a {@link HederaKeySignatureList}
	 * @param keyDescription the description for the key
	 */
	public HederaKeySignature(HederaKeySignatureList keySigList, String keyDescription) {

		this.keyType = KeyType.LIST;
		this.keySigList = keySigList;
		this.keyDescription = keyDescription;

	}
	/**
	 * Constructor from a {@link HederaKeySignatureList} 
	 * @param keySigList a {@link HederaKeySignatureList}
	 */
	public HederaKeySignature(HederaKeySignatureList keySigList) {
		this(keySigList,"");
	}
	/**
	 * Constructor for a {@link Key} protobuf and associated {@link Signature} protobuf with a description for the key
	 * Note: It is assumed that keys and signatures are matching in both protobuf objects
	 * @param protobufKey a {@link Key} protobuf
	 * @param protobufSig a {@link Signature} protobug
	 * @param keyDescription the description for the key
	 */
//	public HederaKeySignature(Key protobufKey, Signature protobufSig, String keyDescription) {
//
//		
//		HederaKeyPair hederaKey = new HederaKeyPair(protobufKey);
//		HederaSignature hederaSignature = new HederaSignature(protobufSig);
//
//		switch (protobufKey.getKeyCase()) {
//		case KEYLIST:
//			this.keyType = hederaKey.getKeyType();
//			this.keySigList = new HederaKeySignatureList(protobufKey.getKeyList(), protobufSig.getSignatureList());
//			break;
//		case THRESHOLDKEY:
//			this.keyType = hederaKey.getKeyType();
//			this.keySigThreshold = new HederaKeySignatureThreshold(protobufKey.getThresholdKey(), protobufSig.getThresholdSignature());
//			break;
//		case KEY_NOT_SET:
//            throw new IllegalArgumentException("Key not set in protobuf data.");			
//		case CONTRACTID:
//			this.keyType = hederaKey.getKeyType();
//			this.contractIDKey = hederaKey.getContractIDKey();
//			this.publicKey = null;
//			this.signature = null;
//			break;
//		default: //ECDSA_384, ED25519, RSA_3072
//			this.keyType = hederaKey.getKeyType();
//			this.publicKey = hederaKey.getPublicKeyEncoded();
//			this.signature = hederaSignature.getSignature();
//		}
//		this.keyDescription = keyDescription;
//
//	}
//	/**
//	 * Constructor for a {@link Key} protobuf and associated {@link Signature} protobuf 
//	 * Note: It is assumed that keys and signatures are matching in both protobuf objects
//	 * @param protobufKey a {@link Key} protobuf
//	 * @param protobufSig a {@link Signature} protobug
//	 */
//	public HederaKeySignature(Key protobufKey, Signature protobufSig) {
//		this(protobufKey, protobufSig, "");
//	}
	/**
	 * Gets the type of the key stored in this object
	 * @return {@link KeyType}
	 */
	public KeyType getKeyType() {

		return this.keyType;
	}
	/** 
	 * Gets the key held in this object
	 * Note: this will be null if not set
	 * @return byte[]
	 */
	public byte[] getKey() {

		return this.publicKey;
	}
	/**
	 * Gets the signature held in this object
	 * Note: this will be null if not set
	 * @return byte[]
	 */
	public byte[] getSignature() {

		return this.signature;
	}
	/**
	 * Gets the {@link HederaContractID} held in this object
	 * Note: this will be null if not set
	 * @return {@link HederaContractID}
	 */
	public HederaContractID getContractIDKey() {

		return this.contractIDKey;
	}
	/**
	 * Gets the signature for the contract ID
	 * Note: This is always an empty byte array, contractIDs do not have signatures
	 * @return byte[0]
	 */
	public byte[] getContractIDSignature() {

		return new byte[0]; //this.signature;
	}
	/**
	 * Gets the {@link HederaKeySignatureThreshold} held in this object
	 * Note: Returns null if not set 
	 * @return {@link HederaKeySignatureThreshold}
	 */
	public HederaKeySignatureThreshold getThresholdKeySignaturePair() {

		return this.keySigThreshold;
	}
	/**
	 * Gets the {@link HederaKeySignatureList} held in this object
	 * @return {@link HederaKeySignatureList}
	 */
	public HederaKeySignatureList getKeySignaturePairList() {

		return this.keySigList;
	}
	/**
	 * Gets the protobuf representation of the key held in this object
	 * @return {@link Key}
	 */
	public Key getKeyProtobuf() {

		// Generates the protobuf payload for this class
		Key.Builder keyProtobuf = Key.newBuilder();
		
		switch (this.keyType) {
		case ED25519:
			if (this.publicKey != null) {
				keyProtobuf.setEd25519(ByteString.copyFrom(this.publicKey));
			}
			break;
//		case RSA3072:
//			if (this.publicKey != null) {
//				keyProtobuf.setRSA3072(ByteString.copyFrom(this.publicKey));
//			}
//			break;
//		case ECDSA384:
//			if (this.publicKey != null) {
//				keyProtobuf.setECDSA384(ByteString.copyFrom(this.publicKey));
//			}
//			break;
		case CONTRACT:
			if (this.contractIDKey != null) {
				keyProtobuf.setContractID(this.contractIDKey.getProtobuf());
			}
			break;
		case THRESHOLD:
			if (this.keySigThreshold != null) {
				keyProtobuf.setThresholdKey(this.keySigThreshold.getKeyProtobuf());
			}
			break;
		case LIST:
			if (this.keySigList != null) {
				keyProtobuf.setKeyList(this.keySigList.getProtobufKeys());
			}
			break;
		case NOTSET:
            throw new IllegalArgumentException("Key type not set, unable to generate data.");			
		}

		
		return keyProtobuf.build();
	}
	/** 
	 * Returns the {@link Signature} protobuf for the signature held in this object
	 * @return {@link Signature}
	 */
	public Signature getSignatureProtobuf() {

		// Generates the protobuf payload for this class
		Signature.Builder signatureProtobuf = Signature.newBuilder();
		
		switch (this.keyType) {
		case ED25519:
			if (this.signature != null) {
				signatureProtobuf.setEd25519(ByteString.copyFrom(this.signature));
			}
			break;
//		case RSA3072:
//			if (this.signature != null) {
//				signatureProtobuf.setRSA3072(ByteString.copyFrom(this.signature));
//			}
//			break;
//		case ECDSA384:
//			if (this.signature != null) {
//				signatureProtobuf.setECDSA384(ByteString.copyFrom(this.signature));
//			}
//			break;
		case CONTRACT:
			signatureProtobuf.setContract(ByteString.copyFrom(new byte[0]));
			break;
		case THRESHOLD:
			if (this.keySigThreshold != null) {
				signatureProtobuf.setThresholdSignature(this.keySigThreshold.getSignatureProtobuf());
			}
			break;
		case LIST:
			if (this.keySigList != null) {
				signatureProtobuf.setSignatureList(this.keySigList.getProtobufSignatures());
			}
			break;
		case NOTSET:
            throw new IllegalArgumentException("Signature type not set, unable to generate data.");			
		}

		
		return signatureProtobuf.build();
	}
	/**
	 * Sets the signatures against a matching key
	 * If they key is found but has signature already set, looks for another matching key
	 * If stopAtFirst is true, only the first matching key with no signature is set
	 * otherwise all matching keys with no signature are set
	 * @param key the key to look for
	 * @param signature the signature value to set
	 * @param stopAtFirst sets only the first matching key with empty signature if true
	 * @return boolean true if a signature was set
	 */
	public boolean setSignatureForKey(byte[] key, byte[] signature, boolean stopAtFirst) {

		boolean foundOne = false;
		
		// exit if key type is not set
		if (this.keyType == KeyType.NOTSET) {
			return false;
		}

		// first check this key if it's neither a list of threshold
//		if ((this.keyType == KeyType.ECDSA384) || (this.keyType == KeyType.ED25519) || (this.keyType == KeyType.RSA3072)) {
		if (this.keyType == KeyType.ED25519) {
			if (Arrays.equals(this.publicKey,key)) {
				// it's a match, set the signature
				// Is the signature empty
				if (this.signature == null) {
					// yes it's not set so set it.
					foundOne = true;
					this.signature = signature;
					if (stopAtFirst) {
				
						return true;
					}
				}
			}
		}
		if (this.keyType == KeyType.THRESHOLD) {
			foundOne = keySigThreshold.setSignatureForKey(key, signature, stopAtFirst);
		} else if (this.keyType == KeyType.LIST) {
			foundOne = keySigList.setSignatureForKey(key, signature, stopAtFirst);
		}

		
		return foundOne;
	}
	/**
	 * Sets the signatures against matching keys
	 * If a key is found but has signature already set, looks for another matching key
	 * If stopAtFirst is true, only the first matching key with no signature is set
	 * otherwise all matching keys with no signature are set
	 * @param keys a byte[][] array of keys to look for
	 * @param signatures a byte[][] array of signatures for the keys
	 * @param stopAtFirst sets only the first matching key with empty signature if true
	 * @return boolean true if a signature was set
	 */
	public boolean setSignatureForKeys(byte[][] keys, byte[][] signatures, boolean stopAtFirst) {

		boolean foundOne = false;
		
		for (int i=0; i < keys.length; i++) {
			if (setSignatureForKey(keys[i], signatures[i], stopAtFirst)) {
				foundOne = true;
			}
		}

		
		return foundOne;
	}
	
	/**
	 * Sets the signature against a key matching the supplied UUID
	 * if the signature is already set, it will be overwritten
	 * @param uuid the UUID of the key to update
	 * @param signature the signature
	 * @return boolean true if key was found
	 */
	public boolean setSignatureForKeyUUID(String uuid, byte[] signature) {

		boolean foundOne = false;
		
		// exit if key type is not set
		if (this.keyType == KeyType.NOTSET) {
	
			return false;
		}

		// first check this key if it's neither a list of threshold
//		if ((this.keyType == KeyType.ECDSA384) || (this.keyType == KeyType.ED25519) || (this.keyType == KeyType.RSA3072)) {
		if (this.keyType == KeyType.ED25519) {
			if (this.uuid.equals(uuid)) {
				// it's a match, set the signature
				this.signature = signature;
		
				return true;
			}
		}
		if (this.keyType == KeyType.THRESHOLD) {
			foundOne = keySigThreshold.setSignatureForKeyUUID(uuid, signature);
		} else if (this.keyType == KeyType.LIST) {
			foundOne = keySigList.setSignatureForKeyUUID(uuid, signature);
		}

		
		return foundOne;
	}
	/**
	 * Sets the signatures against keys matching the supplied UUIDs
	 * if the signature is already set, it will be overwritten
	 * @param uuids a String[] of key UUIDs
	 * @param signatures byte[][] of matching signatures for the UUIDs
	 * @return boolean true if a signature was set
	 */
	public boolean setSignatureForKeyUUIDs(String[] uuids, byte[][] signatures) {

		boolean foundOne = false;

		for (int i=0; i < uuids.length; i++) {
			if (setSignatureForKeyUUID(uuids[i], signatures[i])) {
				foundOne = true;
			}
		}

		return foundOne;
	}
	/**
	 * Updates the signature for the matching key only if it's already set
	 * all matching keys are updated
	 * @param key the key to search
	 * @param signature the new signature value
	 * @return true if a signature was updated
	 */
	public boolean updateSignatureForKey(byte[] key, byte[] signature) {

		boolean foundOne = false;
		
		// exit if key type is not set
		if (this.keyType == KeyType.NOTSET) {
	
			return false;
		}

		// first check this key if it's neither a list of threshold
//		if ((this.keyType == KeyType.ECDSA384) || (this.keyType == KeyType.ED25519) || (this.keyType == KeyType.RSA3072)) {
		if (this.keyType == KeyType.ED25519) {
			if (Arrays.equals(this.publicKey,key)) {
				// it's a match, set the signature
				if (this.signature != null) {
					// yes it's set so update it.
					this.signature = signature;
					foundOne = true;
				}
			}
		}
		if (this.keyType == KeyType.THRESHOLD) {
			foundOne = keySigThreshold.updateSignatureForKey(key, signature);
		} else if (this.keyType == KeyType.LIST) {
			foundOne = keySigList.updateSignatureForKey(key, signature);
		}

		
		return foundOne;
	}
	/**
	 * Updates the signatures for the matching keys only if already set
	 * all matching keys are updated
	 * @param keys a byte[][] array of keys to search for
	 * @param signatures a byte[][] array of signatures to match the keys
	 * @return true if a signature was updated
	 */
	public boolean updateSignatureForKeys(byte[][] keys, byte[][] signatures) {

		boolean foundOne = false;
		
		for (int i=0; i < keys.length; i++) {
			if (updateSignatureForKey(keys[i], signatures[i])) {
				foundOne = true;
			}
		}

		
		return foundOne;
	}
	/**
	 * Gets an array of keys and UUIDS {@link HederaKeyUUIDDescription} for a given public key
	 * 
	 * @param hederaKeyUUIDDescriptions a List of {@link HederaKeyUUIDDescription} containing the result
	 * Note: Due to the recursive nature of this method, you must initialise this List before calling the method.
	 * The result will be in the same parameter  
	 * @param publicKey the public key to look for
	 */
	public void getKeyUUIDs(List<HederaKeyUUIDDescription> hederaKeyUUIDDescriptions, byte[] publicKey) {

		// exit if key type is not set
		if (this.keyType != KeyType.NOTSET) {

			// first check this key if it's neither a list of threshold
//			if ((this.keyType == KeyType.ECDSA384) || (this.keyType == KeyType.ED25519) || (this.keyType == KeyType.RSA3072)) {
			if (this.keyType == KeyType.ED25519) {
				if (Arrays.equals(this.publicKey, publicKey)) {
					HederaKeyUUIDDescription hederaKeyUUIDDescription = new HederaKeyUUIDDescription(this.uuid, this.keyDescription);
					hederaKeyUUIDDescriptions.add(hederaKeyUUIDDescription);
				}
			}
			if (this.keyType == KeyType.THRESHOLD) {
				keySigThreshold.getKeyUUIDs(hederaKeyUUIDDescriptions, publicKey);
			} else if (this.keyType == KeyType.LIST) {
				keySigList.getKeyUUIDs(hederaKeyUUIDDescriptions, publicKey);
			}
		}		

	}

	/**
	 *  Gets a {@link JSONObject} representation of this {@link HederaKeySignature}
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {

		
	   	JSONObject jsonKey = new JSONObject();
	   	
	   	jsonKey.put(JSON_DESCRIPTION, this.keyDescription);
	   	jsonKey.put(JSON_UUID, this.uuid);
	   	if (this.signature != null) {
	   		jsonKey.put(JSON_SIGNATURE, DatatypeConverter.printBase64Binary(this.signature));
	   	}

		switch (this.keyType) {
		case CONTRACT:
			jsonKey.put(JSON_TYPE, "CONTRACT");
			jsonKey.put(JSON_SIGNATURE_TYPE, "CONTRACT");
			jsonKey.put(JSON_KEY, this.contractIDKey.JSON());
			break;
//		case ECDSA384:
//			jsonKey.put(JSON_TYPE, "ECDSA384");
//			jsonKey.put(JSON_SIGNATURE_TYPE, "ECDSA384");
//			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.publicKey));
//			break;
		case ED25519:
			jsonKey.put(JSON_TYPE, "ED25519");
			jsonKey.put(JSON_SIGNATURE_TYPE, "ED25519");
			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.publicKey));
			break;
		case LIST:
			jsonKey.put(JSON_TYPE, "KEYLIST");
			jsonKey.put(JSON_SIGNATURE_TYPE, "KEYLIST");
			jsonKey.put(JSON_KEYS, this.keySigList.JSON());
			break;
//		case RSA3072:
//			jsonKey.put(JSON_TYPE, "RSA3072");
//			jsonKey.put(JSON_SIGNATURE_TYPE, "RSA3072");
//			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.publicKey));
//			break;
		case THRESHOLD:
			jsonKey.put(JSON_TYPE, "THRESHOLD");
			jsonKey.put(JSON_SIGNATURE_TYPE, "THRESHOLD");
			jsonKey.put(JSON_KEY, this.keySigThreshold.JSON());
			break;
		case NOTSET:
			jsonKey.put(JSON_TYPE, "NOTSET");
			jsonKey.put(JSON_SIGNATURE_TYPE, "NOTSET");
			break;
		}

		
		return jsonKey;
	}
	/**
	 * Gets a {@link String} value for the JSON object representing this object
	 * @return {@link String}
	 */
	public String JSONString() {


		return JSON().toJSONString();
	}

	/**
	 * Populates values for this object from a {@link JSONObject}
	 * @param jsonKey the {@link JSONObject} to populate this object with
	 */
	public void fromJSON(JSONObject jsonKey) {

		
		if (jsonKey.containsKey(JSON_DESCRIPTION)) {
			this.keyDescription = (String) jsonKey.get(JSON_DESCRIPTION);
		} else {
			this.keyDescription = "";
		}
		if (jsonKey.containsKey(JSON_UUID)) {
			this.uuid = (String) jsonKey.get(JSON_UUID);
		} else {
			this.uuid = UUID.randomUUID().toString();
		}
		if (jsonKey.containsKey(JSON_TYPE)) {
			// reset  key just in case
			this.keySigThreshold = null;
			this.contractIDKey = null;
			this.keySigList = null;
			this.publicKey = null;
			this.signature = null;
			
			JSONObject oneKey = new JSONObject();
			
			switch ((String) jsonKey.get(JSON_TYPE)) {
			case  "CONTRACT":
				this.keyType = KeyType.CONTRACT;
				oneKey = (JSONObject) jsonKey.get(JSON_KEY);
				this.contractIDKey = new HederaContractID();
				this.contractIDKey.fromJSON(oneKey);
				if (jsonKey.containsKey(JSON_SIGNATURE)) {
					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
				} else {
					this.signature = null;
				}
				break;
//			case "ECDSA384":
//				this.keyType = KeyType.ECDSA384;
//				this.publicKey = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
//				if (jsonKey.containsKey(JSON_SIGNATURE)) {
//					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
//				} else {
//					this.signature = null;
//				}
//				break;
			case "ED25519":
				this.keyType = KeyType.ED25519;
				this.publicKey = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
				if (jsonKey.containsKey(JSON_SIGNATURE)) {
					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
				} else {
					this.signature = null;
				}
				break;
			case "KEYLIST":
				this.keyType = KeyType.LIST;
				JSONArray listOfKeys = new JSONArray();
				listOfKeys = (JSONArray) jsonKey.get(JSON_KEYS);
				this.keySigList = new HederaKeySignatureList();
				this.keySigList.fromJSON(listOfKeys);
				if (jsonKey.containsKey(JSON_SIGNATURE)) {
					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
				} else {
					this.signature = null;
				}
				break;
//			case "RSA3072":
//				this.keyType = KeyType.RSA3072;
//				this.publicKey = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
//				if (jsonKey.containsKey(JSON_SIGNATURE)) {
//					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
//				} else {
//					this.signature = null;
//				}
//				break;
			case "THRESHOLD":
				this.keyType = KeyType.THRESHOLD;
				oneKey = (JSONObject) jsonKey.get(JSON_KEY);
				this.keySigThreshold = new HederaKeySignatureThreshold();
				this.keySigThreshold.fromJSON(oneKey);
				if (jsonKey.containsKey(JSON_SIGNATURE)) {
					this.signature = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_SIGNATURE));
				} else {
					this.signature = null;
				}
				break;
			case "NOTSET":
				this.keyType = KeyType.NOTSET;
				break;
			}
		} else {
			throw new IllegalStateException("Key type isn't set in JSON.");
		}

	}
	
	/**
	 * Runs a query to get entities related to this key from the Hedera Network
	 * If successful, the method populates the entityIDs, cost and stateProof for this object depending on the type of answer requested
	 * @param payment a {@link HederaTransaction} message to indicate how this query will be paid for, this can be null for Cost queries
	 * @param responseType the type of response requested from the query
	 * @return {@link Boolean} indicating success or failure of the query
	 * @throws InterruptedException should an exception occur during communication with the node
	 */
	public boolean getEntities(HederaTransaction payment, HederaQueryHeader.QueryResponseType responseType) throws InterruptedException {
		boolean result = true;
		

		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get by key query
		GetByKeyQuery.Builder getByKeyQuery = GetByKeyQuery.newBuilder();
		getByKeyQuery.setKey(this.getKeyProtobuf());
		getByKeyQuery.setHeader(queryHeader.getProtobuf());
		
		// the query itself
		HederaQuery query = new HederaQuery();
		query.queryType = QueryType.FILEGETINFO;
		query.queryData = getByKeyQuery.build();
		
		// query now set, send to network
		Response response = this.node.getFileInfo(query);

		FileGetInfoResponse.Builder fileGetInfoResponse = response.getFileGetInfo().toBuilder();
		
		// check response header first
		ResponseHeader.Builder responseHeader = fileGetInfoResponse.getHeaderBuilder();
		
		this.precheckResult = responseHeader.getNodeTransactionPrecheckCode();

		if (this.precheckResult == ResponseCodeEnum.OK) {
			GetByKeyResponse queryResponse = response.getGetByKey();
			// cost
			this.cost = responseHeader.getCost();
			//state proof
			this.stateProof = responseHeader.getStateProof().toByteArray();
			
			this.entityIDs.clear();
			for (int i=0; i < queryResponse.getEntitiesCount(); i++) {
				HederaEntityID entity = new HederaEntityID(queryResponse.getEntities(i));
				this.entityIDs.add(entity);
			}
		} else {
			result = false;
		}
		

	   	return result;
	}
	/**
	 * Gets the entities related to this key from the network, requesting only an answer
	 * If successful, the method populates the entityIDs and cost for this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesAnswerOnly(HederaTransaction payment) throws InterruptedException {

	   	return getEntities(payment, QueryResponseType.ANSWER_ONLY);
	}
	/**
	 * Gets the entities related to this key from the network, requesting a state proof
	 * If successful, the method populates the entityIDs,state proof and cost for this object
	 * @param payment the {@link HederaTransaction} payload containing payment information for the query
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesStateProof(HederaTransaction payment) throws InterruptedException {

		return getEntities(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Gets the cost of running a query to get entities with only an answer
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesCostAnswer() throws InterruptedException {

		return getEntities(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Gets the cost of running a query to get entities with a state proof
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesCostAnswerStateProof() throws InterruptedException {

		return getEntities(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
}
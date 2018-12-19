package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.SignatureList;
import com.hederahashgraph.api.proto.java.ThresholdKey;
import com.hederahashgraph.api.proto.java.ThresholdSignature;
/**
 * This class is a helper for managing keys and signatures in tandem. 
 * Each instance of the object stores a threshold and List of {@link HederaKeySignature}
 */
public class HederaKeySignatureThreshold implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaKeySignatureThreshold.class);
	private static final long serialVersionUID = 1;
	private static String JSON_KEYS = "keys";
	private static String JSON_THRESHOLD = "threshold";

	/**
	 * The desired threshold for these keys
	 */
	public int threshold;
	/**
	 * The List of {@link HederaKeySignature}
	 * initially empty 
	 */
	public List<HederaKeySignature> keySigPairs  = new ArrayList<HederaKeySignature>();
	/**
	 * Default constructor
	 */
	public HederaKeySignatureThreshold() {


	}
	/**
	 * Constructs from a threshold and List of {@link HederaKeySignature}
	 * @param threshold the integer threshold value
	 * @param keySigPairs the List of {@link HederaKeySignature}
	 */
	public HederaKeySignatureThreshold(int threshold, List<HederaKeySignature> keySigPairs) {

		if (keySigPairs != null) {
			for (HederaKeySignature keySigPair : keySigPairs) {
				this.keySigPairs.add(keySigPair);
			}
		}
		
		this.threshold = threshold;

	}
//	/**
//	 * Constructs from a {@link ThresholdKey} protobuf and associated {@link ThresholdSignature} protobuf
//	 * Note: it is assumed that keys and signatures match in both protobuf objects
//	 * @param protobufKey the keys
//	 * @param protobufSig the signatures
//	 */
//	public HederaKeySignatureThreshold(ThresholdKey protobufKey, ThresholdSignature protobufSig) {
//
//		// convert a protobuf payload into class data
//		this.threshold = protobufKey.getThreshold();
//		this.keySigPairs.clear();
//		
//		KeyList protoKeys = protobufKey.getKeys();
//		SignatureList protoSigs = protobufSig.getSigs();
//		
//		for (int index = 0; index < protoKeys.getKeysCount(); index++) {
//			this.keySigPairs.add(new HederaKeySignature(protoKeys.getKeys(index), protoSigs.getSigs(index)));
//		}
//
//	}
	/**
	 * Gets the protobuf {@link ThresholdKey} for the key 
	 * @return {@link ThresholdKey}
	 */
	public ThresholdKey getKeyProtobuf() {

		// Generates the protobuf payload for this class
		ThresholdKey.Builder keysProtobuf = ThresholdKey.newBuilder();
		keysProtobuf.setThreshold(this.threshold);
		
		KeyList protoKeyList;
		
		if (!this.keySigPairs.isEmpty()) {
			protoKeyList = Utilities.getProtoKeyFromKeySigList(this.keySigPairs);
			keysProtobuf.setKeys(protoKeyList);
		}
		else {
			return null;
		}		
	

		return keysProtobuf.build();
	}
	/**
	 * Gets the protobuf {@link ThresholdSignature} for the signatures
	 * @return {@link ThresholdSignature}
	 */
	public ThresholdSignature getSignatureProtobuf() {

		// Generates the protobuf payload for this class
		ThresholdSignature.Builder sigProtobuf = ThresholdSignature.newBuilder();
		
		SignatureList protoKeyList;
		
		if (!this.keySigPairs.isEmpty()) {
			protoKeyList = Utilities.getProtoSignatureFromKeySigList(this.keySigPairs);
			sigProtobuf.setSigs(protoKeyList);
		} else {
			return null;
		}
		

		return sigProtobuf.build();
	}
	/**
	 * Adds a key/signature pair to the list
	 * @param keySigPair a {@link HederaKeySignature}
	 */
	public void addKeySigPair(HederaKeySignature keySigPair) {

		this.keySigPairs.add(keySigPair);

	}
	/**
	 * Deletes a key/signature pair from the list
	 * @param keySigPair the {@link HederaKeySignature} to delete
	 * @return true if found and deleted successfully
	 */
	public boolean deleteKey(HederaKeySignature keySigPair) {


		return this.keySigPairs.remove(keySigPair);
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
	
		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.setSignatureForKey(key, signature, stopAtFirst)) {
				foundOne = true;
				if (stopAtFirst) {
					return true;
				}
			}
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

	
		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.setSignatureForKeyUUID(uuid, signature)) {
				return true;
			}
		}

		return false;
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
	
		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.updateSignatureForKey(key, signature)) {
				foundOne = true;
			}
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

		for (HederaKeySignature keySigPair : keySigPairs) {
			keySigPair.getKeyUUIDs(hederaKeyUUIDDescriptions, publicKey);
		}

	}	
	/**
	 *  Gets a {@link JSONObject} representation of this {@link HederaKeySignatureThreshold}
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {

	   	
	   	JSONObject jsonKey = new JSONObject();
  	   	
	   	jsonKey.put(JSON_THRESHOLD, this.threshold);

		JSONArray jsonKeys = new JSONArray();
		for (HederaKeySignature hederaKey : this.keySigPairs) {
			jsonKeys.add(hederaKey.JSON());
		}
		jsonKey.put(JSON_KEYS, jsonKeys);


		return jsonKey;
	}
	/**
	 *  Gets a {@link String} value of the {@link JSONObject} representation of this {@link HederaKeySignatureThreshold}
	 * @return {@link JSONObject}
	 */
	public String JSONString() {

		return JSON().toJSONString();
	}
	/**
	 * Populates values for this object from a {@link JSONObject}
	 * @param jsonKey the {@link JSONObject} to populate this object with
	 */
	public void fromJSON(JSONObject jsonKey) {

		// delete all keys
		this.keySigPairs.clear();
		// add keys from json array
		
		if (jsonKey.containsKey(JSON_THRESHOLD)) {
			this.threshold = (int)jsonKey.get(JSON_THRESHOLD);
		} else {
			this.threshold = 0;
		}
		
		if (jsonKey.containsKey(JSON_KEYS)) {
			JSONArray keys = new JSONArray();
			keys = (JSONArray) jsonKey.get(JSON_KEYS);
			
			for (int i=0; i < keys.size(); i++) {
				HederaKeySignature key = new HederaKeySignature();
				JSONObject oneKey = new JSONObject();
				oneKey = (JSONObject) keys.get(i);
				key.fromJSON(oneKey);
				this.addKeySigPair(key);
			}
		}

	}
}
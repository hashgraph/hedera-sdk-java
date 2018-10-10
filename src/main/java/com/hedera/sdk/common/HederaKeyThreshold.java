package com.hedera.sdk.common;

import com.hedera.sdk.common.HederaKey;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.ThresholdKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A set of public keys that are used together to form a threshold signature. 
 * If the threshold is N and there are M keys, then this is an N of M threshold signature. 
 * If an account is associated with ThresholdKeys, then a transaction to move cryptocurrency out of it must be signed by a list of M signatures, 
 * where at most M-N of them are blank, and the other at least N of them are valid signatures corresponding to at least N of the public keys listed here.
 */
public class HederaKeyThreshold implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaKeyThreshold.class);
	private static String JSON_KEYS = "keys";
	private static String JSON_THRESHOLD = "threshold";
	private static final long serialVersionUID = 1;
	/**
	 * The desired threshold for these keys
	 */
	public int threshold;
	/**
	 * The List of {@link HederaKey}
	 * initially empty 
	 */
	public List<HederaKey> keys  = new ArrayList<HederaKey>();
	/**
	 * Default constructor
	 */
	public HederaKeyThreshold() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a threshold and List of {@link HederaKey}
	 * @param threshold the integer threshold value
	 * @param keys the List of {@link HederaKey}
	 */
	public HederaKeyThreshold(int threshold, List<HederaKey> keys) {
	   	logger.trace("Start - Object init threshold {}, keys {}", threshold, keys);

	   	if (keys != null) {
		   	for (HederaKey hederaKey : keys) {
				this.keys.add(hederaKey);
			}
	   	}
		this.threshold = threshold;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a {@link ThresholdKey} protobuf 
	 * @param protobufKey the keys
	 */
	public HederaKeyThreshold(ThresholdKey protobufKey) {
	   	logger.trace("Start - Object init protobuf {}", protobufKey);

	   	// convert a protobuf payload into class data
		this.threshold = protobufKey.getThreshold();
		this.keys.clear();
		KeyList protoKeys = protobufKey.getKeys();
		
		for (Key key : protoKeys.getKeysList()) {
			this.keys.add(new HederaKey(key));
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Gets the protobuf {@link ThresholdKey} for the key 
	 * @return {@link ThresholdKey}
	 */
	public ThresholdKey getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		ThresholdKey.Builder keysProtobuf = ThresholdKey.newBuilder();
		keysProtobuf.setThreshold(this.threshold);
		
		KeyList protoKeyList;
		
		if (!this.keys.isEmpty()) {
			protoKeyList = Utilities.getProtoKeyList(this.keys);
			keysProtobuf.setKeys(protoKeyList);
		}
		else {
			logger.trace("End - getKeyProtobuf: return NULL");
			return null;
		}
	   	logger.trace("End - getProtobuf");
		return keysProtobuf.build();
	}
	/**
	 * Adds a key to the list
	 * @param key a {@link HederaKey}
	 */
	public void addKey(HederaKey key) {
	   	logger.trace("Start - addKey key {}", key);
		this.keys.add(key);
	   	logger.trace("End - addKey");
	}
	/**
	 * Deletes a key from the list
	 * @param key a {@link HederaKey}
	 * @return true if found and successfully deleted
	 */
	public boolean deleteKey(HederaKey key) {
	   	logger.trace("Start - deleteKey key {}", key);
	   	logger.trace("End - deleteKey");
		return this.keys.remove(key);
	}
	/**
	 *  Gets a {@link JSONObject} representation of this {@link HederaKeyThreshold}
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {
	   	logger.trace("Start - JSON");

	   	JSONObject jsonKey = new JSONObject();
	   	jsonKey.put(JSON_THRESHOLD, this.threshold);

		JSONArray jsonKeys = new JSONArray();
		for (HederaKey hederaKey : this.keys) {
			jsonKeys.add(hederaKey.JSON());
		}
	   	
		jsonKey.put(JSON_KEYS, jsonKeys);
	   	
		logger.trace("End - JSON");
		
		return jsonKey;
	}
	/**
	 *  Gets a {@link String} representation of this {@link HederaKeyThreshold}'s JSON
	 * @return {@link String}
	 */
	public String JSONString() {
	   	logger.trace("Start - JSONString");
	   	logger.trace("End - JSONString");
		return JSON().toJSONString();
	}
	/**
	 * Populates values for this object from a {@link JSONObject}
	 * @param jsonKey the {@link JSONObject} to populate this object with
	 */
	public void fromJSON(JSONObject jsonKey) {
	   	logger.trace("Start - fromJSON");
		// delete all keys
		this.keys.clear();
		// add keys from json array
		
		if (jsonKey.containsKey(JSON_THRESHOLD)) {
			//long tempThreshold = (long) ;
			this.threshold = (int) jsonKey.get(JSON_THRESHOLD); //oIntExact(tempThreshold);
		} else {
			this.threshold = 0;
		}
		
		if (jsonKey.containsKey(JSON_KEYS)) {
			JSONArray keys = new JSONArray();
			keys = (JSONArray) jsonKey.get(JSON_KEYS);
			
			for (int i=0; i < keys.size(); i++) {
				HederaKey key = new HederaKey();
				JSONObject oneKey = new JSONObject();
				oneKey = (JSONObject) keys.get(i);
				key.fromJSON(oneKey);
				this.addKey(key);
			}
		}
	   	logger.trace("End - fromJSON");
	}
}
package com.hedera.sdk.common;

import com.hedera.sdk.common.HederaKeyPair;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.ThresholdKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
/**
 * A set of public keys that are used together to form a threshold signature. 
 * If the threshold is N and there are M keys, then this is an N of M threshold signature. 
 * If an account is associated with ThresholdKeys, then a transaction to move cryptocurrency out of it must be signed by a list of M signatures, 
 * where at most M-N of them are blank, and the other at least N of them are valid signatures corresponding to at least N of the public keys listed here.
 */
public class HederaKeyThreshold implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaKeyThreshold.class);
	private static String JSON_KEYS = "keys";
	private static String JSON_THRESHOLD = "threshold";
	private static final long serialVersionUID = 1;
	/**
	 * The desired threshold for these keys
	 */
	public int threshold;
	/**
	 * The List of {@link HederaKeyPair}
	 * initially empty 
	 */
	public List<HederaKeyPair> keys  = new ArrayList<HederaKeyPair>();
	/**
	 * Default constructor
	 */
	public HederaKeyThreshold() {


	}
	/**
	 * Constructs from a threshold and List of {@link HederaKeyPair}
	 * @param threshold the integer threshold value
	 * @param keys the List of {@link HederaKeyPair}
	 */
	public HederaKeyThreshold(int threshold, List<HederaKeyPair> keys) {


	   	if (keys != null) {
		   	for (HederaKeyPair hederaKey : keys) {
				this.keys.add(hederaKey);
			}
	   	}
		this.threshold = threshold;

	}
	/**
	 * Constructs from a {@link ThresholdKey} protobuf 
	 * @param protobufKey the keys
	 */
	public HederaKeyThreshold(ThresholdKey protobufKey) {


	   	// convert a protobuf payload into class data
		this.threshold = protobufKey.getThreshold();
		this.keys.clear();
		KeyList protoKeys = protobufKey.getKeys();
		
		for (Key key : protoKeys.getKeysList()) {
			this.keys.add(new HederaKeyPair(key));
		}

	}
	/**
	 * Gets the protobuf {@link ThresholdKey} for the key 
	 * @return {@link ThresholdKey}
	 */
	public ThresholdKey getProtobuf() {

		// Generates the protobuf payload for this class
		ThresholdKey.Builder keysProtobuf = ThresholdKey.newBuilder();
		keysProtobuf.setThreshold(this.threshold);
		
		KeyList protoKeyList;
		
		if (!this.keys.isEmpty()) {
			protoKeyList = Utilities.getProtoKeyList(this.keys);
			keysProtobuf.setKeys(protoKeyList);
		}
		else {

			return null;
		}

		return keysProtobuf.build();
	}
	/**
	 * Adds a key to the list
	 * @param key a {@link HederaKeyPair}
	 */
	public void addKey(HederaKeyPair key) {

		this.keys.add(key);

	}
	/**
	 * Deletes a key from the list
	 * @param key a {@link HederaKeyPair}
	 * @return true if found and successfully deleted
	 */
	public boolean deleteKey(HederaKeyPair key) {


		return this.keys.remove(key);
	}
	/**
	 *  Gets a {@link JSONObject} representation of this {@link HederaKeyThreshold}
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {


	   	JSONObject jsonKey = new JSONObject();
	   	jsonKey.put(JSON_THRESHOLD, this.threshold);

		JSONArray jsonKeys = new JSONArray();
		for (HederaKeyPair hederaKey : this.keys) {
			jsonKeys.add(hederaKey.JSON());
		}
	   	
		jsonKey.put(JSON_KEYS, jsonKeys);
	   	

		
		return jsonKey;
	}
	/**
	 *  Gets a {@link String} representation of this {@link HederaKeyThreshold}'s JSON
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
				HederaKeyPair key = new HederaKeyPair();
				JSONObject oneKey = new JSONObject();
				oneKey = (JSONObject) keys.get(i);
				key.fromJSON(oneKey);
				this.addKey(key);
			}
		}

	}
}
package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
/**
 * A HederaKeyList is a list of {@link HederaKey}
 */
public class HederaKeyList implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaKeyList.class);
	private static final long serialVersionUID = 1;

	/**
	 * an {@link ArrayList} of {@link HederaKey}, initially empty
	 */
	public List<HederaKey> keys  = new ArrayList<HederaKey>();
	
	/**
	 * Default constructor for the {@link HederaKeyList} with an empty set of {@link HederaKey}
	 */
	public HederaKeyList() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a {@link HederaKeyList} from a List of {@link HederaKey}
	 * @param keys a {@link List} of {@link HederaKey}
	 */
	public HederaKeyList(List<HederaKey> keys) {
	   	logger.trace("Start - Object init keyList {}", keys);
		
		for (HederaKey hederaKey : keys) {
			this.keys.add(hederaKey);
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from a protobuf {@link KeyList}
	 * @param protobuf the {@link KeyList} protobuf to construct this object from
	 */
	public HederaKeyList(KeyList protobuf) {
	   	logger.trace("Start - Object init protobuf");
		// convert a protobuf payload into class data
		this.keys.clear();
		
		for (Key key : protobuf.getKeysList()) {
			this.keys.add(new HederaKey(key));
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Returns a protobuf {@link KeyList} representation of this object
	 * @return {@link KeyList}
	 */
	public KeyList getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		KeyList.Builder keyListProtobuf = KeyList.newBuilder();
		for (HederaKey key : this.keys) {
			keyListProtobuf.addKeys(key.getProtobuf());
		}
	   	logger.trace("End - getProtobuf");
		
		return keyListProtobuf.build();
	}
	/**
	 * Adds a {@link HederaKey} to the list of keys
	 * @param keyType the type of key to add
	 * @param key the byte array of the key value
	 */
	public void addKey(HederaKey.KeyType keyType, byte[] key) {
	   	logger.trace("Start - addKey keyType {}, key {}", keyType, key);
		HederaKey hederaKey = new HederaKey(keyType, key);
		addKey(hederaKey);
	   	logger.trace("End - addKey");
	}
	/**
	 * Adds a {@link HederaKey} to the list of keys
	 * @param key a {@link HederaKey} to add to the list
	 */
	public void addKey(HederaKey key) {
	   	logger.trace("Start - addKey key {}", key);
		this.keys.add(key);
	   	logger.trace("End - addKey");
	}
	/**
	 * Deletes a {@link HederaKey} from the list
	 * @param key the {@link HederaKey} to remove
	 * @return boolean if successful
	 */
	public boolean deleteKey(HederaKey key) {
	   	logger.trace("deleteKey key {}", key);
		return this.keys.remove(key);
	}
	/** 
	 * Generates a {@link JSONArray} for the list of keys 
	 * @return {@link JSONArray}
	 */
	@SuppressWarnings("unchecked")
	public JSONArray JSON() {
	   	logger.trace("Start - JSON");
	   	
		JSONArray jsonKeys =new JSONArray();

		for (HederaKey hederaKey : this.keys) {
			jsonKeys.add(hederaKey.JSON());
		}
	   	
		logger.trace("End - JSON");
		
		return jsonKeys;
	}
	/**
	 * Generates a {@link String} representation of the key list in JSON
	 * @return {@link String}
	 */
	public String JSONString() {
	   	logger.trace("JSONString");
		return JSON().toJSONString();
	}
	/**
	 * Populates the list of keys from a {@link JSONArray}
	 * Note: This deletes any previously stored keys
	 * @param jsonKeys a {@link JSONArray} of keys
	 */
	public void fromJSON(JSONArray jsonKeys) {
	   	logger.trace("Start - fromJSON");
		
		// delete all keys
		this.keys.clear();
		// add keys from json array
		
		for (int i=0; i < jsonKeys.size(); i++) {
			JSONObject jsonKey = (JSONObject) jsonKeys.get(i);
			HederaKey key = new HederaKey();
			key.fromJSON(jsonKey);
			this.addKey(key);
		}
	   	logger.trace("End - fromJSON");
	}
}
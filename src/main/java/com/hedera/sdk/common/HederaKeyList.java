package com.hedera.sdk.common;

import java.io.Serializable;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
/**
 * A HederaKeyList is a list of {@link HederaKeyPair}
 */
public class HederaKeyList implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaKeyList.class);
	private static final long serialVersionUID = 1;

	/**
	 * an {@link ArrayList} of {@link HederaKeyPair}, initially empty
	 */
	public List<HederaKeyPair> keys  = new ArrayList<HederaKeyPair>();
	
	/**
	 * Default constructor for the {@link HederaKeyList} with an empty set of {@link HederaKeyPair}
	 */
	public HederaKeyList() {


	}
	/**
	 * Constructs a {@link HederaKeyList} from a List of {@link HederaKeyPair}
	 * @param keys a {@link List} of {@link HederaKeyPair}
	 */
	public HederaKeyList(List<HederaKeyPair> keys) {

		
		for (HederaKeyPair hederaKey : keys) {
			this.keys.add(hederaKey);
		}

	}
	/**
	 * Constructor from a protobuf {@link KeyList}
	 * @param protobuf the {@link KeyList} protobuf to construct this object from
	 */
	public HederaKeyList(KeyList protobuf) {

		// convert a protobuf payload into class data
		this.keys.clear();
		
		for (Key key : protobuf.getKeysList()) {
			this.keys.add(new HederaKeyPair(key));
		}

	}
	/**
	 * Returns a protobuf {@link KeyList} representation of this object
	 * @return {@link KeyList}
	 */
	public KeyList getProtobuf() {

		// Generates the protobuf payload for this class
		KeyList.Builder keyListProtobuf = KeyList.newBuilder();
		for (HederaKeyPair key : this.keys) {
			keyListProtobuf.addKeys(key.getProtobuf());
		}

		
		return keyListProtobuf.build();
	}
	/**
	 * Adds a {@link HederaKeyPair} to the list of keys
	 * @param keyType the type of key to add
	 * @param publicKey the byte array of the public key value
	 * @param privateKey the byte array of the private key value
	 * @throws InvalidKeySpecException if the keypair cannot be created
	 */
	public void addKey(HederaKeyPair.KeyType keyType, byte[] publicKey, byte[] privateKey) throws InvalidKeySpecException {

		HederaKeyPair hederaKey = new HederaKeyPair(keyType, publicKey, privateKey);
		addKey(hederaKey);

	}
	/**
	 * Adds a {@link HederaKeyPair} to the list of keys
	 * @param key a {@link HederaKeyPair} to add to the list
	 */
	public void addKey(HederaKeyPair key) {

		this.keys.add(key);

	}
	/**
	 * Deletes a {@link HederaKeyPair} from the list
	 * @param key the {@link HederaKeyPair} to remove
	 * @return boolean if successful
	 */
	public boolean deleteKey(HederaKeyPair key) {

		return this.keys.remove(key);
	}
	/** 
	 * Generates a {@link JSONArray} for the list of keys 
	 * @return {@link JSONArray}
	 */
	@SuppressWarnings("unchecked")
	public JSONArray JSON() {

	   	
		JSONArray jsonKeys =new JSONArray();

		for (HederaKeyPair hederaKey : this.keys) {
			jsonKeys.add(hederaKey.JSON());
		}
	   	
		return jsonKeys;
	}
	/**
	 * Generates a {@link String} representation of the key list in JSON
	 * @return {@link String}
	 */
	public String JSONString() {

		return JSON().toJSONString();
	}
	/**
	 * Populates the list of keys from a {@link JSONArray}
	 * Note: This deletes any previously stored keys
	 * @param jsonKeys a {@link JSONArray} of keys
	 */
	public void fromJSON(JSONArray jsonKeys) {

		
		// delete all keys
		this.keys.clear();
		// add keys from json array
		
		for (int i=0; i < jsonKeys.size(); i++) {
			JSONObject jsonKey = (JSONObject) jsonKeys.get(i);
			HederaKeyPair key = new HederaKeyPair();
			key.fromJSON(jsonKey);
			this.addKey(key);
		}

	}
}
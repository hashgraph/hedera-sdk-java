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
import com.hederahashgraph.api.proto.java.Signature;
import com.hederahashgraph.api.proto.java.SignatureList;
/**
 * A HederaKeySignatureList is a list of {@link HederaKeySignature}
 */

public class HederaKeySignatureList implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaKeySignatureList.class);
	private static final long serialVersionUID = 1;
	/**
	 * The list of key signature pairs {@link HederaKeySignature}
	 * default is empty
	 */
	public List<HederaKeySignature> keySigPairs  = new ArrayList<HederaKeySignature>();
	/**
	 * Default constructor
	 */
	public HederaKeySignatureList() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a list of {@link HederaKeySignature}
	 * @param keySigPairs the list of {@link HederaKeySignature}
	 */
	public HederaKeySignatureList(List<HederaKeySignature> keySigPairs) {
	   	logger.trace("Start - Object init keySigPairs {}", keySigPairs);
		for (HederaKeySignature keySigPair : keySigPairs) {
			this.keySigPairs.add(keySigPair);
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs from a {@link KeyList} protobuf and matching {@link SignatureList}
	 * Note: It is assumed the two protobuf structures are exact matches
	 * @param protobufKeys the protobuf for the keys
	 * @param protobufSignatures the protobuf for the signatures
	 */
	public HederaKeySignatureList(KeyList protobufKeys, SignatureList protobufSignatures) {
	   	logger.trace("Start - Object init protobufKeys {}, protobufSignatures {}", protobufKeys, protobufSignatures);
		// convert a protobuf payload into class data
		this.keySigPairs.clear();
		
		for (int index=0; index < protobufKeys.getKeysCount(); index++) {
			Key key = protobufKeys.getKeys(index);
			Signature sig = protobufSignatures.getSigs(index);
			this.keySigPairs.add(new HederaKeySignature(key, sig));
		}
	   	logger.trace("End - Object init");
	}
	/**
	 * Gets the protobuf for the keys in this list
	 * @return {@link KeyList}
	 */
	public KeyList getProtobufKeys() {
	   	logger.trace("Start - getProtobufKeys");
		// Generates the protobuf payload for this class
		KeyList.Builder keyListProtobuf = KeyList.newBuilder();
		for (HederaKeySignature key : this.keySigPairs) {
			keyListProtobuf.addKeys(key.getKeyProtobuf());
		}
	   	logger.trace("End - getProtobufKeys");
		
		return keyListProtobuf.build();
	}
	/**
	 * Gets the protobuf for the signatures in this list
	 * @return {@link SignatureList}
	 */
	public SignatureList getProtobufSignatures() {
	   	logger.trace("Start - getProtobufSignatures");
		// Generates the protobuf payload for this class
		SignatureList.Builder sigListProtobuf = SignatureList.newBuilder();
		for (HederaKeySignature sig : this.keySigPairs) {
			sigListProtobuf.addSigs(sig.getSignatureProtobuf());
		}
	   	logger.trace("End - getProtobufSignatures");
		
		return sigListProtobuf.build();
	}
	/**
	 * Adds a key/signature pair to the list
	 * @param keyType the type of key to add
	 * @param key the key as a byte array 
	 * @param signature the signature as a byte array 
	 */
	public void addKeySignaturePair(HederaKeyPair.KeyType keyType, byte[] key, byte[] signature) {
	   	logger.trace("Start - addKeySignaturePair keyType {}, key {}, signature {}", keyType, key, signature);
		HederaKeySignature hederaKeySigPair = new HederaKeySignature(keyType, key, signature);
		addKeySignaturePair(hederaKeySigPair);
	   	logger.trace("End - addKeySignaturePair");
	}
	/**
	 * Adds a {@link HederaKeySignature} to the list
	 * @param keySigPair a {@link HederaKeySignature} object
	 */
	public void addKeySignaturePair(HederaKeySignature keySigPair) {
	   	logger.trace("Start - addKey keySigPair {}", keySigPair);
		this.keySigPairs.add(keySigPair);
	   	logger.trace("End - addKey");
	}
	/**
	 * Deletes a matching {@link HederaKeySignature} from the list
	 * @param keySigPair the {@link HederaKeySignature} to remove
	 * @return true if successfully found and removed
	 */
	public boolean deleteKeySigPair(HederaKeySignature keySigPair) {
	   	logger.trace("deleteKeySigPair keySigPair {}", keySigPair);
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
	   	logger.trace("Start - setSignatureForKey key {}, signature {}, stopAtFirst {}", key, signature, stopAtFirst);
		boolean foundOne = false;

		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.setSignatureForKey(key, signature, stopAtFirst)) {
				foundOne = true;
				if (stopAtFirst) {
				   	logger.trace("End - setSignatureForKey");
					return true;
				}
			}
		}
	   	logger.trace("End - setSignatureForKey");
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
	   	logger.trace("Start - setSignatureForKeys keys {}, signatures {}, stopAtFirst {}", keys, signatures, stopAtFirst);
	   	boolean foundOne = false;

	   	for (int i=0; i < keys.length; i++) {
			if (setSignatureForKey(keys[i], signatures[i], stopAtFirst)) {
				foundOne = true;
			}
	   	}
	   	logger.trace("End - setSignatureForKeys");

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
	   	logger.trace("Start - setSignatureForKeyUUID uuid {}, signature {}", uuid, signature);

		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.setSignatureForKeyUUID(uuid, signature)) {
			   	logger.trace("End - setSignatureForKeyUUID");
				return true;
			}
		}
	   	logger.trace("End - setSignatureForKeyUUID");
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
	   	logger.trace("Start - setSignatureForKeyUUIDs uuids {}, signatures {}", uuids, signatures);
		boolean foundOne = false;

		for (int i=0; i < uuids.length; i++) {
			if (setSignatureForKeyUUID(uuids[i], signatures[i])) {
				foundOne = true;
			}
		}
	   	logger.trace("End - setSignatureForKeyUUIDs");
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
	   	logger.trace("Start - updateSignatureForKey key {}, signature {}", key, signature);
		boolean foundOne = false;

		for (HederaKeySignature keySigPair : keySigPairs) {
			if (keySigPair.updateSignatureForKey(key, signature)) {
				foundOne = true;
			}
		}
	   	logger.trace("End - updateSignatureForKey");
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
	   	logger.trace("Start - updateSignatureForKeys keys {}, signatures {}", keys, signatures);
		boolean foundOne = false;
		
		for (int i=0; i < keys.length; i++) {
			if (updateSignatureForKey(keys[i], signatures[i])) {
				foundOne = true;
			}
		}
	   	logger.trace("End - updateSignatureForKeys");
		
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
	   	logger.trace("Start - getKeyUUIDs hederaKeyUUIDDescriptions {}, publicKey {}", hederaKeyUUIDDescriptions, publicKey);

		for (HederaKeySignature keySigPair : keySigPairs) {
			keySigPair.getKeyUUIDs(hederaKeyUUIDDescriptions, publicKey);
		}
	   	logger.trace("End - getKeyUUIDs");
	}	

	/**
	 *  Gets a {@link JSONObject} representation of this {@link HederaKeySignatureList}
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONArray JSON() {
	   	logger.trace("Start - JSON");

		JSONArray jsonKeys = new JSONArray();
		for (HederaKeySignature hederaKeySignature : this.keySigPairs) {
			jsonKeys.add(hederaKeySignature.JSON());
		}

		logger.trace("End - JSON");
		
		return jsonKeys;
	}
	/**
	 *  Gets a {@link String} representation of the JSONObject this {@link HederaKeySignatureList}
	 * @return {@link String}
	 */
	public String JSONString() {
	   	logger.trace("Start - JSONString");
	   	logger.trace("End - JSONString");
		return JSON().toJSONString();
	}
	/**
	 * Populates values for this object from a {@link JSONArray}
	 * @param jsonKeys the {@link JSONArray} to populate this object with
	 */
	public void fromJSON(JSONArray jsonKeys) {
	   	logger.trace("Start - fromJSON");
		
		// delete all keys
		this.keySigPairs.clear();
		// add keys from json array
		
		for (int i=0; i < jsonKeys.size(); i++) {
			JSONObject jsonKey = (JSONObject) jsonKeys.get(i);
			HederaKeySignature key = new HederaKeySignature();
			key.fromJSON(jsonKey);
			this.addKeySignaturePair(key);
		}
	   	logger.trace("End - fromJSON");
	}
}
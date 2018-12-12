package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.protobuf.ByteString;
import com.hedera.sdk.common.HederaContractID;
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

/**
 * A Key can be a public key from one of the three supported systems (ed25519, RSA-3072, ECDSA with p384). 
 * Or, it can be the ID of a smart contract instance, which is authorized to act as if it had a key. 
 * If an account has an ed25519 key associated with it, then the corresponding private key must sign any transaction to transfer cryptocurrency out of it. 
 * And similarly for RSA and ECDSA.
 * A Key can be a smart contract ID, which means that smart contract is to authorize operations as if it had signed with a key that it owned. 
 * The smart contract doesn't actually have a key, and doesn't actually sign a transaction. 
 * But it's as if a virtual transaction were created, and the smart contract signed it with a private key.
 * A key can be a "threshold key", which means a list of M keys, any N of which must sign in order for the threshold signature to be considered valid. 
 * The keys within a threshold signature may themselves be threshold signatures, to allow complex signature requirements.
 * A Key can be a list of keys. Their use is dependent on context. 
 * For example, a Hedera file is created with a list of keys, where all of them must sign a transaction to create or modify the file, 
 * but only one of them is needed to sign a transaction to delete the file. So it's a single list that sometimes acts as a 1-of-M threshold key, 
 * and sometimes acts as an M-of-M threshold key.
 * A Key can contain a ThresholdKey or KeyList, which in turn contain a Key, so this mutual recursion would allow nesting arbitrarily deep. 
 * The current API only allows the nesting to a depth of 3 levels, such as the key being a list of threshold keys, 
 * each of which contains a list of primitive keys (e.g., ed25519). 
 * In the future, this requirement may be relaxed, to allow deeper nesting.
 */
public class HederaKey implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaKey.class);
	private static String JSON_DESCRIPTION = "description";
	private static String JSON_UUID = "uuid";
	private static String JSON_TYPE = "type";
	private static String JSON_KEY = "key";
	private static String JSON_KEYS = "keys";
	private static final long serialVersionUID = 1;
	private byte[] key = null;
	private KeyType keyType = KeyType.NOTSET;
	private HederaContractID contractIDKey = null;
	private HederaKeyThreshold thresholdKey = null;
	private HederaKeyList keyList = null;
	private ResponseCodeEnum precheckResult = ResponseCodeEnum.UNKNOWN;
	private long cost = 0;
	private byte[] stateProof = new byte[0];
	private HederaNode node = new HederaNode();

	/**
	 * The type of key held in this object
	 */
	public enum KeyType {
	    CONTRACT,
	    ED25519,
	    RSA3072,
	    ECDSA384,
	    THRESHOLD, 
	    LIST,
	    NOTSET
	}
	/**
	 * A description for the key
	 */
	public String keyDescription = "";

	/**
	 * A UUID for the key, automatically set when a key object is created, can be overwritten
	 */
	public String uuid = UUID.randomUUID().toString();

	/**
	 * List of HederaEntityIDs returned when a query is run
	 * for this key
	 */
	public List<HederaEntityID> entityIDs = new ArrayList<HederaEntityID>();

	/**
	 * Method to set the node object to be used to communicate
	 * with a node
	 * @param node {@link HederaNode} the node for communication
	 */
	public void setNode (HederaNode node) {
		this.node = node;
	}
	/**
	 * Method to get the node object to be used to communicate
	 * with a node
	 * @return {@link HederaNode} the node for communication
	 */
	public HederaNode getNode () {
		return this.node;
	}
	/**
	 * Returns the cost of running a query in relation to this key
	 * @return long
	 */
	public long getCost() {
		return this.cost;
	}
	/**
	 * Returns the stateProof if requested during a query in relation to this key
	 * @return byte array (byte[])
	 */
	public byte[] getStateProof() {
		return this.stateProof;
	}
	/**
	 * Default constructor
	 */
	public HederaKey() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from type, key bytes and description
	 * @param keyType the type of key
	 * @param key a byte array containing the value of the key
	 * @param keyDescription a description for the key
	 */
	public HederaKey(KeyType keyType, byte[] key, String keyDescription) {
	   	logger.trace("Start - Object init key {}, type {}, description", key, keyType, keyDescription);
		this.key = key.clone();
		this.keyType = keyType;
		this.keyDescription = keyDescription;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from type, key bytes
	 * @param keyType the type of key
	 * @param key a byte array containing the value of the key
	 */
	public HederaKey(KeyType keyType, byte[] key) {
		this(keyType, key, "");
	}
	/**
	 * Constructs a HederaKey from a HederaContractID and description
	 * @param contractKey the HederaContractID used for this key
	 * @param keyDescription the description of the key
	 */
	public HederaKey(HederaContractID contractKey, String keyDescription) {
	   	logger.trace("Start - Object init key {}, type CONTRACT, description {}", contractKey, keyDescription);
		this.keyType = KeyType.CONTRACT;
		this.contractIDKey = contractKey;
		this.keyDescription = keyDescription;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from a HederaContractID
	 * @param contractKey the HederaContractID used for this key
	 */
	public HederaKey(HederaContractID contractKey) {
		this(contractKey,"");
	}
	/**
	 * Constructs a HederaKey from a HederaKeyThreshold and description
	 * @param thresholdKey the HederaContractID used for this key
	 * @param keyDescription the description for the key
	 */
	public HederaKey(HederaKeyThreshold thresholdKey, String keyDescription) {
	   	logger.trace("Start - Object init key {}, type THRESHOLD, description {}", thresholdKey, keyDescription);
		this.keyType = KeyType.THRESHOLD;
		this.thresholdKey = thresholdKey;
		this.keyDescription = keyDescription;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from a HederaKeyThreshold
	 * @param thresholdKey the HederaContractID used for this key
	 */
	public HederaKey(HederaKeyThreshold thresholdKey) {
		this(thresholdKey,"");
	}
	/**
	 * Constructs a HederaKey from a HederaKeyList and description
	 * @param keyList the HederaKeyList to use for this key
	 * @param keyDescription the description for the key
	 */
	public HederaKey(HederaKeyList keyList, String keyDescription) {
	   	logger.trace("Start - Object init key {}, type LIST, description {}", keyList, keyDescription);
		this.keyType = KeyType.LIST;
		this.keyList = keyList;
		this.keyDescription = keyDescription;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from a HederaKeyList
	 * @param keyList the HederaKeyList to use for this key
	 */
	public HederaKey(HederaKeyList keyList) {
		this(keyList,"");
	}
	/**
	 * Constructs a HederaKey from a protobuf and description
	 * @param protobuf the protobuf to build the key with
	 * @param keyDescription the description for the key
	 */
	public HederaKey(Key protobuf, String keyDescription) {
		// convert a protobuf payload into class data
	   	logger.trace("Start - Object init key from protobuf {}, description {}", protobuf, keyDescription);
		// reset  key just in case
		this.thresholdKey = null;
		this.contractIDKey = null;
		this.keyList = null;
		this.key = null;

		switch (protobuf.getKeyCase()) {
		case ED25519:
			this.key = protobuf.getEd25519().toByteArray();
//			this.key = Hex.decode(this.key);
			this.keyType = KeyType.ED25519;
			break;
		case RSA_3072:
			this.key = protobuf.getRSA3072().toByteArray();
			this.keyType = KeyType.RSA3072;
			break;
		case ECDSA_384:
			this.key = protobuf.getECDSA384().toByteArray();
			this.keyType = KeyType.ECDSA384;
			break;
		case CONTRACTID:
			this.contractIDKey = new HederaContractID(protobuf.getContractID());
			this.keyType = KeyType.CONTRACT;
			break;
		case THRESHOLDKEY:
			this.thresholdKey = new HederaKeyThreshold(protobuf.getThresholdKey());
			this.keyType = KeyType.THRESHOLD;
			break;
		case KEYLIST:
			this.keyList = new HederaKeyList(protobuf.getKeyList());
			this.keyType = KeyType.LIST;
			break;
		case KEY_NOT_SET:
			this.keyType = KeyType.NOTSET;
            throw new IllegalArgumentException("Key not set in protobuf data.");			
		default:
            throw new IllegalArgumentException("Key Type not recognized. You may be using an old sdk.");			
		}
		this.keyDescription = keyDescription;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructs a HederaKey from a protobuf 
	 * @param protobuf the protobuf to build the key with
	 */
	public HederaKey(Key protobuf) {
		this(protobuf,"");
	}
	/**
	 * Gets the type of key for this object
	 * @return KeyType
	 */
	public KeyType getKeyType() {
	
	   	logger.trace("getKeyType");
		return this.keyType;
	}
	/**
	 * Gets the key value as a byte array
	 * value will be null if not set
	 * @return byte array (byte[])
	 */
	public byte[] getKey() {
		logger.trace("getKey");
		return this.key;
	}
	/**
	 * Gets the contractID stored in this key
	 * return will be null if not set
	 * @return {@link HederaContractID}
	 */
	public HederaContractID getContractIDKey() {
	   	logger.trace("getContractIDKey");
		return this.contractIDKey;
	}
	/**
	 * Gets the HederaKeyThreshold stored in this key
	 * return will be null if not set
	 * @return {@link HederaKeyThreshold}
	 */
	public HederaKeyThreshold getThresholdKey() {
	   	logger.trace("getThresholdKey");
		return this.thresholdKey;
	}
	/**
	 * Gets the {@link HederaKeyList} stored in this key
	 * return will be null if not set
	 * @return {@link HederaKeyList}
	 */
	public HederaKeyList getKeyList() {
	   	logger.trace("getKeyList");
		return this.keyList;
	}
	/** 
	 * Gets the protobuf representation of this key object 
	 * @return {@link Key} protobuf
	 */
	public Key getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		// Generates the protobuf payload for this class
		Key.Builder keyProtobuf = Key.newBuilder();
		
		switch (this.keyType) {
		case ED25519:
			if (this.key != null) {
				keyProtobuf.setEd25519(ByteString.copyFrom(this.key));
			}
			break;
		case RSA3072:
			if (this.key != null) {
				keyProtobuf.setRSA3072(ByteString.copyFrom(this.key));
			}
			break;
		case ECDSA384:
			if (this.key != null) {
				keyProtobuf.setECDSA384(ByteString.copyFrom(this.key));
			}
			break;
		case CONTRACT:
			if (this.contractIDKey != null) {
				keyProtobuf.setContractID(this.contractIDKey.getProtobuf());
			}
			break;
		case THRESHOLD:
			if (this.thresholdKey != null) {
				keyProtobuf.setThresholdKey(this.thresholdKey.getProtobuf());
			}
			break;
		case LIST:
			if (this.keyList != null) {
				keyProtobuf.setKeyList(this.keyList.getProtobuf());
			}
			break;
		case NOTSET:
            throw new IllegalArgumentException("Key type not set, unable to generate data.");			
		}
		
	   	logger.trace("End - getProtobuf");
		return keyProtobuf.build();
	}
	
	/** 
	 * Generates a {@link JSONObject} representation of this key object
	 * @return {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject JSON() {
	   	logger.trace("Start - JSON");

	   	JSONObject jsonKey = new JSONObject();
	   	jsonKey.put(JSON_DESCRIPTION, this.keyDescription);
	   	jsonKey.put(JSON_UUID, this.uuid);

		switch (this.keyType) {
		case CONTRACT:
			jsonKey.put(JSON_TYPE, "CONTRACT");
			jsonKey.put(JSON_KEY, this.contractIDKey.JSON());
			break;
		case ECDSA384:
			jsonKey.put(JSON_TYPE, "ECDSA384");
			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.key));
			break;
		case ED25519:
			jsonKey.put(JSON_TYPE, "ED25519");
			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.key));
			break;
		case LIST:
			jsonKey.put(JSON_TYPE, "KEYLIST");
			jsonKey.put(JSON_KEYS, this.keyList.JSON());
			break;
		case RSA3072:
			jsonKey.put(JSON_TYPE, "RSA3072");
			jsonKey.put(JSON_KEY,DatatypeConverter.printBase64Binary(this.key));
			break;
		case THRESHOLD:
			jsonKey.put(JSON_TYPE, "THRESHOLD");
			jsonKey.put(JSON_KEY, this.thresholdKey.JSON());
			break;
		case NOTSET:
			jsonKey.put(JSON_TYPE, "NOTSET");
			break;
		}
	   	logger.trace("End - JSON");
		
		return jsonKey;
	}
	/**
	 * Generates a {@link String} value for the JSON representation of this key object
	 * @return {@link String}
	 */
	public String JSONString() {
	   	logger.trace("Start - JSONString");
	   	logger.trace("End - JSONString");
		return JSON().toJSONString();
	}
	/**
	 * Sets this key object properties from a {@link JSONObject} representation
	 * @param jsonKey the {@link JSONObject} from which to set key values
	 */
	public void fromJSON(JSONObject jsonKey) {
	   	logger.trace("Start - fromJSON");
		
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
			this.thresholdKey = null;
			this.contractIDKey = null;
			this.keyList = null;
			this.key = null;
			
			JSONObject oneKey = new JSONObject();
			
			switch ((String) jsonKey.get(JSON_TYPE)) {
			case  "CONTRACT":
				this.keyType = KeyType.CONTRACT;
				oneKey = (JSONObject) jsonKey.get(JSON_KEY);
				this.contractIDKey = new HederaContractID();
				contractIDKey.fromJSON(oneKey);
				break;
			case "ECDSA384":
				this.keyType = KeyType.ECDSA384;
				this.key = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
				break;
			case "ED25519":
				this.keyType = KeyType.ED25519;
				this.key = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
				break;
			case "KEYLIST":
				this.keyType = KeyType.LIST;
				JSONArray listOfKeys = new JSONArray();
				listOfKeys = (JSONArray) jsonKey.get(JSON_KEYS);
				this.keyList = new HederaKeyList();
				this.keyList.fromJSON(listOfKeys);
				break;
			case "RSA3072":
				this.keyType = KeyType.RSA3072;
				this.key = DatatypeConverter.parseBase64Binary((String) jsonKey.get(JSON_KEY));
				break;
			case "THRESHOLD":
				this.keyType = KeyType.THRESHOLD;
				oneKey = (JSONObject) jsonKey.get(JSON_KEY);
				this.thresholdKey = new HederaKeyThreshold();
				this.thresholdKey.fromJSON(oneKey);
				break;
			case "NOTSET":
				this.keyType = KeyType.NOTSET;
				break;
			}
		} else {
			throw new IllegalStateException("Key type isn't set in JSON.");
		}
	   	logger.trace("End - fromJSON");
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
		
	   	logger.trace("Start - getEntities payment {}, responseType {}", payment, responseType);
		// build the query
	   	// Header
		HederaQueryHeader queryHeader = new HederaQueryHeader();
		if (payment != null) {
			queryHeader.payment = payment;
			queryHeader.responseType = responseType;
		}
		
		// get by key query
		GetByKeyQuery.Builder getByKeyQuery = GetByKeyQuery.newBuilder();
		getByKeyQuery.setKey(this.getProtobuf());
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
		
	   	logger.trace("End - getEntities");
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
	   	logger.trace("Start - getEntitiesAnswerOnly");
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
	   	logger.trace("getEntitiesStateProof");
		return getEntities(payment, HederaQueryHeader.QueryResponseType.ANSWER_STATE_PROOF);
	}
	/**
	 * Gets the cost of running a query to get entities with only an answer
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesCostAnswer() throws InterruptedException {
	   	logger.trace("getEntitiesCostAnswer");
		return getEntities(null, HederaQueryHeader.QueryResponseType.COST_ANSWER);
	}
	/**
	 * Gets the cost of running a query to get entities with a state proof
	 * If successful, the method populates the cost for this object
	 * @return {@link Boolean} indicating if query was successful or not
	 * @throws InterruptedException should a communication error occur with the node
	 */
	public boolean getEntitiesCostAnswerStateProof() throws InterruptedException {
	   	logger.trace("getEntitiesCostAnswerStateProof");
		return getEntities(null, HederaQueryHeader.QueryResponseType.COST_ANSWER_STATE_PROOF);
	}
}
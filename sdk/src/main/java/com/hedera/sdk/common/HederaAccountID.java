package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hederahashgraph.api.proto.java.AccountID;

/**
 * The ID for a cryptocurrency account, it is composed
 * of a shard number, a realm number and an account number
 */
public class HederaAccountID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaAccountID.class);

	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;

	/**
	 * the realm number
	 * note: if set to -1, in future versions it will be automatically assigned by the Hedera network
	 */
	public long realmNum = 0;

	/**
	 * the account number (nonnegative and unique within its realm)
	 */
	public long accountNum = 1;
	
	/**
	 * Default constructor, creates a HederaAccountID with default values
	 */
	public HederaAccountID() {
	}

	/**
	 * Constructor for a HederaAccountID from a string
	 * @param accountID a colon or dot separated list of shard, realm and account numbers (e.g. 0:0:1020)
	 */
	public HederaAccountID(String accountID) {
		if ((!accountID.contains(":")) && (!accountID.contains("."))) {
			throw new IllegalArgumentException("accountID must contain '.' or ':' to separate values");
		}
		String[] IDs; 
		if (accountID.contains(":")) {
			IDs = accountID.split(":");
		} else {
			IDs = accountID.split("\\.");
		}

		if (IDs.length !=3) {
			throw new IllegalArgumentException("accountID must contain 3 values for shardNum, realmNum and accountNum");
		}
		long realmNum;
		try {
			realmNum = Long.parseLong(IDs[0]);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("realmNum must be numeric");
		}		
		long shardNum;
		try {
			shardNum = Long.parseLong(IDs[1]);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("shardNum must be numeric");
		}		
		long accountNum;
		try {
			accountNum = Long.parseLong(IDs[2]);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("accountNum must be numeric");
		}		
 		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
	}
	/**
	 * Constructor for a HederaAccountID from specified parameter values
	 * shard and realm are defaulted to 0
	 * @param accountNum the account number (unique within its realm)
	 */
	public HederaAccountID(long accountNum) {
 		this.shardNum = 0;
		this.realmNum = 0;
		this.accountNum = accountNum;
	}
	/**
	 * Constructor for a HederaAccountID from specified parameter values
	 * @param shardNum the shard number
	 * @param realmNum the realm number
	 * @param accountNum the account number (unique within its realm)
	 */
	public HederaAccountID(long shardNum, long realmNum, long accountNum) {
 		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
	}

	/**
	 * Constructor for a HederaAccountID from protobuf
	 * @param accountIDProtobuf the protobuf from which to create the account ID
	 */
	public HederaAccountID(AccountID accountIDProtobuf) {
		this.shardNum = accountIDProtobuf.getShardNum();
		this.realmNum = accountIDProtobuf.getRealmNum();
		this.accountNum = accountIDProtobuf.getAccountNum();
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf AccountID 
	 */
	public AccountID getProtobuf() {
		AccountID.Builder accountID = AccountID.newBuilder();
		
		if (this.realmNum != -1) {
			accountID.setRealmNum(this.realmNum);
		}
		accountID.setShardNum(this.shardNum);
		accountID.setAccountNum(this.accountNum);

		return accountID.build();
	}
	/**
	 * Generate a string for this object
	 * @return a string 
	 */
	public String toString(){ 
		return this.shardNum + "." + this.realmNum + "." + this.accountNum;  
	} 
}
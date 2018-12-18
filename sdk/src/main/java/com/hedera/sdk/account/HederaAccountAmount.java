package com.hedera.sdk.account;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaAccountID;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.AccountID;

/**
 * An account, and the amount that it sends or receives during a cryptocurrency transfer.
 */

public class HederaAccountAmount implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaAccountAmount.class);
	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;
	/**
	 * the realm number (nonnegative)
	 */
	public long realmNum = 0;
	/**
	 * a nonnegative number unique within its realm
	 */
	public long accountNum = 0;
	/**
	 * the amount to transfer or receive
	 */
	public long amount = 0;
	/**
	 * Default constructor
	 */
	public HederaAccountAmount() {
	}
	/**
	 * Constructor from shard, realm, account numbers and amount
	 * @param shardNum the shard number for the account amounts
	 * @param realmNum the realm number for the account amounts
	 * @param accountNum the account number for the account amounts
	 * @param amount the amount to transfer
	 */
	public HederaAccountAmount(long shardNum, long realmNum, long accountNum, long amount) {
 		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.accountNum = accountNum;
		this.amount = amount;
		
	}
	/**
	 * Constructor from HederaAccountID and amount
	 * @param accountID the accountID
	 * @param amount the amount
	 */
	public HederaAccountAmount(HederaAccountID accountID, long amount) {
 		this.shardNum = accountID.shardNum;
		this.realmNum = accountID.realmNum;
		this.accountNum = accountID.accountNum;
		this.amount = amount;
		
	}
	/**
	 * Construct from a {@link AccountAmount} protobuf stream
	 * @param accountAmount the account amount
	 */
	public HederaAccountAmount(AccountAmount accountAmount) {
		this.shardNum = accountAmount.getAccountID().getShardNum();
		this.realmNum = accountAmount.getAccountID().getRealmNum();
		this.accountNum = accountAmount.getAccountID().getAccountNum();
		this.amount = accountAmount.getAmount();
	}

	/**
	 * Generate a {@link AccountAmount} protobuf payload for this object 
	 * @return {@link AccountAmount}
	 */
	public AccountAmount getProtobuf() {
		
	   	AccountAmount.Builder accountAmount = AccountAmount.newBuilder();
		AccountID.Builder accountID = AccountID.newBuilder();
		
		accountID.setAccountNum(this.accountNum);
		accountID.setRealmNum(this.realmNum);
		accountID.setShardNum(this.shardNum);
		
		accountAmount.setAccountID(accountID);
		accountAmount.setAmount(this.amount);
	   	
		return accountAmount.build();
	}
}
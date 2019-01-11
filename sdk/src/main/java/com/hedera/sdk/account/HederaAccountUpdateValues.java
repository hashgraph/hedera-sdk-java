package com.hedera.sdk.account;

import com.hedera.sdk.common.HederaKeyPair;
import org.slf4j.LoggerFactory;
/**
 * This class enables you to easily identify which parameters can be updated on an account
 * and send to an account.update method
 */
public class HederaAccountUpdateValues {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaAccountUpdateValues.class);
	/**
	 * the details of the new public/private key if an update is required
	 */
	public HederaKeyPair newKey = null;
	/**
	 * the shard number of the account to proxy to
	 * note: you must also supply the new realm and account numbers
	 */
	public long proxyAccountShardNum = -1;
	/**
	 * the realm number of the account to proxy to
	 * note: you must also supply the new shard and account numbers
	 */
	public long proxyAccountRealmNum = -1;
	/**
	 * the account number of the account to proxy to
	 * note: you must also supply the new shard and realm numbers
	 */
	public long proxyAccountAccountNum = -1;
	/**
	 * the new proxy Fraction
	 */
	public int proxyFraction = -1;
	/**
	 * the new send record threshold
	 */
	public long sendRecordThreshold = -1;
	/**
	 * the new receive record threshold
	 */
	public long receiveRecordThreshold = -1;
	/**
	 * the new auto renew period seconds
	 * note: you must also supply the new renew perios nanos
	 */
	public long autoRenewPeriodSeconds = -1;
	/**
	 * the new auto renew period nanos
	 * note: you must also supply the new renew perios seconds
	 */
	public int autoRenewPeriosNanos = -1;
	/**
	 * the new expiration time seconds
	 * note: you must also supply the new expiration time nanos
	 */
	public long expirationTimeSeconds = -1;
	/**
	 * the new expiration time nanos
	 * note: you must also supply the new expiration time seconds
	 */
	public int expirationTimeNanos = -1;
}

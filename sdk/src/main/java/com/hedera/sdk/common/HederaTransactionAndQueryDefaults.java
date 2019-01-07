package com.hedera.sdk.common;

import org.slf4j.LoggerFactory;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.node.HederaNode;

public class HederaTransactionAndQueryDefaults {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTransactionAndQueryDefaults.class);
	// set transport
	public HederaNode node = new HederaNode();
	public HederaAccountID payingAccountID = new HederaAccountID();
	public HederaDuration transactionValidDuration = new HederaDuration();
	public boolean generateRecord = false;
	public String memo = "";
	public HederaKeyPair payingKeyPair = null;
	public HederaKeyPair fileWacl = null;
}

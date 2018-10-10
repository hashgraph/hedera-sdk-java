package com.hedera.sdk.common;

import java.io.Serializable;
/**
 * The consensus result for a transaction, which might not be currently known, or may succeed or fail.
 */
public enum HederaTransactionStatus implements Serializable {
	UNKNOWN
	,SUCCESS
	,FAIL_INVALID
	,FAIL_FEE
	,FAIL_BALANCE
	,NOTSET
}

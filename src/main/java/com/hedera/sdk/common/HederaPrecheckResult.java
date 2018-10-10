package com.hedera.sdk.common;

import java.io.Serializable;
/**
 * SDK definitions of precheck values
 *
 */
public enum HederaPrecheckResult implements Serializable {
	OK
	,INVALID_TRANSACTION
	,INVALID_ACCOUNT
	,INSUFFICIENT_FEE
	,INSUFFICIENT_BALANCE
	,DUPLICATE
	,UNRECOGNIZED
	,NOTSET
	,ERROR
}

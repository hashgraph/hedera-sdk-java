/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

/**
 * Returned in {@link TransactionReceipt}, {@link PrecheckStatusException}
 * and {@link ReceiptStatusException}.
 * <p>
 * The success variant is {@link #SUCCESS} which is what a {@link TransactionReceipt} will contain for a
 * successful transaction.
 */
public enum Status {
    /**
     * The transaction passed the precheck validations.
     */
    OK(ResponseCodeEnum.OK),

    /**
     * For any error not handled by specific error codes listed below.
     */
    INVALID_TRANSACTION(ResponseCodeEnum.INVALID_TRANSACTION),

    /**
     * Payer account does not exist.
     */
    PAYER_ACCOUNT_NOT_FOUND(ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND),

    /**
     * Node Account provided does not match the node account of the node the transaction was submitted
     * to.
     */
    INVALID_NODE_ACCOUNT(ResponseCodeEnum.INVALID_NODE_ACCOUNT),

    /**
     * Pre-Check error when TransactionValidStart + transactionValidDuration is less than current
     * consensus time.
     */
    TRANSACTION_EXPIRED(ResponseCodeEnum.TRANSACTION_EXPIRED),

    /**
     * Transaction start time is greater than current consensus time
     */
    INVALID_TRANSACTION_START(ResponseCodeEnum.INVALID_TRANSACTION_START),

    /**
     * The given transactionValidDuration was either non-positive, or greater than the maximum
     * valid duration of 180 secs.
     */
    INVALID_TRANSACTION_DURATION(ResponseCodeEnum.INVALID_TRANSACTION_DURATION),

    /**
     * The transaction signature is not valid
     */
    INVALID_SIGNATURE(ResponseCodeEnum.INVALID_SIGNATURE),

    /**
     * Transaction memo size exceeded 100 bytes
     */
    MEMO_TOO_LONG(ResponseCodeEnum.MEMO_TOO_LONG),

    /**
     * The fee provided in the transaction is insufficient for this type of transaction
     */
    INSUFFICIENT_TX_FEE(ResponseCodeEnum.INSUFFICIENT_TX_FEE),

    /**
     * The payer account has insufficient cryptocurrency to pay the transaction fee
     */
    INSUFFICIENT_PAYER_BALANCE(ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE),

    /**
     * This transaction ID is a duplicate of one that was submitted to this node or reached consensus
     * in the last 180 seconds (receipt period)
     */
    DUPLICATE_TRANSACTION(ResponseCodeEnum.DUPLICATE_TRANSACTION),

    /**
     * If API is throttled out
     */
    BUSY(ResponseCodeEnum.BUSY),

    /**
     * The API is not currently supported
     */
    NOT_SUPPORTED(ResponseCodeEnum.NOT_SUPPORTED),

    /**
     * The file id is invalid or does not exist
     */
    INVALID_FILE_ID(ResponseCodeEnum.INVALID_FILE_ID),

    /**
     * The account id is invalid or does not exist
     */
    INVALID_ACCOUNT_ID(ResponseCodeEnum.INVALID_ACCOUNT_ID),

    /**
     * The contract id is invalid or does not exist
     */
    INVALID_CONTRACT_ID(ResponseCodeEnum.INVALID_CONTRACT_ID),

    /**
     * Transaction id is not valid
     */
    INVALID_TRANSACTION_ID(ResponseCodeEnum.INVALID_TRANSACTION_ID),

    /**
     * Receipt for given transaction id does not exist
     */
    RECEIPT_NOT_FOUND(ResponseCodeEnum.RECEIPT_NOT_FOUND),

    /**
     * Record for given transaction id does not exist
     */
    RECORD_NOT_FOUND(ResponseCodeEnum.RECORD_NOT_FOUND),

    /**
     * The solidity id is invalid or entity with this solidity id does not exist
     */
    INVALID_SOLIDITY_ID(ResponseCodeEnum.INVALID_SOLIDITY_ID),

    /**
     * The responding node has submitted the transaction to the network. Its final status is still
     * unknown.
     */
    UNKNOWN(ResponseCodeEnum.UNKNOWN),

    /**
     * The transaction succeeded
     */
    SUCCESS(ResponseCodeEnum.SUCCESS),

    /**
     * There was a system error and the transaction failed because of invalid request parameters.
     */
    FAIL_INVALID(ResponseCodeEnum.FAIL_INVALID),

    /**
     * There was a system error while performing fee calculation, reserved for future.
     */
    FAIL_FEE(ResponseCodeEnum.FAIL_FEE),

    /**
     * There was a system error while performing balance checks, reserved for future.
     */
    FAIL_BALANCE(ResponseCodeEnum.FAIL_BALANCE),

    /**
     * Key not provided in the transaction body
     */
    KEY_REQUIRED(ResponseCodeEnum.KEY_REQUIRED),

    /**
     * Unsupported algorithm/encoding used for keys in the transaction
     */
    BAD_ENCODING(ResponseCodeEnum.BAD_ENCODING),

    /**
     * When the account balance is not sufficient for the transfer
     */
    INSUFFICIENT_ACCOUNT_BALANCE(ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE),

    /**
     * During an update transaction when the system is not able to find the Users Solidity address
     */
    INVALID_SOLIDITY_ADDRESS(ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS),

    /**
     * Not enough gas was supplied to execute transaction
     */
    INSUFFICIENT_GAS(ResponseCodeEnum.INSUFFICIENT_GAS),

    /**
     * contract byte code size is over the limit
     */
    CONTRACT_SIZE_LIMIT_EXCEEDED(ResponseCodeEnum.CONTRACT_SIZE_LIMIT_EXCEEDED),

    /**
     * local execution (query) is requested for a function which changes state
     */
    LOCAL_CALL_MODIFICATION_EXCEPTION(ResponseCodeEnum.LOCAL_CALL_MODIFICATION_EXCEPTION),

    /**
     * Contract REVERT OPCODE executed
     */
    CONTRACT_REVERT_EXECUTED(ResponseCodeEnum.CONTRACT_REVERT_EXECUTED),

    /**
     * For any contract execution related error not handled by specific error codes listed above.
     */
    CONTRACT_EXECUTION_EXCEPTION(ResponseCodeEnum.CONTRACT_EXECUTION_EXCEPTION),

    /**
     * In Query validation, account with +ve(amount) value should be Receiving node account, the
     * receiver account should be only one account in the list
     */
    INVALID_RECEIVING_NODE_ACCOUNT(ResponseCodeEnum.INVALID_RECEIVING_NODE_ACCOUNT),

    /**
     * Header is missing in Query request
     */
    MISSING_QUERY_HEADER(ResponseCodeEnum.MISSING_QUERY_HEADER),

    /**
     * The update of the account failed
     */
    ACCOUNT_UPDATE_FAILED(ResponseCodeEnum.ACCOUNT_UPDATE_FAILED),

    /**
     * Provided key encoding was not supported by the system
     */
    INVALID_KEY_ENCODING(ResponseCodeEnum.INVALID_KEY_ENCODING),

    /**
     * null solidity address
     */
    NULL_SOLIDITY_ADDRESS(ResponseCodeEnum.NULL_SOLIDITY_ADDRESS),

    /**
     * update of the contract failed
     */
    CONTRACT_UPDATE_FAILED(ResponseCodeEnum.CONTRACT_UPDATE_FAILED),

    /**
     * the query header is invalid
     */
    INVALID_QUERY_HEADER(ResponseCodeEnum.INVALID_QUERY_HEADER),

    /**
     * Invalid fee submitted
     */
    INVALID_FEE_SUBMITTED(ResponseCodeEnum.INVALID_FEE_SUBMITTED),

    /**
     * Payer signature is invalid
     */
    INVALID_PAYER_SIGNATURE(ResponseCodeEnum.INVALID_PAYER_SIGNATURE),

    /**
     * The keys were not provided in the request.
     */
    KEY_NOT_PROVIDED(ResponseCodeEnum.KEY_NOT_PROVIDED),

    /**
     * Expiration time provided in the transaction was invalid.
     */
    INVALID_EXPIRATION_TIME(ResponseCodeEnum.INVALID_EXPIRATION_TIME),

    /**
     * WriteAccess Control Keys are not provided for the file
     */
    NO_WACL_KEY(ResponseCodeEnum.NO_WACL_KEY),

    /**
     * The contents of file are provided as empty.
     */
    FILE_CONTENT_EMPTY(ResponseCodeEnum.FILE_CONTENT_EMPTY),

    /**
     * The crypto transfer credit and debit do not sum equal to 0
     */
    INVALID_ACCOUNT_AMOUNTS(ResponseCodeEnum.INVALID_ACCOUNT_AMOUNTS),

    /**
     * Transaction body provided is empty
     */
    EMPTY_TRANSACTION_BODY(ResponseCodeEnum.EMPTY_TRANSACTION_BODY),

    /**
     * Invalid transaction body provided
     */
    INVALID_TRANSACTION_BODY(ResponseCodeEnum.INVALID_TRANSACTION_BODY),

    /**
     * the type of key (base ed25519 key, KeyList, or ThresholdKey) does not match the type of
     * signature (base ed25519 signature, SignatureList, or ThresholdKeySignature)
     */
    INVALID_SIGNATURE_TYPE_MISMATCHING_KEY(ResponseCodeEnum.INVALID_SIGNATURE_TYPE_MISMATCHING_KEY),

    /**
     * the number of key (KeyList, or ThresholdKey) does not match that of signature (SignatureList,
     * or ThresholdKeySignature). e.g. if a keyList has 3 base keys, then the corresponding
     * signatureList should also have 3 base signatures.
     */
    INVALID_SIGNATURE_COUNT_MISMATCHING_KEY(ResponseCodeEnum.INVALID_SIGNATURE_COUNT_MISMATCHING_KEY),

    /**
     * the livehash body is empty
     */
    EMPTY_LIVE_HASH_BODY(ResponseCodeEnum.EMPTY_LIVE_HASH_BODY),

    /**
     * the livehash data is missing
     */
    EMPTY_LIVE_HASH(ResponseCodeEnum.EMPTY_LIVE_HASH),

    /**
     * the keys for a livehash are missing
     */
    EMPTY_LIVE_HASH_KEYS(ResponseCodeEnum.EMPTY_LIVE_HASH_KEYS),

    /**
     * the livehash data is not the output of a SHA-384 digest
     */
    INVALID_LIVE_HASH_SIZE(ResponseCodeEnum.INVALID_LIVE_HASH_SIZE),

    /**
     * the query body is empty
     */
    EMPTY_QUERY_BODY(ResponseCodeEnum.EMPTY_QUERY_BODY),

    /**
     * the crypto livehash query is empty
     */
    EMPTY_LIVE_HASH_QUERY(ResponseCodeEnum.EMPTY_LIVE_HASH_QUERY),

    /**
     * the livehash is not present
     */
    LIVE_HASH_NOT_FOUND(ResponseCodeEnum.LIVE_HASH_NOT_FOUND),

    /**
     * the account id passed has not yet been created.
     */
    ACCOUNT_ID_DOES_NOT_EXIST(ResponseCodeEnum.ACCOUNT_ID_DOES_NOT_EXIST),

    /**
     * the livehash already exists for a given account
     */
    LIVE_HASH_ALREADY_EXISTS(ResponseCodeEnum.LIVE_HASH_ALREADY_EXISTS),

    /**
     * File WACL keys are invalid
     */
    INVALID_FILE_WACL(ResponseCodeEnum.INVALID_FILE_WACL),

    /**
     * Serialization failure
     */
    SERIALIZATION_FAILED(ResponseCodeEnum.SERIALIZATION_FAILED),

    /**
     * The size of the Transaction is greater than transactionMaxBytes
     */
    TRANSACTION_OVERSIZE(ResponseCodeEnum.TRANSACTION_OVERSIZE),

    /**
     * The Transaction has more than 50 levels
     */
    TRANSACTION_TOO_MANY_LAYERS(ResponseCodeEnum.TRANSACTION_TOO_MANY_LAYERS),

    /**
     * Contract is marked as deleted
     */
    CONTRACT_DELETED(ResponseCodeEnum.CONTRACT_DELETED),

    /**
     * the platform node is either disconnected or lagging behind.
     */
    PLATFORM_NOT_ACTIVE(ResponseCodeEnum.PLATFORM_NOT_ACTIVE),

    /**
     * one public key matches more than one prefixes on the signature map
     */
    KEY_PREFIX_MISMATCH(ResponseCodeEnum.KEY_PREFIX_MISMATCH),

    /**
     * transaction not created by platform due to large backlog
     */
    PLATFORM_TRANSACTION_NOT_CREATED(ResponseCodeEnum.PLATFORM_TRANSACTION_NOT_CREATED),

    /**
     * auto renewal period is not a positive number of seconds
     */
    INVALID_RENEWAL_PERIOD(ResponseCodeEnum.INVALID_RENEWAL_PERIOD),

    /**
     * the response code when a smart contract id is passed for a crypto API request
     */
    INVALID_PAYER_ACCOUNT_ID(ResponseCodeEnum.INVALID_PAYER_ACCOUNT_ID),

    /**
     * the account has been marked as deleted
     */
    ACCOUNT_DELETED(ResponseCodeEnum.ACCOUNT_DELETED),

    /**
     * the file has been marked as deleted
     */
    FILE_DELETED(ResponseCodeEnum.FILE_DELETED),

    /**
     * same accounts repeated in the transfer account list
     */
    ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS(ResponseCodeEnum.ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS),

    /**
     * attempting to set negative balance value for crypto account
     */
    SETTING_NEGATIVE_ACCOUNT_BALANCE(ResponseCodeEnum.SETTING_NEGATIVE_ACCOUNT_BALANCE),

    /**
     * when deleting smart contract that has crypto balance either transfer account or transfer smart
     * contract is required
     */
    OBTAINER_REQUIRED(ResponseCodeEnum.OBTAINER_REQUIRED),

    /**
     * when deleting smart contract that has crypto balance you can not use the same contract id as
     * transferContractId as the one being deleted
     */
    OBTAINER_SAME_CONTRACT_ID(ResponseCodeEnum.OBTAINER_SAME_CONTRACT_ID),

    /**
     * transferAccountId or transferContractId specified for contract delete does not exist
     */
    OBTAINER_DOES_NOT_EXIST(ResponseCodeEnum.OBTAINER_DOES_NOT_EXIST),

    /**
     * attempting to modify (update or delete a immutable smart contract, i.e. one created without a
     * admin key)
     */
    MODIFYING_IMMUTABLE_CONTRACT(ResponseCodeEnum.MODIFYING_IMMUTABLE_CONTRACT),

    /**
     * Unexpected exception thrown by file system functions
     */
    FILE_SYSTEM_EXCEPTION(ResponseCodeEnum.FILE_SYSTEM_EXCEPTION),

    /**
     * the duration is not a subset of [MINIMUM_AUTORENEW_DURATION,MAXIMUM_AUTORENEW_DURATION]
     */
    AUTORENEW_DURATION_NOT_IN_RANGE(ResponseCodeEnum.AUTORENEW_DURATION_NOT_IN_RANGE),

    /**
     * Decoding the smart contract binary to a byte array failed. Check that the input is a valid hex
     * string.
     */
    ERROR_DECODING_BYTESTRING(ResponseCodeEnum.ERROR_DECODING_BYTESTRING),

    /**
     * File to create a smart contract was of length zero
     */
    CONTRACT_FILE_EMPTY(ResponseCodeEnum.CONTRACT_FILE_EMPTY),

    /**
     * Bytecode for smart contract is of length zero
     */
    CONTRACT_BYTECODE_EMPTY(ResponseCodeEnum.CONTRACT_BYTECODE_EMPTY),

    /**
     * Attempt to set negative initial balance
     */
    INVALID_INITIAL_BALANCE(ResponseCodeEnum.INVALID_INITIAL_BALANCE),

    /**
     * [Deprecated]. attempt to set negative receive record threshold
     */
    INVALID_RECEIVE_RECORD_THRESHOLD(ResponseCodeEnum.INVALID_RECEIVE_RECORD_THRESHOLD),

    /**
     * [Deprecated]. attempt to set negative send record threshold
     */
    INVALID_SEND_RECORD_THRESHOLD(ResponseCodeEnum.INVALID_SEND_RECORD_THRESHOLD),

    /**
     * Special Account Operations should be performed by only Genesis account, return this code if it
     * is not Genesis Account
     */
    ACCOUNT_IS_NOT_GENESIS_ACCOUNT(ResponseCodeEnum.ACCOUNT_IS_NOT_GENESIS_ACCOUNT),

    /**
     * The fee payer account doesn't have permission to submit such Transaction
     */
    PAYER_ACCOUNT_UNAUTHORIZED(ResponseCodeEnum.PAYER_ACCOUNT_UNAUTHORIZED),

    /**
     * FreezeTransactionBody is invalid
     */
    INVALID_FREEZE_TRANSACTION_BODY(ResponseCodeEnum.INVALID_FREEZE_TRANSACTION_BODY),

    /**
     * FreezeTransactionBody does not exist
     */
    FREEZE_TRANSACTION_BODY_NOT_FOUND(ResponseCodeEnum.FREEZE_TRANSACTION_BODY_NOT_FOUND),

    /**
     * Exceeded the number of accounts (both from and to) allowed for crypto transfer list
     */
    TRANSFER_LIST_SIZE_LIMIT_EXCEEDED(ResponseCodeEnum.TRANSFER_LIST_SIZE_LIMIT_EXCEEDED),

    /**
     * Smart contract result size greater than specified maxResultSize
     */
    RESULT_SIZE_LIMIT_EXCEEDED(ResponseCodeEnum.RESULT_SIZE_LIMIT_EXCEEDED),

    /**
     * The payer account is not a special account(account 0.0.55)
     */
    NOT_SPECIAL_ACCOUNT(ResponseCodeEnum.NOT_SPECIAL_ACCOUNT),

    /**
     * Negative gas was offered in smart contract call
     */
    CONTRACT_NEGATIVE_GAS(ResponseCodeEnum.CONTRACT_NEGATIVE_GAS),

    /**
     * Negative value / initial balance was specified in a smart contract call / create
     */
    CONTRACT_NEGATIVE_VALUE(ResponseCodeEnum.CONTRACT_NEGATIVE_VALUE),

    /**
     * Failed to update fee file
     */
    INVALID_FEE_FILE(ResponseCodeEnum.INVALID_FEE_FILE),

    /**
     * Failed to update exchange rate file
     */
    INVALID_EXCHANGE_RATE_FILE(ResponseCodeEnum.INVALID_EXCHANGE_RATE_FILE),

    /**
     * Payment tendered for contract local call cannot cover both the fee and the gas
     */
    INSUFFICIENT_LOCAL_CALL_GAS(ResponseCodeEnum.INSUFFICIENT_LOCAL_CALL_GAS),

    /**
     * Entities with Entity ID below 1000 are not allowed to be deleted
     */
    ENTITY_NOT_ALLOWED_TO_DELETE(ResponseCodeEnum.ENTITY_NOT_ALLOWED_TO_DELETE),

    /**
     * Violating one of these rules: 1) treasury account can update all entities below 0.0.1000, 2)
     * account 0.0.50 can update all entities from 0.0.51 - 0.0.80, 3) Network Function Master Account
     * A/c 0.0.50 - Update all Network Function accounts and perform all the Network Functions listed
     * below, 4) Network Function Accounts: i) A/c 0.0.55 - Update Address Book files (0.0.101/102),
     * ii) A/c 0.0.56 - Update Fee schedule (0.0.111), iii) A/c 0.0.57 - Update Exchange Rate
     * (0.0.112).
     */
    AUTHORIZATION_FAILED(ResponseCodeEnum.AUTHORIZATION_FAILED),

    /**
     * Fee Schedule Proto uploaded but not valid (append or update is required)
     */
    FILE_UPLOADED_PROTO_INVALID(ResponseCodeEnum.FILE_UPLOADED_PROTO_INVALID),

    /**
     * Fee Schedule Proto uploaded but not valid (append or update is required)
     */
    FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK(ResponseCodeEnum.FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK),

    /**
     * Fee Schedule Proto File Part uploaded
     */
    FEE_SCHEDULE_FILE_PART_UPLOADED(ResponseCodeEnum.FEE_SCHEDULE_FILE_PART_UPLOADED),

    /**
     * The change on Exchange Rate exceeds Exchange_Rate_Allowed_Percentage
     */
    EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED(ResponseCodeEnum.EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED),

    /**
     * Contract permanent storage exceeded the currently allowable limit
     */
    MAX_CONTRACT_STORAGE_EXCEEDED(ResponseCodeEnum.MAX_CONTRACT_STORAGE_EXCEEDED),

    /**
     * Transfer Account should not be same as Account to be deleted
     */
    TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT(ResponseCodeEnum.TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT),

    TOTAL_LEDGER_BALANCE_INVALID(ResponseCodeEnum.TOTAL_LEDGER_BALANCE_INVALID),

    /**
     * The expiration date/time on a smart contract may not be reduced
     */
    EXPIRATION_REDUCTION_NOT_ALLOWED(ResponseCodeEnum.EXPIRATION_REDUCTION_NOT_ALLOWED),

    /**
     * Gas exceeded currently allowable gas limit per transaction
     */
    MAX_GAS_LIMIT_EXCEEDED(ResponseCodeEnum.MAX_GAS_LIMIT_EXCEEDED),

    /**
     * File size exceeded the currently allowable limit
     */
    MAX_FILE_SIZE_EXCEEDED(ResponseCodeEnum.MAX_FILE_SIZE_EXCEEDED),

    /**
     * When a valid signature is not provided for operations on account with receiverSigRequired=true
     */
    RECEIVER_SIG_REQUIRED(ResponseCodeEnum.RECEIVER_SIG_REQUIRED),

    /**
     * The Topic ID specified is not in the system.
     */
    INVALID_TOPIC_ID(ResponseCodeEnum.INVALID_TOPIC_ID),

    /**
     * A provided admin key was invalid.
     */
    INVALID_ADMIN_KEY(ResponseCodeEnum.INVALID_ADMIN_KEY),

    /**
     * A provided submit key was invalid.
     */
    INVALID_SUBMIT_KEY(ResponseCodeEnum.INVALID_SUBMIT_KEY),

    /**
     * An attempted operation was not authorized (ie - a deleteTopic for a topic with no adminKey).
     */
    UNAUTHORIZED(ResponseCodeEnum.UNAUTHORIZED),

    /**
     * A ConsensusService message is empty.
     */
    INVALID_TOPIC_MESSAGE(ResponseCodeEnum.INVALID_TOPIC_MESSAGE),

    /**
     * The autoRenewAccount specified is not a valid, active account.
     */
    INVALID_AUTORENEW_ACCOUNT(ResponseCodeEnum.INVALID_AUTORENEW_ACCOUNT),

    /**
     * An adminKey was not specified on the topic, so there must not be an autoRenewAccount.
     */
    AUTORENEW_ACCOUNT_NOT_ALLOWED(ResponseCodeEnum.AUTORENEW_ACCOUNT_NOT_ALLOWED),

    /**
     * The topic has expired, was not automatically renewed, and is in a 7 day grace period before the
     * topic will be deleted unrecoverably. This error response code will not be returned until
     * autoRenew functionality is supported by HAPI.
     */
    TOPIC_EXPIRED(ResponseCodeEnum.TOPIC_EXPIRED),

    /**
     * chunk number must be from 1 to total (chunks) inclusive.
     */
    INVALID_CHUNK_NUMBER(ResponseCodeEnum.INVALID_CHUNK_NUMBER),

    /**
     * For every chunk, the payer account that is part of initialTransactionID must match the Payer Account of this transaction. The entire initialTransactionID should match the transactionID of the first chunk, but this is not checked or enforced by Hedera except when the chunk number is 1.
     */
    INVALID_CHUNK_TRANSACTION_ID(ResponseCodeEnum.INVALID_CHUNK_TRANSACTION_ID),

    /**
     * Account is frozen and cannot transact with the token
     */
    ACCOUNT_FROZEN_FOR_TOKEN(ResponseCodeEnum.ACCOUNT_FROZEN_FOR_TOKEN),

    /**
     * An involved account already has more than tokens.maxPerAccount associations with non-deleted tokens.
     */
    TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED(ResponseCodeEnum.TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED),

    /**
     * The token is invalid or does not exist
     */
    INVALID_TOKEN_ID(ResponseCodeEnum.INVALID_TOKEN_ID),

    /**
     * Invalid token decimals
     */
    INVALID_TOKEN_DECIMALS(ResponseCodeEnum.INVALID_TOKEN_DECIMALS),

    /**
     * Invalid token initial supply
     */
    INVALID_TOKEN_INITIAL_SUPPLY(ResponseCodeEnum.INVALID_TOKEN_INITIAL_SUPPLY),

    /**
     * Treasury Account does not exist or is deleted
     */
    INVALID_TREASURY_ACCOUNT_FOR_TOKEN(ResponseCodeEnum.INVALID_TREASURY_ACCOUNT_FOR_TOKEN),

    /**
     * Token Symbol is not UTF-8 capitalized alphabetical string
     */
    INVALID_TOKEN_SYMBOL(ResponseCodeEnum.INVALID_TOKEN_SYMBOL),

    /**
     * Freeze key is not set on token
     */
    TOKEN_HAS_NO_FREEZE_KEY(ResponseCodeEnum.TOKEN_HAS_NO_FREEZE_KEY),

    /**
     * Amounts in transfer list are not net zero
     */
    TRANSFERS_NOT_ZERO_SUM_FOR_TOKEN(ResponseCodeEnum.TRANSFERS_NOT_ZERO_SUM_FOR_TOKEN),

    /**
     * A token symbol was not provided
     */
    MISSING_TOKEN_SYMBOL(ResponseCodeEnum.MISSING_TOKEN_SYMBOL),

    /**
     * The provided token symbol was too long
     */
    TOKEN_SYMBOL_TOO_LONG(ResponseCodeEnum.TOKEN_SYMBOL_TOO_LONG),

    /**
     * KYC must be granted and account does not have KYC granted
     */
    ACCOUNT_KYC_NOT_GRANTED_FOR_TOKEN(ResponseCodeEnum.ACCOUNT_KYC_NOT_GRANTED_FOR_TOKEN),

    /**
     * KYC key is not set on token
     */
    TOKEN_HAS_NO_KYC_KEY(ResponseCodeEnum.TOKEN_HAS_NO_KYC_KEY),

    /**
     * Token balance is not sufficient for the transaction
     */
    INSUFFICIENT_TOKEN_BALANCE(ResponseCodeEnum.INSUFFICIENT_TOKEN_BALANCE),

    /**
     * Token transactions cannot be executed on deleted token
     */
    TOKEN_WAS_DELETED(ResponseCodeEnum.TOKEN_WAS_DELETED),

    /**
     * Supply key is not set on token
     */
    TOKEN_HAS_NO_SUPPLY_KEY(ResponseCodeEnum.TOKEN_HAS_NO_SUPPLY_KEY),

    /**
     * Wipe key is not set on token
     */
    TOKEN_HAS_NO_WIPE_KEY(ResponseCodeEnum.TOKEN_HAS_NO_WIPE_KEY),

    /**
     * The requested token mint amount would cause an invalid total supply
     */
    INVALID_TOKEN_MINT_AMOUNT(ResponseCodeEnum.INVALID_TOKEN_MINT_AMOUNT),

    /**
     * The requested token burn amount would cause an invalid total supply
     */
    INVALID_TOKEN_BURN_AMOUNT(ResponseCodeEnum.INVALID_TOKEN_BURN_AMOUNT),

    /**
     * A required token-account relationship is missing
     */
    TOKEN_NOT_ASSOCIATED_TO_ACCOUNT(ResponseCodeEnum.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT),

    /**
     * The target of a wipe operation was the token treasury account
     */
    CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT(ResponseCodeEnum.CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT),

    /**
     * The provided KYC key was invalid.
     */
    INVALID_KYC_KEY(ResponseCodeEnum.INVALID_KYC_KEY),

    /**
     * The provided wipe key was invalid.
     */
    INVALID_WIPE_KEY(ResponseCodeEnum.INVALID_WIPE_KEY),

    /**
     * The provided freeze key was invalid.
     */
    INVALID_FREEZE_KEY(ResponseCodeEnum.INVALID_FREEZE_KEY),

    /**
     * The provided supply key was invalid.
     */
    INVALID_SUPPLY_KEY(ResponseCodeEnum.INVALID_SUPPLY_KEY),

    /**
     * Token Name is not provided
     */
    MISSING_TOKEN_NAME(ResponseCodeEnum.MISSING_TOKEN_NAME),

    /**
     * Token Name is too long
     */
    TOKEN_NAME_TOO_LONG(ResponseCodeEnum.TOKEN_NAME_TOO_LONG),

    /**
     * The provided wipe amount must not be negative, zero or bigger than the token holder balance
     */
    INVALID_WIPING_AMOUNT(ResponseCodeEnum.INVALID_WIPING_AMOUNT),

    /**
     * Token does not have Admin key set, thus update/delete transactions cannot be performed
     */
    TOKEN_IS_IMMUTABLE(ResponseCodeEnum.TOKEN_IS_IMMUTABLE),

    /**
     * An associateToken operation specified a token already associated to the account
     */
    TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT(ResponseCodeEnum.TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT),

    /**
     * An attempted operation is invalid until all token balances for the target account are zero
     */
    TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES(ResponseCodeEnum.TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES),

    /**
     * An attempted operation is invalid because the account is a treasury
     */
    ACCOUNT_IS_TREASURY(ResponseCodeEnum.ACCOUNT_IS_TREASURY),

    /**
     * Same TokenIDs present in the token list
     */
    TOKEN_ID_REPEATED_IN_TOKEN_LIST(ResponseCodeEnum.TOKEN_ID_REPEATED_IN_TOKEN_LIST),

    /**
     * Exceeded the number of token transfers (both from and to) allowed for token transfer list
     */
    TOKEN_TRANSFER_LIST_SIZE_LIMIT_EXCEEDED(ResponseCodeEnum.TOKEN_TRANSFER_LIST_SIZE_LIMIT_EXCEEDED),

    /**
     * TokenTransfersTransactionBody has no TokenTransferList
     */
    EMPTY_TOKEN_TRANSFER_BODY(ResponseCodeEnum.EMPTY_TOKEN_TRANSFER_BODY),

    /**
     * TokenTransfersTransactionBody has a TokenTransferList with no AccountAmounts
     */
    EMPTY_TOKEN_TRANSFER_ACCOUNT_AMOUNTS(ResponseCodeEnum.EMPTY_TOKEN_TRANSFER_ACCOUNT_AMOUNTS),

    /**
     * The Scheduled entity does not exist; or has now expired, been deleted, or been executed
     */
    INVALID_SCHEDULE_ID(ResponseCodeEnum.INVALID_SCHEDULE_ID),

    /**
     * The Scheduled entity cannot be modified. Admin key not set
     */
    SCHEDULE_IS_IMMUTABLE(ResponseCodeEnum.SCHEDULE_IS_IMMUTABLE),

    /**
     * The provided Scheduled Payer does not exist
     */
    INVALID_SCHEDULE_PAYER_ID(ResponseCodeEnum.INVALID_SCHEDULE_PAYER_ID),

    /**
     * The Schedule Create Transaction TransactionID account does not exist
     */
    INVALID_SCHEDULE_ACCOUNT_ID(ResponseCodeEnum.INVALID_SCHEDULE_ACCOUNT_ID),

    /**
     * The provided sig map did not contain any new valid signatures from required signers of the scheduled transaction
     */
    NO_NEW_VALID_SIGNATURES(ResponseCodeEnum.NO_NEW_VALID_SIGNATURES),

    /**
     * The required signers for a scheduled transaction cannot be resolved, for example because they do not exist or have been deleted
     */
    UNRESOLVABLE_REQUIRED_SIGNERS(ResponseCodeEnum.UNRESOLVABLE_REQUIRED_SIGNERS),

    /**
     * Only whitelisted transaction types may be scheduled
     */
    SCHEDULED_TRANSACTION_NOT_IN_WHITELIST(ResponseCodeEnum.SCHEDULED_TRANSACTION_NOT_IN_WHITELIST),

    /**
     * At least one of the signatures in the provided sig map did not represent a valid signature for any required signer
     */
    SOME_SIGNATURES_WERE_INVALID(ResponseCodeEnum.SOME_SIGNATURES_WERE_INVALID),

    /**
     * The scheduled field in the TransactionID may not be set to true
     */
    TRANSACTION_ID_FIELD_NOT_ALLOWED(ResponseCodeEnum.TRANSACTION_ID_FIELD_NOT_ALLOWED),

    /**
     * A schedule already exists with the same identifying fields of an attempted ScheduleCreate (that is, all fields other than scheduledPayerAccountID)
     */
    IDENTICAL_SCHEDULE_ALREADY_CREATED(ResponseCodeEnum.IDENTICAL_SCHEDULE_ALREADY_CREATED),

    /**
     * A string field in the transaction has a UTF-8 encoding with the prohibited zero byte
     */
    INVALID_ZERO_BYTE_IN_STRING(ResponseCodeEnum.INVALID_ZERO_BYTE_IN_STRING),

    /**
     * A schedule being signed or deleted has already been deleted
     */
    SCHEDULE_ALREADY_DELETED(ResponseCodeEnum.SCHEDULE_ALREADY_DELETED),

    /**
     * A schedule being signed or deleted has already been executed
     */
    SCHEDULE_ALREADY_EXECUTED(ResponseCodeEnum.SCHEDULE_ALREADY_EXECUTED),

    /**
     * ConsensusSubmitMessage request's message size is larger than allowed.
     */
    MESSAGE_SIZE_TOO_LARGE(ResponseCodeEnum.MESSAGE_SIZE_TOO_LARGE),

    /**
     * An operation was assigned to more than one throttle group in a given bucket
     */
    OPERATION_REPEATED_IN_BUCKET_GROUPS(ResponseCodeEnum.OPERATION_REPEATED_IN_BUCKET_GROUPS),

    /**
     * The capacity needed to satisfy all opsPerSec groups in a bucket overflowed a signed 8-byte integral type
     */
    BUCKET_CAPACITY_OVERFLOW(ResponseCodeEnum.BUCKET_CAPACITY_OVERFLOW),

    /**
     * Given the network size in the address book, the node-level capacity for an operation would never be enough to accept a single request; usually means a bucket burstPeriod should be increased
     */
    NODE_CAPACITY_NOT_SUFFICIENT_FOR_OPERATION(ResponseCodeEnum.NODE_CAPACITY_NOT_SUFFICIENT_FOR_OPERATION),

    /**
     * A bucket was defined without any throttle groups
     */
    BUCKET_HAS_NO_THROTTLE_GROUPS(ResponseCodeEnum.BUCKET_HAS_NO_THROTTLE_GROUPS),

    /**
     * A throttle group was granted zero opsPerSec
     */
    THROTTLE_GROUP_HAS_ZERO_OPS_PER_SEC(ResponseCodeEnum.THROTTLE_GROUP_HAS_ZERO_OPS_PER_SEC),

    /**
     * The throttle definitions file was updated, but some supported operations were not assigned a bucket
     */
    SUCCESS_BUT_MISSING_EXPECTED_OPERATION(ResponseCodeEnum.SUCCESS_BUT_MISSING_EXPECTED_OPERATION),

    /**
     * The new contents for the throttle definitions system file were not valid protobuf
     */
    UNPARSEABLE_THROTTLE_DEFINITIONS(ResponseCodeEnum.UNPARSEABLE_THROTTLE_DEFINITIONS),

    /**
     * The new throttle definitions system file were invalid, and no more specific error could be divined
     */
    INVALID_THROTTLE_DEFINITIONS(ResponseCodeEnum.INVALID_THROTTLE_DEFINITIONS),

    /**
     * The transaction references an account which has passed its expiration without renewal funds available, and currently remains in the ledger only because of the grace period given to expired entities
     */
    ACCOUNT_EXPIRED_AND_PENDING_REMOVAL(ResponseCodeEnum.ACCOUNT_EXPIRED_AND_PENDING_REMOVAL),

    /**
     * Invalid token max supply
     */
    INVALID_TOKEN_MAX_SUPPLY(ResponseCodeEnum.INVALID_TOKEN_MAX_SUPPLY),

    /**
     * Invalid token nft serial number
     */
    INVALID_TOKEN_NFT_SERIAL_NUMBER(ResponseCodeEnum.INVALID_TOKEN_NFT_SERIAL_NUMBER),

    /**
     * Invalid nft id
     */
    INVALID_NFT_ID(ResponseCodeEnum.INVALID_NFT_ID),

    /**
     * Nft metadata is too long
     */
    METADATA_TOO_LONG(ResponseCodeEnum.METADATA_TOO_LONG),

    /**
     * Repeated operations count exceeds the limit
     */
    BATCH_SIZE_LIMIT_EXCEEDED(ResponseCodeEnum.BATCH_SIZE_LIMIT_EXCEEDED),

    /**
     * The range of data to be gathered is out of the set boundaries
     */
    INVALID_QUERY_RANGE(ResponseCodeEnum.INVALID_QUERY_RANGE),

    /**
     * A custom fractional fee set a denominator of zero
     */
    FRACTION_DIVIDES_BY_ZERO(ResponseCodeEnum.FRACTION_DIVIDES_BY_ZERO),

    /**
     * The transaction payer could not afford a custom fee
     */
    INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE(ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE),

    /**
     * More than 10 custom fees were specified
     */
    CUSTOM_FEES_LIST_TOO_LONG(ResponseCodeEnum.CUSTOM_FEES_LIST_TOO_LONG),

    /**
     * Any of the feeCollector accounts for customFees is invalid
     */
    INVALID_CUSTOM_FEE_COLLECTOR(ResponseCodeEnum.INVALID_CUSTOM_FEE_COLLECTOR),

    /**
     * Any of the token Ids in customFees is invalid
     */
    INVALID_TOKEN_ID_IN_CUSTOM_FEES(ResponseCodeEnum.INVALID_TOKEN_ID_IN_CUSTOM_FEES),

    /**
     * Any of the token Ids in customFees are not associated to feeCollector
     */
    TOKEN_NOT_ASSOCIATED_TO_FEE_COLLECTOR(ResponseCodeEnum.TOKEN_NOT_ASSOCIATED_TO_FEE_COLLECTOR),

    /**
     * A token cannot have more units minted due to its configured supply ceiling
     */
    TOKEN_MAX_SUPPLY_REACHED(ResponseCodeEnum.TOKEN_MAX_SUPPLY_REACHED),

    /**
     * The transaction attempted to move an NFT serial number from an account other than its owner
     */
    SENDER_DOES_NOT_OWN_NFT_SERIAL_NO(ResponseCodeEnum.SENDER_DOES_NOT_OWN_NFT_SERIAL_NO),

    /**
     * A custom fee schedule entry did not specify either a fixed or fractional fee
     */
    CUSTOM_FEE_NOT_FULLY_SPECIFIED(ResponseCodeEnum.CUSTOM_FEE_NOT_FULLY_SPECIFIED),

    /**
     * Only positive fees may be assessed at this time
     */
    CUSTOM_FEE_MUST_BE_POSITIVE(ResponseCodeEnum.CUSTOM_FEE_MUST_BE_POSITIVE),

    /**
     * Fee schedule key is not set on token
     */
    TOKEN_HAS_NO_FEE_SCHEDULE_KEY(ResponseCodeEnum.TOKEN_HAS_NO_FEE_SCHEDULE_KEY),

    /**
     * A fractional custom fee exceeded the range of a 64-bit signed integer
     */
    CUSTOM_FEE_OUTSIDE_NUMERIC_RANGE(ResponseCodeEnum.CUSTOM_FEE_OUTSIDE_NUMERIC_RANGE),

    /**
     * A royalty cannot exceed the total fungible value exchanged for an NFT
     */
    ROYALTY_FRACTION_CANNOT_EXCEED_ONE(ResponseCodeEnum.ROYALTY_FRACTION_CANNOT_EXCEED_ONE),

    /**
     * Each fractional custom fee must have its maximum_amount, if specified, at least its minimum_amount
     */
    FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT(ResponseCodeEnum.FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT),

    /**
     * A fee schedule update tried to clear the custom fees from a token whose fee schedule was already empty
     */
    CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES(ResponseCodeEnum.CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES),

    /**
     * Only tokens of type FUNGIBLE_COMMON can be used to as fee schedule denominations
     */
    CUSTOM_FEE_DENOMINATION_MUST_BE_FUNGIBLE_COMMON(ResponseCodeEnum.CUSTOM_FEE_DENOMINATION_MUST_BE_FUNGIBLE_COMMON),

    /**
     * Only tokens of type FUNGIBLE_COMMON can have fractional fees
     */
    CUSTOM_FRACTIONAL_FEE_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON(ResponseCodeEnum.CUSTOM_FRACTIONAL_FEE_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON),

    /**
     * The provided custom fee schedule key was invalid
     */
    INVALID_CUSTOM_FEE_SCHEDULE_KEY(ResponseCodeEnum.INVALID_CUSTOM_FEE_SCHEDULE_KEY),

    /**
     * The requested token mint metadata was invalid
     */
    INVALID_TOKEN_MINT_METADATA(ResponseCodeEnum.INVALID_TOKEN_MINT_METADATA),

    /**
     * The requested token burn metadata was invalid
     */
    INVALID_TOKEN_BURN_METADATA(ResponseCodeEnum.INVALID_TOKEN_BURN_METADATA),

    /**
     * The treasury for a unique token cannot be changed until it owns no NFTs
     */
    CURRENT_TREASURY_STILL_OWNS_NFTS(ResponseCodeEnum.CURRENT_TREASURY_STILL_OWNS_NFTS),

    /**
     * An account cannot be dissociated from a unique token if it owns NFTs for the token
     */
    ACCOUNT_STILL_OWNS_NFTS(ResponseCodeEnum.ACCOUNT_STILL_OWNS_NFTS),

    /**
     * A NFT can only be burned when owned by the unique token's treasury
     */
    TREASURY_MUST_OWN_BURNED_NFT(ResponseCodeEnum.TREASURY_MUST_OWN_BURNED_NFT),

    /**
     * An account did not own the NFT to be wiped
     */
    ACCOUNT_DOES_NOT_OWN_WIPED_NFT(ResponseCodeEnum.ACCOUNT_DOES_NOT_OWN_WIPED_NFT),

    /**
     * An AccountAmount token transfers list referenced a token type other than FUNGIBLE_COMMON
     */
    ACCOUNT_AMOUNT_TRANSFERS_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON(ResponseCodeEnum.ACCOUNT_AMOUNT_TRANSFERS_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON),

    /**
     * All the NFTs allowed in the current price regime have already been minted
     */
    MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED(ResponseCodeEnum.MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED),

    /**
     * The payer account has been marked as deleted
     */
    PAYER_ACCOUNT_DELETED(ResponseCodeEnum.PAYER_ACCOUNT_DELETED),

    /**
     * The reference chain of custom fees for a transferred token exceeded the maximum length of 2
     */
    CUSTOM_FEE_CHARGING_EXCEEDED_MAX_RECURSION_DEPTH(ResponseCodeEnum.CUSTOM_FEE_CHARGING_EXCEEDED_MAX_RECURSION_DEPTH),

    /**
     * More than 20 balance adjustments were to satisfy a CryptoTransfer and its implied custom fee payments
     */
    CUSTOM_FEE_CHARGING_EXCEEDED_MAX_ACCOUNT_AMOUNTS(ResponseCodeEnum.CUSTOM_FEE_CHARGING_EXCEEDED_MAX_ACCOUNT_AMOUNTS),

    /**
     * The sender account in the token transfer transaction could not afford a custom fee
     */
    INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE(ResponseCodeEnum.INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE),

    /**
     * Currently no more than 4,294,967,295 NFTs may be minted for a given unique token type
     */
    SERIAL_NUMBER_LIMIT_REACHED(ResponseCodeEnum.SERIAL_NUMBER_LIMIT_REACHED),

    /**
     * Only tokens of type NON_FUNGIBLE_UNIQUE can have royalty fees
     */
    CUSTOM_ROYALTY_FEE_ONLY_ALLOWED_FOR_NON_FUNGIBLE_UNIQUE(ResponseCodeEnum.CUSTOM_ROYALTY_FEE_ONLY_ALLOWED_FOR_NON_FUNGIBLE_UNIQUE),

    /**
     * The account has reached the limit on the automatic associations count.
     */
    NO_REMAINING_AUTOMATIC_ASSOCIATIONS(ResponseCodeEnum.NO_REMAINING_AUTOMATIC_ASSOCIATIONS),

    /**
     * Already existing automatic associations are more than the new maximum automatic associations.
     */
    EXISTING_AUTOMATIC_ASSOCIATIONS_EXCEED_GIVEN_LIMIT(ResponseCodeEnum.EXISTING_AUTOMATIC_ASSOCIATIONS_EXCEED_GIVEN_LIMIT),

    /**
     * Cannot set the number of automatic associations for an account more than the maximum allowed
     * token associations tokens.maxPerAccount.
     */
    REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT(ResponseCodeEnum.REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT),

    /**
     * Token is paused. This Token cannot be a part of any kind of Transaction until unpaused.
     */
    TOKEN_IS_PAUSED(ResponseCodeEnum.TOKEN_IS_PAUSED),

    /**
     * Pause key is not set on token
     */
    TOKEN_HAS_NO_PAUSE_KEY(ResponseCodeEnum.TOKEN_HAS_NO_PAUSE_KEY),

    /**
     * The provided pause key was invalid
     */
    INVALID_PAUSE_KEY(ResponseCodeEnum.INVALID_PAUSE_KEY),

    /**
     * The update file in a freeze transaction body must exist.
     */
    FREEZE_UPDATE_FILE_DOES_NOT_EXIST(ResponseCodeEnum.FREEZE_UPDATE_FILE_DOES_NOT_EXIST),

    /**
     * The hash of the update file in a freeze transaction body must match the in-memory hash.
     */
    FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH(ResponseCodeEnum.FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH),

    /**
     * A FREEZE_UPGRADE transaction was handled with no previous update prepared.
     */
    NO_UPGRADE_HAS_BEEN_PREPARED(ResponseCodeEnum.NO_UPGRADE_HAS_BEEN_PREPARED),

    /**
     * A FREEZE_ABORT transaction was handled with no scheduled freeze.
     */
    NO_FREEZE_IS_SCHEDULED(ResponseCodeEnum.NO_FREEZE_IS_SCHEDULED),

    /**
     * The update file hash when handling a FREEZE_UPGRADE transaction differs from the file
     * hash at the time of handling the PREPARE_UPGRADE transaction.
     */
    UPDATE_FILE_HASH_CHANGED_SINCE_PREPARE_UPGRADE(ResponseCodeEnum.UPDATE_FILE_HASH_CHANGED_SINCE_PREPARE_UPGRADE),

    /**
     * The given freeze start time was in the (consensus) past.
     */
    FREEZE_START_TIME_MUST_BE_FUTURE(ResponseCodeEnum.FREEZE_START_TIME_MUST_BE_FUTURE),

    /**
     * The prepared update file cannot be updated or appended until either the upgrade has
     * been completed, or a FREEZE_ABORT has been handled.
     */
    PREPARED_UPDATE_FILE_IS_IMMUTABLE(ResponseCodeEnum.PREPARED_UPDATE_FILE_IS_IMMUTABLE),

    /**
     * Once a freeze is scheduled, it must be aborted before any other type of freeze can
     * can be performed.
     */
    FREEZE_ALREADY_SCHEDULED(ResponseCodeEnum.FREEZE_ALREADY_SCHEDULED),

    /**
     * If an NMT upgrade has been prepared, the following operation must be a FREEZE_UPGRADE.
     * (To issue a FREEZE_ONLY, submit a FREEZE_ABORT first.)
     */
    FREEZE_UPGRADE_IN_PROGRESS(ResponseCodeEnum.FREEZE_UPGRADE_IN_PROGRESS),

    /**
     * If an NMT upgrade has been prepared, the subsequent FREEZE_UPGRADE transaction must
     * confirm the id of the file to be used in the upgrade.
     */
    UPDATE_FILE_ID_DOES_NOT_MATCH_PREPARED(ResponseCodeEnum.UPDATE_FILE_ID_DOES_NOT_MATCH_PREPARED),

    /**
     * If an NMT upgrade has been prepared, the subsequent FREEZE_UPGRADE transaction must
     * confirm the hash of the file to be used in the upgrade.
     */
    UPDATE_FILE_HASH_DOES_NOT_MATCH_PREPARED(ResponseCodeEnum.UPDATE_FILE_HASH_DOES_NOT_MATCH_PREPARED),

    /**
     * Consensus throttle did not allow execution of this transaction. System is throttled at
     * consensus level.
     */
    CONSENSUS_GAS_EXHAUSTED(ResponseCodeEnum.CONSENSUS_GAS_EXHAUSTED),

    /**
     * A precompiled contract succeeded, but was later reverted.
     */
    REVERTED_SUCCESS(ResponseCodeEnum.REVERTED_SUCCESS),

    /**
     * All contract storage allocated to the current price regime has been consumed.
     */
    MAX_STORAGE_IN_PRICE_REGIME_HAS_BEEN_USED(ResponseCodeEnum.MAX_STORAGE_IN_PRICE_REGIME_HAS_BEEN_USED),

    /**
     * An alias used in a CryptoTransfer transaction is not the serialization of a primitive Key
     * message--that is, a Key with a single Ed25519 or ECDSA(secp256k1) public key and no
     * unknown protobuf fields.
     */
    INVALID_ALIAS_KEY(ResponseCodeEnum.INVALID_ALIAS_KEY),

    /**
     * A fungible token transfer expected a different number of decimals than the involved
     * type actually has.
     */
    UNEXPECTED_TOKEN_DECIMALS(ResponseCodeEnum.UNEXPECTED_TOKEN_DECIMALS),

    /**
     * The proxy account id is invalid or does not exist.
     */
    INVALID_PROXY_ACCOUNT_ID(ResponseCodeEnum.INVALID_PROXY_ACCOUNT_ID),

    /**
     * The transfer account id in CryptoDelete transaction is invalid or does not exist.
     */
    INVALID_TRANSFER_ACCOUNT_ID(ResponseCodeEnum.INVALID_TRANSFER_ACCOUNT_ID),

    /**
     * The fee collector account id in TokenFeeScheduleUpdate is invalid or does not exist.
     */
    INVALID_FEE_COLLECTOR_ACCOUNT_ID(ResponseCodeEnum.INVALID_FEE_COLLECTOR_ACCOUNT_ID),

    /**
     * The alias already set on an account cannot be updated using CryptoUpdate transaction.
     */
    ALIAS_IS_IMMUTABLE(ResponseCodeEnum.ALIAS_IS_IMMUTABLE),

    /**
     * An approved allowance specifies a spender account that is the same as the hbar/token
     * owner account.
     */
    SPENDER_ACCOUNT_SAME_AS_OWNER(ResponseCodeEnum.SPENDER_ACCOUNT_SAME_AS_OWNER),

    /**
     * The establishment or adjustment of an approved allowance cause the token allowance
     * to exceed the token maximum supply.
     */
    AMOUNT_EXCEEDS_TOKEN_MAX_SUPPLY(ResponseCodeEnum.AMOUNT_EXCEEDS_TOKEN_MAX_SUPPLY),

    /**
     * The specified amount for an approved allowance cannot be negative.
     */
    NEGATIVE_ALLOWANCE_AMOUNT(ResponseCodeEnum.NEGATIVE_ALLOWANCE_AMOUNT),

    /**
     * The approveForAll flag cannot be set for a fungible token.
     */
    CANNOT_APPROVE_FOR_ALL_FUNGIBLE_COMMON(ResponseCodeEnum.CANNOT_APPROVE_FOR_ALL_FUNGIBLE_COMMON),

    /**
     * The spender does not have an existing approved allowance with the hbar/token owner.
     */
    SPENDER_DOES_NOT_HAVE_ALLOWANCE(ResponseCodeEnum.SPENDER_DOES_NOT_HAVE_ALLOWANCE),

    /**
     * The transfer amount exceeds the current approved allowance for the spender account.
     */
    AMOUNT_EXCEEDS_ALLOWANCE(ResponseCodeEnum.AMOUNT_EXCEEDS_ALLOWANCE),

    /**
     * The payer account of an approveAllowances or adjustAllowance transaction is attempting
     * to go beyond the maximum allowed number of allowances.
     */
    MAX_ALLOWANCES_EXCEEDED(ResponseCodeEnum.MAX_ALLOWANCES_EXCEEDED),

    /**
     * No allowances have been specified in the approval transaction.
     */
    EMPTY_ALLOWANCES(ResponseCodeEnum.EMPTY_ALLOWANCES),

    /**
     * Spender is repeated more than once in Crypto or Token or NFT allowance lists in a single
     * CryptoApproveAllowance transaction.
     */
    SPENDER_ACCOUNT_REPEATED_IN_ALLOWANCES(ResponseCodeEnum.SPENDER_ACCOUNT_REPEATED_IN_ALLOWANCES),

    /**
     * Serial numbers are repeated in nft allowance for a single spender account
     */
    REPEATED_SERIAL_NUMS_IN_NFT_ALLOWANCES(ResponseCodeEnum.REPEATED_SERIAL_NUMS_IN_NFT_ALLOWANCES),

    /**
     * Fungible common token used in NFT allowances
     */
    FUNGIBLE_TOKEN_IN_NFT_ALLOWANCES(ResponseCodeEnum.FUNGIBLE_TOKEN_IN_NFT_ALLOWANCES),

    /**
     * Non fungible token used in fungible token allowances
     */
    NFT_IN_FUNGIBLE_TOKEN_ALLOWANCES(ResponseCodeEnum.NFT_IN_FUNGIBLE_TOKEN_ALLOWANCES),

    /**
     * The account id specified as the owner is invalid or does not exist.
     */
    INVALID_ALLOWANCE_OWNER_ID(ResponseCodeEnum.INVALID_ALLOWANCE_OWNER_ID),

    /**
     * The account id specified as the spender is invalid or does not exist.
     */
    INVALID_ALLOWANCE_SPENDER_ID(ResponseCodeEnum.INVALID_ALLOWANCE_SPENDER_ID),

    /**
     * If the CryptoDeleteAllowance transaction has repeated crypto or token or Nft allowances to delete.
     */
    REPEATED_ALLOWANCES_TO_DELETE(ResponseCodeEnum.REPEATED_ALLOWANCES_TO_DELETE),

    /**
     * If the account Id specified as the delegating spender is invalid or does not exist.
     */
    INVALID_DELEGATING_SPENDER(ResponseCodeEnum.INVALID_DELEGATING_SPENDER),

    /**
     * The delegating Spender cannot grant approveForAll allowance on a NFT token type for another spender.
     */
    DELEGATING_SPENDER_CANNOT_GRANT_APPROVE_FOR_ALL(ResponseCodeEnum.DELEGATING_SPENDER_CANNOT_GRANT_APPROVE_FOR_ALL),

    /**
     * The delegating Spender cannot grant allowance on a NFT serial for another spender as it doesnt not have approveForAll
     * granted on token-owner.
     */
    DELEGATING_SPENDER_DOES_NOT_HAVE_APPROVE_FOR_ALL(ResponseCodeEnum.DELEGATING_SPENDER_DOES_NOT_HAVE_APPROVE_FOR_ALL),

    /**
     * The scheduled transaction could not be created because it's expiration_time was too far in the future.
     */
    SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE(ResponseCodeEnum.SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE),

    /**
     * The scheduled transaction could not be created because it's expiration_time was less than or equal to the consensus time.
     */
    SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME(ResponseCodeEnum.SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME),

    /**
     * The scheduled transaction could not be created because it would cause throttles to be violated on the specified expiration_time.
     */
    SCHEDULE_FUTURE_THROTTLE_EXCEEDED(ResponseCodeEnum.SCHEDULE_FUTURE_THROTTLE_EXCEEDED),

    /**
     * The scheduled transaction could not be created because it would cause the gas limit to be violated on the specified expiration_time.
     */
    SCHEDULE_FUTURE_GAS_LIMIT_EXCEEDED(ResponseCodeEnum.SCHEDULE_FUTURE_GAS_LIMIT_EXCEEDED);

    final ResponseCodeEnum code;

    Status(ResponseCodeEnum code) {
        this.code = code;
    }

    static Status valueOf(ResponseCodeEnum code) {
        switch (code) {
            case OK:
                return OK;
            case INVALID_TRANSACTION:
                return INVALID_TRANSACTION;
            case PAYER_ACCOUNT_NOT_FOUND:
                return PAYER_ACCOUNT_NOT_FOUND;
            case INVALID_NODE_ACCOUNT:
                return INVALID_NODE_ACCOUNT;
            case TRANSACTION_EXPIRED:
                return TRANSACTION_EXPIRED;
            case INVALID_TRANSACTION_START:
                return INVALID_TRANSACTION_START;
            case INVALID_TRANSACTION_DURATION:
                return INVALID_TRANSACTION_DURATION;
            case INVALID_SIGNATURE:
                return INVALID_SIGNATURE;
            case MEMO_TOO_LONG:
                return MEMO_TOO_LONG;
            case INSUFFICIENT_TX_FEE:
                return INSUFFICIENT_TX_FEE;
            case INSUFFICIENT_PAYER_BALANCE:
                return INSUFFICIENT_PAYER_BALANCE;
            case DUPLICATE_TRANSACTION:
                return DUPLICATE_TRANSACTION;
            case BUSY:
                return BUSY;
            case NOT_SUPPORTED:
                return NOT_SUPPORTED;
            case INVALID_FILE_ID:
                return INVALID_FILE_ID;
            case INVALID_ACCOUNT_ID:
                return INVALID_ACCOUNT_ID;
            case INVALID_CONTRACT_ID:
                return INVALID_CONTRACT_ID;
            case INVALID_TRANSACTION_ID:
                return INVALID_TRANSACTION_ID;
            case RECEIPT_NOT_FOUND:
                return RECEIPT_NOT_FOUND;
            case RECORD_NOT_FOUND:
                return RECORD_NOT_FOUND;
            case INVALID_SOLIDITY_ID:
                return INVALID_SOLIDITY_ID;
            case UNKNOWN:
                return UNKNOWN;
            case SUCCESS:
                return SUCCESS;
            case FAIL_INVALID:
                return FAIL_INVALID;
            case FAIL_FEE:
                return FAIL_FEE;
            case FAIL_BALANCE:
                return FAIL_BALANCE;
            case KEY_REQUIRED:
                return KEY_REQUIRED;
            case BAD_ENCODING:
                return BAD_ENCODING;
            case INSUFFICIENT_ACCOUNT_BALANCE:
                return INSUFFICIENT_ACCOUNT_BALANCE;
            case INVALID_SOLIDITY_ADDRESS:
                return INVALID_SOLIDITY_ADDRESS;
            case INSUFFICIENT_GAS:
                return INSUFFICIENT_GAS;
            case CONTRACT_SIZE_LIMIT_EXCEEDED:
                return CONTRACT_SIZE_LIMIT_EXCEEDED;
            case LOCAL_CALL_MODIFICATION_EXCEPTION:
                return LOCAL_CALL_MODIFICATION_EXCEPTION;
            case CONTRACT_REVERT_EXECUTED:
                return CONTRACT_REVERT_EXECUTED;
            case CONTRACT_EXECUTION_EXCEPTION:
                return CONTRACT_EXECUTION_EXCEPTION;
            case INVALID_RECEIVING_NODE_ACCOUNT:
                return INVALID_RECEIVING_NODE_ACCOUNT;
            case MISSING_QUERY_HEADER:
                return MISSING_QUERY_HEADER;
            case ACCOUNT_UPDATE_FAILED:
                return ACCOUNT_UPDATE_FAILED;
            case INVALID_KEY_ENCODING:
                return INVALID_KEY_ENCODING;
            case NULL_SOLIDITY_ADDRESS:
                return NULL_SOLIDITY_ADDRESS;
            case CONTRACT_UPDATE_FAILED:
                return CONTRACT_UPDATE_FAILED;
            case INVALID_QUERY_HEADER:
                return INVALID_QUERY_HEADER;
            case INVALID_FEE_SUBMITTED:
                return INVALID_FEE_SUBMITTED;
            case INVALID_PAYER_SIGNATURE:
                return INVALID_PAYER_SIGNATURE;
            case KEY_NOT_PROVIDED:
                return KEY_NOT_PROVIDED;
            case INVALID_EXPIRATION_TIME:
                return INVALID_EXPIRATION_TIME;
            case NO_WACL_KEY:
                return NO_WACL_KEY;
            case FILE_CONTENT_EMPTY:
                return FILE_CONTENT_EMPTY;
            case INVALID_ACCOUNT_AMOUNTS:
                return INVALID_ACCOUNT_AMOUNTS;
            case EMPTY_TRANSACTION_BODY:
                return EMPTY_TRANSACTION_BODY;
            case INVALID_TRANSACTION_BODY:
                return INVALID_TRANSACTION_BODY;
            case INVALID_SIGNATURE_TYPE_MISMATCHING_KEY:
                return INVALID_SIGNATURE_TYPE_MISMATCHING_KEY;
            case INVALID_SIGNATURE_COUNT_MISMATCHING_KEY:
                return INVALID_SIGNATURE_COUNT_MISMATCHING_KEY;
            case EMPTY_LIVE_HASH_BODY:
                return EMPTY_LIVE_HASH_BODY;
            case EMPTY_LIVE_HASH:
                return EMPTY_LIVE_HASH;
            case EMPTY_LIVE_HASH_KEYS:
                return EMPTY_LIVE_HASH_KEYS;
            case INVALID_LIVE_HASH_SIZE:
                return INVALID_LIVE_HASH_SIZE;
            case EMPTY_QUERY_BODY:
                return EMPTY_QUERY_BODY;
            case EMPTY_LIVE_HASH_QUERY:
                return EMPTY_LIVE_HASH_QUERY;
            case LIVE_HASH_NOT_FOUND:
                return LIVE_HASH_NOT_FOUND;
            case ACCOUNT_ID_DOES_NOT_EXIST:
                return ACCOUNT_ID_DOES_NOT_EXIST;
            case LIVE_HASH_ALREADY_EXISTS:
                return LIVE_HASH_ALREADY_EXISTS;
            case INVALID_FILE_WACL:
                return INVALID_FILE_WACL;
            case SERIALIZATION_FAILED:
                return SERIALIZATION_FAILED;
            case TRANSACTION_OVERSIZE:
                return TRANSACTION_OVERSIZE;
            case TRANSACTION_TOO_MANY_LAYERS:
                return TRANSACTION_TOO_MANY_LAYERS;
            case CONTRACT_DELETED:
                return CONTRACT_DELETED;
            case PLATFORM_NOT_ACTIVE:
                return PLATFORM_NOT_ACTIVE;
            case KEY_PREFIX_MISMATCH:
                return KEY_PREFIX_MISMATCH;
            case PLATFORM_TRANSACTION_NOT_CREATED:
                return PLATFORM_TRANSACTION_NOT_CREATED;
            case INVALID_RENEWAL_PERIOD:
                return INVALID_RENEWAL_PERIOD;
            case INVALID_PAYER_ACCOUNT_ID:
                return INVALID_PAYER_ACCOUNT_ID;
            case ACCOUNT_DELETED:
                return ACCOUNT_DELETED;
            case FILE_DELETED:
                return FILE_DELETED;
            case ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS:
                return ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS;
            case SETTING_NEGATIVE_ACCOUNT_BALANCE:
                return SETTING_NEGATIVE_ACCOUNT_BALANCE;
            case OBTAINER_REQUIRED:
                return OBTAINER_REQUIRED;
            case OBTAINER_SAME_CONTRACT_ID:
                return OBTAINER_SAME_CONTRACT_ID;
            case OBTAINER_DOES_NOT_EXIST:
                return OBTAINER_DOES_NOT_EXIST;
            case MODIFYING_IMMUTABLE_CONTRACT:
                return MODIFYING_IMMUTABLE_CONTRACT;
            case FILE_SYSTEM_EXCEPTION:
                return FILE_SYSTEM_EXCEPTION;
            case AUTORENEW_DURATION_NOT_IN_RANGE:
                return AUTORENEW_DURATION_NOT_IN_RANGE;
            case ERROR_DECODING_BYTESTRING:
                return ERROR_DECODING_BYTESTRING;
            case CONTRACT_FILE_EMPTY:
                return CONTRACT_FILE_EMPTY;
            case CONTRACT_BYTECODE_EMPTY:
                return CONTRACT_BYTECODE_EMPTY;
            case INVALID_INITIAL_BALANCE:
                return INVALID_INITIAL_BALANCE;
            case INVALID_RECEIVE_RECORD_THRESHOLD:
                return INVALID_RECEIVE_RECORD_THRESHOLD;
            case INVALID_SEND_RECORD_THRESHOLD:
                return INVALID_SEND_RECORD_THRESHOLD;
            case ACCOUNT_IS_NOT_GENESIS_ACCOUNT:
                return ACCOUNT_IS_NOT_GENESIS_ACCOUNT;
            case PAYER_ACCOUNT_UNAUTHORIZED:
                return PAYER_ACCOUNT_UNAUTHORIZED;
            case INVALID_FREEZE_TRANSACTION_BODY:
                return INVALID_FREEZE_TRANSACTION_BODY;
            case FREEZE_TRANSACTION_BODY_NOT_FOUND:
                return FREEZE_TRANSACTION_BODY_NOT_FOUND;
            case TRANSFER_LIST_SIZE_LIMIT_EXCEEDED:
                return TRANSFER_LIST_SIZE_LIMIT_EXCEEDED;
            case RESULT_SIZE_LIMIT_EXCEEDED:
                return RESULT_SIZE_LIMIT_EXCEEDED;
            case NOT_SPECIAL_ACCOUNT:
                return NOT_SPECIAL_ACCOUNT;
            case CONTRACT_NEGATIVE_GAS:
                return CONTRACT_NEGATIVE_GAS;
            case CONTRACT_NEGATIVE_VALUE:
                return CONTRACT_NEGATIVE_VALUE;
            case INVALID_FEE_FILE:
                return INVALID_FEE_FILE;
            case INVALID_EXCHANGE_RATE_FILE:
                return INVALID_EXCHANGE_RATE_FILE;
            case INSUFFICIENT_LOCAL_CALL_GAS:
                return INSUFFICIENT_LOCAL_CALL_GAS;
            case ENTITY_NOT_ALLOWED_TO_DELETE:
                return ENTITY_NOT_ALLOWED_TO_DELETE;
            case AUTHORIZATION_FAILED:
                return AUTHORIZATION_FAILED;
            case FILE_UPLOADED_PROTO_INVALID:
                return FILE_UPLOADED_PROTO_INVALID;
            case FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK:
                return FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK;
            case FEE_SCHEDULE_FILE_PART_UPLOADED:
                return FEE_SCHEDULE_FILE_PART_UPLOADED;
            case EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED:
                return EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED;
            case MAX_CONTRACT_STORAGE_EXCEEDED:
                return MAX_CONTRACT_STORAGE_EXCEEDED;
            case TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT:
                return TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT;
            case TOTAL_LEDGER_BALANCE_INVALID:
                return TOTAL_LEDGER_BALANCE_INVALID;
            case EXPIRATION_REDUCTION_NOT_ALLOWED:
                return EXPIRATION_REDUCTION_NOT_ALLOWED;
            case MAX_GAS_LIMIT_EXCEEDED:
                return MAX_GAS_LIMIT_EXCEEDED;
            case MAX_FILE_SIZE_EXCEEDED:
                return MAX_FILE_SIZE_EXCEEDED;
            case RECEIVER_SIG_REQUIRED:
                return RECEIVER_SIG_REQUIRED;
            case INVALID_TOPIC_ID:
                return INVALID_TOPIC_ID;
            case INVALID_ADMIN_KEY:
                return INVALID_ADMIN_KEY;
            case INVALID_SUBMIT_KEY:
                return INVALID_SUBMIT_KEY;
            case UNAUTHORIZED:
                return UNAUTHORIZED;
            case INVALID_TOPIC_MESSAGE:
                return INVALID_TOPIC_MESSAGE;
            case INVALID_AUTORENEW_ACCOUNT:
                return INVALID_AUTORENEW_ACCOUNT;
            case AUTORENEW_ACCOUNT_NOT_ALLOWED:
                return AUTORENEW_ACCOUNT_NOT_ALLOWED;
            case TOPIC_EXPIRED:
                return TOPIC_EXPIRED;
            case INVALID_CHUNK_NUMBER:
                return INVALID_CHUNK_NUMBER;
            case INVALID_CHUNK_TRANSACTION_ID:
                return INVALID_CHUNK_TRANSACTION_ID;
            case ACCOUNT_FROZEN_FOR_TOKEN:
                return ACCOUNT_FROZEN_FOR_TOKEN;
            case TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED:
                return TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED;
            case INVALID_TOKEN_ID:
                return INVALID_TOKEN_ID;
            case INVALID_TOKEN_DECIMALS:
                return INVALID_TOKEN_DECIMALS;
            case INVALID_TOKEN_INITIAL_SUPPLY:
                return INVALID_TOKEN_INITIAL_SUPPLY;
            case INVALID_TREASURY_ACCOUNT_FOR_TOKEN:
                return INVALID_TREASURY_ACCOUNT_FOR_TOKEN;
            case INVALID_TOKEN_SYMBOL:
                return INVALID_TOKEN_SYMBOL;
            case TOKEN_HAS_NO_FREEZE_KEY:
                return TOKEN_HAS_NO_FREEZE_KEY;
            case TRANSFERS_NOT_ZERO_SUM_FOR_TOKEN:
                return TRANSFERS_NOT_ZERO_SUM_FOR_TOKEN;
            case MISSING_TOKEN_SYMBOL:
                return MISSING_TOKEN_SYMBOL;
            case TOKEN_SYMBOL_TOO_LONG:
                return TOKEN_SYMBOL_TOO_LONG;
            case ACCOUNT_KYC_NOT_GRANTED_FOR_TOKEN:
                return ACCOUNT_KYC_NOT_GRANTED_FOR_TOKEN;
            case TOKEN_HAS_NO_KYC_KEY:
                return TOKEN_HAS_NO_KYC_KEY;
            case INSUFFICIENT_TOKEN_BALANCE:
                return INSUFFICIENT_TOKEN_BALANCE;
            case TOKEN_WAS_DELETED:
                return TOKEN_WAS_DELETED;
            case TOKEN_HAS_NO_SUPPLY_KEY:
                return TOKEN_HAS_NO_SUPPLY_KEY;
            case TOKEN_HAS_NO_WIPE_KEY:
                return TOKEN_HAS_NO_WIPE_KEY;
            case INVALID_TOKEN_MINT_AMOUNT:
                return INVALID_TOKEN_MINT_AMOUNT;
            case INVALID_TOKEN_BURN_AMOUNT:
                return INVALID_TOKEN_BURN_AMOUNT;
            case TOKEN_NOT_ASSOCIATED_TO_ACCOUNT:
                return TOKEN_NOT_ASSOCIATED_TO_ACCOUNT;
            case CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT:
                return CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT;
            case INVALID_KYC_KEY:
                return INVALID_KYC_KEY;
            case INVALID_WIPE_KEY:
                return INVALID_WIPE_KEY;
            case INVALID_FREEZE_KEY:
                return INVALID_FREEZE_KEY;
            case INVALID_SUPPLY_KEY:
                return INVALID_SUPPLY_KEY;
            case MISSING_TOKEN_NAME:
                return MISSING_TOKEN_NAME;
            case TOKEN_NAME_TOO_LONG:
                return TOKEN_NAME_TOO_LONG;
            case INVALID_WIPING_AMOUNT:
                return INVALID_WIPING_AMOUNT;
            case TOKEN_IS_IMMUTABLE:
                return TOKEN_IS_IMMUTABLE;
            case TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT:
                return TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT;
            case TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES:
                return TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES;
            case ACCOUNT_IS_TREASURY:
                return ACCOUNT_IS_TREASURY;
            case TOKEN_ID_REPEATED_IN_TOKEN_LIST:
                return TOKEN_ID_REPEATED_IN_TOKEN_LIST;
            case TOKEN_TRANSFER_LIST_SIZE_LIMIT_EXCEEDED:
                return TOKEN_TRANSFER_LIST_SIZE_LIMIT_EXCEEDED;
            case EMPTY_TOKEN_TRANSFER_BODY:
                return EMPTY_TOKEN_TRANSFER_BODY;
            case EMPTY_TOKEN_TRANSFER_ACCOUNT_AMOUNTS:
                return EMPTY_TOKEN_TRANSFER_ACCOUNT_AMOUNTS;
            case INVALID_SCHEDULE_ID:
                return INVALID_SCHEDULE_ID;
            case SCHEDULE_IS_IMMUTABLE:
                return SCHEDULE_IS_IMMUTABLE;
            case INVALID_SCHEDULE_PAYER_ID:
                return INVALID_SCHEDULE_PAYER_ID;
            case INVALID_SCHEDULE_ACCOUNT_ID:
                return INVALID_SCHEDULE_ACCOUNT_ID;
            case NO_NEW_VALID_SIGNATURES:
                return NO_NEW_VALID_SIGNATURES;
            case UNRESOLVABLE_REQUIRED_SIGNERS:
                return UNRESOLVABLE_REQUIRED_SIGNERS;
            case SCHEDULED_TRANSACTION_NOT_IN_WHITELIST:
                return SCHEDULED_TRANSACTION_NOT_IN_WHITELIST;
            case SOME_SIGNATURES_WERE_INVALID:
                return SOME_SIGNATURES_WERE_INVALID;
            case TRANSACTION_ID_FIELD_NOT_ALLOWED:
                return TRANSACTION_ID_FIELD_NOT_ALLOWED;
            case IDENTICAL_SCHEDULE_ALREADY_CREATED:
                return IDENTICAL_SCHEDULE_ALREADY_CREATED;
            case INVALID_ZERO_BYTE_IN_STRING:
                return INVALID_ZERO_BYTE_IN_STRING;
            case SCHEDULE_ALREADY_DELETED:
                return SCHEDULE_ALREADY_DELETED;
            case SCHEDULE_ALREADY_EXECUTED:
                return SCHEDULE_ALREADY_EXECUTED;
            case MESSAGE_SIZE_TOO_LARGE:
                return MESSAGE_SIZE_TOO_LARGE;
            case OPERATION_REPEATED_IN_BUCKET_GROUPS:
                return OPERATION_REPEATED_IN_BUCKET_GROUPS;
            case BUCKET_CAPACITY_OVERFLOW:
                return BUCKET_CAPACITY_OVERFLOW;
            case NODE_CAPACITY_NOT_SUFFICIENT_FOR_OPERATION:
                return NODE_CAPACITY_NOT_SUFFICIENT_FOR_OPERATION;
            case BUCKET_HAS_NO_THROTTLE_GROUPS:
                return BUCKET_HAS_NO_THROTTLE_GROUPS;
            case THROTTLE_GROUP_HAS_ZERO_OPS_PER_SEC:
                return THROTTLE_GROUP_HAS_ZERO_OPS_PER_SEC;
            case SUCCESS_BUT_MISSING_EXPECTED_OPERATION:
                return SUCCESS_BUT_MISSING_EXPECTED_OPERATION;
            case UNPARSEABLE_THROTTLE_DEFINITIONS:
                return UNPARSEABLE_THROTTLE_DEFINITIONS;
            case INVALID_THROTTLE_DEFINITIONS:
                return INVALID_THROTTLE_DEFINITIONS;
            case ACCOUNT_EXPIRED_AND_PENDING_REMOVAL:
                return ACCOUNT_EXPIRED_AND_PENDING_REMOVAL;
            case INVALID_TOKEN_MAX_SUPPLY:
                return INVALID_TOKEN_MAX_SUPPLY;
            case INVALID_TOKEN_NFT_SERIAL_NUMBER:
                return INVALID_TOKEN_NFT_SERIAL_NUMBER;
            case INVALID_NFT_ID:
                return INVALID_NFT_ID;
            case METADATA_TOO_LONG:
                return METADATA_TOO_LONG;
            case BATCH_SIZE_LIMIT_EXCEEDED:
                return BATCH_SIZE_LIMIT_EXCEEDED;
            case INVALID_QUERY_RANGE:
                return INVALID_QUERY_RANGE;
            case FRACTION_DIVIDES_BY_ZERO:
                return FRACTION_DIVIDES_BY_ZERO;
            case INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE:
                return INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE;
            case CUSTOM_FEES_LIST_TOO_LONG:
                return CUSTOM_FEES_LIST_TOO_LONG;
            case INVALID_CUSTOM_FEE_COLLECTOR:
                return INVALID_CUSTOM_FEE_COLLECTOR;
            case INVALID_TOKEN_ID_IN_CUSTOM_FEES:
                return INVALID_TOKEN_ID_IN_CUSTOM_FEES;
            case TOKEN_NOT_ASSOCIATED_TO_FEE_COLLECTOR:
                return TOKEN_NOT_ASSOCIATED_TO_FEE_COLLECTOR;
            case TOKEN_MAX_SUPPLY_REACHED:
                return TOKEN_MAX_SUPPLY_REACHED;
            case SENDER_DOES_NOT_OWN_NFT_SERIAL_NO:
                return SENDER_DOES_NOT_OWN_NFT_SERIAL_NO;
            case CUSTOM_FEE_NOT_FULLY_SPECIFIED:
                return CUSTOM_FEE_NOT_FULLY_SPECIFIED;
            case CUSTOM_FEE_MUST_BE_POSITIVE:
                return CUSTOM_FEE_MUST_BE_POSITIVE;
            case TOKEN_HAS_NO_FEE_SCHEDULE_KEY:
                return TOKEN_HAS_NO_FEE_SCHEDULE_KEY;
            case CUSTOM_FEE_OUTSIDE_NUMERIC_RANGE:
                return CUSTOM_FEE_OUTSIDE_NUMERIC_RANGE;
            case ROYALTY_FRACTION_CANNOT_EXCEED_ONE:
                return ROYALTY_FRACTION_CANNOT_EXCEED_ONE;
            case FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT:
                return FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT;
            case CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES:
                return CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES;
            case CUSTOM_FEE_DENOMINATION_MUST_BE_FUNGIBLE_COMMON:
                return CUSTOM_FEE_DENOMINATION_MUST_BE_FUNGIBLE_COMMON;
            case CUSTOM_FRACTIONAL_FEE_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON:
                return CUSTOM_FRACTIONAL_FEE_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON;
            case INVALID_CUSTOM_FEE_SCHEDULE_KEY:
                return INVALID_CUSTOM_FEE_SCHEDULE_KEY;
            case INVALID_TOKEN_MINT_METADATA:
                return INVALID_TOKEN_MINT_METADATA;
            case INVALID_TOKEN_BURN_METADATA:
                return INVALID_TOKEN_BURN_METADATA;
            case CURRENT_TREASURY_STILL_OWNS_NFTS:
                return CURRENT_TREASURY_STILL_OWNS_NFTS;
            case ACCOUNT_STILL_OWNS_NFTS:
                return ACCOUNT_STILL_OWNS_NFTS;
            case TREASURY_MUST_OWN_BURNED_NFT:
                return TREASURY_MUST_OWN_BURNED_NFT;
            case ACCOUNT_DOES_NOT_OWN_WIPED_NFT:
                return ACCOUNT_DOES_NOT_OWN_WIPED_NFT;
            case ACCOUNT_AMOUNT_TRANSFERS_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON:
                return ACCOUNT_AMOUNT_TRANSFERS_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON;
            case MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED:
                return MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED;
            case PAYER_ACCOUNT_DELETED:
                return PAYER_ACCOUNT_DELETED;
            case CUSTOM_FEE_CHARGING_EXCEEDED_MAX_RECURSION_DEPTH:
                return CUSTOM_FEE_CHARGING_EXCEEDED_MAX_RECURSION_DEPTH;
            case CUSTOM_FEE_CHARGING_EXCEEDED_MAX_ACCOUNT_AMOUNTS:
                return CUSTOM_FEE_CHARGING_EXCEEDED_MAX_ACCOUNT_AMOUNTS;
            case INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE:
                return INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE;
            case SERIAL_NUMBER_LIMIT_REACHED:
                return SERIAL_NUMBER_LIMIT_REACHED;
            case CUSTOM_ROYALTY_FEE_ONLY_ALLOWED_FOR_NON_FUNGIBLE_UNIQUE:
                return CUSTOM_ROYALTY_FEE_ONLY_ALLOWED_FOR_NON_FUNGIBLE_UNIQUE;
            case NO_REMAINING_AUTOMATIC_ASSOCIATIONS:
                return NO_REMAINING_AUTOMATIC_ASSOCIATIONS;
            case EXISTING_AUTOMATIC_ASSOCIATIONS_EXCEED_GIVEN_LIMIT:
                return EXISTING_AUTOMATIC_ASSOCIATIONS_EXCEED_GIVEN_LIMIT;
            case REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT:
                return REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT;
            case TOKEN_IS_PAUSED:
                return TOKEN_IS_PAUSED;
            case TOKEN_HAS_NO_PAUSE_KEY:
                return TOKEN_HAS_NO_PAUSE_KEY;
            case INVALID_PAUSE_KEY:
                return INVALID_PAUSE_KEY;
            case FREEZE_UPDATE_FILE_DOES_NOT_EXIST:
                return FREEZE_UPDATE_FILE_DOES_NOT_EXIST;
            case FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH:
                return FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH;
            case NO_UPGRADE_HAS_BEEN_PREPARED:
                return NO_UPGRADE_HAS_BEEN_PREPARED;
            case NO_FREEZE_IS_SCHEDULED:
                return NO_FREEZE_IS_SCHEDULED;
            case UPDATE_FILE_HASH_CHANGED_SINCE_PREPARE_UPGRADE:
                return UPDATE_FILE_HASH_CHANGED_SINCE_PREPARE_UPGRADE;
            case FREEZE_START_TIME_MUST_BE_FUTURE:
                return FREEZE_START_TIME_MUST_BE_FUTURE;
            case PREPARED_UPDATE_FILE_IS_IMMUTABLE:
                return PREPARED_UPDATE_FILE_IS_IMMUTABLE;
            case FREEZE_ALREADY_SCHEDULED:
                return FREEZE_ALREADY_SCHEDULED;
            case FREEZE_UPGRADE_IN_PROGRESS:
                return FREEZE_UPGRADE_IN_PROGRESS;
            case UPDATE_FILE_ID_DOES_NOT_MATCH_PREPARED:
                return UPDATE_FILE_ID_DOES_NOT_MATCH_PREPARED;
            case UPDATE_FILE_HASH_DOES_NOT_MATCH_PREPARED:
                return UPDATE_FILE_HASH_DOES_NOT_MATCH_PREPARED;
            case CONSENSUS_GAS_EXHAUSTED:
                return CONSENSUS_GAS_EXHAUSTED;
            case REVERTED_SUCCESS:
                return REVERTED_SUCCESS;
            case MAX_STORAGE_IN_PRICE_REGIME_HAS_BEEN_USED:
                return MAX_STORAGE_IN_PRICE_REGIME_HAS_BEEN_USED;
            case INVALID_ALIAS_KEY:
                return INVALID_ALIAS_KEY;
            case UNEXPECTED_TOKEN_DECIMALS:
                return UNEXPECTED_TOKEN_DECIMALS;
            case INVALID_PROXY_ACCOUNT_ID:
                return INVALID_PROXY_ACCOUNT_ID;
            case INVALID_TRANSFER_ACCOUNT_ID:
                return INVALID_TRANSFER_ACCOUNT_ID;
            case INVALID_FEE_COLLECTOR_ACCOUNT_ID:
                return INVALID_FEE_COLLECTOR_ACCOUNT_ID;
            case ALIAS_IS_IMMUTABLE:
                return ALIAS_IS_IMMUTABLE;
            case SPENDER_ACCOUNT_SAME_AS_OWNER:
                return SPENDER_ACCOUNT_SAME_AS_OWNER;
            case AMOUNT_EXCEEDS_TOKEN_MAX_SUPPLY:
                return AMOUNT_EXCEEDS_TOKEN_MAX_SUPPLY;
            case NEGATIVE_ALLOWANCE_AMOUNT:
                return NEGATIVE_ALLOWANCE_AMOUNT;
            case CANNOT_APPROVE_FOR_ALL_FUNGIBLE_COMMON:
                return CANNOT_APPROVE_FOR_ALL_FUNGIBLE_COMMON;
            case SPENDER_DOES_NOT_HAVE_ALLOWANCE:
                return SPENDER_DOES_NOT_HAVE_ALLOWANCE;
            case AMOUNT_EXCEEDS_ALLOWANCE:
                return AMOUNT_EXCEEDS_ALLOWANCE;
            case MAX_ALLOWANCES_EXCEEDED:
                return MAX_ALLOWANCES_EXCEEDED;
            case EMPTY_ALLOWANCES:
                return EMPTY_ALLOWANCES;
            case SPENDER_ACCOUNT_REPEATED_IN_ALLOWANCES:
                return SPENDER_ACCOUNT_REPEATED_IN_ALLOWANCES;
            case REPEATED_SERIAL_NUMS_IN_NFT_ALLOWANCES:
                return REPEATED_SERIAL_NUMS_IN_NFT_ALLOWANCES;
            case FUNGIBLE_TOKEN_IN_NFT_ALLOWANCES:
                return FUNGIBLE_TOKEN_IN_NFT_ALLOWANCES;
            case NFT_IN_FUNGIBLE_TOKEN_ALLOWANCES:
                return NFT_IN_FUNGIBLE_TOKEN_ALLOWANCES;
            case INVALID_ALLOWANCE_OWNER_ID:
                return INVALID_ALLOWANCE_OWNER_ID;
            case INVALID_ALLOWANCE_SPENDER_ID:
                return INVALID_ALLOWANCE_SPENDER_ID;
            case REPEATED_ALLOWANCES_TO_DELETE:
                return REPEATED_ALLOWANCES_TO_DELETE;
            case INVALID_DELEGATING_SPENDER:
                return INVALID_DELEGATING_SPENDER;
            case DELEGATING_SPENDER_CANNOT_GRANT_APPROVE_FOR_ALL:
                return DELEGATING_SPENDER_CANNOT_GRANT_APPROVE_FOR_ALL;
            case DELEGATING_SPENDER_DOES_NOT_HAVE_APPROVE_FOR_ALL:
                return DELEGATING_SPENDER_DOES_NOT_HAVE_APPROVE_FOR_ALL;
            case SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE:
                return SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE;
            case SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME:
                return SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME;
            case SCHEDULE_FUTURE_THROTTLE_EXCEEDED:
                return SCHEDULE_FUTURE_THROTTLE_EXCEEDED;
            case SCHEDULE_FUTURE_GAS_LIMIT_EXCEEDED:
                return SCHEDULE_FUTURE_GAS_LIMIT_EXCEEDED;
            case UNRECOGNIZED:
                // NOTE: Protobuf deserialization will not give us the code on the wire
                throw new IllegalArgumentException(
                    "network returned unrecognized response code; your SDK may be out of date");
        }

        // NOTE: This should be unreachable as error prone has enum exhaustiveness checking
        throw new IllegalArgumentException(
            "response code "
                + code.name()
                + " is unhandled by the SDK; update your SDK or open an issue");
    }

    @Override
    public String toString() {
        return code.name();
    }
}

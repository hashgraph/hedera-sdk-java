package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

/**
 * Returned in {@link TransactionReceipt}, {@link HederaPreCheckStatusException}
 * and {@link HederaReceiptStatusException}.
 * <p>
 * The success variant is {@link #SUCCESS} which is what a {@link TransactionReceipt} will contain for a
 * successful transaction.
 */
public enum Status {
    /**
     * The transaction passed the pre-check validation.
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
     * Node Account provided does not match the node account of the node the transaction was
     * submitted to.
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
     * valid transaction duration is a positive non zero number that does not exceed 120 seconds
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
     * This transaction ID is a duplicate of one that was submitted to this node or reached
     * consensus in the last 180 seconds (receipt period)
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
     * Transaction hasn't yet reached consensus, or has already expired
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
     * the claim body is empty
     */
    EMPTY_LIVE_HASH_BODY(ResponseCodeEnum.EMPTY_LIVE_HASH_BODY),

    /**
     * the hash for the claim is empty
     */
    EMPTY_LIVE_HASH_HASH(ResponseCodeEnum.EMPTY_LIVE_HASH),

    /**
     * the key list is empty
     */
    EMPTY_LIVE_HASH_KEYS(ResponseCodeEnum.EMPTY_LIVE_HASH_KEYS),

    /**
     * the size of the claim hash is not 48 bytes
     */
    INVALID_LIVE_HASH_HASH_SIZE(ResponseCodeEnum.INVALID_LIVE_HASH_SIZE),

    /**
     * the query body is empty
     */
    EMPTY_QUERY_BODY(ResponseCodeEnum.EMPTY_QUERY_BODY),

    /**
     * the crypto claim query is empty
     */
    EMPTY_LIVE_HASH_QUERY(ResponseCodeEnum.EMPTY_LIVE_HASH_QUERY),

    /**
     * the crypto claim doesn't exists in the file system. It expired or was never persisted.
     */
    LIVE_HASH_NOT_FOUND(ResponseCodeEnum.LIVE_HASH_NOT_FOUND),

    /**
     * the account id passed has not yet been created.
     */
    ACCOUNT_ID_DOES_NOT_EXIST(ResponseCodeEnum.ACCOUNT_ID_DOES_NOT_EXIST),

    /**
     * the claim hash already exists
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
     * transaction not created by platform due to either large backlog or message size exceeded
     * transactionMaxBytes
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
     * when deleting smart contract that has crypto balance either transfer account or transfer
     * smart contract is required
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
     * Decoding the smart contract binary to a byte array failed. Check that the input is a valid
     * hex string.
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
     * attempt to set negative receive record threshold
     */
    INVALID_RECEIVE_RECORD_THRESHOLD(ResponseCodeEnum.INVALID_RECEIVE_RECORD_THRESHOLD),

    /**
     * attempt to set negative send record threshold
     */
    INVALID_SEND_RECORD_THRESHOLD(ResponseCodeEnum.INVALID_SEND_RECORD_THRESHOLD),

    /**
     * Special Account Operations should be performed by only Genesis account, return this code if
     * it is not Genesis Account
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
     * account 0.0.50 can update all entities from 0.0.51 - 0.0.80, 3) Network Function Master
     * Account A/c 0.0.50 - Update all Network Function accounts &amp; perform all the Network Functions
     * listed below, 4) Network Function Accounts: i) A/c 0.0.55 - Update Address Book files
     * (0.0.101/102), ii) A/c 0.0.56 - Update Fee schedule (0.0.111), iii) A/c 0.0.57 - Update
     * Exchange Rate (0.0.112).
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
     * The Topic ID specified is not in the system.
     */
    INVALID_TOPIC_ID(ResponseCodeEnum.INVALID_TOPIC_ID),

    INVALID_ADMIN_KEY(ResponseCodeEnum.INVALID_ADMIN_KEY),

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
     * The topic has expired, was not automatically renewed, and is in a 7 day grace period before
     * the topic will be deleted unrecoverably. This error response code will not be returned until
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
     *  Account is frozen and cannot transact with the token
     */
    ACCOUNT_FROZEN_FOR_TOKEN(ResponseCodeEnum.ACCOUNT_FROZEN_FOR_TOKEN),

    /**
     * Maximum number of token relations for agiven account is exceeded
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
    INVALID_TOKEN_INITIAL_SUPPLY(ResponseCodeEnum.INVALID_TOKEN_INITIAL_SUPPLY) ,

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
     * Token Symbol is not provided
     */
    MISSING_TOKEN_SYMBOL(ResponseCodeEnum.MISSING_TOKEN_SYMBOL),

    /**
     * Token Symbol is too long
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

    INVALID_TOKEN_MINT_AMOUNT(ResponseCodeEnum.INVALID_TOKEN_MINT_AMOUNT),

    INVALID_TOKEN_BURN_AMOUNT(ResponseCodeEnum.INVALID_TOKEN_BURN_AMOUNT),

    TOKEN_NOT_ASSOCIATED_TO_ACCOUNT(ResponseCodeEnum.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT),

    /**
     * Cannot execute wipe operation on treasury account
     */
    CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT(ResponseCodeEnum.CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT),

    INVALID_KYC_KEY(ResponseCodeEnum.INVALID_KYC_KEY),

    INVALID_WIPE_KEY(ResponseCodeEnum.INVALID_WIPE_KEY),

    INVALID_FREEZE_KEY(ResponseCodeEnum.INVALID_FREEZE_KEY),

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
     * An attempted operation is invalid because the account is a treasury;
     */
    ACCOUNT_IS_TREASURY(ResponseCodeEnum.ACCOUNT_IS_TREASURY);

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
                return EMPTY_LIVE_HASH_HASH;
            case EMPTY_LIVE_HASH_KEYS:
                return EMPTY_LIVE_HASH_KEYS;
            case INVALID_LIVE_HASH_SIZE:
                return INVALID_LIVE_HASH_HASH_SIZE;
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

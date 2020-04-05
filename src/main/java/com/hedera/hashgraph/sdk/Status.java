package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

public enum Status {
    /** The transaction passed the pre-check validation. */
    Ok(ResponseCodeEnum.OK),

    /** For any error not handled by specific error codes listed below. */
    InvalidTransaction(ResponseCodeEnum.INVALID_TRANSACTION),

    /** Payer account does not exist. */
    PayerAccountNotFound(ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND),

    /**
     * Node Account provided does not match the node account of the node the transaction was
     * submitted to.
     */
    InvalidNodeAccount(ResponseCodeEnum.INVALID_NODE_ACCOUNT),

    /**
     * Pre-Check error when TransactionValidStart + transactionValidDuration is less than current
     * consensus time.
     */
    TransactionExpired(ResponseCodeEnum.TRANSACTION_EXPIRED),

    /** Transaction start time is greater than current consensus time */
    InvalidTransactionStart(ResponseCodeEnum.INVALID_TRANSACTION_START),

    /** valid transaction duration is a positive non zero number that does not exceed 120 seconds */
    InvalidTransactionDuration(ResponseCodeEnum.INVALID_TRANSACTION_DURATION),

    /** The transaction signature is not valid */
    InvalidSignature(ResponseCodeEnum.INVALID_SIGNATURE),

    /** Transaction memo size exceeded 100 bytes */
    MemoTooLong(ResponseCodeEnum.MEMO_TOO_LONG),

    /** The fee provided in the transaction is insufficient for this type of transaction */
    InsufficientTxFee(ResponseCodeEnum.INSUFFICIENT_TX_FEE),

    /** The payer account has insufficient cryptocurrency to pay the transaction fee */
    InsufficientPayerBalance(ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE),

    /**
     * This transaction ID is a duplicate of one that was submitted to this node or reached
     * consensus in the last 180 seconds (receipt period)
     */
    DuplicateTransaction(ResponseCodeEnum.DUPLICATE_TRANSACTION),

    /** If API is throttled out */
    Busy(ResponseCodeEnum.BUSY),

    /** The API is not currently supported */
    NotSupported(ResponseCodeEnum.NOT_SUPPORTED),

    /** The file id is invalid or does not exist */
    InvalidFileId(ResponseCodeEnum.INVALID_FILE_ID),

    /** The account id is invalid or does not exist */
    InvalidAccountId(ResponseCodeEnum.INVALID_ACCOUNT_ID),

    /** The contract id is invalid or does not exist */
    InvalidContractId(ResponseCodeEnum.INVALID_CONTRACT_ID),

    /** Transaction id is not valid */
    InvalidTransactionId(ResponseCodeEnum.INVALID_TRANSACTION_ID),

    /** Receipt for given transaction id does not exist */
    ReceiptNotFound(ResponseCodeEnum.RECEIPT_NOT_FOUND),

    /** Record for given transaction id does not exist */
    RecordNotFound(ResponseCodeEnum.RECORD_NOT_FOUND),

    /** The solidity id is invalid or entity with this solidity id does not exist */
    InvalidSolidityId(ResponseCodeEnum.INVALID_SOLIDITY_ID),

    /** Transaction hasn't yet reached consensus, or has already expired */
    Unknown(ResponseCodeEnum.UNKNOWN),

    /** The transaction succeeded */
    Success(ResponseCodeEnum.SUCCESS),

    /**
     * There was a system error and the transaction failed because of invalid request parameters.
     */
    FailInvalid(ResponseCodeEnum.FAIL_INVALID),

    /** There was a system error while performing fee calculation, reserved for future. */
    FailFee(ResponseCodeEnum.FAIL_FEE),

    /** There was a system error while performing balance checks, reserved for future. */
    FailBalance(ResponseCodeEnum.FAIL_BALANCE),

    /** Key not provided in the transaction body */
    KeyRequired(ResponseCodeEnum.KEY_REQUIRED),

    /** Unsupported algorithm/encoding used for keys in the transaction */
    BadEncoding(ResponseCodeEnum.BAD_ENCODING),

    /** When the account balance is not sufficient for the transfer */
    InsufficientAccountBalance(ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE),

    /**
     * During an update transaction when the system is not able to find the Users Solidity address
     */
    InvalidSolidityAddress(ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS),

    /** Not enough gas was supplied to execute transaction */
    InsufficientGas(ResponseCodeEnum.INSUFFICIENT_GAS),

    /** contract byte code size is over the limit */
    ContractSizeLimitExceeded(ResponseCodeEnum.CONTRACT_SIZE_LIMIT_EXCEEDED),

    /** local execution (query) is requested for a function which changes state */
    LocalCallModificationException(ResponseCodeEnum.LOCAL_CALL_MODIFICATION_EXCEPTION),

    /** Contract REVERT OPCODE executed */
    ContractRevertExecuted(ResponseCodeEnum.CONTRACT_REVERT_EXECUTED),

    /**
     * For any contract execution related error not handled by specific error codes listed above.
     */
    ContractExecutionException(ResponseCodeEnum.CONTRACT_EXECUTION_EXCEPTION),

    /**
     * In Query validation, account with +ve(amount) value should be Receiving node account, the
     * receiver account should be only one account in the list
     */
    InvalidReceivingNodeAccount(ResponseCodeEnum.INVALID_RECEIVING_NODE_ACCOUNT),

    /** Header is missing in Query request */
    MissingQueryHeader(ResponseCodeEnum.MISSING_QUERY_HEADER),

    /** The update of the account failed */
    AccountUpdateFailed(ResponseCodeEnum.ACCOUNT_UPDATE_FAILED),

    /** Provided key encoding was not supported by the system */
    InvalidKeyEncoding(ResponseCodeEnum.INVALID_KEY_ENCODING),

    /** null solidity address */
    NullSolidityAddress(ResponseCodeEnum.NULL_SOLIDITY_ADDRESS),

    /** update of the contract failed */
    ContractUpdateFailed(ResponseCodeEnum.CONTRACT_UPDATE_FAILED),

    /** the query header is invalid */
    InvalidQueryHeader(ResponseCodeEnum.INVALID_QUERY_HEADER),

    /** Invalid fee submitted */
    InvalidFeeSubmitted(ResponseCodeEnum.INVALID_FEE_SUBMITTED),

    /** Payer signature is invalid */
    InvalidPayerSignature(ResponseCodeEnum.INVALID_PAYER_SIGNATURE),

    /** The keys were not provided in the request. */
    KeyNotProvided(ResponseCodeEnum.KEY_NOT_PROVIDED),

    /** Expiration time provided in the transaction was invalid. */
    InvalidExpirationTime(ResponseCodeEnum.INVALID_EXPIRATION_TIME),

    /** WriteAccess Control Keys are not provided for the file */
    NoWaclKey(ResponseCodeEnum.NO_WACL_KEY),

    /** The contents of file are provided as empty. */
    FileContentEmpty(ResponseCodeEnum.FILE_CONTENT_EMPTY),

    /** The crypto transfer credit and debit do not sum equal to 0 */
    InvalidAccountAmounts(ResponseCodeEnum.INVALID_ACCOUNT_AMOUNTS),

    /** Transaction body provided is empty */
    EmptyTransactionBody(ResponseCodeEnum.EMPTY_TRANSACTION_BODY),

    /** Invalid transaction body provided */
    InvalidTransactionBody(ResponseCodeEnum.INVALID_TRANSACTION_BODY),

    /**
     * the type of key (base ed25519 key, KeyList, or ThresholdKey) does not match the type of
     * signature (base ed25519 signature, SignatureList, or ThresholdKeySignature)
     */
    InvalidSignatureTypeMismatchingKey(ResponseCodeEnum.INVALID_SIGNATURE_TYPE_MISMATCHING_KEY),

    /**
     * the number of key (KeyList, or ThresholdKey) does not match that of signature (SignatureList,
     * or ThresholdKeySignature). e.g. if a keyList has 3 base keys, then the corresponding
     * signatureList should also have 3 base signatures.
     */
    InvalidSignatureCountMismatchingKey(ResponseCodeEnum.INVALID_SIGNATURE_COUNT_MISMATCHING_KEY),

    /** the claim body is empty */
    EmptyClaimBody(ResponseCodeEnum.EMPTY_CLAIM_BODY),

    /** the hash for the claim is empty */
    EmptyClaimHash(ResponseCodeEnum.EMPTY_CLAIM_HASH),

    /** the key list is empty */
    EmptyClaimKeys(ResponseCodeEnum.EMPTY_CLAIM_KEYS),

    /** the size of the claim hash is not 48 bytes */
    InvalidClaimHashSize(ResponseCodeEnum.INVALID_CLAIM_HASH_SIZE),

    /** the query body is empty */
    EmptyQueryBody(ResponseCodeEnum.EMPTY_QUERY_BODY),

    /** the crypto claim query is empty */
    EmptyClaimQuery(ResponseCodeEnum.EMPTY_CLAIM_QUERY),

    /** the crypto claim doesn't exists in the file system. It expired or was never persisted. */
    ClaimNotFound(ResponseCodeEnum.CLAIM_NOT_FOUND),

    /** the account id passed has not yet been created. */
    AccountIdDoesNotExist(ResponseCodeEnum.ACCOUNT_ID_DOES_NOT_EXIST),

    /** the claim hash already exists */
    ClaimAlreadyExists(ResponseCodeEnum.CLAIM_ALREADY_EXISTS),

    /** File WACL keys are invalid */
    InvalidFileWacl(ResponseCodeEnum.INVALID_FILE_WACL),

    /** Serialization failure */
    SerializationFailed(ResponseCodeEnum.SERIALIZATION_FAILED),

    /** The size of the Transaction is greater than transactionMaxBytes */
    TransactionOversize(ResponseCodeEnum.TRANSACTION_OVERSIZE),

    /** The Transaction has more than 50 levels */
    TransactionTooManyLayers(ResponseCodeEnum.TRANSACTION_TOO_MANY_LAYERS),

    /** Contract is marked as deleted */
    ContractDeleted(ResponseCodeEnum.CONTRACT_DELETED),

    /** the platform node is either disconnected or lagging behind. */
    PlatformNotActive(ResponseCodeEnum.PLATFORM_NOT_ACTIVE),

    /** one public key matches more than one prefixes on the signature map */
    KeyPrefixMismatch(ResponseCodeEnum.KEY_PREFIX_MISMATCH),

    /**
     * transaction not created by platform due to either large backlog or message size exceeded
     * transactionMaxBytes
     */
    PlatformTransactionNotCreated(ResponseCodeEnum.PLATFORM_TRANSACTION_NOT_CREATED),

    /** auto renewal period is not a positive number of seconds */
    InvalidRenewalPeriod(ResponseCodeEnum.INVALID_RENEWAL_PERIOD),

    /** the response code when a smart contract id is passed for a crypto API request */
    InvalidPayerAccountId(ResponseCodeEnum.INVALID_PAYER_ACCOUNT_ID),

    /** the account has been marked as deleted */
    AccountDeleted(ResponseCodeEnum.ACCOUNT_DELETED),

    /** the file has been marked as deleted */
    FileDeleted(ResponseCodeEnum.FILE_DELETED),

    /** same accounts repeated in the transfer account list */
    AccountRepeatedInAccountAmounts(ResponseCodeEnum.ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS),

    /** attempting to set negative balance value for crypto account */
    SettingNegativeAccountBalance(ResponseCodeEnum.SETTING_NEGATIVE_ACCOUNT_BALANCE),

    /**
     * when deleting smart contract that has crypto balance either transfer account or transfer
     * smart contract is required
     */
    ObtainerRequired(ResponseCodeEnum.OBTAINER_REQUIRED),

    /**
     * when deleting smart contract that has crypto balance you can not use the same contract id as
     * transferContractId as the one being deleted
     */
    ObtainerSameContractId(ResponseCodeEnum.OBTAINER_SAME_CONTRACT_ID),

    /** transferAccountId or transferContractId specified for contract delete does not exist */
    ObtainerDoesNotExist(ResponseCodeEnum.OBTAINER_DOES_NOT_EXIST),

    /**
     * attempting to modify (update or delete a immutable smart contract, i.e. one created without a
     * admin key)
     */
    ModifyingImmutableContract(ResponseCodeEnum.MODIFYING_IMMUTABLE_CONTRACT),

    /** Unexpected exception thrown by file system functions */
    FileSystemException(ResponseCodeEnum.FILE_SYSTEM_EXCEPTION),

    /** the duration is not a subset of [MINIMUM_AUTORENEW_DURATION,MAXIMUM_AUTORENEW_DURATION] */
    AutorenewDurationNotInRange(ResponseCodeEnum.AUTORENEW_DURATION_NOT_IN_RANGE),

    /**
     * Decoding the smart contract binary to a byte array failed. Check that the input is a valid
     * hex string.
     */
    ErrorDecodingBytestring(ResponseCodeEnum.ERROR_DECODING_BYTESTRING),

    /** File to create a smart contract was of length zero */
    ContractFileEmpty(ResponseCodeEnum.CONTRACT_FILE_EMPTY),

    /** Bytecode for smart contract is of length zero */
    ContractBytecodeEmpty(ResponseCodeEnum.CONTRACT_BYTECODE_EMPTY),

    /** Attempt to set negative initial balance */
    InvalidInitialBalance(ResponseCodeEnum.INVALID_INITIAL_BALANCE),

    /** attempt to set negative receive record threshold */
    InvalidReceiveRecordThreshold(ResponseCodeEnum.INVALID_RECEIVE_RECORD_THRESHOLD),

    /** attempt to set negative send record threshold */
    InvalidSendRecordThreshold(ResponseCodeEnum.INVALID_SEND_RECORD_THRESHOLD),

    /**
     * Special Account Operations should be performed by only Genesis account, return this code if
     * it is not Genesis Account
     */
    AccountIsNotGenesisAccount(ResponseCodeEnum.ACCOUNT_IS_NOT_GENESIS_ACCOUNT),

    /** The fee payer account doesn't have permission to submit such Transaction */
    PayerAccountUnauthorized(ResponseCodeEnum.PAYER_ACCOUNT_UNAUTHORIZED),

    /** FreezeTransactionBody is invalid */
    InvalidFreezeTransactionBody(ResponseCodeEnum.INVALID_FREEZE_TRANSACTION_BODY),

    /** FreezeTransactionBody does not exist */
    FreezeTransactionBodyNotFound(ResponseCodeEnum.FREEZE_TRANSACTION_BODY_NOT_FOUND),

    /** Exceeded the number of accounts (both from and to) allowed for crypto transfer list */
    TransferListSizeLimitExceeded(ResponseCodeEnum.TRANSFER_LIST_SIZE_LIMIT_EXCEEDED),

    /** Smart contract result size greater than specified maxResultSize */
    ResultSizeLimitExceeded(ResponseCodeEnum.RESULT_SIZE_LIMIT_EXCEEDED),

    /** The payer account is not a special account(account 0.0.55) */
    NotSpecialAccount(ResponseCodeEnum.NOT_SPECIAL_ACCOUNT),

    /** Negative gas was offered in smart contract call */
    ContractNegativeGas(ResponseCodeEnum.CONTRACT_NEGATIVE_GAS),

    /** Negative value / initial balance was specified in a smart contract call / create */
    ContractNegativeValue(ResponseCodeEnum.CONTRACT_NEGATIVE_VALUE),

    /** Failed to update fee file */
    InvalidFeeFile(ResponseCodeEnum.INVALID_FEE_FILE),

    /** Failed to update exchange rate file */
    InvalidExchangeRateFile(ResponseCodeEnum.INVALID_EXCHANGE_RATE_FILE),

    /** Payment tendered for contract local call cannot cover both the fee and the gas */
    InsufficientLocalCallGas(ResponseCodeEnum.INSUFFICIENT_LOCAL_CALL_GAS),

    /** Entities with Entity ID below 1000 are not allowed to be deleted */
    EntityNotAllowedToDelete(ResponseCodeEnum.ENTITY_NOT_ALLOWED_TO_DELETE),

    /**
     * Violating one of these rules: 1) treasury account can update all entities below 0.0.1000, 2)
     * account 0.0.50 can update all entities from 0.0.51 - 0.0.80, 3) Network Function Master
     * Account A/c 0.0.50 - Update all Network Function accounts & perform all the Network Functions
     * listed below, 4) Network Function Accounts: i) A/c 0.0.55 - Update Address Book files
     * (0.0.101/102), ii) A/c 0.0.56 - Update Fee schedule (0.0.111), iii) A/c 0.0.57 - Update
     * Exchange Rate (0.0.112).
     */
    AuthorizationFailed(ResponseCodeEnum.AUTHORIZATION_FAILED),

    /** Fee Schedule Proto uploaded but not valid (append or update is required) */
    FileUploadedProtoInvalid(ResponseCodeEnum.FILE_UPLOADED_PROTO_INVALID),

    /** Fee Schedule Proto uploaded but not valid (append or update is required) */
    FileUploadedProtoNotSavedToDisk(ResponseCodeEnum.FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK),

    /** Fee Schedule Proto File Part uploaded */
    FeeScheduleFilePartUploaded(ResponseCodeEnum.FEE_SCHEDULE_FILE_PART_UPLOADED),

    /** The change on Exchange Rate exceeds Exchange_Rate_Allowed_Percentage */
    ExchangeRateChangeLimitExceeded(ResponseCodeEnum.EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED),

    /** Contract permanent storage exceeded the currently allowable limit */
    MaxContractStorageExceeded(ResponseCodeEnum.MAX_CONTRACT_STORAGE_EXCEEDED),

    /** Transfer Account should not be same as Account to be deleted */
    TransferAccountSameAsDeleteAccount(ResponseCodeEnum.TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT),

    TotalLedgerBalanceInvalid(ResponseCodeEnum.TOTAL_LEDGER_BALANCE_INVALID),

    /** The expiration date/time on a smart contract may not be reduced */
    ExpirationReductionNotAllowed(ResponseCodeEnum.EXPIRATION_REDUCTION_NOT_ALLOWED),

    /** Gas exceeded currently allowable gas limit per transaction */
    MaxGasLimitExceeded(ResponseCodeEnum.MAX_GAS_LIMIT_EXCEEDED),

    /** File size exceeded the currently allowable limit */
    MaxFileSizeExceeded(ResponseCodeEnum.MAX_FILE_SIZE_EXCEEDED),

    /** The Topic ID specified is not in the system. */
    InvalidTopicId(ResponseCodeEnum.INVALID_TOPIC_ID),

    InvalidAdminKey(ResponseCodeEnum.INVALID_ADMIN_KEY),

    InvalidSubmitKey(ResponseCodeEnum.INVALID_SUBMIT_KEY),

    /**
     * An attempted operation was not authorized (ie - a deleteTopic for a topic with no adminKey).
     */
    Unauthorized(ResponseCodeEnum.UNAUTHORIZED),

    /** A ConsensusService message is empty. */
    InvalidTopicMessage(ResponseCodeEnum.INVALID_TOPIC_MESSAGE),

    /** The autoRenewAccount specified is not a valid, active account. */
    InvalidAutorenewAccount(ResponseCodeEnum.INVALID_AUTORENEW_ACCOUNT),

    /** An adminKey was not specified on the topic, so there must not be an autoRenewAccount. */
    AutorenewAccountNotAllowed(ResponseCodeEnum.AUTORENEW_ACCOUNT_NOT_ALLOWED),

    /**
     * The topic has expired, was not automatically renewed, and is in a 7 day grace period before
     * the topic will be deleted unrecoverably. This error response code will not be returned until
     * autoRenew functionality is supported by HAPI.
     */
    TopicExpired(ResponseCodeEnum.TOPIC_EXPIRED);

    private final ResponseCodeEnum code;

    Status(ResponseCodeEnum code) {
        this.code = code;
    }

    static Status valueOf(ResponseCodeEnum code) {
        switch (code) {
            case OK:
                return Ok;
            case INVALID_TRANSACTION:
                return InvalidTransaction;
            case PAYER_ACCOUNT_NOT_FOUND:
                return PayerAccountNotFound;
            case INVALID_NODE_ACCOUNT:
                return InvalidNodeAccount;
            case TRANSACTION_EXPIRED:
                return TransactionExpired;
            case INVALID_TRANSACTION_START:
                return InvalidTransactionStart;
            case INVALID_TRANSACTION_DURATION:
                return InvalidTransactionDuration;
            case INVALID_SIGNATURE:
                return InvalidSignature;
            case MEMO_TOO_LONG:
                return MemoTooLong;
            case INSUFFICIENT_TX_FEE:
                return InsufficientTxFee;
            case INSUFFICIENT_PAYER_BALANCE:
                return InsufficientPayerBalance;
            case DUPLICATE_TRANSACTION:
                return DuplicateTransaction;
            case BUSY:
                return Busy;
            case NOT_SUPPORTED:
                return NotSupported;
            case INVALID_FILE_ID:
                return InvalidFileId;
            case INVALID_ACCOUNT_ID:
                return InvalidAccountId;
            case INVALID_CONTRACT_ID:
                return InvalidContractId;
            case INVALID_TRANSACTION_ID:
                return InvalidTransactionId;
            case RECEIPT_NOT_FOUND:
                return ReceiptNotFound;
            case RECORD_NOT_FOUND:
                return RecordNotFound;
            case INVALID_SOLIDITY_ID:
                return InvalidSolidityId;
            case UNKNOWN:
                return Unknown;
            case SUCCESS:
                return Success;
            case FAIL_INVALID:
                return FailInvalid;
            case FAIL_FEE:
                return FailFee;
            case FAIL_BALANCE:
                return FailBalance;
            case KEY_REQUIRED:
                return KeyRequired;
            case BAD_ENCODING:
                return BadEncoding;
            case INSUFFICIENT_ACCOUNT_BALANCE:
                return InsufficientAccountBalance;
            case INVALID_SOLIDITY_ADDRESS:
                return InvalidSolidityAddress;
            case INSUFFICIENT_GAS:
                return InsufficientGas;
            case CONTRACT_SIZE_LIMIT_EXCEEDED:
                return ContractSizeLimitExceeded;
            case LOCAL_CALL_MODIFICATION_EXCEPTION:
                return LocalCallModificationException;
            case CONTRACT_REVERT_EXECUTED:
                return ContractRevertExecuted;
            case CONTRACT_EXECUTION_EXCEPTION:
                return ContractExecutionException;
            case INVALID_RECEIVING_NODE_ACCOUNT:
                return InvalidReceivingNodeAccount;
            case MISSING_QUERY_HEADER:
                return MissingQueryHeader;
            case ACCOUNT_UPDATE_FAILED:
                return AccountUpdateFailed;
            case INVALID_KEY_ENCODING:
                return InvalidKeyEncoding;
            case NULL_SOLIDITY_ADDRESS:
                return NullSolidityAddress;
            case CONTRACT_UPDATE_FAILED:
                return ContractUpdateFailed;
            case INVALID_QUERY_HEADER:
                return InvalidQueryHeader;
            case INVALID_FEE_SUBMITTED:
                return InvalidFeeSubmitted;
            case INVALID_PAYER_SIGNATURE:
                return InvalidPayerSignature;
            case KEY_NOT_PROVIDED:
                return KeyNotProvided;
            case INVALID_EXPIRATION_TIME:
                return InvalidExpirationTime;
            case NO_WACL_KEY:
                return NoWaclKey;
            case FILE_CONTENT_EMPTY:
                return FileContentEmpty;
            case INVALID_ACCOUNT_AMOUNTS:
                return InvalidAccountAmounts;
            case EMPTY_TRANSACTION_BODY:
                return EmptyTransactionBody;
            case INVALID_TRANSACTION_BODY:
                return InvalidTransactionBody;
            case INVALID_SIGNATURE_TYPE_MISMATCHING_KEY:
                return InvalidSignatureTypeMismatchingKey;
            case INVALID_SIGNATURE_COUNT_MISMATCHING_KEY:
                return InvalidSignatureCountMismatchingKey;
            case EMPTY_CLAIM_BODY:
                return EmptyClaimBody;
            case EMPTY_CLAIM_HASH:
                return EmptyClaimHash;
            case EMPTY_CLAIM_KEYS:
                return EmptyClaimKeys;
            case INVALID_CLAIM_HASH_SIZE:
                return InvalidClaimHashSize;
            case EMPTY_QUERY_BODY:
                return EmptyQueryBody;
            case EMPTY_CLAIM_QUERY:
                return EmptyClaimQuery;
            case CLAIM_NOT_FOUND:
                return ClaimNotFound;
            case ACCOUNT_ID_DOES_NOT_EXIST:
                return AccountIdDoesNotExist;
            case CLAIM_ALREADY_EXISTS:
                return ClaimAlreadyExists;
            case INVALID_FILE_WACL:
                return InvalidFileWacl;
            case SERIALIZATION_FAILED:
                return SerializationFailed;
            case TRANSACTION_OVERSIZE:
                return TransactionOversize;
            case TRANSACTION_TOO_MANY_LAYERS:
                return TransactionTooManyLayers;
            case CONTRACT_DELETED:
                return ContractDeleted;
            case PLATFORM_NOT_ACTIVE:
                return PlatformNotActive;
            case KEY_PREFIX_MISMATCH:
                return KeyPrefixMismatch;
            case PLATFORM_TRANSACTION_NOT_CREATED:
                return PlatformTransactionNotCreated;
            case INVALID_RENEWAL_PERIOD:
                return InvalidRenewalPeriod;
            case INVALID_PAYER_ACCOUNT_ID:
                return InvalidPayerAccountId;
            case ACCOUNT_DELETED:
                return AccountDeleted;
            case FILE_DELETED:
                return FileDeleted;
            case ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS:
                return AccountRepeatedInAccountAmounts;
            case SETTING_NEGATIVE_ACCOUNT_BALANCE:
                return SettingNegativeAccountBalance;
            case OBTAINER_REQUIRED:
                return ObtainerRequired;
            case OBTAINER_SAME_CONTRACT_ID:
                return ObtainerSameContractId;
            case OBTAINER_DOES_NOT_EXIST:
                return ObtainerDoesNotExist;
            case MODIFYING_IMMUTABLE_CONTRACT:
                return ModifyingImmutableContract;
            case FILE_SYSTEM_EXCEPTION:
                return FileSystemException;
            case AUTORENEW_DURATION_NOT_IN_RANGE:
                return AutorenewDurationNotInRange;
            case ERROR_DECODING_BYTESTRING:
                return ErrorDecodingBytestring;
            case CONTRACT_FILE_EMPTY:
                return ContractFileEmpty;
            case CONTRACT_BYTECODE_EMPTY:
                return ContractBytecodeEmpty;
            case INVALID_INITIAL_BALANCE:
                return InvalidInitialBalance;
            case INVALID_RECEIVE_RECORD_THRESHOLD:
                return InvalidReceiveRecordThreshold;
            case INVALID_SEND_RECORD_THRESHOLD:
                return InvalidSendRecordThreshold;
            case ACCOUNT_IS_NOT_GENESIS_ACCOUNT:
                return AccountIsNotGenesisAccount;
            case PAYER_ACCOUNT_UNAUTHORIZED:
                return PayerAccountUnauthorized;
            case INVALID_FREEZE_TRANSACTION_BODY:
                return InvalidFreezeTransactionBody;
            case FREEZE_TRANSACTION_BODY_NOT_FOUND:
                return FreezeTransactionBodyNotFound;
            case TRANSFER_LIST_SIZE_LIMIT_EXCEEDED:
                return TransferListSizeLimitExceeded;
            case RESULT_SIZE_LIMIT_EXCEEDED:
                return ResultSizeLimitExceeded;
            case NOT_SPECIAL_ACCOUNT:
                return NotSpecialAccount;
            case CONTRACT_NEGATIVE_GAS:
                return ContractNegativeGas;
            case CONTRACT_NEGATIVE_VALUE:
                return ContractNegativeValue;
            case INVALID_FEE_FILE:
                return InvalidFeeFile;
            case INVALID_EXCHANGE_RATE_FILE:
                return InvalidExchangeRateFile;
            case INSUFFICIENT_LOCAL_CALL_GAS:
                return InsufficientLocalCallGas;
            case ENTITY_NOT_ALLOWED_TO_DELETE:
                return EntityNotAllowedToDelete;
            case AUTHORIZATION_FAILED:
                return AuthorizationFailed;
            case FILE_UPLOADED_PROTO_INVALID:
                return FileUploadedProtoInvalid;
            case FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK:
                return FileUploadedProtoNotSavedToDisk;
            case FEE_SCHEDULE_FILE_PART_UPLOADED:
                return FeeScheduleFilePartUploaded;
            case EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED:
                return ExchangeRateChangeLimitExceeded;
            case MAX_CONTRACT_STORAGE_EXCEEDED:
                return MaxContractStorageExceeded;
            case TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT:
                return TransferAccountSameAsDeleteAccount;
            case TOTAL_LEDGER_BALANCE_INVALID:
                return TotalLedgerBalanceInvalid;
            case EXPIRATION_REDUCTION_NOT_ALLOWED:
                return ExpirationReductionNotAllowed;
            case MAX_GAS_LIMIT_EXCEEDED:
                return MaxGasLimitExceeded;
            case MAX_FILE_SIZE_EXCEEDED:
                return MaxFileSizeExceeded;
            case INVALID_TOPIC_ID:
                return InvalidTopicId;
            case INVALID_ADMIN_KEY:
                return InvalidAdminKey;
            case INVALID_SUBMIT_KEY:
                return InvalidSubmitKey;
            case UNAUTHORIZED:
                return Unauthorized;
            case INVALID_TOPIC_MESSAGE:
                return InvalidTopicMessage;
            case INVALID_AUTORENEW_ACCOUNT:
                return InvalidAutorenewAccount;
            case AUTORENEW_ACCOUNT_NOT_ALLOWED:
                return AutorenewAccountNotAllowed;
            case TOPIC_EXPIRED:
                return TopicExpired;

            case UNRECOGNIZED:
                // NOTE: Protobuf deserialization will not give us the code on the wire
                throw new IllegalArgumentException(
                        "network return unrecognized response code; update your SDK or open an issue");
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

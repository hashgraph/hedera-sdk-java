package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

import java.util.Arrays;

public enum Status {
    Ok(ResponseCodeEnum.OK),
    InvalidTransaction(ResponseCodeEnum.INVALID_TRANSACTION),
    PayerAccountNotFound(ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND),
    InvalidNodeAccount(ResponseCodeEnum.INVALID_NODE_ACCOUNT),
    TransactionExpired(ResponseCodeEnum.TRANSACTION_EXPIRED),
    InvalidTransactionStart(ResponseCodeEnum.INVALID_TRANSACTION_START),
    InvalidTransactionDuration(ResponseCodeEnum.INVALID_TRANSACTION_DURATION),
    InvalidSignature(ResponseCodeEnum.INVALID_SIGNATURE),
    MemoTooLong(ResponseCodeEnum.MEMO_TOO_LONG),
    InsufficientTxFee(ResponseCodeEnum.INSUFFICIENT_TX_FEE),
    InsufficientPayerBalance(ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE),
    DuplicateTransaction(ResponseCodeEnum.DUPLICATE_TRANSACTION),
    Busy(ResponseCodeEnum.BUSY),
    NotSupported(ResponseCodeEnum.NOT_SUPPORTED),
    InvalidFileId(ResponseCodeEnum.INVALID_FILE_ID),
    InvalidAccountId(ResponseCodeEnum.INVALID_ACCOUNT_ID),
    InvalidContractId(ResponseCodeEnum.INVALID_CONTRACT_ID),
    InvalidTransactionId(ResponseCodeEnum.INVALID_TRANSACTION_ID),
    ReceiptNotFound(ResponseCodeEnum.RECEIPT_NOT_FOUND),
    RecordNotFound(ResponseCodeEnum.RECORD_NOT_FOUND),
    InvalidSolidityId(ResponseCodeEnum.INVALID_SOLIDITY_ID),
    Unknown(ResponseCodeEnum.UNKNOWN),
    Success(ResponseCodeEnum.SUCCESS),
    FailInvalid(ResponseCodeEnum.FAIL_INVALID),
    FailFee(ResponseCodeEnum.FAIL_FEE),
    FailBalance(ResponseCodeEnum.FAIL_BALANCE),
    KeyRequired(ResponseCodeEnum.KEY_REQUIRED),
    BadEncoding(ResponseCodeEnum.BAD_ENCODING),
    InsufficientAccountBalance(ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE),
    InvalidSolidityAddress(ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS),
    InsufficientGas(ResponseCodeEnum.INSUFFICIENT_GAS),
    ContractSizeLimitExceeded(ResponseCodeEnum.CONTRACT_SIZE_LIMIT_EXCEEDED),
    LocalCallModificationException(ResponseCodeEnum.LOCAL_CALL_MODIFICATION_EXCEPTION),
    ContractRevertExecuted(ResponseCodeEnum.CONTRACT_REVERT_EXECUTED),
    ContractExecutionException(ResponseCodeEnum.CONTRACT_EXECUTION_EXCEPTION),
    InvalidReceivingNodeAccount(ResponseCodeEnum.INVALID_RECEIVING_NODE_ACCOUNT),
    MissingQueryHeader(ResponseCodeEnum.MISSING_QUERY_HEADER),
    AccountUpdateFailed(ResponseCodeEnum.ACCOUNT_UPDATE_FAILED),
    InvalidKeyEncoding(ResponseCodeEnum.INVALID_KEY_ENCODING),
    NullSolidityAddress(ResponseCodeEnum.NULL_SOLIDITY_ADDRESS),
    ContractUpdateFailed(ResponseCodeEnum.CONTRACT_UPDATE_FAILED),
    InvalidQueryHeader(ResponseCodeEnum.INVALID_QUERY_HEADER),
    InvalidFeeSubmitted(ResponseCodeEnum.INVALID_FEE_SUBMITTED),
    InvalidPayerSignature(ResponseCodeEnum.INVALID_PAYER_SIGNATURE),
    KeyNotProvided(ResponseCodeEnum.KEY_NOT_PROVIDED),
    InvalidExpirationTime(ResponseCodeEnum.INVALID_EXPIRATION_TIME),
    NoWaclKey(ResponseCodeEnum.NO_WACL_KEY),
    FileContentEmpty(ResponseCodeEnum.FILE_CONTENT_EMPTY),
    InvalidAccountAmounts(ResponseCodeEnum.INVALID_ACCOUNT_AMOUNTS),
    EmptyTransactionBody(ResponseCodeEnum.EMPTY_TRANSACTION_BODY),
    InvalidTransactionBody(ResponseCodeEnum.INVALID_TRANSACTION_BODY),
    InvalidSignatureTypeMismatchingKey(ResponseCodeEnum.INVALID_SIGNATURE_TYPE_MISMATCHING_KEY),
    InvalidSignatureCountMismatchingKey(ResponseCodeEnum.INVALID_SIGNATURE_COUNT_MISMATCHING_KEY),
    EmptyClaimBody(ResponseCodeEnum.EMPTY_CLAIM_BODY),
    EmptyClaimHash(ResponseCodeEnum.EMPTY_CLAIM_HASH),
    EmptyClaimKeys(ResponseCodeEnum.EMPTY_CLAIM_KEYS),
    InvalidClaimHashSize(ResponseCodeEnum.INVALID_CLAIM_HASH_SIZE),
    EmptyQueryBody(ResponseCodeEnum.EMPTY_QUERY_BODY),
    EmptyClaimQuery(ResponseCodeEnum.EMPTY_CLAIM_QUERY),
    ClaimNotFound(ResponseCodeEnum.CLAIM_NOT_FOUND),
    AccountIdDoesNotExist(ResponseCodeEnum.ACCOUNT_ID_DOES_NOT_EXIST),
    ClaimAlreadyExists(ResponseCodeEnum.CLAIM_ALREADY_EXISTS),
    InvalidFileWacl(ResponseCodeEnum.INVALID_FILE_WACL),
    SerializationFailed(ResponseCodeEnum.SERIALIZATION_FAILED),
    TransactionOversize(ResponseCodeEnum.TRANSACTION_OVERSIZE),
    TransactionTooManyLayers(ResponseCodeEnum.TRANSACTION_TOO_MANY_LAYERS),
    ContractDeleted(ResponseCodeEnum.CONTRACT_DELETED),
    PlatformNotActive(ResponseCodeEnum.PLATFORM_NOT_ACTIVE),
    KeyPrefixMismatch(ResponseCodeEnum.KEY_PREFIX_MISMATCH),
    PlatformTransactionNotCreated(ResponseCodeEnum.PLATFORM_TRANSACTION_NOT_CREATED),
    InvalidRenewalPeriod(ResponseCodeEnum.INVALID_RENEWAL_PERIOD),
    InvalidPayerAccountId(ResponseCodeEnum.INVALID_PAYER_ACCOUNT_ID),
    AccountDeleted(ResponseCodeEnum.ACCOUNT_DELETED),
    FileDeleted(ResponseCodeEnum.FILE_DELETED),
    AccountRepeatedInAccountAmounts(ResponseCodeEnum.ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS),
    SettingNegativeAccountBalance(ResponseCodeEnum.SETTING_NEGATIVE_ACCOUNT_BALANCE),
    ObtainerRequired(ResponseCodeEnum.OBTAINER_REQUIRED),
    ObtainerSameContractId(ResponseCodeEnum.OBTAINER_SAME_CONTRACT_ID),
    ObtainerDoesNotExist(ResponseCodeEnum.OBTAINER_DOES_NOT_EXIST),
    ModifyingImmutableContract(ResponseCodeEnum.MODIFYING_IMMUTABLE_CONTRACT),
    FileSystemException(ResponseCodeEnum.FILE_SYSTEM_EXCEPTION),
    AutorenewDurationNotInRange(ResponseCodeEnum.AUTORENEW_DURATION_NOT_IN_RANGE),
    ErrorDecodingBytestring(ResponseCodeEnum.ERROR_DECODING_BYTESTRING),
    ContractFileEmpty(ResponseCodeEnum.CONTRACT_FILE_EMPTY),
    ContractBytecodeEmpty(ResponseCodeEnum.CONTRACT_BYTECODE_EMPTY),
    InvalidInitialBalance(ResponseCodeEnum.INVALID_INITIAL_BALANCE),
    InvalidReceiveRecordThreshold(ResponseCodeEnum.INVALID_RECEIVE_RECORD_THRESHOLD),
    InvalidSendRecordThreshold(ResponseCodeEnum.INVALID_SEND_RECORD_THRESHOLD),
    AccountIsNotGenesisAccount(ResponseCodeEnum.ACCOUNT_IS_NOT_GENESIS_ACCOUNT),
    PayerAccountUnauthorized(ResponseCodeEnum.PAYER_ACCOUNT_UNAUTHORIZED),
    InvalidFreezeTransactionBody(ResponseCodeEnum.INVALID_FREEZE_TRANSACTION_BODY),
    FreezeTransactionBodyNotFound(ResponseCodeEnum.FREEZE_TRANSACTION_BODY_NOT_FOUND),
    TransferListSizeLimitExceeded(ResponseCodeEnum.TRANSFER_LIST_SIZE_LIMIT_EXCEEDED),
    ResultSizeLimitExceeded(ResponseCodeEnum.RESULT_SIZE_LIMIT_EXCEEDED),
    NotSpecialAccount(ResponseCodeEnum.NOT_SPECIAL_ACCOUNT),
    ContractNegativeGas(ResponseCodeEnum.CONTRACT_NEGATIVE_GAS),
    ContractNegativeValue(ResponseCodeEnum.CONTRACT_NEGATIVE_VALUE),
    InvalidFeeFile(ResponseCodeEnum.INVALID_FEE_FILE),
    InvalidExchangeRateFile(ResponseCodeEnum.INVALID_EXCHANGE_RATE_FILE),
    InsufficientLocalCallGas(ResponseCodeEnum.INSUFFICIENT_LOCAL_CALL_GAS),
    EntityNotAllowedToDelete(ResponseCodeEnum.ENTITY_NOT_ALLOWED_TO_DELETE),
    AuthorizationFailed(ResponseCodeEnum.AUTHORIZATION_FAILED),
    FileUploadedProtoInvalid(ResponseCodeEnum.FILE_UPLOADED_PROTO_INVALID),
    FileUploadedProtoNotSavedToDisk(ResponseCodeEnum.FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK),
    FeeScheduleFilePartUploaded(ResponseCodeEnum.FEE_SCHEDULE_FILE_PART_UPLOADED),
    ExchangeRateChangeLimitExceeded(ResponseCodeEnum.EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED),
    MaxContractStorageExceeded(ResponseCodeEnum.MAX_CONTRACT_STORAGE_EXCEEDED),
    TransferAccountSameAsDeleteAccount(ResponseCodeEnum.TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT),
    TotalLedgerBalanceInvalid(ResponseCodeEnum.TOTAL_LEDGER_BALANCE_INVALID),
    ExpirationReductionNotAllowed(ResponseCodeEnum.EXPIRATION_REDUCTION_NOT_ALLOWED),
    MaxGasLimitExceeded(ResponseCodeEnum.MAX_GAS_LIMIT_EXCEEDED),
    MaxFileSizeExceeded(ResponseCodeEnum.MAX_FILE_SIZE_EXCEEDED),
    InvalidTopicId(ResponseCodeEnum.INVALID_TOPIC_ID),
    InvalidAdminKey(ResponseCodeEnum.INVALID_ADMIN_KEY),
    InvalidSubmitKey(ResponseCodeEnum.INVALID_SUBMIT_KEY),
    Unauthorized(ResponseCodeEnum.UNAUTHORIZED),
    InvalidTopicMessage(ResponseCodeEnum.INVALID_TOPIC_MESSAGE),
    InvalidAutorenewAccount(ResponseCodeEnum.INVALID_AUTORENEW_ACCOUNT),
    AutorenewAccountNotAllowed(ResponseCodeEnum.AUTORENEW_ACCOUNT_NOT_ALLOWED),
    TopicExpired(ResponseCodeEnum.TOPIC_EXPIRED);

    private final ResponseCodeEnum responseCode;

    public final int code;

    Status(ResponseCodeEnum responseCode) {
        this.responseCode = responseCode;
        this.code = responseCode.getNumber();
    }

    public String toString() {
        return responseCode.toString();
    }

    boolean equalsAny(Status... statuses) {
        return Arrays.asList(statuses).contains(this);
    }

    static Status valueOf(ResponseCodeEnum responseCode) {
        switch (responseCode) {
            case OK: return Ok;
            case INVALID_TRANSACTION: return InvalidTransaction;
            case PAYER_ACCOUNT_NOT_FOUND: return PayerAccountNotFound;
            case INVALID_NODE_ACCOUNT: return InvalidNodeAccount;
            case TRANSACTION_EXPIRED: return TransactionExpired;
            case INVALID_TRANSACTION_START: return InvalidTransactionStart;
            case INVALID_TRANSACTION_DURATION: return InvalidTransactionDuration;
            case INVALID_SIGNATURE: return InvalidSignature;
            case MEMO_TOO_LONG: return MemoTooLong;
            case INSUFFICIENT_TX_FEE: return InsufficientTxFee;
            case INSUFFICIENT_PAYER_BALANCE: return InsufficientPayerBalance;
            case DUPLICATE_TRANSACTION: return DuplicateTransaction;
            case BUSY: return Busy;
            case NOT_SUPPORTED: return NotSupported;
            case INVALID_FILE_ID: return InvalidFileId;
            case INVALID_ACCOUNT_ID: return InvalidAccountId;
            case INVALID_CONTRACT_ID: return InvalidContractId;
            case INVALID_TRANSACTION_ID: return InvalidTransactionId;
            case RECEIPT_NOT_FOUND: return ReceiptNotFound;
            case RECORD_NOT_FOUND: return RecordNotFound;
            case INVALID_SOLIDITY_ID: return InvalidSolidityId;
            case UNKNOWN: return Unknown;
            case SUCCESS: return Success;
            case FAIL_INVALID: return FailInvalid;
            case FAIL_FEE: return FailFee;
            case FAIL_BALANCE: return FailBalance;
            case KEY_REQUIRED: return KeyRequired;
            case BAD_ENCODING: return BadEncoding;
            case INSUFFICIENT_ACCOUNT_BALANCE: return InsufficientAccountBalance;
            case INVALID_SOLIDITY_ADDRESS: return InvalidSolidityAddress;
            case INSUFFICIENT_GAS: return InsufficientGas;
            case CONTRACT_SIZE_LIMIT_EXCEEDED: return ContractSizeLimitExceeded;
            case LOCAL_CALL_MODIFICATION_EXCEPTION: return LocalCallModificationException;
            case CONTRACT_REVERT_EXECUTED: return ContractRevertExecuted;
            case CONTRACT_EXECUTION_EXCEPTION: return ContractExecutionException;
            case INVALID_RECEIVING_NODE_ACCOUNT: return InvalidReceivingNodeAccount;
            case MISSING_QUERY_HEADER: return MissingQueryHeader;
            case ACCOUNT_UPDATE_FAILED: return AccountUpdateFailed;
            case INVALID_KEY_ENCODING: return InvalidKeyEncoding;
            case NULL_SOLIDITY_ADDRESS: return NullSolidityAddress;
            case CONTRACT_UPDATE_FAILED: return ContractUpdateFailed;
            case INVALID_QUERY_HEADER: return InvalidQueryHeader;
            case INVALID_FEE_SUBMITTED: return InvalidFeeSubmitted;
            case INVALID_PAYER_SIGNATURE: return InvalidPayerSignature;
            case KEY_NOT_PROVIDED: return KeyNotProvided;
            case INVALID_EXPIRATION_TIME: return InvalidExpirationTime;
            case NO_WACL_KEY: return NoWaclKey;
            case FILE_CONTENT_EMPTY: return FileContentEmpty;
            case INVALID_ACCOUNT_AMOUNTS: return InvalidAccountAmounts;
            case EMPTY_TRANSACTION_BODY: return EmptyTransactionBody;
            case INVALID_TRANSACTION_BODY: return InvalidTransactionBody;
            case INVALID_SIGNATURE_TYPE_MISMATCHING_KEY: return InvalidSignatureTypeMismatchingKey;
            case INVALID_SIGNATURE_COUNT_MISMATCHING_KEY: return InvalidSignatureCountMismatchingKey;
            case EMPTY_CLAIM_BODY: return EmptyClaimBody;
            case EMPTY_CLAIM_HASH: return EmptyClaimHash;
            case EMPTY_CLAIM_KEYS: return EmptyClaimKeys;
            case INVALID_CLAIM_HASH_SIZE: return InvalidClaimHashSize;
            case EMPTY_QUERY_BODY: return EmptyQueryBody;
            case EMPTY_CLAIM_QUERY: return EmptyClaimQuery;
            case CLAIM_NOT_FOUND: return ClaimNotFound;
            case ACCOUNT_ID_DOES_NOT_EXIST: return AccountIdDoesNotExist;
            case CLAIM_ALREADY_EXISTS: return ClaimAlreadyExists;
            case INVALID_FILE_WACL: return InvalidFileWacl;
            case SERIALIZATION_FAILED: return SerializationFailed;
            case TRANSACTION_OVERSIZE: return TransactionOversize;
            case TRANSACTION_TOO_MANY_LAYERS: return TransactionTooManyLayers;
            case CONTRACT_DELETED: return ContractDeleted;
            case PLATFORM_NOT_ACTIVE: return PlatformNotActive;
            case KEY_PREFIX_MISMATCH: return KeyPrefixMismatch;
            case PLATFORM_TRANSACTION_NOT_CREATED: return PlatformTransactionNotCreated;
            case INVALID_RENEWAL_PERIOD: return InvalidRenewalPeriod;
            case INVALID_PAYER_ACCOUNT_ID: return InvalidPayerAccountId;
            case ACCOUNT_DELETED: return AccountDeleted;
            case FILE_DELETED: return FileDeleted;
            case ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS: return AccountRepeatedInAccountAmounts;
            case SETTING_NEGATIVE_ACCOUNT_BALANCE: return SettingNegativeAccountBalance;
            case OBTAINER_REQUIRED: return ObtainerRequired;
            case OBTAINER_SAME_CONTRACT_ID: return ObtainerSameContractId;
            case OBTAINER_DOES_NOT_EXIST: return ObtainerDoesNotExist;
            case MODIFYING_IMMUTABLE_CONTRACT: return ModifyingImmutableContract;
            case FILE_SYSTEM_EXCEPTION: return FileSystemException;
            case AUTORENEW_DURATION_NOT_IN_RANGE: return AutorenewDurationNotInRange;
            case ERROR_DECODING_BYTESTRING: return ErrorDecodingBytestring;
            case CONTRACT_FILE_EMPTY: return ContractFileEmpty;
            case CONTRACT_BYTECODE_EMPTY: return ContractBytecodeEmpty;
            case INVALID_INITIAL_BALANCE: return InvalidInitialBalance;
            case INVALID_RECEIVE_RECORD_THRESHOLD: return InvalidReceiveRecordThreshold;
            case INVALID_SEND_RECORD_THRESHOLD: return InvalidSendRecordThreshold;
            case ACCOUNT_IS_NOT_GENESIS_ACCOUNT: return AccountIsNotGenesisAccount;
            case PAYER_ACCOUNT_UNAUTHORIZED: return PayerAccountUnauthorized;
            case INVALID_FREEZE_TRANSACTION_BODY: return InvalidFreezeTransactionBody;
            case FREEZE_TRANSACTION_BODY_NOT_FOUND: return FreezeTransactionBodyNotFound;
            case TRANSFER_LIST_SIZE_LIMIT_EXCEEDED: return TransferListSizeLimitExceeded;
            case RESULT_SIZE_LIMIT_EXCEEDED: return ResultSizeLimitExceeded;
            case NOT_SPECIAL_ACCOUNT: return NotSpecialAccount;
            case CONTRACT_NEGATIVE_GAS: return ContractNegativeGas;
            case CONTRACT_NEGATIVE_VALUE: return ContractNegativeValue;
            case INVALID_FEE_FILE: return InvalidFeeFile;
            case INVALID_EXCHANGE_RATE_FILE: return InvalidExchangeRateFile;
            case INSUFFICIENT_LOCAL_CALL_GAS: return InsufficientLocalCallGas;
            case ENTITY_NOT_ALLOWED_TO_DELETE: return EntityNotAllowedToDelete;
            case AUTHORIZATION_FAILED: return AuthorizationFailed;
            case FILE_UPLOADED_PROTO_INVALID: return FileUploadedProtoInvalid;
            case FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK: return FileUploadedProtoNotSavedToDisk;
            case FEE_SCHEDULE_FILE_PART_UPLOADED: return FeeScheduleFilePartUploaded;
            case EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED: return ExchangeRateChangeLimitExceeded;
            case MAX_CONTRACT_STORAGE_EXCEEDED: return MaxContractStorageExceeded;
            case TRANSFER_ACCOUNT_SAME_AS_DELETE_ACCOUNT: return TransferAccountSameAsDeleteAccount;
            case TOTAL_LEDGER_BALANCE_INVALID: return TotalLedgerBalanceInvalid;
            case EXPIRATION_REDUCTION_NOT_ALLOWED: return ExpirationReductionNotAllowed;
            case MAX_GAS_LIMIT_EXCEEDED: return MaxGasLimitExceeded;
            case MAX_FILE_SIZE_EXCEEDED: return MaxFileSizeExceeded;
            case INVALID_TOPIC_ID: return InvalidTopicId;
            case INVALID_ADMIN_KEY: return InvalidAdminKey;
            case INVALID_SUBMIT_KEY: return InvalidSubmitKey;
            case UNAUTHORIZED: return Unauthorized;
            case INVALID_TOPIC_MESSAGE: return InvalidTopicMessage;
            case INVALID_AUTORENEW_ACCOUNT: return InvalidAutorenewAccount;
            case AUTORENEW_ACCOUNT_NOT_ALLOWED: return AutorenewAccountNotAllowed;
            case TOPIC_EXPIRED: return TopicExpired;

            case UNRECOGNIZED:
                // protobufs won't give us the actual value that was unexpected, unfortunately
                throw new IllegalArgumentException(
                    "network returned unrecognized response code; your SDK may be out of date");

            default:
                throw new IllegalArgumentException(
                    "(BUG) unhandled response code: " + responseCode);
        }
    }
}

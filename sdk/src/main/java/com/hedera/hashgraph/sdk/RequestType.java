package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.HederaFunctionality;

public enum RequestType {
    /**
     * UNSPECIFIED - Need to keep first value as unspecified because first element is ignored and not parsed (0 is ignored by parser)
     */
    NONE(HederaFunctionality.NONE),

    /**
     * crypto transfer
     */
    CRYPTO_TRANSFER(HederaFunctionality.CryptoTransfer),

    /**
     * crypto update account
     */
    CRYPTO_UPDATE(HederaFunctionality.CryptoUpdate),

    /**
     * crypto delete account
     */
    CRYPTO_DELETE(HederaFunctionality.CryptoDelete),

    /**
     * Add a livehash to a crypto account
     */
    CRYPTO_ADD_LIVE_HASH(HederaFunctionality.CryptoAddLiveHash),

    /**
     * Delete a livehash from a crypto account
     */
    CRYPTO_DELETE_LIVE_HASH(HederaFunctionality.CryptoDeleteLiveHash),

    /**
     * Smart Contract Call
     */
    CONTRACT_CALL(HederaFunctionality.ContractCall),

    /**
     * Smart Contract Create Contract
     */
    CONTRACT_CREATE(HederaFunctionality.ContractCreate),

    /**
     * Smart Contract update contract
     */
    CONTRACT_UPDATE(HederaFunctionality.ContractUpdate),

    /**
     * File Operation create file
     */
    FILE_CREATE(HederaFunctionality.FileCreate),

    /**
     * File Operation append file
     */
    FILE_APPEND(HederaFunctionality.FileAppend),

    /**
     * File Operation update file
     */
    FILE_UPDATE(HederaFunctionality.FileUpdate),

    /**
     * File Operation delete file
     */
    FILE_DELETE(HederaFunctionality.FileDelete),

    /**
     * crypto get account balance
     */
    CRYPTO_GET_ACCOUNT_BALANCE(HederaFunctionality.CryptoGetAccountBalance),

    /**
     * crypto get account record
     */
    CRYPTO_GET_ACCOUNT_RECORDS(HederaFunctionality.CryptoGetAccountRecords),

    /**
     * Crypto get info
     */
    CRYPTO_GET_INFO(HederaFunctionality.CryptoGetInfo),

    /**
     * Smart Contract Call
     */
    CONTRACT_CALL_LOCAL(HederaFunctionality.ContractCallLocal),

    /**
     * Smart Contract get info
     */
    CONTRACT_GET_INFO(HederaFunctionality.ContractGetInfo),

    /**
     * Smart Contract, get the byte code
     */
    CONTRACT_GET_BYTECODE(HederaFunctionality.ContractGetBytecode),

    /**
     * Smart Contract, get by solidity ID
     */
    GET_BY_SOLIDITY_ID(HederaFunctionality.GetBySolidityID),

    /**
     * Smart Contract, get by key
     */
    GET_BY_KEY(HederaFunctionality.GetByKey),

    /**
     * Get a live hash from a crypto account
     */
    CRYPTO_GET_LIVE_HASH(HederaFunctionality.CryptoGetLiveHash),

    /**
     * Crypto, get the stakers for the node
     */
    CRYPTO_GET_STAKERS(HederaFunctionality.CryptoGetStakers),

    /**
     * File Operations get file contents
     */
    FILE_GET_CONTENTS(HederaFunctionality.FileGetContents),

    /**
     * File Operations get the info of the file
     */
    FILE_GET_INFO(HederaFunctionality.FileGetInfo),

    /**
     * Crypto get the transaction records
     */
    TRANSACTION_GET_RECORD(HederaFunctionality.TransactionGetRecord),

    /**
     * Contract get the transaction records
     */
    CONTRACT_GET_RECORDS(HederaFunctionality.ContractGetRecords),

    /**
     * crypto create account
     */
    CRYPTO_CREATE(HederaFunctionality.CryptoCreate),

    /**
     * system delete file
     */
    SYSTEM_DELETE(HederaFunctionality.SystemDelete),

    /**
     * system undelete file
     */
    SYSTEM_UNDELETE(HederaFunctionality.SystemUndelete),

    /**
     * delete contract
     */
    CONTRACT_DELETE(HederaFunctionality.ContractDelete),

    /**
     * freeze
     */
    FREEZE(HederaFunctionality.Freeze),

    /**
     * Create Tx Record
     */
    CREATE_TRANSACTION_RECORD(HederaFunctionality.CreateTransactionRecord),

    /**
     * Crypto Auto Renew
     */
    CRYPTO_ACCOUNT_AUTO_RENEW(HederaFunctionality.CryptoAccountAutoRenew),

    /**
     * Contract Auto Renew
     */
    CONTRACT_AUTO_RENEW(HederaFunctionality.ContractAutoRenew),

    /**
     * Get Version
     */
    GET_VERSION_INFO(HederaFunctionality.GetVersionInfo),

    /**
     * Transaction Get Receipt
     */
    TRANSACTION_GET_RECEIPT(HederaFunctionality.TransactionGetReceipt),

    /**
     * Create Topic
     */
    CONSENSUS_CREATE_TOPIC(HederaFunctionality.ConsensusCreateTopic),

    /**
     * Update Topic
     */
    CONSENSUS_UPDATE_TOPIC(HederaFunctionality.ConsensusUpdateTopic),

    /**
     * Delete Topic
     */
    CONSENSUS_DELETE_TOPIC(HederaFunctionality.ConsensusDeleteTopic),

    /**
     * Get Topic information
     */
    CONSENSUS_GET_TOPIC_INFO(HederaFunctionality.ConsensusGetTopicInfo),

    /**
     * Submit message to topic
     */
    CONSENSUS_SUBMIT_MESSAGE(HederaFunctionality.ConsensusSubmitMessage),

    UNCHECKED_SUBMIT(HederaFunctionality.UncheckedSubmit),
    /**
     * Create Token
     */
    TOKEN_CREATE(HederaFunctionality.TokenCreate),

    /**
     * Get Token information
     */
    TOKEN_GET_INFO(HederaFunctionality.TokenGetInfo),

    /**
     * Freeze Account
     */
    TOKEN_FREEZE_ACCOUNT(HederaFunctionality.TokenFreezeAccount),

    /**
     * Unfreeze Account
     */
    TOKEN_UNFREEZE_ACCOUNT(HederaFunctionality.TokenUnfreezeAccount),

    /**
     * Grant KYC to Account
     */
    TOKEN_GRANT_KYC_TO_ACCOUNT(HederaFunctionality.TokenGrantKycToAccount),

    /**
     * Revoke KYC from Account
     */
    TOKEN_REVOKE_KYC_FROM_ACCOUNT(HederaFunctionality.TokenRevokeKycFromAccount),

    /**
     * Delete Token
     */
    TOKEN_DELETE(HederaFunctionality.TokenDelete),

    /**
     * Update Token
     */
    TOKEN_UPDATE(HederaFunctionality.TokenUpdate),

    /**
     * Mint tokens to treasury
     */
    TOKEN_MINT(HederaFunctionality.TokenMint),

    /**
     * Burn tokens from treasury
     */
    TOKEN_BURN(HederaFunctionality.TokenBurn),

    /**
     * Wipe token amount from Account holder
     */
    TOKEN_ACCOUNT_WIPE(HederaFunctionality.TokenAccountWipe),

    /**
     * Associate tokens to an account
     */
    TOKEN_ASSOCIATE_TO_ACCOUNT(HederaFunctionality.TokenAssociateToAccount),

    /**
     * Dissociate tokens from an account
     */
    TOKEN_DISSOCIATE_FROM_ACCOUNT(HederaFunctionality.TokenDissociateFromAccount),

    /**
     * Create Scheduled Transaction
     */
    SCHEDULE_CREATE(HederaFunctionality.ScheduleCreate),

    /**
     * Delete Scheduled Transaction
     */
    SCHEDULE_DELETE(HederaFunctionality.ScheduleDelete),

    /**
     * Sign Scheduled Transaction
     */
    SCHEDULE_SIGN(HederaFunctionality.ScheduleSign),

    /**
     * Get Scheduled Transaction Information
     */
    SCHEDULE_GET_INFO(HederaFunctionality.ScheduleGetInfo);

    final HederaFunctionality code;

    RequestType(HederaFunctionality code) {
        this.code = code;
    }

    static RequestType valueOf(HederaFunctionality code) {
        if(code == HederaFunctionality.NONE) {
            return NONE;
        } else if(code == HederaFunctionality.CryptoTransfer) {
            return CRYPTO_TRANSFER;
        } else if(code == HederaFunctionality.CryptoUpdate) {
            return CRYPTO_UPDATE;
        } else if(code == HederaFunctionality.CryptoDelete) {
            return CRYPTO_DELETE;
        } else if(code == HederaFunctionality.CryptoAddLiveHash) {
            return CRYPTO_ADD_LIVE_HASH;
        } else if(code == HederaFunctionality.CryptoDeleteLiveHash) {
            return CRYPTO_DELETE_LIVE_HASH;
        } else if(code == HederaFunctionality.ContractCall) {
            return CONTRACT_CALL;
        } else if(code == HederaFunctionality.ContractCreate) {
            return CONTRACT_CREATE;
        } else if(code == HederaFunctionality.ContractUpdate) {
            return CONTRACT_UPDATE;
        } else if(code == HederaFunctionality.FileCreate) {
            return FILE_CREATE;
        } else if(code == HederaFunctionality.FileAppend) {
            return FILE_APPEND;
        } else if(code == HederaFunctionality.FileUpdate) {
            return FILE_UPDATE;
        } else if(code == HederaFunctionality.FileDelete) {
            return FILE_DELETE;
        } else if(code == HederaFunctionality.CryptoGetAccountBalance) {
            return CRYPTO_GET_ACCOUNT_BALANCE;
        } else if(code == HederaFunctionality.CryptoGetAccountRecords) {
            return CRYPTO_GET_ACCOUNT_RECORDS;
        } else if(code == HederaFunctionality.CryptoGetInfo) {
            return CRYPTO_GET_INFO;
        } else if(code == HederaFunctionality.ContractCallLocal) {
            return CONTRACT_CALL_LOCAL;
        } else if(code == HederaFunctionality.ContractGetInfo) {
            return CONTRACT_GET_INFO;
        } else if(code == HederaFunctionality.ContractGetBytecode) {
            return CONTRACT_GET_BYTECODE;
        } else if(code == HederaFunctionality.GetBySolidityID) {
            return GET_BY_SOLIDITY_ID;
        } else if(code == HederaFunctionality.GetByKey) {
            return GET_BY_KEY;
        } else if(code == HederaFunctionality.CryptoGetLiveHash) {
            return CRYPTO_GET_LIVE_HASH;
        } else if(code == HederaFunctionality.CryptoGetStakers) {
            return CRYPTO_GET_STAKERS;
        } else if(code == HederaFunctionality.FileGetContents) {
            return FILE_GET_CONTENTS;
        } else if(code == HederaFunctionality.FileGetInfo) {
            return FILE_GET_INFO;
        } else if(code == HederaFunctionality.TransactionGetRecord) {
            return TRANSACTION_GET_RECORD;
        } else if(code == HederaFunctionality.ContractGetRecords) {
            return CONTRACT_GET_RECORDS;
        } else if(code == HederaFunctionality.CryptoCreate) {
            return CRYPTO_CREATE;
        } else if(code == HederaFunctionality.SystemDelete) {
            return SYSTEM_DELETE;
        } else if(code == HederaFunctionality.SystemUndelete) {
            return SYSTEM_UNDELETE;
        } else if(code == HederaFunctionality.ContractDelete) {
            return CONTRACT_DELETE;
        } else if(code == HederaFunctionality.Freeze) {
            return FREEZE;
        } else if(code == HederaFunctionality.CreateTransactionRecord) {
            return CREATE_TRANSACTION_RECORD;
        } else if(code == HederaFunctionality.CryptoAccountAutoRenew) {
            return CRYPTO_ACCOUNT_AUTO_RENEW;
        } else if(code == HederaFunctionality.ContractAutoRenew) {
            return CONTRACT_AUTO_RENEW;
        } else if(code == HederaFunctionality.GetVersionInfo) {
            return GET_VERSION_INFO;
        } else if(code == HederaFunctionality.TransactionGetReceipt) {
            return TRANSACTION_GET_RECEIPT;
        } else if(code == HederaFunctionality.ConsensusCreateTopic) {
            return CONSENSUS_CREATE_TOPIC;
        } else if(code == HederaFunctionality.ConsensusUpdateTopic) {
            return CONSENSUS_UPDATE_TOPIC;
        } else if(code == HederaFunctionality.ConsensusDeleteTopic) {
            return CONSENSUS_DELETE_TOPIC;
        } else if(code == HederaFunctionality.ConsensusGetTopicInfo) {
            return CONSENSUS_GET_TOPIC_INFO;
        } else if(code == HederaFunctionality.ConsensusSubmitMessage) {
            return CONSENSUS_SUBMIT_MESSAGE;
        } else if(code == HederaFunctionality.TokenCreate) {
            return TOKEN_CREATE;
        } else if(code == HederaFunctionality.TokenGetInfo) {
            return TOKEN_GET_INFO;
        } else if(code == HederaFunctionality.TokenFreezeAccount) {
            return TOKEN_FREEZE_ACCOUNT;
        } else if(code == HederaFunctionality.TokenUnfreezeAccount) {
            return TOKEN_UNFREEZE_ACCOUNT;
        } else if(code == HederaFunctionality.TokenGrantKycToAccount) {
            return TOKEN_GRANT_KYC_TO_ACCOUNT;
        } else if(code == HederaFunctionality.TokenRevokeKycFromAccount) {
            return TOKEN_REVOKE_KYC_FROM_ACCOUNT;
        } else if(code == HederaFunctionality.TokenDelete) {
            return TOKEN_DELETE;
        } else if(code == HederaFunctionality.TokenUpdate) {
            return TOKEN_UPDATE;
        } else if(code == HederaFunctionality.TokenMint) {
            return TOKEN_MINT;
        } else if(code == HederaFunctionality.TokenBurn) {
            return TOKEN_BURN;
        } else if(code == HederaFunctionality.TokenAccountWipe) {
            return TOKEN_ACCOUNT_WIPE;
        } else if(code == HederaFunctionality.TokenAssociateToAccount) {
            return TOKEN_ASSOCIATE_TO_ACCOUNT;
        } else if(code == HederaFunctionality.TokenDissociateFromAccount) {
            return TOKEN_DISSOCIATE_FROM_ACCOUNT;
        } else if(code == HederaFunctionality.ScheduleCreate) {
            return SCHEDULE_CREATE;
        } else if(code == HederaFunctionality.ScheduleDelete) {
            return SCHEDULE_DELETE;
        } else if(code == HederaFunctionality.ScheduleSign) {
            return SCHEDULE_SIGN;
        } else if(code == HederaFunctionality.ScheduleGetInfo) {
            return SCHEDULE_GET_INFO;
        } else {
            return NONE;
        }
    }

    @Override
    public String toString() {
        switch(this)
        {
            case NONE:
                return "NONE";
            case CRYPTO_TRANSFER:
                return "CRYPTO_TRANSFER";
            case CRYPTO_UPDATE:
                return "CRYPTO_UPDATE";
            case CRYPTO_DELETE:
                return "CRYPTO_DELETE";
            case CRYPTO_ADD_LIVE_HASH:
                return "CRYPTO_ADD_LIVE_HASH";
            case CRYPTO_DELETE_LIVE_HASH:
                return "CRYPTO_DELETE_LIVE_HASH";
            case CONTRACT_CALL:
                return "CONTRACT_CALL";
            case CONTRACT_CREATE:
                return "CONTRACT_CREATE";
            case CONTRACT_UPDATE:
                return "CONTRACT_UPDATE";
            case FILE_CREATE:
                return "FILE_CREATE";
            case FILE_APPEND:
                return "FILE_APPEND";
            case FILE_UPDATE:
                return "FILE_UPDATE";
            case FILE_DELETE:
                return "FILE_DELETE";
            case CRYPTO_GET_ACCOUNT_BALANCE:
                return "CRYPTO_GET_ACCOUNT_BALANCE";
            case CRYPTO_GET_ACCOUNT_RECORDS:
                return "CRYPTO_GET_ACCOUNT_RECORDS";
            case CRYPTO_GET_INFO:
                return "CRYPTO_GET_INFO";
            case CONTRACT_CALL_LOCAL:
                return "CONTRACT_CALL_LOCAL";
            case CONTRACT_GET_INFO:
                return "CONTRACT_GET_INFO";
            case CONTRACT_GET_BYTECODE:
                return "CONTRACT_GET_BYTECODE";
            case GET_BY_SOLIDITY_ID:
                return "GET_BY_SOLIDITY_ID";
            case GET_BY_KEY:
                return "GET_BY_KEY";
            case CRYPTO_GET_LIVE_HASH:
                return "CRYPTO_GET_LIVE_HASH";
            case CRYPTO_GET_STAKERS:
                return "CRYPTO_GET_STAKERS";
            case FILE_GET_CONTENTS:
                return "FILE_GET_CONTENTS";
            case FILE_GET_INFO:
                return "FILE_GET_INFO";
            case TRANSACTION_GET_RECORD:
                return "TRANSACTION_GET_RECORD";
            case CONTRACT_GET_RECORDS:
                return "CONTRACT_GET_RECORDS";
            case CRYPTO_CREATE:
                return "CRYPTO_CREATE";
            case SYSTEM_DELETE:
                return "SYSTEM_DELETE";
            case SYSTEM_UNDELETE:
                return "SYSTEM_UNDELETE";
            case CONTRACT_DELETE:
                return "CONTRACT_DELETE";
            case FREEZE:
                return "FREEZE";
            case CREATE_TRANSACTION_RECORD:
                return "CREATE_TRANSACTION_RECORD";
            case CRYPTO_ACCOUNT_AUTO_RENEW:
                return "CRYPTO_ACCOUNT_AUTO_RENEW";
            case CONTRACT_AUTO_RENEW:
                return "CONTRACT_AUTO_RENEW";
            case GET_VERSION_INFO:
                return "GET_VERSION_INFO";
            case TRANSACTION_GET_RECEIPT:
                return "TRANSACTION_GET_RECEIPT";
            case CONSENSUS_CREATE_TOPIC:
                return "CONSENSUS_CREATE_TOPIC";
            case CONSENSUS_UPDATE_TOPIC:
                return "CONSENSUS_UPDATE_TOPIC";
            case CONSENSUS_DELETE_TOPIC:
                return "CONSENSUS_DELETE_TOPIC";
            case CONSENSUS_GET_TOPIC_INFO:
                return "CONSENSUS_GET_TOPIC_INFO";
            case CONSENSUS_SUBMIT_MESSAGE:
                return "CONSENSUS_SUBMIT_MESSAGE";
            case TOKEN_CREATE:
                return "TOKEN_CREATE";
            case TOKEN_GET_INFO:
                return "TOKEN_GET_INFO";
            case TOKEN_FREEZE_ACCOUNT:
                return "TOKEN_FREEZE_ACCOUNT";
            case TOKEN_UNFREEZE_ACCOUNT:
                return "TOKEN_UNFREEZE_ACCOUNT";
            case TOKEN_GRANT_KYC_TO_ACCOUNT:
                return "TOKEN_GRANT_KYC_TO_ACCOUNT";
            case TOKEN_REVOKE_KYC_FROM_ACCOUNT:
                return "TOKEN_REVOKE_KYC_FROM_ACCOUNT";
            case TOKEN_DELETE:
                return "TOKEN_DELETE";
            case TOKEN_UPDATE:
                return "TOKEN_UPDATE";
            case TOKEN_MINT:
                return "TOKEN_MINT";
            case TOKEN_BURN:
                return "TOKEN_BURN";
            case TOKEN_ACCOUNT_WIPE:
                return "TOKEN_ACCOUNT_WIPE";
            case TOKEN_ASSOCIATE_TO_ACCOUNT:
                return "TOKEN_ASSOCIATE_TO_ACCOUNT";
            case TOKEN_DISSOCIATE_FROM_ACCOUNT:
                return "TOKEN_DISSOCIATE_FROM_ACCOUNT";
            case SCHEDULE_CREATE:
                return "SCHEDULE_CREATE";
            case SCHEDULE_DELETE:
                return "SCHEDULE_DELETE";
            case SCHEDULE_SIGN:
                return "SCHEDULE_SIGN";
            case SCHEDULE_GET_INFO:
                return "SCHEDULE_GET_INFO";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}
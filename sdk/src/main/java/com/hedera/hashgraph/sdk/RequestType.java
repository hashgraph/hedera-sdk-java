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
    SCHEDULE_GET_INFO(HederaFunctionality.ScheduleGetInfo),

    /**
     * Get Token Account Nft Information
     */
    TOKEN_GET_ACCOUNT_NFT_INFOS(HederaFunctionality.TokenGetAccountNftInfos),

    /**
     * Get Token Nft Information
     */
    TOKEN_GET_NFT_INFO(HederaFunctionality.TokenGetNftInfo),

    /**
     * Get Token Nft List Information
     */
    TOKEN_GET_NFT_INFOS(HederaFunctionality.TokenGetNftInfos),

    /**
     * Update a token's custom fee schedule, if permissible
     */
    TOKEN_FEE_SCHEDULE_UPDATE(HederaFunctionality.TokenFeeScheduleUpdate);

    final HederaFunctionality code;

    RequestType(HederaFunctionality code) {
        this.code = code;
    }

    static RequestType valueOf(HederaFunctionality code) {
        switch (code) {

            case NONE:
                return NONE;
            case CryptoTransfer:
                return CRYPTO_TRANSFER;
            case CryptoUpdate:
                return CRYPTO_UPDATE;
            case CryptoDelete:
                return CRYPTO_DELETE;
            case CryptoAddLiveHash:
                return CRYPTO_ADD_LIVE_HASH;
            case CryptoDeleteLiveHash:
                return CRYPTO_DELETE_LIVE_HASH;
            case ContractCall:
                return CONTRACT_CALL;
            case ContractCreate:
                return CONTRACT_CREATE;
            case ContractUpdate:
                return CONTRACT_UPDATE;
            case FileCreate:
                return FILE_CREATE;
            case FileAppend:
                return FILE_APPEND;
            case FileUpdate:
                return FILE_UPDATE;
            case FileDelete:
                return FILE_DELETE;
            case CryptoGetAccountBalance:
                return CRYPTO_GET_ACCOUNT_BALANCE;
            case CryptoGetAccountRecords:
                return CRYPTO_GET_ACCOUNT_RECORDS;
            case CryptoGetInfo:
                return CRYPTO_GET_INFO;
            case ContractCallLocal:
                return CONTRACT_CALL_LOCAL;
            case ContractGetInfo:
                return CONTRACT_GET_INFO;
            case ContractGetBytecode:
                return CONTRACT_GET_BYTECODE;
            case GetBySolidityID:
                return GET_BY_SOLIDITY_ID;
            case GetByKey:
                return GET_BY_KEY;
            case CryptoGetLiveHash:
                return CRYPTO_GET_LIVE_HASH;
            case CryptoGetStakers:
                return CRYPTO_GET_STAKERS;
            case FileGetContents:
                return FILE_GET_CONTENTS;
            case FileGetInfo:
                return FILE_GET_INFO;
            case TransactionGetRecord:
                return TRANSACTION_GET_RECORD;
            case ContractGetRecords:
                return CONTRACT_GET_RECORDS;
            case CryptoCreate:
                return CRYPTO_CREATE;
            case SystemDelete:
                return SYSTEM_DELETE;
            case SystemUndelete:
                return SYSTEM_UNDELETE;
            case ContractDelete:
                return CONTRACT_DELETE;
            case Freeze:
                return FREEZE;
            case CreateTransactionRecord:
                return CREATE_TRANSACTION_RECORD;
            case CryptoAccountAutoRenew:
                return CRYPTO_ACCOUNT_AUTO_RENEW;
            case ContractAutoRenew:
                return CONTRACT_AUTO_RENEW;
            case GetVersionInfo:
                return GET_VERSION_INFO;
            case TransactionGetReceipt:
                return TRANSACTION_GET_RECEIPT;
            case ConsensusCreateTopic:
                return CONSENSUS_CREATE_TOPIC;
            case ConsensusUpdateTopic:
                return CONSENSUS_UPDATE_TOPIC;
            case ConsensusDeleteTopic:
                return CONSENSUS_DELETE_TOPIC;
            case ConsensusGetTopicInfo:
                return CONSENSUS_GET_TOPIC_INFO;
            case ConsensusSubmitMessage:
                return CONSENSUS_SUBMIT_MESSAGE;
            case UncheckedSubmit:
                return UNCHECKED_SUBMIT;
            case TokenCreate:
                return TOKEN_CREATE;
            case TokenGetInfo:
                return TOKEN_GET_INFO;
            case TokenFreezeAccount:
                return TOKEN_FREEZE_ACCOUNT;
            case TokenUnfreezeAccount:
                return TOKEN_UNFREEZE_ACCOUNT;
            case TokenGrantKycToAccount:
                return TOKEN_GRANT_KYC_TO_ACCOUNT;
            case TokenRevokeKycFromAccount:
                return TOKEN_REVOKE_KYC_FROM_ACCOUNT;
            case TokenDelete:
                return TOKEN_DELETE;
            case TokenUpdate:
                return TOKEN_UPDATE;
            case TokenMint:
                return TOKEN_MINT;
            case TokenBurn:
                return TOKEN_BURN;
            case TokenAccountWipe:
                return TOKEN_ACCOUNT_WIPE;
            case TokenAssociateToAccount:
                return TOKEN_ASSOCIATE_TO_ACCOUNT;
            case TokenDissociateFromAccount:
                return TOKEN_DISSOCIATE_FROM_ACCOUNT;
            case ScheduleCreate:
                return SCHEDULE_CREATE;
            case ScheduleDelete:
                return SCHEDULE_DELETE;
            case ScheduleSign:
                return SCHEDULE_SIGN;
            case ScheduleGetInfo:
                return SCHEDULE_GET_INFO;
            case TokenGetAccountNftInfos:
                return TOKEN_GET_ACCOUNT_NFT_INFOS;
            case TokenGetNftInfo:
                return TOKEN_GET_NFT_INFO;
            case TokenGetNftInfos:
                return TOKEN_GET_NFT_INFOS;
            case TokenFeeScheduleUpdate:
                return TOKEN_FEE_SCHEDULE_UPDATE;
            default:
                throw new IllegalStateException("(BUG) unhandled HederaFunctionality");
        }
    }

    @Override
    public String toString() {
        switch(this) {

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
            case UNCHECKED_SUBMIT:
                return "UNCHECKED_SUBMIT";
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
            case TOKEN_GET_ACCOUNT_NFT_INFOS:
                return "TOKEN_GET_ACCOUNT_NFT_INFOS";
            case TOKEN_GET_NFT_INFO:
                return "TOKEN_GET_NFT_INFO";
            case TOKEN_GET_NFT_INFOS:
                return "TOKEN_GET_NFT_INFOS";
            case TOKEN_FEE_SCHEDULE_UPDATE:
                return "TOKEN_FEE_SCHEDULE_UPDATE";
            default:
                return "<UNRECOGNIZED VALUE>";
        }
    }
}

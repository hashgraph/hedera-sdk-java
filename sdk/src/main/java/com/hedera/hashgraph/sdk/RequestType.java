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
}
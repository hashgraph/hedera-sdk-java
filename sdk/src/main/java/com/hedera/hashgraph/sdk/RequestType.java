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

import com.hedera.hashgraph.sdk.proto.HederaFunctionality;

/**
 * Enum for the request types.
 */
public enum RequestType {
    /**
     * UNSPECIFIED - Need to keep first value as unspecified because first element is ignored and
     * not parsed (0 is ignored by parser)
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
     * Smart Contract, get the runtime code
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
    TOKEN_FEE_SCHEDULE_UPDATE(HederaFunctionality.TokenFeeScheduleUpdate),

    /**
     * Get execution time(s) by TransactionID, if available
     */
    NETWORK_GET_EXECUTION_TIME(HederaFunctionality.NetworkGetExecutionTime),

    /**
     * Pause the Token
     */
    TOKEN_PAUSE(HederaFunctionality.TokenPause),

    /**
     * Unpause the Token
     */
    TOKEN_UNPAUSE(HederaFunctionality.TokenUnpause),

    /**
     * Approve allowance for a spender relative to the owner account
     */
    CRYPTO_APPROVE_ALLOWANCE(HederaFunctionality.CryptoApproveAllowance),

    /**
     * Deletes granted allowances on owner account
     */
    CRYPTO_DELETE_ALLOWANCE(HederaFunctionality.CryptoDeleteAllowance),

    /**
     * Gets all the information about an account, including balance and allowances. This does not get the list of
     * account records.
     */
    GET_ACCOUNT_DETAILS(HederaFunctionality.GetAccountDetails),

    /**
     * Ethereum Transaction
     */
    ETHEREUM_TRANSACTION(HederaFunctionality.EthereumTransaction),

    /**
     * Updates the staking info at the end of staking period to indicate new staking period has started.
     */
    NODE_STAKE_UPDATE(HederaFunctionality.NodeStakeUpdate),

    /**
     * Generates a pseudorandom number.
     */
    PRNG(HederaFunctionality.UtilPrng);

    final HederaFunctionality code;

    RequestType(HederaFunctionality code) {
        this.code = code;
    }

    static RequestType valueOf(HederaFunctionality code) {
        return switch (code) {
            case NONE -> NONE;
            case CryptoTransfer -> CRYPTO_TRANSFER;
            case CryptoUpdate -> CRYPTO_UPDATE;
            case CryptoDelete -> CRYPTO_DELETE;
            case CryptoAddLiveHash -> CRYPTO_ADD_LIVE_HASH;
            case CryptoDeleteLiveHash -> CRYPTO_DELETE_LIVE_HASH;
            case ContractCall -> CONTRACT_CALL;
            case ContractCreate -> CONTRACT_CREATE;
            case ContractUpdate -> CONTRACT_UPDATE;
            case FileCreate -> FILE_CREATE;
            case FileAppend -> FILE_APPEND;
            case FileUpdate -> FILE_UPDATE;
            case FileDelete -> FILE_DELETE;
            case CryptoGetAccountBalance -> CRYPTO_GET_ACCOUNT_BALANCE;
            case CryptoGetAccountRecords -> CRYPTO_GET_ACCOUNT_RECORDS;
            case CryptoGetInfo -> CRYPTO_GET_INFO;
            case ContractCallLocal -> CONTRACT_CALL_LOCAL;
            case ContractGetInfo -> CONTRACT_GET_INFO;
            case ContractGetBytecode -> CONTRACT_GET_BYTECODE;
            case GetBySolidityID -> GET_BY_SOLIDITY_ID;
            case GetByKey -> GET_BY_KEY;
            case CryptoGetLiveHash -> CRYPTO_GET_LIVE_HASH;
            case CryptoGetStakers -> CRYPTO_GET_STAKERS;
            case FileGetContents -> FILE_GET_CONTENTS;
            case FileGetInfo -> FILE_GET_INFO;
            case TransactionGetRecord -> TRANSACTION_GET_RECORD;
            case ContractGetRecords -> CONTRACT_GET_RECORDS;
            case CryptoCreate -> CRYPTO_CREATE;
            case SystemDelete -> SYSTEM_DELETE;
            case SystemUndelete -> SYSTEM_UNDELETE;
            case ContractDelete -> CONTRACT_DELETE;
            case Freeze -> FREEZE;
            case CreateTransactionRecord -> CREATE_TRANSACTION_RECORD;
            case CryptoAccountAutoRenew -> CRYPTO_ACCOUNT_AUTO_RENEW;
            case ContractAutoRenew -> CONTRACT_AUTO_RENEW;
            case GetVersionInfo -> GET_VERSION_INFO;
            case TransactionGetReceipt -> TRANSACTION_GET_RECEIPT;
            case ConsensusCreateTopic -> CONSENSUS_CREATE_TOPIC;
            case ConsensusUpdateTopic -> CONSENSUS_UPDATE_TOPIC;
            case ConsensusDeleteTopic -> CONSENSUS_DELETE_TOPIC;
            case ConsensusGetTopicInfo -> CONSENSUS_GET_TOPIC_INFO;
            case ConsensusSubmitMessage -> CONSENSUS_SUBMIT_MESSAGE;
            case UncheckedSubmit -> UNCHECKED_SUBMIT;
            case TokenCreate -> TOKEN_CREATE;
            case TokenGetInfo -> TOKEN_GET_INFO;
            case TokenFreezeAccount -> TOKEN_FREEZE_ACCOUNT;
            case TokenUnfreezeAccount -> TOKEN_UNFREEZE_ACCOUNT;
            case TokenGrantKycToAccount -> TOKEN_GRANT_KYC_TO_ACCOUNT;
            case TokenRevokeKycFromAccount -> TOKEN_REVOKE_KYC_FROM_ACCOUNT;
            case TokenDelete -> TOKEN_DELETE;
            case TokenUpdate -> TOKEN_UPDATE;
            case TokenMint -> TOKEN_MINT;
            case TokenBurn -> TOKEN_BURN;
            case TokenAccountWipe -> TOKEN_ACCOUNT_WIPE;
            case TokenAssociateToAccount -> TOKEN_ASSOCIATE_TO_ACCOUNT;
            case TokenDissociateFromAccount -> TOKEN_DISSOCIATE_FROM_ACCOUNT;
            case ScheduleCreate -> SCHEDULE_CREATE;
            case ScheduleDelete -> SCHEDULE_DELETE;
            case ScheduleSign -> SCHEDULE_SIGN;
            case ScheduleGetInfo -> SCHEDULE_GET_INFO;
            case TokenGetAccountNftInfos -> TOKEN_GET_ACCOUNT_NFT_INFOS;
            case TokenGetNftInfo -> TOKEN_GET_NFT_INFO;
            case TokenGetNftInfos -> TOKEN_GET_NFT_INFOS;
            case TokenFeeScheduleUpdate -> TOKEN_FEE_SCHEDULE_UPDATE;
            case NetworkGetExecutionTime -> NETWORK_GET_EXECUTION_TIME;
            case TokenPause -> TOKEN_PAUSE;
            case TokenUnpause -> TOKEN_UNPAUSE;
            case CryptoApproveAllowance -> CRYPTO_APPROVE_ALLOWANCE;
            case CryptoDeleteAllowance -> CRYPTO_DELETE_ALLOWANCE;
            case GetAccountDetails -> GET_ACCOUNT_DETAILS;
            case EthereumTransaction -> ETHEREUM_TRANSACTION;
            case NodeStakeUpdate -> NODE_STAKE_UPDATE;
            case UtilPrng -> PRNG;
            default -> throw new IllegalStateException("(BUG) unhandled HederaFunctionality");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "NONE";
            case CRYPTO_TRANSFER -> "CRYPTO_TRANSFER";
            case CRYPTO_UPDATE -> "CRYPTO_UPDATE";
            case CRYPTO_DELETE -> "CRYPTO_DELETE";
            case CRYPTO_ADD_LIVE_HASH -> "CRYPTO_ADD_LIVE_HASH";
            case CRYPTO_DELETE_LIVE_HASH -> "CRYPTO_DELETE_LIVE_HASH";
            case CONTRACT_CALL -> "CONTRACT_CALL";
            case CONTRACT_CREATE -> "CONTRACT_CREATE";
            case CONTRACT_UPDATE -> "CONTRACT_UPDATE";
            case FILE_CREATE -> "FILE_CREATE";
            case FILE_APPEND -> "FILE_APPEND";
            case FILE_UPDATE -> "FILE_UPDATE";
            case FILE_DELETE -> "FILE_DELETE";
            case CRYPTO_GET_ACCOUNT_BALANCE -> "CRYPTO_GET_ACCOUNT_BALANCE";
            case CRYPTO_GET_ACCOUNT_RECORDS -> "CRYPTO_GET_ACCOUNT_RECORDS";
            case CRYPTO_GET_INFO -> "CRYPTO_GET_INFO";
            case CONTRACT_CALL_LOCAL -> "CONTRACT_CALL_LOCAL";
            case CONTRACT_GET_INFO -> "CONTRACT_GET_INFO";
            case CONTRACT_GET_BYTECODE -> "CONTRACT_GET_BYTECODE";
            case GET_BY_SOLIDITY_ID -> "GET_BY_SOLIDITY_ID";
            case GET_BY_KEY -> "GET_BY_KEY";
            case CRYPTO_GET_LIVE_HASH -> "CRYPTO_GET_LIVE_HASH";
            case CRYPTO_GET_STAKERS -> "CRYPTO_GET_STAKERS";
            case FILE_GET_CONTENTS -> "FILE_GET_CONTENTS";
            case FILE_GET_INFO -> "FILE_GET_INFO";
            case TRANSACTION_GET_RECORD -> "TRANSACTION_GET_RECORD";
            case CONTRACT_GET_RECORDS -> "CONTRACT_GET_RECORDS";
            case CRYPTO_CREATE -> "CRYPTO_CREATE";
            case SYSTEM_DELETE -> "SYSTEM_DELETE";
            case SYSTEM_UNDELETE -> "SYSTEM_UNDELETE";
            case CONTRACT_DELETE -> "CONTRACT_DELETE";
            case FREEZE -> "FREEZE";
            case CREATE_TRANSACTION_RECORD -> "CREATE_TRANSACTION_RECORD";
            case CRYPTO_ACCOUNT_AUTO_RENEW -> "CRYPTO_ACCOUNT_AUTO_RENEW";
            case CONTRACT_AUTO_RENEW -> "CONTRACT_AUTO_RENEW";
            case GET_VERSION_INFO -> "GET_VERSION_INFO";
            case TRANSACTION_GET_RECEIPT -> "TRANSACTION_GET_RECEIPT";
            case CONSENSUS_CREATE_TOPIC -> "CONSENSUS_CREATE_TOPIC";
            case CONSENSUS_UPDATE_TOPIC -> "CONSENSUS_UPDATE_TOPIC";
            case CONSENSUS_DELETE_TOPIC -> "CONSENSUS_DELETE_TOPIC";
            case CONSENSUS_GET_TOPIC_INFO -> "CONSENSUS_GET_TOPIC_INFO";
            case CONSENSUS_SUBMIT_MESSAGE -> "CONSENSUS_SUBMIT_MESSAGE";
            case UNCHECKED_SUBMIT -> "UNCHECKED_SUBMIT";
            case TOKEN_CREATE -> "TOKEN_CREATE";
            case TOKEN_GET_INFO -> "TOKEN_GET_INFO";
            case TOKEN_FREEZE_ACCOUNT -> "TOKEN_FREEZE_ACCOUNT";
            case TOKEN_UNFREEZE_ACCOUNT -> "TOKEN_UNFREEZE_ACCOUNT";
            case TOKEN_GRANT_KYC_TO_ACCOUNT -> "TOKEN_GRANT_KYC_TO_ACCOUNT";
            case TOKEN_REVOKE_KYC_FROM_ACCOUNT -> "TOKEN_REVOKE_KYC_FROM_ACCOUNT";
            case TOKEN_DELETE -> "TOKEN_DELETE";
            case TOKEN_UPDATE -> "TOKEN_UPDATE";
            case TOKEN_MINT -> "TOKEN_MINT";
            case TOKEN_BURN -> "TOKEN_BURN";
            case TOKEN_ACCOUNT_WIPE -> "TOKEN_ACCOUNT_WIPE";
            case TOKEN_ASSOCIATE_TO_ACCOUNT -> "TOKEN_ASSOCIATE_TO_ACCOUNT";
            case TOKEN_DISSOCIATE_FROM_ACCOUNT -> "TOKEN_DISSOCIATE_FROM_ACCOUNT";
            case SCHEDULE_CREATE -> "SCHEDULE_CREATE";
            case SCHEDULE_DELETE -> "SCHEDULE_DELETE";
            case SCHEDULE_SIGN -> "SCHEDULE_SIGN";
            case SCHEDULE_GET_INFO -> "SCHEDULE_GET_INFO";
            case TOKEN_GET_ACCOUNT_NFT_INFOS -> "TOKEN_GET_ACCOUNT_NFT_INFOS";
            case TOKEN_GET_NFT_INFO -> "TOKEN_GET_NFT_INFO";
            case TOKEN_GET_NFT_INFOS -> "TOKEN_GET_NFT_INFOS";
            case TOKEN_FEE_SCHEDULE_UPDATE -> "TOKEN_FEE_SCHEDULE_UPDATE";
            case NETWORK_GET_EXECUTION_TIME -> "NETWORK_GET_EXECUTION_TIME";
            case TOKEN_PAUSE -> "TOKEN_PAUSE";
            case TOKEN_UNPAUSE -> "TOKEN_UNPAUSE";
            case CRYPTO_APPROVE_ALLOWANCE -> "CRYPTO_APPROVE_ALLOWANCE";
            case CRYPTO_DELETE_ALLOWANCE -> "CRYPTO_DELETE_ALLOWANCE";
            case GET_ACCOUNT_DETAILS -> "GET_ACCOUNT_DETAILS";
            case ETHEREUM_TRANSACTION -> "ETHEREUM_TRANSACTION";
            case NODE_STAKE_UPDATE -> "NODE_STAKE_UPDATE";
            case PRNG -> "PRNG";
        };
    }
}

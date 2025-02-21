// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.account.*;
import com.hedera.hashgraph.tck.methods.sdk.response.AccountAllowanceResponse;
import com.hedera.hashgraph.tck.methods.sdk.response.AccountResponse;
import com.hedera.hashgraph.tck.util.KeyUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * AccountService for account related methods
 */
@JSONRPC2Service
public class AccountService extends AbstractJSONRPC2Service {
    private final SdkService sdkService;

    public AccountService(SdkService sdkService) {
        this.sdkService = sdkService;
    }

    @JSONRPC2Method("createAccount")
    public AccountResponse createAccount(final AccountCreateParams params) throws Exception {
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction();
        params.getKey().ifPresent(key -> {
            try {
                accountCreateTransaction.setKeyWithoutAlias(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getInitialBalance()
                .ifPresent(initialBalanceTinybars -> accountCreateTransaction.setInitialBalance(
                        Hbar.from(Long.parseLong(initialBalanceTinybars), HbarUnit.TINYBAR)));

        params.getReceiverSignatureRequired().ifPresent(accountCreateTransaction::setReceiverSignatureRequired);

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds -> accountCreateTransaction.setAutoRenewPeriod(
                        Duration.ofSeconds(Long.parseLong(autoRenewPeriodSeconds))));

        params.getMemo().ifPresent(accountCreateTransaction::setAccountMemo);

        params.getMaxAutoTokenAssociations()
                .ifPresent(autoAssociations ->
                        accountCreateTransaction.setMaxAutomaticTokenAssociations(autoAssociations.intValue()));

        params.getStakedAccountId()
                .ifPresent(stakedAccountId ->
                        accountCreateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId()
                .ifPresent(stakedNodeId -> accountCreateTransaction.setStakedNodeId(Long.parseLong(stakedNodeId)));

        params.getDeclineStakingReward().ifPresent(accountCreateTransaction::setDeclineStakingReward);

        params.getAlias().ifPresent(accountCreateTransaction::setAlias);

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(accountCreateTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                accountCreateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        String stringAccountId = "";
        if (transactionReceipt.status == Status.SUCCESS) {
            stringAccountId = transactionReceipt.accountId.toString();
        }

        return new AccountResponse(stringAccountId, transactionReceipt.status);
    }

    @JSONRPC2Method("updateAccount")
    public AccountResponse updateAccount(final AccountUpdateParams params) throws Exception {
        AccountUpdateTransaction accountUpdateTransaction = new AccountUpdateTransaction();

        params.getAccountId()
                .ifPresent(accountId -> accountUpdateTransaction.setAccountId(AccountId.fromString(accountId)));

        params.getKey().ifPresent(key -> {
            try {
                accountUpdateTransaction.setKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getReceiverSignatureRequired().ifPresent(accountUpdateTransaction::setReceiverSignatureRequired);

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds -> accountUpdateTransaction.setAutoRenewPeriod(
                        Duration.ofSeconds(Long.parseLong(autoRenewPeriodSeconds))));

        params.getMemo().ifPresent(accountUpdateTransaction::setAccountMemo);

        params.getExpirationTime()
                .ifPresent(expirationTime -> accountUpdateTransaction.setExpirationTime(
                        Instant.ofEpochSecond(Long.parseLong(expirationTime))));

        params.getMaxAutoTokenAssociations()
                .ifPresent(autoAssociations ->
                        accountUpdateTransaction.setMaxAutomaticTokenAssociations(autoAssociations.intValue()));

        params.getStakedAccountId()
                .ifPresent(stakedAccountId ->
                        accountUpdateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId()
                .ifPresent(stakedNodeId -> accountUpdateTransaction.setStakedNodeId(Long.parseLong(stakedNodeId)));

        params.getDeclineStakingReward().ifPresent(accountUpdateTransaction::setDeclineStakingReward);

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(accountUpdateTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                accountUpdateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new AccountResponse(null, transactionReceipt.status);
    }

    @JSONRPC2Method("deleteAccount")
    public AccountResponse deleteAccount(final AccountDeleteParams params) throws Exception {
        AccountDeleteTransaction accountDeleteTransaction = new AccountDeleteTransaction();

        params.getDeleteAccountId()
                .ifPresent(accountId -> accountDeleteTransaction.setAccountId(AccountId.fromString(accountId)));

        params.getTransferAccountId()
                .ifPresent(accountId -> accountDeleteTransaction.setTransferAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(accountDeleteTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                accountDeleteTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new AccountResponse(null, transactionReceipt.status);
    }

    @JSONRPC2Method("approveAllowance")
    public AccountAllowanceResponse approveAllowance(final AccountAllowanceParams params) throws Exception {
        AccountAllowanceApproveTransaction tx = new AccountAllowanceApproveTransaction();

        params.getAllowances().ifPresent(allowances -> allowances.forEach(allowance -> approve(tx, allowance)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(tx, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                tx.execute(sdkService.getClient()).getReceipt(sdkService.getClient());
        return new AccountAllowanceResponse(transactionReceipt.status);
    }

    @JSONRPC2Method("deleteAllowance")
    public AccountAllowanceResponse deleteAllowance(final AccountAllowanceParams params) throws Exception {
        AccountAllowanceDeleteTransaction tx = new AccountAllowanceDeleteTransaction();

        params.getAllowances().ifPresent(allowances -> allowances.forEach(allowance -> delete(tx, allowance)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(tx, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                tx.execute(sdkService.getClient()).getReceipt(sdkService.getClient());
        return new AccountAllowanceResponse(transactionReceipt.status);
    }

    private void approve(AccountAllowanceApproveTransaction tx, AllowanceParams allowance) {
        AccountId owner = AccountId.fromString(allowance.getOwnerAccountId().orElseThrow());
        AccountId spender = AccountId.fromString(allowance.getSpenderAccountId().orElseThrow());

        allowance
                .getHbar()
                .ifPresent(hbar ->
                        tx.approveHbarAllowance(owner, spender, Hbar.fromTinybars(Long.parseLong(hbar.getAmount()))));

        allowance
                .getToken()
                .ifPresent(token -> tx.approveTokenAllowance(
                        TokenId.fromString(token.getTokenId()), owner, spender, token.getAmount()));

        allowance.getNft().ifPresent(nft -> approveNFT(tx, owner, spender, nft));
    }

    private void delete(AccountAllowanceDeleteTransaction tx, AllowanceParams allowance) {
        var owner = AccountId.fromString(allowance.getOwnerAccountId().orElseThrow());
        var tokenId = allowance.getTokenId().orElseThrow();

        if (allowance.getSerialNumbers().isPresent()) {
            allowance.getSerialNumbers().get().forEach(serialNumber -> {
                var nftId = new NftId(TokenId.fromString(tokenId), Long.parseLong(serialNumber));
                tx.deleteAllTokenNftAllowances(nftId, owner);
            });
        }
    }

    private void approveNFT(
            AccountAllowanceApproveTransaction tx,
            AccountId owner,
            AccountId spender,
            AllowanceParams.TokenNftAllowance nft) {
        TokenId tokenId = TokenId.fromString(nft.getTokenId());
        Optional<String> delegateSpender = Optional.ofNullable(nft.getDelegatingSpender());

        if (!nft.getSerialNumbers().isEmpty()) {
            nft.getSerialNumbers().forEach(serial -> {
                NftId nftId = new NftId(tokenId, serial);
                delegateSpender.ifPresentOrElse(
                        ds -> tx.approveTokenNftAllowance(nftId, owner, spender, AccountId.fromString(ds)),
                        () -> tx.approveTokenNftAllowance(nftId, owner, spender));
            });
        } else if (nft.getAllSerials()) {
            tx.approveTokenNftAllowanceAllSerials(tokenId, owner, spender);
        } else {
            tx.deleteTokenNftAllowanceAllSerials(tokenId, owner, spender);
        }
    }
}

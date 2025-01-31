// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.account.AccountCreateParams;
import com.hedera.hashgraph.tck.methods.sdk.param.account.AccountDeleteParams;
import com.hedera.hashgraph.tck.methods.sdk.param.account.AccountUpdateParams;
import com.hedera.hashgraph.tck.methods.sdk.response.AccountResponse;
import com.hedera.hashgraph.tck.util.KeyUtils;
import java.time.Duration;
import java.time.Instant;

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
                accountCreateTransaction.setKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getInitialBalance()
                .ifPresent(initialBalanceTinybars -> accountCreateTransaction.setInitialBalance(
                        Hbar.from(initialBalanceTinybars, HbarUnit.TINYBAR)));

        params.getReceiverSignatureRequired().ifPresent(accountCreateTransaction::setReceiverSignatureRequired);

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds ->
                        accountCreateTransaction.setAutoRenewPeriod(Duration.ofSeconds(autoRenewPeriodSeconds)));

        params.getMemo().ifPresent(accountCreateTransaction::setAccountMemo);

        params.getMaxAutoTokenAssociations()
                .ifPresent(autoAssociations ->
                        accountCreateTransaction.setMaxAutomaticTokenAssociations(autoAssociations.intValue()));

        params.getStakedAccountId()
                .ifPresent(stakedAccountId ->
                        accountCreateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId().ifPresent(accountCreateTransaction::setStakedNodeId);

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
                .ifPresent(autoRenewPeriodSeconds ->
                        accountUpdateTransaction.setAutoRenewPeriod(Duration.ofSeconds(autoRenewPeriodSeconds)));

        params.getMemo().ifPresent(accountUpdateTransaction::setAccountMemo);

        params.getExpirationTime()
                .ifPresent(expirationTime ->
                        accountUpdateTransaction.setExpirationTime(Instant.ofEpochSecond(expirationTime)));

        params.getMaxAutoTokenAssociations()
                .ifPresent(autoAssociations ->
                        accountUpdateTransaction.setMaxAutomaticTokenAssociations(autoAssociations.intValue()));

        params.getStakedAccountId()
                .ifPresent(stakedAccountId ->
                        accountUpdateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId().ifPresent(accountUpdateTransaction::setStakedNodeId);

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
}

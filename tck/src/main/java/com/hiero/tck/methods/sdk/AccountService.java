/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hiero.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.AccountDeleteTransaction;
import com.hiero.sdk.AccountId;
import com.hiero.sdk.AccountUpdateTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.HbarUnit;
import com.hiero.sdk.Status;
import com.hiero.sdk.TransactionReceipt;
import com.hiero.tck.annotation.JSONRPC2Method;
import com.hiero.tck.annotation.JSONRPC2Service;
import com.hiero.tck.methods.AbstractJSONRPC2Service;
import com.hiero.tck.methods.sdk.param.AccountCreateParams;
import com.hiero.tck.methods.sdk.param.AccountDeleteParams;
import com.hiero.tck.methods.sdk.param.AccountUpdateParams;
import com.hiero.tck.methods.sdk.response.AccountResponse;
import com.hiero.tck.util.KeyUtils;
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

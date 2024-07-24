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
package com.hedera.hashgraph.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.AccountCreateParams;
import com.hedera.hashgraph.tck.methods.sdk.response.AccountCreateResponse;
import com.hedera.hashgraph.tck.util.KeyUtils;
import java.time.Duration;

/**
 * AccountCreateService for account related methods
 */
@JSONRPC2Service
public class AccountService extends AbstractJSONRPC2Service {
    private final SdkService sdkService;

    public AccountService(SdkService sdkService) {
        this.sdkService = sdkService;
    }

    @JSONRPC2Method("createAccount")
    public AccountCreateResponse createAccount(final AccountCreateParams params) throws Exception {
        try {
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
                    .ifPresent(commonTransactionParams -> commonTransactionParams.fillOutTransaction(
                            accountCreateTransaction, sdkService.getClient()));

            TransactionReceipt transactionReceipt =
                    accountCreateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

            String stringAccountId = "";
            if (transactionReceipt.status == Status.SUCCESS) {
                stringAccountId = transactionReceipt.accountId.toString();
            }

            return new AccountCreateResponse(stringAccountId, transactionReceipt.status);

        } finally {
            sdkService.getClient().close();
        }
    }
}

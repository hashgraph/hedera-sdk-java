package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransferTransaction;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.AccountCreateParams;
import com.hedera.hashgraph.tck.methods.sdk.param.AccountCreateParamsFromAlias;
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
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction();

        params.getPublicKey().ifPresent(key -> accountCreateTransaction.setKey(KeyUtils.getKeyFromStringDER(key)));

        params.getInitialBalance()
                .ifPresent(initialBalanceTinybars -> accountCreateTransaction.setInitialBalance(
                        Hbar.from(initialBalanceTinybars, HbarUnit.TINYBAR)));

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

        params.getStakedNodeId().ifPresent(accountCreateTransaction::setStakedNodeId);

        params.getDeclineStakingReward().ifPresent(accountCreateTransaction::setDeclineStakingReward);

        params.getAlias().ifPresent(accountCreateTransaction::setAlias);

        params.getPrivateKey().ifPresent(pk -> {
            var privateKey = PrivateKey.fromString(pk);
            accountCreateTransaction.freezeWith(sdkService.getClient()).sign(privateKey);
        });

        TransactionReceipt transactionReceipt =
                accountCreateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        String stringAccountId = "";
        if (transactionReceipt.status == Status.SUCCESS) {
            stringAccountId = transactionReceipt.accountId.toString();
        }

        return new AccountCreateResponse(stringAccountId, transactionReceipt.status);
    }

    @JSONRPC2Method("createAccountFromAlias")
    public TransactionReceipt createAccountFromAlias(final AccountCreateParamsFromAlias params) throws Exception {
        TransferTransaction transferTransaction = new TransferTransaction();
        AccountId operator = new AccountId(0);
        AccountId alias = new AccountId(0);
        Hbar amount = Hbar.ZERO;
        if (params.getOperatorID().isPresent()) {
            operator = AccountId.fromString(params.getOperatorID().get());
        }
        if (params.getAliasAccountID().isPresent()) {
            alias = AccountId.fromString(params.getAliasAccountID().get());
        }
        if (params.getInitialBalance().isPresent()) {
            amount = Hbar.from(params.getInitialBalance().get());
        }

        return transferTransaction
                .addHbarTransfer(operator, amount.negated())
                .addHbarTransfer(alias, amount)
                .execute(sdkService.getClient())
                .getReceipt(sdkService.getClient());
    }
}

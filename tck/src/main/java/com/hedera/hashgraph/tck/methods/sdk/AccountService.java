package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
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
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AccountCreateService for account related methods
 */
@JSONRPC2Service
public class AccountService extends AbstractJSONRPC2Service {

    @Autowired
    private SdkService sdkService;

    @JSONRPC2Method("createAccount")
    public AccountCreateResponse createAccount(final AccountCreateParams params)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction();

        params.getKey().ifPresent(key -> accountCreateTransaction.setKey(KeyUtils.getKeyFromStringDER(key)));

        params.getInitialBalance()
                .ifPresent(initialBalanceTinybars -> accountCreateTransaction.setInitialBalance(
                        Hbar.from(initialBalanceTinybars, HbarUnit.TINYBAR)));

        params.getReceiverSignatureRequired().ifPresent(accountCreateTransaction::setReceiverSignatureRequired);

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds ->
                        accountCreateTransaction.setAutoRenewPeriod(Duration.ofSeconds(autoRenewPeriodSeconds)));

        params.getMemo().ifPresent(accountCreateTransaction::setAccountMemo);

        params.getMaxAutoTokenAssociations().ifPresent(accountCreateTransaction::setMaxAutomaticTokenAssociations);

        params.getStakedAccountId()
                .ifPresent(stakedAccountId ->
                        accountCreateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId().ifPresent(accountCreateTransaction::setStakedNodeId);

        params.getDeclineStakingReward().ifPresent(accountCreateTransaction::setDeclineStakingReward);

        params.getAlias().ifPresent(accountCreateTransaction::setAlias);

        TransactionReceipt transactionReceipt =
                accountCreateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new AccountCreateResponse(transactionReceipt.accountId, transactionReceipt.status);
    }

    @JSONRPC2Method("createAccountFromAlias")
    public TransactionReceipt createAccountFromAlias(final AccountCreateParamsFromAlias params)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        TransferTransaction transferTransaction = new TransferTransaction();
        var operator = AccountId.fromString(params.getOperatorID());

        var alias = AccountId.fromString(params.getAliasAccountID());
        var amount = Hbar.from(params.getInitialBalance());

        TransactionReceipt transactionReceipt = transferTransaction
                .addHbarTransfer(operator, amount.negated())
                .addHbarTransfer(alias, amount)
                .execute(sdkService.getClient())
                .getReceipt(sdkService.getClient());

        return transactionReceipt;
    }
}

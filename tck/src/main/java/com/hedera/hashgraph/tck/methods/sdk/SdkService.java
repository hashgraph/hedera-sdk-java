package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.exception.HederaException;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.AccountCreateParams;
import com.hedera.hashgraph.tck.methods.sdk.param.SetupParams;
import com.hedera.hashgraph.tck.methods.sdk.response.AccountCreateResponse;
import com.hedera.hashgraph.tck.methods.sdk.response.SetupResponse;
import com.hedera.hashgraph.tck.util.KeyUtils;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * SdkService for managing the {@link Client} setup and reset
 */
@JSONRPC2Service
public class SdkService extends AbstractJSONRPC2Service {
    // this is shared state to all requests so there could be race conditions
    // although the tck driver would not call these methods in such way
    private Client client;

    @JSONRPC2Method("setup")
    public SetupResponse setup(final SetupParams params) throws HederaException {
        String clientType;
        try {
            if (params.getNodeIp() != null
                    && params.getNodeAccountId() != null
                    && params.getMirrorNetworkIp() != null) {
                // Custom client setup
                Map<String, AccountId> node = new HashMap<>();
                var nodeId = new AccountId(Integer.parseInt(params.getNodeAccountId()));
                node.put(params.getNodeIp(), nodeId);
                client = Client.forNetwork(node);
                clientType = "custom";
                client.setMirrorNetwork(List.of(params.getMirrorNetworkIp()));
            } else {
                // Default to testnet
                client = Client.forTestnet();
                clientType = "testnet";
            }

            client.setOperator(
                    AccountId.fromString(params.getOperatorAccountId()),
                    PrivateKey.fromString(params.getOperatorPrivateKey()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HederaException(e);
        } catch (Exception e) {
            throw new HederaException(e);
        }
        return new SetupResponse("Successfully setup " + clientType + " client.");
    }

    @JSONRPC2Method("reset")
    public SetupResponse reset() {
        client = null;
        return new SetupResponse("");
    }

    @JSONRPC2Method("createAccount")
    public AccountCreateResponse createAccount(final AccountCreateParams params)
        throws HederaException, PrecheckStatusException, TimeoutException, ReceiptStatusException {
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction();

        params.getKey().ifPresent(key -> accountCreateTransaction.setKey(KeyUtils.getKeyFromStringDER(key)));

        params.getInitialBalance().ifPresent(initialBalanceTinybars -> accountCreateTransaction.setInitialBalance(Hbar.from(initialBalanceTinybars, HbarUnit.TINYBAR)));

        params.getReceiverSignatureRequired().ifPresent(accountCreateTransaction::setReceiverSignatureRequired);

        params.getAutoRenewPeriod().ifPresent(autoRenewPeriodSeconds -> accountCreateTransaction.setAutoRenewPeriod(
            Duration.ofSeconds(autoRenewPeriodSeconds)));

        params.getMemo().ifPresent(accountCreateTransaction::setAccountMemo);

        params.getMaxAutoTokenAssociations().ifPresent(accountCreateTransaction::setMaxAutomaticTokenAssociations);

        params.getStakedAccountId().ifPresent(stakedAccountId -> accountCreateTransaction.setStakedAccountId(AccountId.fromString(stakedAccountId)));

        params.getStakedNodeId().ifPresent(accountCreateTransaction::setStakedNodeId);

        params.getDeclineStakingReward().ifPresent(accountCreateTransaction::setDeclineStakingReward);

        params.getAlias().ifPresent(accountCreateTransaction::setAlias);

//        params.get

        TransactionReceipt transactionReceipt = accountCreateTransaction.execute(client).getReceipt(client);

        return new AccountCreateResponse(
            transactionReceipt.accountId, transactionReceipt.status
        );
    }


    public Client getClient() {
        return this.client;
    }
}

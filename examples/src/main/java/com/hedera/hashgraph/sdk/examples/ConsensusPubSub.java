package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.HashMap;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class ConsensusPubSub {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private static final String MIRROR_NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"));

    private ConsensusPubSub() { }

    public static void main(String[] args) throws InterruptedException, HederaStatusException {
        final ConsensusClient consensusClient = new ConsensusClient(MIRROR_NODE_ADDRESS)
            .setErrorHandler(e -> System.out.println("error in consensus client: " + e));

        // To improve responsiveness, you should specify multiple nodes
        Client client = new Client(new HashMap<AccountId, String>() {
            {
                put(NODE_ID, NODE_ADDRESS);
            }
        });

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final TransactionId transactionId = new ConsensusTopicCreateTransaction()
            .setMaxTransactionFee(1_000_000_000)
            .execute(client);

        final ConsensusTopicId topicId = transactionId.getReceipt(client).getConsensusTopicId();

        consensusClient.subscribe(topicId, message -> {
            System.out.println(message.consensusTimestamp + " received topic message: " + message.getMessageString());
        });

        // keep the main thread from exiting because the listeners run on daemon threads
        for (int i = 0; ; i++) {
            new ConsensusMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("hello, HCS! " + i)
                .execute(client)
                .getReceipt(client);

            Thread.sleep(2500);
        }
    }
}

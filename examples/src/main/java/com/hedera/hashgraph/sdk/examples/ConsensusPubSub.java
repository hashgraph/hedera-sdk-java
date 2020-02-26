package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.HederaTopicMessageException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicState;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;
import io.github.cdimascio.dotenv.Dotenv;

public final class ConsensusPubSub {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private static final String MIRROR_NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"));

    private ConsensusPubSub() {
    }

    public static void main(String[] args) throws InterruptedException, HederaStatusException {
        final MirrorClient mirrorClient = new MirrorClient(MIRROR_NODE_ADDRESS);

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        final Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final TransactionId transactionId = new ConsensusTopicCreateTransaction()
            .execute(client);

        final ConsensusTopicId topicId = transactionId.getReceipt(client).getConsensusTopicId();
        System.out.println("New topic created: " + topicId);

        final ConsensusTopicState topicState = ConsensusTopicState.forNewTopic(topicId);

        new MirrorConsensusTopicQuery()
            .setTopicId(topicId)
            .subscribe(mirrorClient, resp -> {

                try {
                    topicState.update(resp.message, resp.sequenceNumber, resp.runningHash, resp.consensusTimestamp);
                } catch (HederaTopicMessageException ex) {
                    ex.printStackTrace();
                    try {
                        mirrorClient.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String messageAsString = new String(resp.message, StandardCharsets.UTF_8);

                System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
            },
                // On gRPC error, print the stack trace
                Throwable::printStackTrace);

        // keep the main thread from exiting because the listeners run on daemon threads
        // noinspection InfiniteLoopStatement
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

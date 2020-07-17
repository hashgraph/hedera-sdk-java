package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ConsensusPubSubChunked {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private static final String MIRROR_NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"));

    private ConsensusPubSubChunked() {
    }

    public static void main(String[] args) throws InterruptedException, HederaStatusException {
        final MirrorClient mirrorClient = new MirrorClient(MIRROR_NODE_ADDRESS);

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // make a new topic ID to use
        ConsensusTopicId newTopicId = new ConsensusTopicCreateTransaction()
            .setTopicMemo("hedera-sdk-java/ConsensusPubSubChunkedExample")
            .execute(client)
            .getReceipt(client)
            .getConsensusTopicId();

        System.out.println("for topic " + newTopicId);

        // Let's wait a bit
        System.out.println("wait 10s to propagate to the mirror ...");
        Thread.sleep(10000);

        // setup a mirror client to print out messages as we receive them
        new MirrorConsensusTopicQuery()
            .setTopicId(newTopicId)
            .subscribe(mirrorClient, resp -> {
                    System.out.println("at " + resp.consensusTimestamp + " ( seq = " + resp.sequenceNumber + " ) received topic message of " + resp.message.length + " bytes");
                },
                // On gRPC error, print the stack trace
                Throwable::printStackTrace);

        // get a large file to send
        ClassLoader classLoader = ConsensusPubSubChunked.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("large_message.txt");
        StringBuilder bigContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bigContents.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("about to send a message of " + bigContents.length() + " bytes");

        // send a message that would fit into more than one chunk (4-6k per chunk)
        TransactionId initialId = new ConsensusMessageSubmitTransaction()
            .setMaxChunks(5) // this is 10 by default
            .setTopicId(newTopicId)
            .setMessage(bigContents.toString())
            .execute(client);

        for (int i = 0; ; i++) {
            System.out.println("waiting ...");
            Thread.sleep(2500);
        }
    }
}

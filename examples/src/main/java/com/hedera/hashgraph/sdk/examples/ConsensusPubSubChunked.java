package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // make a new topic ID to use
        ConsensusTopicId newTopicId = new ConsensusTopicCreateTransaction()
            .execute(client)
            .getReceipt(client)
            .getConsensusTopicId();

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

        // send a message that would fit into more than one chunk (4-6k per chunk)
        List<TransactionId> ids = new ConsensusMessageSubmitTransaction()
            .setMaxChunks(5) // this is 10 by default
            .setTopicId(newTopicId)
            .setMessage(bigContents.toString())
            .execute(client);

        System.out.println("ids = " + Arrays.toString(ids.toArray()));
    }
}

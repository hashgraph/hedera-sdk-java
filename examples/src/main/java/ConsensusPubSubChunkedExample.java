import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicId;
import com.hedera.hashgraph.sdk.TopicMessageQuery;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.Transaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class ConsensusPubSubChunkedExample {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private ConsensusPubSubChunkedExample() {
    }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException, InterruptedException {
        Client client = Client.forTestnet().setOperator(OPERATOR_ID, OPERATOR_KEY);

        // prepare a second client without an operator for use by "somewhere else"
        Client clientWithoutOperator = Client.forTestnet();

        // generate a submit key to use with the topic
        PrivateKey submitKey = PrivateKey.generate();

        // make a new topic ID to use
        TopicId newTopicId = new TopicCreateTransaction()
            .setTopicMemo("hedera-sdk-java/ConsensusPubSubChunkedExample")
            .setSubmitKey(submitKey)
            .execute(client)
            .transactionId
            .getReceipt(client)
            .topicId;

        assert newTopicId != null;

        System.out.println("for topic " + newTopicId);

        // Let's wait a bit
        System.out.println("wait 10s to propagate to the mirror ...");
        Thread.sleep(10000);

        // setup a mirror client to print out messages as we receive them
        new TopicMessageQuery()
            .setTopicId(newTopicId)
            .subscribe(client, topicMessage -> {
                System.out.println("at " + topicMessage.consensusTimestamp + " ( seq = " + topicMessage.sequenceNumber + " ) received topic message of " + topicMessage.contents.length + " bytes");
            });

        // get a large file to send
        String bigContents = readResources("large_message.txt");

        System.out.println("about to prepare a transaction to send a message of " + bigContents.length() + " bytes");

        // prepare a message send transaction that requires a submit key from "somewhere else"
        Transaction<?> transaction = new TopicMessageSubmitTransaction()
            .setMaxChunks(5) // this is 10 by default
            .setTopicId(newTopicId)
            .setMessage(bigContents)
            // sign with the operator or "sender" of the message
            // this is the party who will be charged the transaction fee
            .signWithOperator(client);

        // serialize to bytes so we can be signed "somewhere else" by the submit key
        byte[] transactionBytes = transaction.toBytes();

        // now pretend we sent those bytes across the network
        // parse them into a transaction so we can sign as the submit key
        transaction = Transaction.fromBytes(transactionBytes);

        // view out the message size from the parsed transaction
        // this can be useful to display what we are about to sign
        var transactionMessageSize = ((TopicMessageSubmitTransaction)transaction).getMessage().size();
        System.out.println("about to send a transaction with a message of " + transactionMessageSize + " bytes");

        // sign with that submit key
        transaction.sign(submitKey);

        // now actually submit the transaction
        // get the receipt to ensure there were no errors
        transaction.execute(clientWithoutOperator).transactionId.getReceipt(clientWithoutOperator);

        for (int i = 0; ; i++) {
            System.out.println("waiting ...");
            Thread.sleep(2500);
        }
    }

    private static String readResources(String filename) {
        ClassLoader classLoader = ConsensusPubSubChunkedExample.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        StringBuilder bigContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bigContents.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bigContents.toString();
    }
}

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import java.util.HashMap;
import java.util.List;

public class ClientHelper {
    public static final String LOCAL_NETWORK_NAME = "localhost";
    private static final String DEFAULT_LOCAL_NODE_ADDRESS = "127.0.0.1:50211";
    private static final String DEFAULT_LOCAL_MIRROR_NODE_ADDRESS = "127.0.0.1:5600";

    public static Client forName(String network) throws InterruptedException {
        if (network.equals(LOCAL_NETWORK_NAME)) {
            return forLocalNetwork();
        } else {
            return Client.forName(network);
        }
    }

    public static Client forLocalNetwork() throws InterruptedException {
        var network = new HashMap<String, AccountId>();
        network.put(DEFAULT_LOCAL_NODE_ADDRESS, new AccountId(3));

        return Client
            .forNetwork(network)
            .setMirrorNetwork(List.of(DEFAULT_LOCAL_MIRROR_NODE_ADDRESS));
    }
}

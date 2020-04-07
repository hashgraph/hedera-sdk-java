import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

class GenerateKeysExample {
    private GenerateKeysExample() {
    }

    public static void main(String[] args) {
        PrivateKey privateKey = PrivateKey.generateEd25519();
        PublicKey publicKey = privateKey.getPublicKey();

        System.out.println("private = " + privateKey);
        System.out.println("public = " + publicKey);
    }
}

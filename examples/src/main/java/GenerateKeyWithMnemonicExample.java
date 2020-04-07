import com.hedera.hashgraph.sdk.Mnemonic;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

class GenerateKeyWithMnemonicExample {
    private GenerateKeyWithMnemonicExample() {
    }

    public static void main(String[] args) {
        Mnemonic mnemonic = Mnemonic.generate();
        PrivateKey privateKey = mnemonic.toPrivateKey();
        PublicKey publicKey = privateKey.getPublicKey();

        System.out.println("mnemonic = " + mnemonic);
        System.out.println("private key = " + privateKey);
        System.out.println("public key = " + publicKey);
    }
}

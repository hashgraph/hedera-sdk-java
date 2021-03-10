import com.hedera.hashgraph.sdk.BadMnemonicException;
import com.hedera.hashgraph.sdk.Mnemonic;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

import java.security.NoSuchAlgorithmException;

class GenerateKeyWithMnemonicExample {
    private GenerateKeyWithMnemonicExample() {
    }

    public static void main(String[] args) {
        Mnemonic mnemonic = Mnemonic.generate24();
        PrivateKey privateKey;
        try{
            privateKey = mnemonic.toPrivateKey();
        }
        catch(BadMnemonicException e){
            throw new Error(e.reason.toString());
        }
        PublicKey publicKey = privateKey.getPublicKey();

        Mnemonic mnemonic12 = Mnemonic.generate12();
        PrivateKey privateKey12;
        try{
            privateKey12 = mnemonic12.toPrivateKey();
        }
        catch(BadMnemonicException e){
            throw new Error(e.reason.toString());
        }
        PublicKey publicKey12 = privateKey12.getPublicKey();

        System.out.println("mnemonic 24 word = " + mnemonic);
        System.out.println("private key = " + privateKey);
        System.out.println("public key = " + publicKey);

        System.out.println("mnemonic 12 word = " + mnemonic12);
        System.out.println("private key = " + privateKey12);
        System.out.println("public key = " + publicKey12);
    }
}

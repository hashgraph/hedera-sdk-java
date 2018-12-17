package test.hedera.sdk.cryptography;

import java.util.Arrays;
import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import java.util.List;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.cryptography.CryptoUtils;
import com.hedera.sdk.cryptography.Reference;
import com.hedera.sdk.cryptography.Seed;

import org.spongycastle.util.encoders.Hex;


@TestInstance(Lifecycle.PER_CLASS)
class CryptoUtilsTest {

  @BeforeAll
  void setup() {
  }

  @Test
  final void testEd25519DerivedKey() {

    // From SeedHex get private key
    String seedHex = "cf831ccb83f7d1d6a0261e2a6f69552dbd452d7b3a8fb4f5f960e8aafcf0d32f";
    byte[] edseed1 = Hex.decode(seedHex);
    HederaKeyPair ed25519Keys1 = new HederaKeyPair(KeyType.ED25519, edseed1);
    byte[] edPrivateKey1 = Arrays.copyOfRange(ed25519Keys1.getSecretKey(), 16, 48);

    // From Recovery Words get private key
    String ed25519RecoveryWords = "embark port duly poetry front verity quote lake toast Austin pig far pink clergy allied still injury night canine frenzy gamma heel";
    List<String> allWords = Reference.wordIndices(ed25519RecoveryWords);
    Seed seed = Seed.fromWordList(allWords);
    byte[] edseed2 = seed.toBytes();
    HederaKeyPair ed25519Keys2 = new HederaKeyPair(KeyType.ED25519, edseed2);
    byte[] edPrivateKey2 = Arrays.copyOfRange(ed25519Keys2.getSecretKey(), 16, 48);
    Assert.assertArrayEquals(edPrivateKey1, edPrivateKey2);

    // Derive Key and compare with above 2 seeds
    long index = 0;
    int length = 32;
    byte[] edPrivateKey3 = CryptoUtils.deriveKey(edseed1, index, length);
    byte[] edPrivateKey4 = CryptoUtils.deriveKey(edseed2, index, length);
    Assert.assertArrayEquals(edPrivateKey3, edPrivateKey4);
  }

//  @Test

//  final void testECDSA384DerivedKey() {
//
//    // From SeedHex get private key
//    String ecSeedHex = "a5c5f697c872703ae5a046772f13ae5fa2501e348e35e992598017851bcc6170af487a3a8e94be6a26f6c6e15cee407d";
//    byte[] ecseed1 = Hex.decode(ecSeedHex);
//    HederaKeyPair ecdsa384Keys1 = new HederaKeyPair(KeyType.ECDSA384, ecseed1);
//    byte[] ecdsaPrivateKey1 = Arrays.copyOfRange(ecdsa384Keys1.getSecretKey(), 1, 49);
//
//    // From Recovery Words get private key
//    String ecdsa384RecoveryWords = "Alaska crown July defy death skinny ego Rex hurl tense win flower buffet shah wolves bloody depart insult hazard treat strand loan trot method rich Hanoi whole steer issue Baltic Spain rash social";
//    List<String> allWords = Reference.wordIndices(ecdsa384RecoveryWords);
//    Seed seed = Seed.fromWordList(allWords);
//    byte[] ecseed2 = seed.toBytes();
//    HederaKeyPair ecdsa384Keys2 = new HederaKeyPair(KeyType.ECDSA384, ecseed2);
//    byte[] ecdsaPrivateKey2 = Arrays.copyOfRange(ecdsa384Keys2.getSecretKey(), 1, 49);
//    Assert.assertArrayEquals(ecdsaPrivateKey1, ecdsaPrivateKey2);
//
//    long index = 0;
//    int length = 48;
//    byte[] ecdsaPrivateKey3 = CryptoUtils.deriveKey(ecseed1, index, length);
//    byte[] ecdsaPrivateKey4 = CryptoUtils.deriveKey(ecseed2, index, length);
//    Assert.assertArrayEquals(ecdsaPrivateKey3, ecdsaPrivateKey4);
//
//  }
}   
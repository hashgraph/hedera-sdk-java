package test.hedera.sdk.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;

@Disabled
class KeyTestED25519 {

	protected static HederaKeyPair keyPaired1;
	protected static List<String> recoveryed;
	protected static HederaKeyPair keyPaired2;
	protected static HederaKeyPair ed25519KeyFromPrivate;
	protected static String ed25519PublicKey;

	@BeforeAll
	static void initAll() {
		try {
			// word recovery
			keyPaired1 = new HederaKeyPair(KeyType.ED25519);
			recoveryed = keyPaired1.recoveryWordsList();
			keyPaired2 = new HederaKeyPair(KeyType.ED25519, recoveryed);
			// check against android unit tests
			// String seedHex =
			// "aabbccdd11223344aabbccdd11223344aaaaaaaabbbbcc59aa2244116688bb22";
			String seedHex = "cf831ccb83f7d1d6a0261e2a6f69552dbd452d7b3a8fb4f5f960e8aafcf0d32f";
			byte[] seed = Hex.decode(seedHex);
			ed25519KeyFromPrivate = new HederaKeyPair(KeyType.ED25519, seed);
			ed25519PublicKey = Hex.toHexString(ed25519KeyFromPrivate.getPublicKey());

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("ED25519 Check word recovery")
	void testED25519() {
		Assert.assertArrayEquals(keyPaired1.getPublicKey(), keyPaired2.getPublicKey());
		Assert.assertArrayEquals(keyPaired1.getSecretKey(), keyPaired2.getSecretKey());
	}

	@Test
	@DisplayName("ED25519 Check key generation against android")
	void testECDSA384_vs_Android() {
		assertEquals("2e9cae72c094bb978b9757cbc054443348ae91385b778dae268c37e24bab2fbb", ed25519PublicKey);
	}

	@Test
	@DisplayName("ED25519 Check signature")
	void testECDSA384_Signature() throws Exception {
		byte[] message = Hex.decode("3c147d61");
		byte[] signature = keyPaired1.signMessage(message);
		boolean verified = keyPaired1.verifySignature(message, signature);
		assertTrue(verified);
	}

}

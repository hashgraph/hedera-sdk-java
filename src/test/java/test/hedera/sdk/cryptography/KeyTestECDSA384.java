//package test.hedera.sdk.cryptography;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import java.security.NoSuchAlgorithmException;
//import java.util.List;
//import org.junit.Assert;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.spongycastle.util.encoders.Hex;
//
//import com.hedera.sdk.common.HederaKeyPair.KeyType;
//import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
//
//@Disabled
//class KeyTestECDSA384 {
//
//	protected static HederaCryptoKeyPair keyPairec1;
//	protected static List<String> recoveryec;
//	protected static HederaCryptoKeyPair keyPairec2;
//	protected static HederaCryptoKeyPair ecdsa384KeyFromPrivate;
//	protected static String ecdsa384PublicKey;
//	protected static String publicKeyED;
//	
//	@BeforeAll
//	static void initAll() {
//		try {
//			// word recovery
//			keyPairec1 = new HederaCryptoKeyPair(KeyType.ECDSA384);
//			recoveryec = keyPairec1.recoveryWordsList();
//			keyPairec2 = new HederaCryptoKeyPair(KeyType.ECDSA384, recoveryec);
//			// check against android unit tests
//	        //String ecDSA384PrivateKeyHex = "3c147d61b4f1666352179abd091c9b042f661423a4fa0476af0941c13e49f9c155b1bd65ea4b551a801b64477e758656";
//			String ecDSA384PrivateKeyHex = "a5c5f697c872703ae5a046772f13ae5fa2501e348e35e992598017851bcc6170af487a3a8e94be6a26f6c6e15cee407d";
//	        byte[] privateKeyECSeed = Hex.decode(ecDSA384PrivateKeyHex);
//	        ecdsa384KeyFromPrivate = new HederaCryptoKeyPair(KeyType.ECDSA384, privateKeyECSeed);
//	        ecdsa384PublicKey = Hex.toHexString(ecdsa384KeyFromPrivate.getPublicKey());
//
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Test
//	@DisplayName("ECDSA384 Check word recovery")
//	void testECDSA384() {
//		Assert.assertArrayEquals(keyPairec1.getPublicKey(), keyPairec2.getPublicKey());
//		Assert.assertArrayEquals(keyPairec1.getSecretKey(), keyPairec2.getSecretKey());
//	}
//	
//	@Test
//	@DisplayName("ECDSA384 Check key generation against android")
//	void testECDSA384_Android() {
////        assertEquals("041c30405f001d83a779d43d6e8be9f35d34583bc95fbf1e626092f3aca766fa8343dc4cd014c994574f50c9a1498099422753737f68fb565103143cf68cd54cb49fdee36f2c5be2cca1ecda376ee296111f4d2171ef15d17ea98d40e166943809"
////        			,ecdsa384PublicKey);
//        assertEquals("04ad17283160d3bcc97ec3bb2b0d7a407fd7114594d48edcc8ed7de4917c0feb11e421d5966520ecb700961810329de43179268291ce4dca66560314b5d0d27d303a1108cb042269e1c1c8770335df777e96e82b210b68b6ff0d2d83e5161e522c"
//    			,ecdsa384PublicKey);
//	}
//
//	@Test
//	@DisplayName("ECDSA384 Sign / verify")
//	void testECDSA384Sign_Verify() throws Exception {
//        byte[] message = Hex.decode("3c147d61");
//        byte[] signature = keyPairec1.signMessage(message);
////        String signatureString = Hex.toHexString(signature);
//        boolean verified = keyPairec1.verifySignature(message, signature);
//        assertTrue(verified);
//	}
//}

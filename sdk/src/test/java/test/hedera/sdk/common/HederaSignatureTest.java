package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaSignatureThreshold;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HederaSignatureTest {
	protected static byte[] sigBytes = new byte[] {12};
	protected static List<HederaSignature> sigList = new ArrayList<HederaSignature>();
	protected static HederaSignatureThreshold thresholdSig;
	protected static int threshold = 10;
	protected static HederaSignatureList hederaSigList = new HederaSignatureList();

	@BeforeAll
	static void initAll() {
		sigList.add(new HederaSignature(KeyType.ED25519, sigBytes));
		thresholdSig = new HederaSignatureThreshold(sigList);
		hederaSigList.addSignature(new HederaSignature(KeyType.ED25519, sigBytes));
	}
	
	@ParameterizedTest
	@DisplayName("Checking constructor sig init")
	@MethodSource("sigInit")
	void testKeyInit(HederaSignature masterSig, KeyType sigType, byte[] bytes) { 
		assertEquals(sigType, masterSig.getSignatureType());
		assertArrayEquals(bytes, masterSig.getSignature());

		HederaSignature protobufSig = new HederaSignature(masterSig.getProtobuf());
		assertEquals(masterSig.getSignatureType(), protobufSig.getSignatureType());
		assertArrayEquals(masterSig.getSignature(), protobufSig.getSignature());
	}
	 
	private static Stream<Arguments> sigInit() {
		return Stream.of(
			Arguments.of(new HederaSignature(KeyType.ED25519, sigBytes), KeyType.ED25519, sigBytes),
			Arguments.of(new HederaSignature(KeyType.ED25519, sigBytes), KeyType.ED25519, sigBytes),
			Arguments.of(new HederaSignature(KeyType.ED25519, sigBytes), KeyType.ED25519, sigBytes),
			Arguments.of(new HederaSignature(KeyType.CONTRACT, new byte[0]), KeyType.CONTRACT, new byte[0])
		);
	}

	@ParameterizedTest
	@DisplayName("Checking constructor sig init 2")
	@MethodSource("sigInit2")
	void testKeyInit2(HederaSignature masterSig, KeyType sigType, byte[] bytes) { 
		assertEquals(sigType, masterSig.getSignatureType());
		assertArrayEquals(bytes, masterSig.getSignature());
	}
	 
	private static Stream<Arguments> sigInit2() {
		return Stream.of(
			Arguments.of(new HederaSignature(KeyType.THRESHOLD, sigBytes), KeyType.THRESHOLD, sigBytes),
			Arguments.of(new HederaSignature(KeyType.LIST, sigBytes), KeyType.LIST, sigBytes)
		);
	}
	
	@Test
	@DisplayName("Checking THRESHOLD sig init")
	void testSigInitThreshold() {
		HederaSignatureThreshold sigThreshold = new HederaSignatureThreshold();
		sigThreshold.signatures = sigList;
		HederaSignature masterSig = new HederaSignature(sigThreshold);
		
		assertEquals(KeyType.THRESHOLD, masterSig.getSignatureType());
		assertEquals(sigThreshold.signatures.size(), masterSig.getThresholdSignature().signatures.size());
		assertArrayEquals(sigThreshold.signatures.get(0).getSignature(), masterSig.getThresholdSignature().signatures.get(0).getSignature());

		HederaSignature protobufSig = new HederaSignature(masterSig.getProtobuf());
		assertEquals(KeyType.THRESHOLD, protobufSig.getSignatureType());
		assertEquals(sigThreshold.signatures.size(), protobufSig.getThresholdSignature().signatures.size());
		assertArrayEquals(sigThreshold.signatures.get(0).getSignature(), protobufSig.getThresholdSignature().signatures.get(0).getSignature());
	}
	 
	@Test
	@DisplayName("Checking LIST sig init")
	void testKeyInitList() {
		HederaSignatureList signatureList = new HederaSignatureList();
		signatureList.signatures = sigList;
		HederaSignature masterSig = new HederaSignature(signatureList);
		
		assertEquals(KeyType.LIST, masterSig.getSignatureType());
		assertEquals(signatureList.signatures.size(), masterSig.getSignatureList().signatures.size());
		assertArrayEquals(signatureList.signatures.get(0).getSignature(), masterSig.getSignatureList().signatures.get(0).getSignature());

		HederaSignature protobufSig = new HederaSignature(masterSig.getProtobuf());
		assertEquals(KeyType.LIST, protobufSig.getSignatureType());
		assertEquals(signatureList.signatures.size(), protobufSig.getSignatureList().signatures.size());
		assertArrayEquals(signatureList.signatures.get(0).getSignature(), protobufSig.getSignatureList().signatures.get(0).getSignature());
	}
}

package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureThreshold;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaSignatureThresholdTest {

	@Test
	@DisplayName("Checking siglist init")
	void testSigListInit() {
		HederaSignature sig1 = new HederaSignature(KeyType.ED25519, new byte[] {12,34,56});
		HederaSignature sig2 = new HederaSignature(KeyType.ED25519, new byte[] {78,90,12});
		List<HederaSignature> sigs = new ArrayList<HederaSignature>();
		sigs.add(sig1);
		sigs.add(sig2);
		
		HederaSignatureThreshold masterSigThreshold = new HederaSignatureThreshold(sigs);
		
		assertEquals(sigs.size(), masterSigThreshold.signatures.size());
		
		masterSigThreshold.addSignature(sig1);
		masterSigThreshold.addSignature(sig2);

		assertEquals(4, masterSigThreshold.signatures.size());

		masterSigThreshold.deleteSignature(sig1);
		masterSigThreshold.deleteSignature(sig2);

		assertEquals(2, masterSigThreshold.signatures.size());
		
		HederaSignatureThreshold protobufList = new HederaSignatureThreshold(masterSigThreshold.getProtobuf());
		assertEquals(masterSigThreshold.signatures.size(), protobufList.signatures.size());
		assertArrayEquals(masterSigThreshold.signatures.get(0).getSignature(), protobufList.signatures.get(0).getSignature());
		assertArrayEquals(masterSigThreshold.signatures.get(1).getSignature(), protobufList.signatures.get(1).getSignature());
	}

}

package test.hedera.sdk.common;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaKeyPair.KeyType;

class HederaSignatureListTest {

	@Test
	@DisplayName("Checking siglist init")
	void testSigListInit() {
		HederaSignature sig1 = new HederaSignature(KeyType.ED25519, new byte[] {12,34,56});
		HederaSignature sig2 = new HederaSignature(KeyType.ED25519, new byte[] {78,90,12});
		List<HederaSignature> sigs = new ArrayList<HederaSignature>();
		sigs.add(sig1);
		sigs.add(sig2);
		
		HederaSignatureList masterSigList = new HederaSignatureList(sigs);
		
		assertEquals(sigs.size(), masterSigList.signatures.size());
		
		masterSigList.addSignature(sig1);
		masterSigList.addSignature(sig2);

		assertEquals(4, masterSigList.signatures.size());

		masterSigList.deleteSignature(sig1);
		masterSigList.deleteSignature(sig2);

		assertEquals(2, masterSigList.signatures.size());
		
		HederaSignatureList protobufList = new HederaSignatureList(masterSigList.getProtobuf());
		assertEquals(masterSigList.signatures.size(), protobufList.signatures.size());
		assertArrayEquals(masterSigList.signatures.get(0).getSignature(), protobufList.signatures.get(0).getSignature());
		assertArrayEquals(masterSigList.signatures.get(1).getSignature(), protobufList.signatures.get(1).getSignature());
	}
}
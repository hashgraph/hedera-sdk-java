package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;

class HederaTransactionAndQueryDefaultsTest {

	@Test
	@DisplayName("HederaTransactionAndQueryDefaultsTest")
	void HederaTXQueryDefaultsTest() {
		HederaTransactionAndQueryDefaults txDefaults = new HederaTransactionAndQueryDefaults();
		
		assertNotNull(txDefaults.node);
		assertNotNull(txDefaults.payingAccountID);
		assertNotNull(txDefaults.transactionValidDuration);
		assertFalse(txDefaults.generateRecord);
		assertTrue(txDefaults.memo.equals(""));
		assertNull(txDefaults.payingKeyPair);
		assertNull(txDefaults.fileWacl);
	}
}

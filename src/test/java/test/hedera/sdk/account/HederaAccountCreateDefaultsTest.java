package test.hedera.sdk.account;

import static org.junit.jupiter.api.Assertions.*;

import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountCreateDefaults;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;

class HederaAccountCreateDefaultsTest {

	@Test
	@DisplayName("HederaAccountCreateDefaultsTest")
	void test() throws InvalidKeySpecException {
		HederaAccountCreateDefaults values = new HederaAccountCreateDefaults();
		HederaAccount accountDefaultsFromClass = new HederaAccount();

		assertEquals(accountDefaultsFromClass.autoRenewPeriod.seconds, values.autoRenewPeriodSeconds);
		assertEquals(accountDefaultsFromClass.autoRenewPeriod.nanos, values.autoRenewPeriodNanos);
		assertEquals(accountDefaultsFromClass.receiverSigRequired, values.receiverSignatureRequired);
		assertEquals(accountDefaultsFromClass.maxReceiveProxyFraction, values.maxReceiveProxyFraction);
		assertEquals(accountDefaultsFromClass.proxyFraction, values.proxyFraction);
		
		assertEquals(accountDefaultsFromClass.autoRenewPeriod.seconds, values.autoRenewPeriodSeconds);
		assertEquals(accountDefaultsFromClass.autoRenewPeriod.seconds, values.autoRenewPeriodSeconds);
		assertEquals(accountDefaultsFromClass.receiveRecordThreshold, values.receiveRecordThreshold);
		assertEquals(accountDefaultsFromClass.sendRecordThreshold, values.sendRecordThreshold);


		values.setProxyAccountID(2, 3, 4);
		assertEquals(2, values.getProxyAccountID().shardNum);
		assertEquals(3, values.getProxyAccountID().realmNum);
		assertEquals(4, values.getProxyAccountID().accountNum);
		values.resetProxyAccountID();
		assertNull(values.getProxyAccountID());

		HederaKeyPair key = new HederaKeyPair(KeyType.ED25519);
		values.setNewRealmAdminPublicKey(KeyType.ED25519, key.getPublicKey(), null);
		assertEquals(KeyType.ED25519, values.getNewRealmAdminPublicKey().getKeyType());
		assertArrayEquals(key.getPublicKey(), values.getNewRealmAdminPublicKey().getPublicKey());

		values.setNewRealmAdminPublicKey(KeyType.ED25519, "302a300506032b6570032100af0cff0d2d603e21c2fb7b8747d08990dde88e1f6f9dd9df55af09f77a991f60", null);
		byte[] hexKey = Hex.decode("302a300506032b6570032100af0cff0d2d603e21c2fb7b8747d08990dde88e1f6f9dd9df55af09f77a991f60");
		assertEquals(KeyType.ED25519, values.getNewRealmAdminPublicKey().getKeyType());
		assertArrayEquals(hexKey, values.getNewRealmAdminPublicKey().getPublicKeyEncoded());
		
	}
}


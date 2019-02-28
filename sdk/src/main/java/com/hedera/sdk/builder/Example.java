package com.hedera.sdk.builder;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.cryptography.EDKeyPair;
import com.hedera.sdk.transaction.HederaTransactionResult;

public class Example {

	public static void main (String... arguments) throws Exception {
		
		HederaKeyPair test = new HederaKeyPair(KeyType.ED25519,"","");
		EDKeyPair operatorSecret = Hedera.SecretKeyFromString("");

		EDKeyPair secret = Hedera.GenerateSecretKey();

		System.out.println("secret = " + secret.getPrivateKeyEncodedHex());
		System.out.println("public = " + secret.getPrivateKeyEncodedHex());

		HederaAccountID nodeAccountID = Hedera.AccountID(3);
		HederaAccountID operatorAccountID = Hedera.AccountID(2);

		HederaTransactionResult result = new Client
				.CreateAccount()
//				.dial("testnet.hedera.com:50003")
				.dial()
				.key(secret.getPublicKeyEncodedHex())
				.initialBalance(10)
				.operator(operatorAccountID)
				.node(nodeAccountID)
				.memo("Builder Test")
				.sign(operatorSecret)
				.execute();
		System.out.println(result.hederaTransactionID.getProtobuf());
		
		
	}
}

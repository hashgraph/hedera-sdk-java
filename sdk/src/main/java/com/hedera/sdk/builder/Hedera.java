package com.hedera.sdk.builder;

import org.bouncycastle.util.encoders.Hex;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.cryptography.CryptoUtils;
import com.hedera.sdk.cryptography.EDKeyPair;
import com.hedera.sdk.cryptography.Seed;

public class Hedera {
	public static EDKeyPair SecretKeyFromString(String privateKey) {
		EDKeyPair keyPair = EDKeyPair.buildFromPrivateKey(Hex.decode(privateKey));
		return keyPair;
	}
	public static EDKeyPair GenerateSecretKey() {
		return GenerateSecretKey(-1);	
	}
	public static EDKeyPair GenerateSecretKey(int index) {
		byte[] seed = CryptoUtils.getSecureRandomData(32);
		Seed keySeed = Seed.fromEntropy(seed);
		byte[] privateKey = CryptoUtils.deriveKey(keySeed.toBytes(), index, 32);
		return new EDKeyPair(privateKey);	
	}
	
	public static HederaAccountID AccountID (String accountID) {
		return new HederaAccountID(accountID);
	}
	
	public static HederaAccountID AccountID (long shardNum, long realmNum, long accountNum) {
		return new HederaAccountID(shardNum, realmNum, accountNum);
	}
	
	public static HederaAccountID AccountID (long accountNum) {
		return new HederaAccountID(0, 0, accountNum);
	}
}

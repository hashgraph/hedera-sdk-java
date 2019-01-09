package com.hedera.sdk.file;

import org.spongycastle.util.encoders.Hex;

import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
/**
 * This class holds default values for an account creation
 * You can optionally create an instance of this class to set different default values
 * and re-use it on every account creation by passing it into the HederaCryptoCurrency.CreateAccount 
 * method for the account to override defaults
 */
public class HederaFileCreateDefaults {
	private HederaFile fileDefaultsFromClass = new HederaFile();

	private HederaKeyPair newRealmAdminPublicKey = fileDefaultsFromClass.newRealmAdminKey;
	/**
	 * The expiration time for a file in seconds
	 */
	public long expirationTimeSeconds = fileDefaultsFromClass.expirationTime.getEpochSecond();
	/**
	 * The nanoseconds element of the file's expiration time 
	 */
	public int expirationTimeNanos = 0;

	// Methods
	/**
	 * if realmID is -1, then this the admin key for the new realm that will be created
	 * it is ignored otherwise
	 * @param keyType the type of key
	 * @param newRealmAdminKey the new realm admin key
	 */
	public void setNewRealmAdminPublicKey(KeyType keyType,byte[] newRealmAdminKey) {
		this.newRealmAdminPublicKey = new HederaKeyPair(keyType, newRealmAdminKey, null);
	}
	/**
	 * if realmID is -1, then this the admin key for the new realm that will be created
	 * it is ignored otherwise
	 * @param keyType the type of key
	 * @param newRealmAdminKeyHex the new realm admin key in string hex format
	 */
	public void setNewRealmAdminPublicKey(KeyType keyType,String newRealmAdminKeyHex) {
		this.newRealmAdminPublicKey = new HederaKeyPair(keyType, Hex.decode(newRealmAdminKeyHex));
	}
	/**
	 * Gets the new realm admin key
	 * @return {@link HederaKeyPair}
	 */
	public HederaKeyPair getNewRealmAdminPublicKey() {
		return this.newRealmAdminPublicKey;
	}
}

package com.hedera.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaKey.KeyType;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.node.HederaNode;

public class ExampleUtilities {
	final Logger logger = LoggerFactory.getLogger(ExampleUtilities.class);
	public static String nodeAddress = "";
	public static int nodePort = 0;

	public static long nodeAccountShard = 0;
	public static long nodeAccountRealm = 0;
	public static long nodeAccountNum = 0;
	
	public static String pubKey = "";
	public static String privKey = "";
	
	public static long payAccountShard = 0;
	public static long payAccountRealm = 0;
	public static long payAccountNum = 0;

	public static void getNodeDetails() {

		// load application properties
		Properties applicationProperties = new Properties();
		InputStream propertiesInputStream = null;
			
		try {

			propertiesInputStream = new FileInputStream("node.properties");

			// load a properties file
			applicationProperties.load(propertiesInputStream);

			// get the property value and print it out
			nodeAddress = applicationProperties.getProperty("nodeaddress");
			nodePort = Integer.parseInt(applicationProperties.getProperty("nodeport"));

			nodeAccountShard = Long.parseLong(applicationProperties.getProperty("nodeAccountShard"));
			nodeAccountRealm = Long.parseLong(applicationProperties.getProperty("nodeAccountRealm"));
			nodeAccountNum = Long.parseLong(applicationProperties.getProperty("nodeAccountNum"));
			
			pubKey = applicationProperties.getProperty("pubkey");
			privKey = applicationProperties.getProperty("privkey");
			
			payAccountShard = Long.parseLong(applicationProperties.getProperty("payingAccountShard"));
			payAccountRealm = Long.parseLong(applicationProperties.getProperty("payingAccountRealm"));
			payAccountNum = Long.parseLong(applicationProperties.getProperty("payingAccountNum"));

		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		} finally {
			if (propertiesInputStream != null) {
				try {
					propertiesInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static HederaTransactionAndQueryDefaults getTxQueryDefaults() throws InvalidKeySpecException {
		// Get node details 
		ExampleUtilities.getNodeDetails();
		
		// setup node account ID
		HederaAccountID nodeAccountID = new HederaAccountID(ExampleUtilities.nodeAccountShard, ExampleUtilities.nodeAccountRealm, ExampleUtilities.nodeAccountNum);
		// setup node
		HederaNode node = new HederaNode(ExampleUtilities.nodeAddress, ExampleUtilities.nodePort, nodeAccountID);
		
		// setup paying account
		HederaAccountID payingAccountID = new HederaAccountID(ExampleUtilities.payAccountShard, ExampleUtilities.payAccountRealm, ExampleUtilities.payAccountNum);
		
		// setup paying keypair
		HederaCryptoKeyPair payingKeyPair = new HederaCryptoKeyPair(KeyType.ED25519, ExampleUtilities.pubKey, ExampleUtilities.privKey);
		
		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		
		txQueryDefaults.memo = "Demo memo";
		txQueryDefaults.node = node;
		txQueryDefaults.payingAccountID = payingAccountID;
		txQueryDefaults.payingKeyPair = payingKeyPair;
		txQueryDefaults.transactionValidDuration = new HederaDuration(120, 0);
		
		return txQueryDefaults;
	}
	
}

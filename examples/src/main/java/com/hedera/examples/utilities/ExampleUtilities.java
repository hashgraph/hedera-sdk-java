package com.hedera.examples.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import org.apache.commons.codec.DecoderException;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKeyList;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.node.HederaNode;


public class ExampleUtilities {
	final static ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ExampleUtilities.class);
	public static String nodeAddress = "";
	public static int nodePort = 0;

	public static long nodeAccountShard = 0;
	public static long nodeAccountRealm = 0;
	public static long nodeAccountNum = 0;
	
	public static String pubKey = "";
	public static String privKey = "";
	public static String keyType = "";
	
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
			keyType = applicationProperties.getProperty("keyType");
			
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
	
	public static HederaTransactionAndQueryDefaults getTxQueryDefaults() throws InvalidKeySpecException, DecoderException {
		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();

		// Get node details 
		ExampleUtilities.getNodeDetails();
		
		// setup node account ID
		HederaAccountID nodeAccountID = new HederaAccountID(ExampleUtilities.nodeAccountShard, ExampleUtilities.nodeAccountRealm, ExampleUtilities.nodeAccountNum);
		// setup node
		HederaNode node = new HederaNode(ExampleUtilities.nodeAddress, ExampleUtilities.nodePort, nodeAccountID);
		
		// setup paying account
		HederaAccountID payingAccountID = new HederaAccountID(ExampleUtilities.payAccountShard, ExampleUtilities.payAccountRealm, ExampleUtilities.payAccountNum);
		
		// setup paying keypair
		if (keyType ==  null) {
			keyType = "SINGLE";
		}
//		if (keyType.equals("LIST")) {
//			// create a new key list
//			HederaKeyPair payingKeyPair = new HederaKeyPair(KeyType.ED25519, ExampleUtilities.pubKey, ExampleUtilities.privKey);
//			HederaKeyList keyList = new HederaKeyList();
//			keyList.addKey(payingKeyPair);
//			txQueryDefaults.payingKeyPair = new HederaKeyPair(keyList);
//		} else {
			txQueryDefaults.payingKeyPair = new HederaKeyPair(KeyType.ED25519, ExampleUtilities.pubKey, ExampleUtilities.privKey);
//		}
		
		txQueryDefaults.memo = "Demo memo";
		txQueryDefaults.node = node;
		txQueryDefaults.payingAccountID = payingAccountID;
		txQueryDefaults.transactionValidDuration = new HederaDuration(120);
		
		return txQueryDefaults;
	}
	
	public static void showResult(String result) {
		String stars = "***********************************************************************************************";
		String log = String.format("%s\n%s\n%s\n%s\n%s", "", stars, result, stars, "");
		logger.info(log);
	}

	public static byte[] copyBytes(int start, int length, byte[] bytes) {
		byte[] rv = new byte[length];
		for (int i = 0; i < length; i++) {
			rv[i] = bytes[start + i];
		}
		return rv;
	}

	public static void checkBinFile(byte[] fileContents) {
		// look for unwanted characters in the file
		final String error = "Invalid character in bin file at position ";
		for (int i=0; i < fileContents.length; i++) {
			boolean bOk = false;
			if ((fileContents[i] >= 48) && (fileContents[i] <= 57)) { // 0-9
				bOk = true;
			} else if ((fileContents[i] >= 65) && (fileContents[i] <= 70)) { // A-F
				bOk = true;
			} else if ((fileContents[i] >= 97) && (fileContents[i] <= 102)) { // a-f
				bOk = true;
			}
			if (!bOk) {
				System.out.println(error + i + " - found ASCII code " + fileContents[i]);
				throw new IllegalArgumentException(error + i);
			}
		}
	}
	public static byte[] readFile(String filePath) throws IOException, URISyntaxException {

		byte[] fileContents = new byte[0];
		File file = new File(filePath);
		Path path = file.toPath();
		fileContents = Files.readAllBytes(path);
		
		return fileContents;
	}
	
}

package com.hedera.examples.simple;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;
import org.ethereum.core.CallTransaction;

public final class DemoContractToken {

	public static void main(String... arguments) throws Exception {

		byte[] fileContents = ExampleUtilities.readFile("/scExamples/token.bin");
		// look for unwanted characters in the file
		ExampleUtilities.checkBinFile(fileContents);

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

		// my account
		HederaAccount myAccount = new HederaAccount();
		// setup transaction/query defaults (durations, etc...)
		myAccount.txQueryDefaults = txQueryDefaults;
		myAccount.accountNum = myAccount.txQueryDefaults.payingAccountID.accountNum;
		txQueryDefaults.fileWacl = myAccount.txQueryDefaults.payingKeyPair;

//		 // init token owners
//		 Map<String, String> tokenOwners = new HashMap<String, String>();
//		
//		 // token issuer
//		 HederaKeyPair tokenIssureKeyPair = new HederaKeyPair(KeyType.ED25519);
//		 HederaAccount tokenIssuer = new HederaAccount();
//		 tokenIssuer.txQueryDefaults = txQueryDefaults;
//		 tokenIssuer = AccountCreate.create(tokenIssuer,
//		 tokenIssureKeyPair.getPublicKeyHex(), KeyType.ED25519, 1000000000000L);
//		 System.out.println("Token Isssuer account created");
//		 tokenIssuer.getInfo();
//		 String tokenIssuerEthAddress = tokenIssuer.getSolidityContractAccountID();
//		
//		 // alice
//		 HederaKeyPair aliceKeyPair = new HederaKeyPair(KeyType.ED25519);
//		 HederaAccount alice = new HederaAccount();
//		 alice.txQueryDefaults = txQueryDefaults;
//		 alice = AccountCreate.create(alice, aliceKeyPair.getPublicKeyHex(),
//		 KeyType.ED25519, 10000000000L);
//		 System.out.println("Alice account created");
//		 alice.getInfo();
//		 String aliceEthAddress = alice.getSolidityContractAccountID();
//		
//		 // bob
//		 HederaKeyPair bobKeyPair = new HederaKeyPair(KeyType.ED25519);
//		 HederaAccount bob = new HederaAccount();
//		 bob.txQueryDefaults = txQueryDefaults;
//		 bob = AccountCreate.create(bob, bobKeyPair.getPublicKeyHex(),
//		 KeyType.ED25519, 10000000000L);
//		 System.out.println("Alice account created");
//		 bob.getInfo();
//		 String bobEthAddress = bob.getSolidityContractAccountID();
//		
//		 tokenOwners.put("Issuer", tokenIssuerEthAddress);
//		 tokenOwners.put("Alice", aliceEthAddress);
//		 tokenOwners.put("Bob", bobEthAddress);

		// create a file
		// new file object
		HederaFile file = new HederaFile();
		// setup transaction/query defaults (durations, etc...)
		file.txQueryDefaults = txQueryDefaults;

		// create a file with contents
		file = FileCreate.create(file, fileContents);

		 final String TOKEN_ERC20_CONSTRUCTOR_ABI =
		 "{\"inputs\":[{\"name\":\"initialSupply\",\"type\":\"uint256\"},{\"name\":\"tokenName\",\"type\":\"string\"},{\"name\":\"tokenSymbol\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}";
		 String funcJson = TOKEN_ERC20_CONSTRUCTOR_ABI.replaceAll("'", "\"");
		 CallTransaction.Function function =
		 CallTransaction.Function.fromJsonInterface(funcJson);
		
		 byte[] constructorData = function.encodeArguments(1000_000L, "Test Token", "OCT");

		// new contract object
		HederaContract createdContract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		createdContract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 1250000;
		 createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0, constructorData);
//		createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0);

	}
}
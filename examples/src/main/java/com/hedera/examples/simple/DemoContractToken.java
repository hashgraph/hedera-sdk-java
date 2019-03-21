package com.hedera.examples.simple;

import com.hedera.examples.accountWrappers.AccountCreate;
import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractToken {
	static ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
	
	public static void main(String... arguments) throws Exception {

		// set the wrapper's abi from a file. A string with the ABI 
		// could also be supplied to the wrapper.setABI(String) method
		wrapper.setABIFromFile("./src/main/resources/scExamples/token.abi");
		
		byte[] fileContents = ExampleUtilities.readFile("./src/main/resources/scExamples/token.bin");

		// check the bin file only contains 0-9,A-F,a-f
		ExampleUtilities.checkBinFile(fileContents);

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

		// my account
		HederaAccount myAccount = new HederaAccount();
		// setup transaction/query defaults (durations, etc...)

		// my account is the token issuer
		myAccount.txQueryDefaults = txQueryDefaults;
		txQueryDefaults.fileWacl = txQueryDefaults.payingKeyPair;
		
		// setup the account number on my account so we can getInfo
		myAccount.accountNum = txQueryDefaults.payingAccountID.accountNum;
		myAccount.getInfo();
		String tokenIssuerSolidityAddress = myAccount.getSolidityContractAccountID();
		
		// alice
		HederaKeyPair aliceKeyPair = new HederaKeyPair(KeyType.ED25519);
		HederaAccount alice = new HederaAccount();
		alice.txQueryDefaults = txQueryDefaults;
		alice = AccountCreate.create(alice, aliceKeyPair.getPublicKeyHex(), KeyType.ED25519, 400000);
		ExampleUtilities.showResult("Alice account created");
		alice.getInfo();
		String aliceSolidityAddress = alice.getSolidityContractAccountID();

		// bob
		HederaKeyPair bobKeyPair = new HederaKeyPair(KeyType.ED25519);
		HederaAccount bob = new HederaAccount();
		bob.txQueryDefaults = txQueryDefaults;
		bob = AccountCreate.create(bob, bobKeyPair.getPublicKeyHex(), KeyType.ED25519, 400000);
		ExampleUtilities.showResult("Bob account created");
		bob.getInfo();
		String bobSolidityAddress = bob.getSolidityContractAccountID();

		// create a file
		// new file object
		HederaFile file = new HederaFile();
		// setup transaction/query defaults (durations, etc...)
		file.txQueryDefaults = txQueryDefaults;

		// create a file with contents
		file = FileCreate.create(file, fileContents);

		long initialSupply = 1000000L; //1000_000L;
		byte[] constructorData = wrapper.constructor(initialSupply, "Test Token", "OCT");
		
		// new contract object
		HederaContract createdContract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		createdContract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 1427770;
		createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0, constructorData);

		if (createdContract != null) {
			createdContract.getInfo();
			ExampleUtilities.showResult("@@@ Contract ID is  " + createdContract.contractNum);

			long tokenDecimals = getDecimals(createdContract);
			ExampleUtilities.showResult("decimals = " + tokenDecimals);

			String symbol = getSymbol(createdContract);

			ExampleUtilities.showResult("symbol = " + symbol);

			long tokenMultiplier = (long) Math.pow(10, tokenDecimals);

			long balanceOfTokenIssuer = getBalance(createdContract, tokenIssuerSolidityAddress);

			ExampleUtilities.showResult("@@@ Balance of token issuer  " + balanceOfTokenIssuer / tokenMultiplier + " " + symbol
					+ "  decimals = " + tokenDecimals);

			ExampleUtilities.showResult("token owner transfers 1000 tokens to Alice ");
			transfer(createdContract, aliceSolidityAddress, 1000 * tokenMultiplier);

			ExampleUtilities.showResult("token owner transfers 2000 tokens to Bob");
			transfer(createdContract, bobSolidityAddress, 2000 * tokenMultiplier);

			balanceOfTokenIssuer = getBalance(createdContract, tokenIssuerSolidityAddress);
			ExampleUtilities.showResult("Token Issuer Balance = " + balanceOfTokenIssuer);

			long balanceOfAlice = getBalance(createdContract, aliceSolidityAddress);
			ExampleUtilities.showResult("Alice Balance = " + balanceOfAlice);

			long balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			ExampleUtilities.showResult("Bob Balance = " + balanceOfBob);

			HederaKeyPair carolKeyPair = new HederaKeyPair(KeyType.ED25519);
			HederaAccount carol = new HederaAccount();
			carol.txQueryDefaults = txQueryDefaults;
			carol = AccountCreate.create(carol, carolKeyPair.getPublicKeyHex(), KeyType.ED25519, 300000);
			ExampleUtilities.showResult("Carol account created");
			carol.getInfo();
			String carolSolidityAddress = carol.getSolidityContractAccountID();

			ExampleUtilities.showResult("Bob transfers 500 tokens to Carol");
			// bob MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = bob.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = bobKeyPair;
			transfer(createdContract, carolSolidityAddress, 500 * tokenMultiplier);

			// reset transaction originator to myAccount
			createdContract.txQueryDefaults.payingAccountID = myAccount.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = ExampleUtilities.getTxQueryDefaults().payingKeyPair;
			balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			ExampleUtilities.showResult("Bob Balance = " + balanceOfBob);
			long balanceOfCarol = getBalance(createdContract, carolSolidityAddress);
			ExampleUtilities.showResult("Carol Balance = " + balanceOfCarol);

			// Create new account Dave
			HederaKeyPair daveKeyPair = new HederaKeyPair(KeyType.ED25519);
			HederaAccount dave = new HederaAccount();
			dave.txQueryDefaults = txQueryDefaults;
			// We switched to BOB being the payer, need to switch back to our own account here
			dave.txQueryDefaults  = ExampleUtilities.getTxQueryDefaults();
			
			dave = AccountCreate.create(dave, daveKeyPair.getPublicKeyHex(), KeyType.ED25519, 400000);
			ExampleUtilities.showResult("Dave account created");
			dave.getInfo();
			String daveSolidityAddress = dave.getSolidityContractAccountID();

			ExampleUtilities.showResult("Alice Allows to Dave to spend up to 200 Alice's tokens");
			// Alice MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = alice.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = aliceKeyPair;
			approve(createdContract, daveSolidityAddress, 200 * tokenMultiplier);

			ExampleUtilities.showResult("Dave transfers 100 token from Alice account into Bob's");
			// Dave MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = dave.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = daveKeyPair;

			transferFrom(createdContract, aliceSolidityAddress, bobSolidityAddress, 100 * tokenMultiplier);

			// We switched to DAVE being the payer, need to switch back to our own account here
			createdContract.txQueryDefaults.payingAccountID = myAccount.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = ExampleUtilities.getTxQueryDefaults().payingKeyPair;
			balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			ExampleUtilities.showResult("Bob Balance = " + balanceOfBob);
			balanceOfAlice = getBalance(createdContract, aliceSolidityAddress);
			ExampleUtilities.showResult("Alice Balance = " + balanceOfAlice);

		}
	}

	private static long getBalance(HederaContract contract, String solAddress) throws Exception {
		long balance = wrapper.callLocalLong(contract, 250000L, 5000, "balanceOf", solAddress);
		return balance;
	}

	private static void transfer(HederaContract contract, String toAddress, long amount) throws Exception {
		wrapper.call(contract, 250000, 0, "transfer", toAddress, amount);
	}

	private static long getDecimals(HederaContract contract) throws Exception {
		long tokenDecimals = wrapper.callLocalLong(contract, 250000L, 5000, "decimals");
		return tokenDecimals;
	}

	private static String getSymbol(HederaContract contract) throws Exception {
		String symbol = wrapper.callLocalString(contract, 250000L, 5000, "symbol");
		return symbol;
	}

	private static void approve(HederaContract contract, String spenderAddress, long valueToApprove) throws Exception {
		wrapper.call(contract, 250000, 0, "approve", spenderAddress, valueToApprove);
	}

	private static void transferFrom(HederaContract contract, String fromAccountAddress, String toAccountAddress,
			long valueToTransfer) throws Exception {
		wrapper.call(contract, 250000, 0, "transferFrom", fromAccountAddress, toAccountAddress, valueToTransfer);
	}
}
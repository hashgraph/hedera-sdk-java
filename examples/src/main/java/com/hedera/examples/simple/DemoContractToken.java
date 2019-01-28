package com.hedera.examples.simple;

import com.hedera.examples.accountWrappers.AccountCreate;
import com.hedera.examples.contractWrappers.ContractCall;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.contractWrappers.ContractRunLocal;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaKeyPair;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.file.HederaFile;
import com.hedera.sdk.transaction.HederaTransactionResult;
import java.math.BigInteger;
import org.ethereum.core.CallTransaction;

public final class DemoContractToken {

	public static void main(String... arguments) throws Exception {

		byte[] fileContents = ExampleUtilities.readFile("/scExamples/token.bin");

		// check the bin file only contains 0-9,A-F,a-f
		ExampleUtilities.checkBinFile(fileContents);

		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

		// my account
		HederaAccount myAccount = new HederaAccount();
		// setup transaction/query defaults (durations, etc...)
		myAccount.txQueryDefaults = txQueryDefaults;
		
		txQueryDefaults.fileWacl = txQueryDefaults.payingKeyPair;
		
		// token issuer
		HederaKeyPair tokenIssureKeyPair = new HederaKeyPair(KeyType.ED25519);
		HederaAccount tokenIssuer = new HederaAccount();
		tokenIssuer.txQueryDefaults = txQueryDefaults;
		tokenIssuer = AccountCreate.create(tokenIssuer, tokenIssureKeyPair.getPublicKeyHex(), KeyType.ED25519,
				1000000000000L);
		System.out.println("Token Isssuer account created");
		tokenIssuer.getInfo();
		String tokenIssuerSolidityAddress = tokenIssuer.getSolidityContractAccountID();

		// alice
		HederaKeyPair aliceKeyPair = new HederaKeyPair(KeyType.ED25519);
		HederaAccount alice = new HederaAccount();
		alice.txQueryDefaults = txQueryDefaults;
		alice = AccountCreate.create(alice, aliceKeyPair.getPublicKeyHex(), KeyType.ED25519, 10000000000L);
		System.out.println("Alice account created");
		alice.getInfo();
		String aliceSolidityAddress = alice.getSolidityContractAccountID();

		// bob
		HederaKeyPair bobKeyPair = new HederaKeyPair(KeyType.ED25519);
		HederaAccount bob = new HederaAccount();
		bob.txQueryDefaults = txQueryDefaults;
		bob = AccountCreate.create(bob, bobKeyPair.getPublicKeyHex(), KeyType.ED25519, 10000000000L);
		System.out.println("Alice account created");
		bob.getInfo();
		String bobSolidityAddress = bob.getSolidityContractAccountID();

		// create a file
		// new file object
		HederaFile file = new HederaFile();
		// setup transaction/query defaults (durations, etc...)
		file.txQueryDefaults = txQueryDefaults;

		// create a file with contents
		file = FileCreate.create(file, fileContents);

		final String TOKEN_ERC20_CONSTRUCTOR_ABI = "{\"inputs\":[{\"name\":\"initialSupply\",\"type\":\"uint256\"},{\"name\":\"tokenName\",\"type\":\"string\"},{\"name\":\"tokenSymbol\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}";
		String funcJson = TOKEN_ERC20_CONSTRUCTOR_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);

		long initialSupply = 1000000L; //1000_000L;
		byte[] constructorData = function.encodeArguments(initialSupply, "Test Token", "OCT");

		// new contract object
		HederaContract createdContract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		createdContract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 750000;
		createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0, constructorData);

		if (createdContract != null) {
			createdContract.getInfo();
			System.out.println("@@@ Contract ID is  " + createdContract.contractNum);

			long tokenDecimals = getDecimals(createdContract);
			System.out.println("decimals = " + tokenDecimals);

			String symbol = getSymbol(createdContract);

			long tokenMultiplier = (long) Math.pow(10, tokenDecimals);

			long balanceOfTokenIssuer = getBalance(createdContract, tokenIssuerSolidityAddress);

			System.out.println("@@@ Balance of token issuer  " + balanceOfTokenIssuer / tokenMultiplier + " " + symbol
					+ "  decimals = " + tokenDecimals);

			System.out.println("token owner transfers 1000 tokens to Alice ");
			transfer(createdContract, aliceSolidityAddress, 1000 * tokenMultiplier);

			System.out.println("token owner transfers 2000 tokens to Bob");
			transfer(createdContract, bobSolidityAddress, 2000 * tokenMultiplier);

			balanceOfTokenIssuer = getBalance(createdContract, tokenIssuerSolidityAddress);
			System.out.println("Token Issuer Balance = " + balanceOfTokenIssuer);

			long balanceOfAlice = getBalance(createdContract, aliceSolidityAddress);
			System.out.println("Alice Balance = " + balanceOfAlice);

			long balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			System.out.println("Bob Balance = " + balanceOfBob);

			HederaKeyPair carolKeyPair = new HederaKeyPair(KeyType.ED25519);
			HederaAccount carol = new HederaAccount();
			carol.txQueryDefaults = txQueryDefaults;
			carol = AccountCreate.create(carol, carolKeyPair.getPublicKeyHex(), KeyType.ED25519, 100000000000L);
			System.out.println("Carol account created");
			carol.getInfo();
			String carolSolidityAddress = carol.getSolidityContractAccountID();

			System.out.println("Bob transfers 500 tokens to Carol");
			// bob MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = bob.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = bobKeyPair;
			transfer(createdContract, carolSolidityAddress, 500 * tokenMultiplier);

			balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			System.out.println("Bob Balance = " + balanceOfBob);
			long balanceOfCarol = getBalance(createdContract, carolSolidityAddress);
			System.out.println("Carol Balance = " + balanceOfCarol);

			// Create new account Dave
			HederaKeyPair daveKeyPair = new HederaKeyPair(KeyType.ED25519);
			HederaAccount dave = new HederaAccount();
			dave.txQueryDefaults = txQueryDefaults;
			// We switched to BOB being the payer, need to switch back to our own account here
			dave.txQueryDefaults  = ExampleUtilities.getTxQueryDefaults();
			
			dave = AccountCreate.create(dave, daveKeyPair.getPublicKeyHex(), KeyType.ED25519, 100000000000L);
			System.out.println("Dave account created");
			dave.getInfo();
			String daveSolidityAddress = dave.getSolidityContractAccountID();

			System.out.println("Alice Allows to Dave to spend up to 200 Alice's tokens");
			// Alice MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = alice.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = aliceKeyPair;
			approve(createdContract, daveSolidityAddress, 200 * tokenMultiplier);

			System.out.println("Dave transfers 100 token from Alice account into Bob's");
			// Dave MUST be the originator of the transaction
			createdContract.txQueryDefaults.payingAccountID = dave.getHederaAccountID();
			createdContract.txQueryDefaults.payingKeyPair = daveKeyPair;

			transferFrom(createdContract, aliceSolidityAddress, bobSolidityAddress, 100 * tokenMultiplier);
			balanceOfBob = getBalance(createdContract, bobSolidityAddress);
			System.out.println("Bob Balance = " + balanceOfBob);
			balanceOfAlice = getBalance(createdContract, aliceSolidityAddress);
			System.out.println("Alice Balance = " + balanceOfAlice);

		}
	}

	private static long getBalance(HederaContract contract, String solAddress) throws Exception {
		long balance = 0;
		final String BALANCE_OF_ABI = "{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
		String funcJson = BALANCE_OF_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		byte[] encodedFunc = function.encode(solAddress);

		HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, 250000L, 5000, encodedFunc);

		Object[] retResults = function.decodeResult(functionResult.contractCallResult());
		if (retResults != null && retResults.length > 0) {
			BigInteger retBi = (BigInteger) retResults[0];
			balance = retBi.longValue();
		}
		return balance;
	}

	private static void transfer(HederaContract contract, String toAddress, long amount) throws Exception {
		final String TRANSFER_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transfer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
		String funcJson = TRANSFER_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);

		byte[] encodedFunc = function.encode(toAddress, amount);
		HederaTransactionResult result = ContractCall.call(contract, 250000, 0, encodedFunc);
	}

	private static long getDecimals(HederaContract contract) throws Exception {
		final String DECIMALS_ABI = "{\"constant\":true,\"inputs\":[],\"name\":\"decimals\",\"outputs\":[{\"name\":\"\",\"type\":\"uint8\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
		String funcJson = DECIMALS_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		byte[] encodedFunc = function.encode();

		// decode value from results
		HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, 250000L, 5000, encodedFunc);

		long tokenDecimals = 0;
		Object[] retResults = function.decodeResult(functionResult.contractCallResult());
		if (retResults != null && retResults.length > 0) {
			BigInteger retBi = (BigInteger) retResults[0];
			tokenDecimals = retBi.longValue();
		}
		return tokenDecimals;
	}

	private static String getSymbol(HederaContract contract) throws Exception {
		final String SYMBOL_ABI = "{\"constant\":true,\"inputs\":[],\"name\":\"symbol\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
		String funcJson = SYMBOL_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		byte[] encodedFunc = function.encode();

		HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, 250000L, 5000, encodedFunc);

		String symbol = "";
		// decode value from results
		Object[] retResults = function.decodeResult(functionResult.contractCallResult());
		if (retResults != null && retResults.length > 0) {
			symbol = (String) retResults[0];
		}
		return symbol;
	}

	private static void approve(HederaContract contract, String spenderAddress, long valueToApprove) throws Exception {
		final String APPROVE_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"_spender\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"approve\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
		String funcJson = APPROVE_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		byte[] encodedFunc = function.encode(spenderAddress, valueToApprove);
		HederaTransactionResult result = ContractCall.call(contract, 250000, 0, encodedFunc);
	}

	private static void transferFrom(HederaContract contract, String fromAccountAddress, String toAccountAddress,
			long valueToTransfer) throws Exception {
		final String TRANSFER_FROM_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"_from\",\"type\":\"address\"},{\"name\":\"_to\",\"type\":\"address\"},{\"name\":\"_value\",\"type\":\"uint256\"}],\"name\":\"transferFrom\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
		String funcJson = TRANSFER_FROM_ABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		byte[] encodedFunc = function.encode(fromAccountAddress, toAccountAddress, valueToTransfer);

		HederaTransactionResult result = ContractCall.call(contract, 250000, 0, encodedFunc);
	}
}
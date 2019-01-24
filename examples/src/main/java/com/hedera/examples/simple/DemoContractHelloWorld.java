package com.hedera.examples.simple;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractHelloWorld {

	public static void main(String... arguments) throws Exception {

		byte[] fileContents = ExampleUtilities.readFile("/scExamples/HelloWorld.bin");

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

		// create a file
		// new file object
		HederaFile file = new HederaFile();
		// setup transaction/query defaults (durations, etc...)
		file.txQueryDefaults = txQueryDefaults;

		// create a file with contents
		file = FileCreate.create(file, fileContents);

		// new contract object
		HederaContract createdContract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		createdContract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 1250000;
		createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0);

	}
}
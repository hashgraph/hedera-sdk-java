package com.hedera.examples.simple;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractGetRecords;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractSimpleStorage {

	public static void main(String... arguments) throws Exception {
		
		byte[] fileContents = ExampleUtilities.readFile("./src/main/resources/scExamples/simpleStorage.bin");
		
		// check the bin file only contains 0-9,A-F,a-f
		ExampleUtilities.checkBinFile(fileContents);
		
		// setup a set of defaults for query and transactions
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();

		txQueryDefaults.fileWacl = txQueryDefaults.payingKeyPair;

		// create a file
		// new file object
		HederaFile file = new HederaFile();
		// setup transaction/query defaults (durations, etc...)
		file.txQueryDefaults = txQueryDefaults;

		// create a file with contents
		file = FileCreate.create(file, fileContents);

		// new contract object
		HederaContract contract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		contract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 176103;
		contract = ContractCreate.create(contract, file.getFileID(), gas, 0);
		if (contract != null) {

			ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
			// set the wrapper's abi from a file. A string with the ABI 
			// could also be supplied to the wrapper.setABI(String) method
			wrapper.setABIFromFile("./src/main/resources/scExamples/simpleStorage.abi");
			
			int decodeResult = wrapper.callLocalInt(contract, 25000, 5000, "get");
    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
			
			wrapper.call(contract, 48000, 0, "set", 10);

			decodeResult = wrapper.callLocalInt(contract, 25000, 5000, "get");
    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));

    		// get records for the contract if any
    		ContractGetRecords.getRecords(contract);
		}
	}
}
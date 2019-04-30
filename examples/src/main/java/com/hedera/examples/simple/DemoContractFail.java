package com.hedera.examples.simple;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractGetRecords;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class DemoContractFail {

	public static void main(String... arguments) throws Exception {
		
		byte[] fileContents = ExampleUtilities.readFile("./src/main/resources/scExamples/failContract.bin");
		
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
		long payableAmount = 0;
		contract = ContractCreate.create(contract, file.getFileID(), gas, 0);
		if (contract != null) {

			ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
			// set the wrapper's abi from a file. A string with the ABI 
			// could also be supplied to the wrapper.setABI(String) method
			wrapper.setABIFromFile("./src/main/resources/scExamples/failContract.abi");
			
			gas = 22000;
			HederaTransactionRecord record = wrapper.callForRecord(contract, gas, payableAmount, "fail");
			// this contract will have failed (reverted), let's get the error from it
			if (record.transactionReceipt.transactionStatus == ResponseCodeEnum.CONTRACT_REVERT_EXECUTED) {
				System.out.println(wrapper.outputData(record.contractCallResult));
			}
			
		}
	}
}
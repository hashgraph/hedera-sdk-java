package com.hedera.examples.simple;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractFail {

	public static void main(String... arguments) throws Exception {
		
		byte[] fileContents = ExampleUtilities.readFile("/scExamples/failing.bin");
		
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
		long gas = 119300;
		contract = ContractCreate.create(contract, file.getFileID(), gas, 0);
		if (contract != null) {
			// update the contract
			HederaTimeStamp expirationTime = new HederaTimeStamp(100, 10);
			HederaDuration autoRenewDuration = new HederaDuration(10, 20);

			if (contract != null) {

				String fullABI = "[\n" + 
						"	{\n" + 
						"		\"constant\": true,\n" + 
						"		\"inputs\": [],\n" + 
						"		\"name\": \"buy\",\n" + 
						"		\"outputs\": [],\n" + 
						"		\"payable\": false,\n" + 
						"		\"stateMutability\": \"pure\",\n" + 
						"		\"type\": \"function\"\n" + 
						"	}\n" + 
						"]";
				
				ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
				wrapper.setABI(fullABI);

				gas = 407000;
				HederaContractFunctionResult result = wrapper.call(contract, gas, 0, "buy");
				if (result.errorMessage().isEmpty()) {
					// get the results
				} else {
					// output the error
					System.out.println("Error=" + result.errorMessage());
					System.out.println("Address=" + wrapper.addressFromOutput(result));
					System.out.println("Data=" + new String(wrapper.outputData(result)));
				}
			}
		}
	}
}
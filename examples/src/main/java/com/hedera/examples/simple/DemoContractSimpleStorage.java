package com.hedera.examples.simple;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractSimpleStorage {

	public static void main(String... arguments) throws Exception {
		
		byte[] fileContents = ExampleUtilities.readFile("/scExamples/simpleStorage.bin");
		
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
		long gas = 99300;
		contract = ContractCreate.create(contract, file.getFileID(), gas, 0);
		if (contract != null) {
			// update the contract
			HederaTimeStamp expirationTime = new HederaTimeStamp(100, 10);
			HederaDuration autoRenewDuration = new HederaDuration(10, 20);

			if (contract != null) {

				String fullABI = "[\n" + 
						"	{\n" + 
						"		\"constant\": false,\n" + 
						"		\"inputs\": [\n" + 
						"			{\n" + 
						"				\"name\": \"x\",\n" + 
						"				\"type\": \"uint256\"\n" + 
						"			}\n" + 
						"		],\n" + 
						"		\"name\": \"set\",\n" + 
						"		\"outputs\": [],\n" + 
						"		\"payable\": false,\n" + 
						"		\"stateMutability\": \"nonpayable\",\n" + 
						"		\"type\": \"function\"\n" + 
						"	},\n" + 
						"	{\n" + 
						"		\"constant\": true,\n" + 
						"		\"inputs\": [],\n" + 
						"		\"name\": \"get\",\n" + 
						"		\"outputs\": [\n" + 
						"			{\n" + 
						"				\"name\": \"\",\n" + 
						"				\"type\": \"uint256\"\n" + 
						"			}\n" + 
						"		],\n" + 
						"		\"payable\": false,\n" + 
						"		\"stateMutability\": \"view\",\n" + 
						"		\"type\": \"function\"\n" + 
						"	}\n" + 
						"]";
				
				ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
				wrapper.setABI(fullABI);

				gas = 22000;
				long maxResultSize = 5000;
				
				int decodeResult = wrapper.callLocalInt(contract, gas, maxResultSize, "get");
	    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
				
				gas = 27000;
				long amount = 0;
				wrapper.call(contract, gas, amount, "set", 10);

				gas = 22000;
				decodeResult = wrapper.callLocalInt(contract, gas, maxResultSize, "get");
	    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
			}
		}
	}
}
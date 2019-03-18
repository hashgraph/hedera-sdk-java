package com.hedera.examples.simple;
import java.math.BigInteger;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractHelloWorld {

	public static void main(String... arguments) throws Exception {

		byte[] fileContents = ExampleUtilities.readFile("/scExamples/HelloWorld.bin");

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
		HederaContract createdContract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		createdContract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 117000;
		createdContract = ContractCreate.create(createdContract, file.getFileID(), gas, 0);

		String fullABI = "[\n" + 
				"	{\n" + 
				"		\"constant\": true,\n" + 
				"		\"inputs\": [],\n" + 
				"		\"name\": \"getInt\",\n" + 
				"		\"outputs\": [\n" + 
				"			{\n" + 
				"				\"name\": \"\",\n" + 
				"				\"type\": \"uint256\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\"payable\": false,\n" + 
				"		\"stateMutability\": \"pure\",\n" + 
				"		\"type\": \"function\"\n" + 
				"	},\n" + 
				"	{\n" + 
				"		\"constant\": true,\n" + 
				"		\"inputs\": [],\n" + 
				"		\"name\": \"getString\",\n" + 
				"		\"outputs\": [\n" + 
				"			{\n" + 
				"				\"name\": \"\",\n" + 
				"				\"type\": \"string\"\n" + 
				"			}\n" + 
				"		],\n" + 
				"		\"payable\": false,\n" + 
				"		\"stateMutability\": \"pure\",\n" + 
				"		\"type\": \"function\"\n" + 
				"	}\n" + 
				"]";
		
		ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
		wrapper.setABI(fullABI);
		
		long localGas = 22000;
		long maxResultSize = 5000;
		
		BigInteger decodeResult = wrapper.callLocalBigInt(createdContract, localGas, maxResultSize, "getInt");
		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %s", decodeResult.toString()));

		String decodeResult2 = wrapper.callLocalString(createdContract, localGas, maxResultSize, "getString");
		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %s", decodeResult2));
	}
}
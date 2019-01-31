package com.hedera.examples.simple;
import com.hedera.examples.contractWrappers.ContractCall;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.contractWrappers.ContractRunLocal;
import com.hedera.examples.contractWrappers.SoliditySupport;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
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

		final String SC_GETINT_ABI = "{\"constant\": true,\"inputs\": [],\"name\": \"getInt\",\"outputs\": [{\"name\": \"\",\"type\": \"uint256\"}],\"payable\": false,\"stateMutability\": \"pure\",\"type\": \"function\"}";
		byte[] function = SoliditySupport.encodeGetValue(SC_GETINT_ABI);
		long localGas = 22000;
		long maxResultSize = 5000;
		HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(createdContract, localGas, maxResultSize, function);
		int decodeResult = SoliditySupport.decodeGetValueResultInt(functionResult.contractCallResult(),SC_GETINT_ABI);
		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
	
		final String SC_GETSTRING_ABI = "{\"constant\": true,\"inputs\": [],\"name\": \"getString\",\"outputs\": [{	\"name\": \"\",\"type\": \"string\"	}],\"payable\": false,\"stateMutability\": \"pure\",\"type\": \"function\"}";
		function = SoliditySupport.encodeGetValue(SC_GETSTRING_ABI);
		localGas = 22000;
		maxResultSize = 5000;
		functionResult = ContractRunLocal.runLocal(createdContract, localGas, maxResultSize, function);
		String result = SoliditySupport.decodeGetValueResultString(functionResult.contractCallResult(),SC_GETSTRING_ABI);
		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %s", result));
	}
}
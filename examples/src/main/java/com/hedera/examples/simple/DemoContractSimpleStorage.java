package com.hedera.examples.simple;
import com.hedera.examples.contractWrappers.ContractCall;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.contractWrappers.ContractGetBytecode;
import com.hedera.examples.contractWrappers.ContractGetInfo;
import com.hedera.examples.contractWrappers.ContractRunLocal;
import com.hedera.examples.contractWrappers.ContractUpdate;
import com.hedera.examples.contractWrappers.SoliditySupport;
import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractSimpleStorage {

	public static void main(String... arguments) throws Exception {
		boolean getInfo = false;
		boolean update = false;
		boolean getByteCode = false;
		boolean setCall = false;
		boolean getBeforeSetCall = false;
		boolean getAfterSetCall = false;
		
		// set flags to enable/disable demo features
		getInfo = false;
		update = false;
		getByteCode = false;
		setCall = true;
		getBeforeSetCall = true;
		getAfterSetCall = true;
		
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
				if (update) {
					contract = ContractUpdate.update(contract, expirationTime, autoRenewDuration);
				}
				
				// getinfo
				if (getInfo) {
					ContractGetInfo.getInfo(contract);
				}
				// get bytecode
				if (getByteCode) {
					ContractGetBytecode.getByteCode(contract);
				}

				// call local before set
				if (getBeforeSetCall) {
					String SC_GET_ABI = "{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
					
					byte[] function = SoliditySupport.encodeGetValue(SC_GET_ABI);
					long localGas = 22000;
					long maxResultSize = 5000;
					HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, localGas, maxResultSize, function);
					int decodeResult = SoliditySupport.decodeGetValueResultInt(functionResult.contractCallResult(),SC_GET_ABI);
		    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
				}

				// call to set value
				if (setCall) {
					final String SC_SET_ABI = "{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}";
					gas = 27000;
					long amount = 0;
					byte[] functionParameters = SoliditySupport.encodeSet(10,SC_SET_ABI);
					
					ContractCall.call(contract, gas, amount, functionParameters);
					HederaTransactionRecord record = new HederaTransactionRecord(contract.hederaTransactionID, contract.txQueryDefaults.node.contractGetRecordsQueryFee, contract.txQueryDefaults);
				}
				
				// call local after set
				if (getAfterSetCall) {
					String SC_GET_ABI = "{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}";
					
					byte[] function = SoliditySupport.encodeGetValue(SC_GET_ABI);
					long localGas = 22000;
					long maxResultSize = 5000;
					HederaContractFunctionResult functionResult = ContractRunLocal.runLocal(contract, localGas, maxResultSize, function);
					int decodeResult = SoliditySupport.decodeGetValueResultInt(functionResult.contractCallResult(),SC_GET_ABI);
		    		ExampleUtilities.showResult(String.format("===>Decoded functionResult= %d", decodeResult));
				}
			}
		}
	}
}
package com.hedera.examples.simple;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.hedera.examples.fileWrappers.FileCreate;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.examples.contractWrappers.ContractCall;
import com.hedera.examples.contractWrappers.ContractCreate;
import com.hedera.examples.contractWrappers.ContractGetBytecode;
import com.hedera.examples.contractWrappers.ContractGetInfo;
import com.hedera.examples.contractWrappers.ContractRunLocal;
import com.hedera.examples.contractWrappers.ContractUpdate;
import com.hedera.examples.contractWrappers.SoliditySupport;
import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaTimeStamp;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.file.HederaFile;

public final class DemoContractBen {

	public static void main(String... arguments) throws Exception {

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

		// get file contents
		InputStream is = DemoContractBen.class.getResourceAsStream("/main/resources/EncryptedDataSharing.bin");
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int nRead;
	    byte[] data = new byte[4096];
	    while ((nRead = is.read(data, 0, data.length)) != -1) {
	        buffer.write(data, 0, nRead);
	    }
		 
	    buffer.flush();
	    byte[] fileContents = buffer.toByteArray();
	    
		// create a file with contents
		file = FileCreate.create(file, fileContents);

		file.getInfo();
		byte[] contents = file.getContents();
		
				
		
		// new contract object
		HederaContract contract = new HederaContract();
		// setup transaction/query defaults (durations, etc...)
		contract.txQueryDefaults = txQueryDefaults;

		// create a contract
		long gas = 71000;
		contract = ContractCreate.create(contract, file.getFileID(), gas, 0);
		//contract = create(contract, file.getFileID(), 1);
		if (contract != null) {
			
			// getinfo
			ContractGetInfo.getInfo(contract);
			// get bytecode
			ContractGetBytecode.getByteCode(contract);
		}
	}
}
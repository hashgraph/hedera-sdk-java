package com.hedera.examples.complex;

import com.hedera.examples.contractWrappers.ContractFunctionsWrapper;
import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaContractID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKeyPair.KeyType;
import com.hedera.sdk.common.HederaSignature;
import com.hedera.sdk.common.HederaSignatureList;
import com.hedera.sdk.common.HederaSignatures;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.common.HederaTransactionID;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hedera.sdk.transaction.HederaTransactionBody;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hedera.sdk.transaction.HederaTransactionBody.TransactionType;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public class DemoContract {

	public static void main(String... arguments) throws Exception {

		// txQueryDefaults
		HederaTransactionAndQueryDefaults txQueryDefaults = new HederaTransactionAndQueryDefaults();
		txQueryDefaults = ExampleUtilities.getTxQueryDefaults();
		/**
		 * setup inputs
		 */
		// we are calling this from an account, using txQueryDefaults here
		HederaAccountID payingAccount = txQueryDefaults.payingAccountID;
		// we'll need a node account ID, get this from txQueryDefaults
		HederaAccountID nodeAccount = txQueryDefaults.node.getAccountID();
		// this example assumes a "SimpleStorage" contract already exists
		long contractNum = 1332;
		// setup a contract object
		HederaContract contractToCall = new HederaContract();
		contractToCall.setHederaContractID(new HederaContractID(contractNum));

		// generate the transaction body
		HederaTransactionBody txBody = callOnBehalfOfTransaction(contractToCall, payingAccount, nodeAccount);
		
		/**
		 * interaction with the outside world here
		 */
		// send to external party to sign

		// receive the signature as a byte array
		byte[] receivedSignature = new byte[0];
		
		/**
		 * append the signature to the transaction and send to node
		 */
		// create a signature object from the received signature
		byte[] transactionSignature = receivedSignature;
		// remove this later
		transactionSignature = txQueryDefaults.payingKeyPair.signMessage(txBody.getProtobuf().toByteArray());
		
		// and a signature list to add the signature to
		HederaSignatures sigsForTransaction = new HederaSignatures();
		//paying signature
		sigsForTransaction.addSignature(txQueryDefaults.payingKeyPair.getPublicKeyEncodedHex(), transactionSignature);

		// create the transaction to send to the node
		HederaTransaction transaction = new HederaTransaction();
		// set its body
		transaction.body = txBody;
		// add the signatures
		transaction.signatures = sigsForTransaction;
		
		// now we can send to the network and check response
		HederaTransactionResult hederaTransactionResult = txQueryDefaults.node.contractCall(transaction);
		hederaTransactionResult.hederaTransactionID = transaction.body.transactionId;

		if (hederaTransactionResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(transaction.body.transactionId,
					txQueryDefaults.node);
			
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// and print it out
				ExampleUtilities.showResult(String.format("**    Smart Contract call success"));
			} else {
				ExampleUtilities.showResult("**    Failed with transactionStatus:" + receipt.transactionStatus.toString());
				System.exit(0);
			}
		} else if (hederaTransactionResult.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			ExampleUtilities.showResult("system busy, try again later");
			System.exit(0);
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + hederaTransactionResult.getPrecheckResult());
			System.exit(0);
		}
	}
	
	private static HederaTransactionBody callOnBehalfOfTransaction(HederaContract contract, HederaAccountID onBehalfOfAccount, HederaAccountID nodeAccount) throws Exception {
		
		// need to seed the contract object with some parameters for the call
		// set the amount to 0
		contract.amount = 0;
		// build the function call parameters
		ContractFunctionsWrapper wrapper = new ContractFunctionsWrapper();
		wrapper.setABIFromFile("./src/main/resources/scExamples/simpleStorage.abi");
		// calling the set function with a value of 10
		contract.functionParameters = wrapper.functionCall("set", 10);
		// set the gas required
		contract.gas = 48000;
		
		// generate a hedera Transaction ID
		HederaTransactionID transactionID = new HederaTransactionID(onBehalfOfAccount);

		HederaTransactionBody transactionBody = new HederaTransactionBody(
				TransactionType.CONTRACTCALL
				, transactionID
				, nodeAccount
				, 100_000 // transaction fee
				, new HederaDuration(120) // transaction valid duration 2 minutes 
				, false // generate record
				, "Community test phase 2"
				, contract.getCallTransactionBody());
		
		// build the transaction
		return transactionBody;
	}
}

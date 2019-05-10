package com.hedera.examples.contractWrappers;

import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;

import org.ethereum.core.CallTransaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.hedera.examples.utilities.ExampleUtilities;
import com.hedera.sdk.common.HederaTransactionReceipt;
import com.hedera.sdk.common.HederaTransactionRecord;
import com.hedera.sdk.common.Utilities;
import com.hedera.sdk.contract.HederaContract;
import com.hedera.sdk.contract.HederaContractFunctionResult;
import com.hedera.sdk.node.HederaNodeList;
import com.hedera.sdk.transaction.HederaTransactionResult;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ContractFunctionsWrapper {
	
	private JSONArray abis;
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ContractFunctionsWrapper.class);
	
	public void setABI(String abi) throws ParseException {
		this.abis = (JSONArray) JSONValue.parseWithException(abi);
	}

	public void setABIFromFile(String fileLocation) throws Exception {
		File abiFile = new File(fileLocation);
		JSONParser parser = new JSONParser();
		Object abisObject = parser.parse(new FileReader(abiFile));
		this.abis = (JSONArray) abisObject;
	}
	
	private JSONObject findABI(String functionName) throws Exception {
		JSONObject abi = new JSONObject();
		boolean bFound = false;
		
		for (int i=0; i < this.abis.size(); i++) {
			abi = (JSONObject) this.abis.get(i);
			if (abi.get("name").equals(functionName) ) {
				bFound = true;
				break;
			}
		}
		if (!bFound) { 
			throw new Exception ("Function " + functionName + " not found in ABI.");
		}
		return abi;
	}

	private JSONObject findConstructor() throws Exception {
		JSONObject abi = new JSONObject();
		boolean bFound = false;
		
		for (int i=0; i < this.abis.size(); i++) {
			abi = (JSONObject) this.abis.get(i);
			if (abi.get("type").equals("constructor") ) {
				bFound = true;
				break;
			}
		}
		if (!bFound) { 
			throw new Exception ("Constructor not found in ABI.");
		}
		return abi;
	}
	
	public byte[] constructor(Object ... parameterValues) throws Exception {

		JSONObject abiJSON = findConstructor();
		String abi = abiJSON.toString();
		
		JSONArray inputs = (JSONArray) abiJSON.get("inputs");
		
		if (inputs.size() != parameterValues.length) {
			throw new Exception ("Numer of parameter values mismatch with abi inputs");
		}
		
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abi);
		
		byte[] constructor = new byte[0];
		if (parameterValues.length == 0) {
			constructor = function.encode();
		} else {
			constructor = function.encodeArguments(parameterValues);
		}

		return constructor;
	}
	
	public byte[] functionCall(String functionName, Object ... parameterValues) throws Exception {

		JSONObject abiJSON = findABI(functionName);
		String abi = abiJSON.toString();
		
		JSONArray inputs = (JSONArray) abiJSON.get("inputs");
		
		if (inputs.size() != parameterValues.length) {
			throw new Exception ("Numer of parameter values mismatch with abi inputs");
		}
		
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abi);
		
		byte[] encodedFunc = new byte[0];
		if (parameterValues.length == 0) {
			encodedFunc = function.encode();
		} else {
			encodedFunc = function.encode(parameterValues);
		}
		return encodedFunc;
	}

	public HederaContractFunctionResult callLocal(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		
		JSONObject abiJSON = findABI(functionName);
		String abi = abiJSON.toString();
		
		JSONArray inputs = (JSONArray) abiJSON.get("inputs");
		
		if (inputs.size() != parameterValues.length) {
			throw new Exception ("Numer of parameter values mismatch with abi inputs");
		}
		
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abi);
		
		byte[] encodedFunc = new byte[0];
		if (parameterValues.length == 0) {
			encodedFunc = function.encode();
		} else {
			encodedFunc = function.encode(parameterValues);
		}

		ExampleUtilities.showResult("**    CONTRACT RUN LOCAL");

		HederaContractFunctionResult functionResult = new HederaContractFunctionResult();
		// run a call local query
		functionResult = contract.callLocal(localGas, encodedFunc, maxResultSize);
		if (functionResult != null) {
			// it was successful, print it
			ExampleUtilities.showResult(String.format("**   Got error message=%s\n"
					+ "**    Got gas used=%d\n"
					+ "**    Got contract num=%d"
					,functionResult.errorMessage()
					,functionResult.gasUsed()
					,functionResult.contractID().contractNum));
			
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			functionResult = null;
		} else {
			// an error occurred
			ExampleUtilities.showResult("**    Running local function - precheck ERROR " + contract.getPrecheckResult());
		}

		if (functionResult == null) {
			throw new Exception ("Error running local smart contract function " + functionName);
		}
		
		return functionResult;
	}
	
	public int callLocalInt(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		
		BigInteger bigResult = (BigInteger) retResults[0];
		return bigResult.intValue();
	}
	public String callLocalString(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (String) retResults[0];
	}
	public BigInteger callLocalBigInt(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (BigInteger) retResults[0];
	}
	public boolean callLocalBoolean(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (boolean) retResults[0];
	}
	public long callLocalLong(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		BigInteger bigResult = (BigInteger) retResults[0];
		return bigResult.longValue();
	}
	public String callLocalAddress(HederaContract contract, long localGas, long maxResultSize, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callLocal(contract, localGas, maxResultSize, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return Hex.toHexString((byte[]) retResults[0]);
	}
			
	public HederaContractFunctionResult callFunctionResult(HederaContract contract, long gas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaTransactionRecord record = this.callForRecord(contract, gas, amount, functionName, parameterValues);
		
		return record.contractCallResult;
	}

	public Object[] callForObjectArray(HederaContract contract, long gas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaTransactionRecord record = this.callForRecord(contract, gas, amount, functionName, parameterValues);
		
		return getFunctionResult(functionName, record.contractCallResult);
	}
	
	public HederaTransactionRecord callForRecord(HederaContract contract, long gas, long amount, String functionName, Object ... parameterValues) throws Exception {
		
		JSONObject abiJSON = findABI(functionName);
		String abi = abiJSON.toString();
		
		JSONArray inputs = (JSONArray) abiJSON.get("inputs");
		
		if (inputs.size() != parameterValues.length) {
			throw new Exception ("Numer of parameter values mismatch with abi inputs");
		}
		
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abi);
		
		byte[] encodedFunc = new byte[0];
		if (parameterValues.length == 0) {
			encodedFunc = function.encode();
		} else {
			encodedFunc = function.encode(parameterValues);
		}

		ExampleUtilities.showResult("**    CONTRACT CALL");

		// call the smart contract
		// smart contract call transaction
		HederaTransactionResult callResult = contract.call(gas, amount, encodedFunc);
		// was it successful ?
		if (callResult.getPrecheckResult() == ResponseCodeEnum.OK) {
			// yes, get a receipt for the transaction
			HederaTransactionReceipt receipt = Utilities.getReceipt(contract.hederaTransactionID,
					HederaNodeList.randomNode());
			// was that successful ?
			if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
				// and print it out
				ExampleUtilities.showResult(String.format("**    Smart Contract call success"));
			} else {
				ExampleUtilities.showResult("**    TransactionStatus:" + receipt.transactionStatus.toString());
//				callResult = null;
			}
		} else if (contract.getPrecheckResult() == ResponseCodeEnum.BUSY) {
			logger.info("system busy, try again later");
			callResult = null;
		} else {
			ExampleUtilities.showResult("**    Failed with getPrecheckResult:" + contract.getPrecheckResult());
			callResult = null;
		}
		
		if (callResult == null) {
			throw new Exception ("Error running local smart contract function " + functionName);
		}

		HederaTransactionRecord record = new HederaTransactionRecord(contract.hederaTransactionID, HederaNodeList.randomNode().contractGetRecordsQueryFee, contract.txQueryDefaults);
		return record;
	}
	public int callInt(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		
		BigInteger bigResult = (BigInteger) retResults[0];
		return bigResult.intValue();
	}
	public String callString(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (String) retResults[0];
	}
	public BigInteger callBigInt(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (BigInteger) retResults[0];
	}
	public boolean callBoolean(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return (boolean) retResults[0];
	}
	public long callLong(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		BigInteger bigResult = (BigInteger) retResults[0];
		return bigResult.longValue();
	}
	
	public String callAddress(HederaContract contract, long localGas, long amount, String functionName, Object ... parameterValues) throws Exception {
		HederaContractFunctionResult result = this.callFunctionResult(contract, localGas, amount, functionName, parameterValues);
		Object[] retResults = getFunctionResult(functionName, result);
		return Hex.toHexString((byte[]) retResults[0]);
	}

	public String outputData(HederaContractFunctionResult result) {
    	byte[] callResult = result.contractCallResult();
    	String encodeResult = Hex.toHexString(callResult);
    	int dataOffsetStart = 8;
    	int dataOffsetEnd = 8 + 64;
    	int offset = 2 * Integer.parseInt(encodeResult.substring(dataOffsetStart, dataOffsetEnd), 16);
    	int lengthStart = 8 + 64;
    	int lengthEnd = 8 + 64 + 64;
    	int len = 2 * Integer.parseInt(encodeResult.substring(lengthStart, lengthEnd), 16);
    	
    	byte[] resultBytes = Arrays.copyOfRange(callResult, offset, offset+len);
    	return new String(resultBytes);
    }
    
	public Object[] getFunctionResult(String functionName, HederaContractFunctionResult result) throws Exception {
    	JSONObject abiJSON = findABI(functionName);
    	String abi = abiJSON.toString();
    	CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abi);
    	return function.decodeResult(result.contractCallResult());
    }
	
}
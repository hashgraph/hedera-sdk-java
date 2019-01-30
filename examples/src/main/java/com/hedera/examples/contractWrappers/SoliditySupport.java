package com.hedera.examples.contractWrappers;

import java.math.BigInteger;

import org.ethereum.core.CallTransaction;
import org.spongycastle.util.encoders.Hex;

public final class SoliditySupport {
	// support for smart contract functions
	public static CallTransaction.Function getGetValueFunction(String getABI) {
		String funcJson = getABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		return function;
	}

	public static CallTransaction.Function getSetFunction(String setABI) {
		String funcJson = setABI.replaceAll("'", "\"");
		CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(funcJson);
		return function;
	}

	public static byte[] encodeGetValue(String getABI) {
		CallTransaction.Function function = getGetValueFunction(getABI);
		byte[] encodedFunc = function.encode();
		return encodedFunc;
	}

	public static byte[] encodeSetInt(int valueToAdd, String setABI) {
		CallTransaction.Function function = getSetFunction(setABI);
		byte[] encodedFunc = function.encode(valueToAdd);

		return encodedFunc;
	}

	public static byte[] encodeSetString(String valueToAdd, String setABI) {
		CallTransaction.Function function = getSetFunction(setABI);
		byte[] encodedFunc = function.encode(valueToAdd);

		return encodedFunc;
	}
	
	
	public static int decodeGetValueResultInt(byte[] value, String getABI) {
		int decodedReturnedValue = 0;
		CallTransaction.Function function = getGetValueFunction(getABI);
		Object[] retResults = function.decodeResult(value);
		if (retResults != null && retResults.length > 0) {
			BigInteger retBi = (BigInteger) retResults[0];
			decodedReturnedValue = retBi.intValue();
		}
		return decodedReturnedValue;
	}

	public static boolean decodeGetValueResultBoolean(byte[] value, String getABI) {
		boolean decodedReturnedValue = false;
		CallTransaction.Function function = getGetValueFunction(getABI);
		Object[] retResults = function.decodeResult(value);
		if (retResults != null && retResults.length > 0) {
			decodedReturnedValue = (boolean) retResults[0];
		}
		return decodedReturnedValue;
	}

	public static long decodeGetValueResultLong(byte[] value, String getABI) {
		long decodedReturnedValue = 0;
		CallTransaction.Function function = getGetValueFunction(getABI);
		Object[] retResults = function.decodeResult(value);
		if (retResults != null && retResults.length > 0) {
			BigInteger retBi = (BigInteger) retResults[0];
			decodedReturnedValue = retBi.longValueExact();
		}
		return decodedReturnedValue;
	}

	public static String decodeGetValueResultString(byte[] value, String getABI) {
		String result = "";
		CallTransaction.Function function = getGetValueFunction(getABI);
		Object[] retResults = function.decodeResult(value);
		if (retResults != null && retResults.length > 0) {
			result = (String) retResults[0];
		}
		return result;
	}

	public static String decodeGetValueResultAddress(byte[] value, String getABI) {
		String result = "";
		CallTransaction.Function function = getGetValueFunction(getABI);
		Object[] retResults = function.decodeResult(value);
		if (retResults != null && retResults.length > 0) {
			byte[] resultBytes = (byte[]) retResults[0];
			result = new String(resultBytes);
			result = Hex.toHexString(resultBytes);
		}
		return result;
	}
}

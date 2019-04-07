package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.HashMap;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.CurrentAndNextFeeSchedule;
import com.hederahashgraph.api.proto.java.FeeSchedule;
import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.TransactionFeeSchedule;

/**
 * The fee schedule for a specific hedera functionality and the time period this fee will be valid for the transaction
 */
public class HederaFeeSchedule implements Serializable {
	private static final long serialVersionUID = 1;

	private HashMap<HederaFunctionality, HederaTransactionFeeSchedule> currentTransactionFeeSchedule = null;
	private long currentValidExpiry = 0;
	private HashMap<HederaFunctionality, HederaTransactionFeeSchedule> nextTransactionFeeSchedule = null;
	private long nextValidExpiry = 0;
	/**
	 * Build up the fee schedule from input from a file loaded from the Hedera File System
	 * @param scheduleBytes the byte array containing the fee schedule
	 * @throws InvalidProtocolBufferException in the event of an exception
	 */
	public HederaFeeSchedule(byte[] scheduleBytes) throws InvalidProtocolBufferException {
		parseScheduleBytes(scheduleBytes);
	}
	
	private void parseScheduleBytes(byte[] scheduleBytes) throws InvalidProtocolBufferException {
		CurrentAndNextFeeSchedule currentAndNextFeeSchedule = CurrentAndNextFeeSchedule.parseFrom(scheduleBytes);
		
		FeeSchedule feeSchedule = currentAndNextFeeSchedule.getCurrentFeeSchedule();
		this.currentValidExpiry = feeSchedule.getExpiryTime().getSeconds();
		
		for (int i=0; i < feeSchedule.getTransactionFeeScheduleCount(); i++) {
			TransactionFeeSchedule transactionFeeScheduleProto = feeSchedule.getTransactionFeeSchedule(i);
			
			HederaTransactionFeeSchedule hederaTransactionFeeSchedule = new HederaTransactionFeeSchedule();
			hederaTransactionFeeSchedule.hederaFunctionality = transactionFeeScheduleProto.getHederaFunctionality();
			HederaFeeData hederaFeeData = new HederaFeeData(transactionFeeScheduleProto.getFeeData());
			hederaTransactionFeeSchedule.feeData = hederaFeeData;
			
			this.currentTransactionFeeSchedule.put(transactionFeeScheduleProto.getHederaFunctionality(), hederaTransactionFeeSchedule);
		}

		feeSchedule = currentAndNextFeeSchedule.getNextFeeSchedule();
		this.nextValidExpiry = feeSchedule.getExpiryTime().getSeconds();
		
		for (int i=0; i < feeSchedule.getTransactionFeeScheduleCount(); i++) {
			TransactionFeeSchedule transactionFeeScheduleProto = feeSchedule.getTransactionFeeSchedule(i);
			
			HederaTransactionFeeSchedule hederaTransactionFeeSchedule = new HederaTransactionFeeSchedule();
			hederaTransactionFeeSchedule.hederaFunctionality = transactionFeeScheduleProto.getHederaFunctionality();
			HederaFeeData hederaFeeData = new HederaFeeData(transactionFeeScheduleProto.getFeeData());
			hederaTransactionFeeSchedule.feeData = hederaFeeData;
			
			this.nextTransactionFeeSchedule.put(transactionFeeScheduleProto.getHederaFunctionality(), hederaTransactionFeeSchedule);
		}
	}
	/**
	 * Gets the current {@link HederaTransactionFeeSchedule} for a given {@link HederaFunctionality}
	 * @param hederaFunctionality {@link HederaFunctionality} to return the {@link HederaTransactionFeeSchedule} for
	 * @return {@link HederaTransactionFeeSchedule}
	 * @throws Exception in the event of an error
	 */
	public HederaTransactionFeeSchedule getCurrentTransactionFeeSchedule(HederaFunctionality hederaFunctionality) throws Exception {
		if (this.currentTransactionFeeSchedule == null) {
			throw new Exception("Fee schedule not initialised");
		} else {
			return this.currentTransactionFeeSchedule.get(hederaFunctionality);
		}
	}
	/**
	 * Gets the next {@link HederaTransactionFeeSchedule} for a given {@link HederaFunctionality}
	 * @param hederaFunctionality {@link HederaFunctionality} to return the {@link HederaTransactionFeeSchedule} for
	 * @return {@link HederaTransactionFeeSchedule}
	 * @throws Exception in the event of an error
	 */
	public HederaTransactionFeeSchedule getNextTransactionFeeSchedule(HederaFunctionality hederaFunctionality) throws Exception {
		if (this.nextTransactionFeeSchedule == null) {
			throw new Exception("Fee schedule not initialised");
		} else {
			return this.nextTransactionFeeSchedule.get(hederaFunctionality);
		}
	}
}
package com.hedera.sdk.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.hedera.sdk.node.HederaNode;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
/**
 * This class exists in order to enable asynchronous handling of Transaction Receipts
 * It is fully synchronised and therefore thread safe
 * The class holds an internal hashmap of node and receipt, indexed by TransactionID
 * Adding a transaction to the hashmap means it will be queried for a receipt when refresh() is called
 * Transactions are automatically removed from the hashmap when the collected receipt means there is no reason
 * to continue polling for one.
 * Note: No callback has been implemented on completion of receipt acquisition.
 */
public class HederaTransactionState {

	private class TransactionState {
		public HederaNode node;
		public HederaTransactionReceipt receipt;
	}
	
	private static Map<HederaTransactionID, TransactionState> stateMap = new HashMap<>();

	/**
	 * Adds (or replaces) a transaction to the internal hashmap
	 * @param transactionID the {@link HederaTransactionID} to add
	 * @param node the {@link HederaNode} against which the receipt should be polled for
	 */
	public void setTransaction(HederaTransactionID transactionID, HederaNode node) 
    { 
        TransactionState state = new TransactionState();
        state.node = node;
        state.receipt = new HederaTransactionReceipt();
        
        // Only one thread is permitted 
        synchronized(this) 
        { 
            stateMap.put(transactionID, state); 
        } 
    } 	

	/**
	 * Adds (or replaces) a transaction to the internal hashmap
	 * @param transactionID the {@link HederaTransactionID} to add
	 * @param node the {@link HederaNode} against which the receipt should be polled for
	 * @param receipt the updated {@link HederaTransactionReceipt}
	 */
	public void setTransaction(HederaTransactionID transactionID, HederaNode node, HederaTransactionReceipt receipt) 
    { 
        TransactionState state = new TransactionState();
        state.node = node;
        state.receipt = receipt;
        
        // Only one thread is permitted 
        synchronized(this) 
        { 
            stateMap.put(transactionID, state); 
        } 
    } 	
	/**
	 * Removes a transaction from the hashmap so that it's no longer polled for
	 * @param transactionID the {@link HederaTransactionID} to remove
	 */
	public void removeTransaction(HederaTransactionID transactionID) 
    { 
        // Only one thread is permitted 
        synchronized(this) 
        { 
            stateMap.remove(transactionID); 
        } 
    } 	
	/**
	 * Gets the receipt associated with a {@link HederaTransactionID}
	 * @param transactionID the transaction ID for which the receipt is requested
	 * @return {@link HederaTransactionReceipt} the receipt
	 */
	public HederaTransactionReceipt getReceipt(HederaTransactionID transactionID) 
    { 
        // Only one thread is permitted 
        synchronized(this) 
        { 
        	return stateMap.get(transactionID).receipt;
        } 
    } 	
	/**
	 * refreshes the hashmap by querying node(s) for a receipt for each TransactionID
	 * Note: This implementation removes receipts once they have reached a logical conclusion
	 * @throws InterruptedException in the event of an error 
	 */
	public void refresh() throws InterruptedException 
    { 
        // Only one thread is permitted 
        synchronized(this) 
        { 
        	Iterator it = stateMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                HederaTransactionID transactionID = (HederaTransactionID)pair.getKey();
                TransactionState state = (TransactionState)pair.getValue();
                
                HederaTransactionReceipt receipt = Utilities.getReceipt(transactionID, state.node, 1, 0, 0);
                
                it.remove(); // avoids a ConcurrentModificationException

				switch (receipt.nodePrecheck) {
					case SUCCESS:
						if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
							// a callback would be implemented here to deal with the operation
							stateMap.remove(transactionID);
						}
						break;
					case OK:
						if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
							// a callback would be implemented here to deal with the operation
							stateMap.remove(transactionID);
						}
						break;
					case BUSY:
						break;
					default:
						// a callback would be implemented here to deal with the operation
						stateMap.remove(transactionID);
						break;
					case INVALID_SIGNATURE_TYPE_MISMATCHING_KEY:
						// a callback would be implemented here to deal with the operation
						stateMap.remove(transactionID);
						break;
					case INVALID_SIGNATURE_COUNT_MISMATCHING_KEY:
						// a callback would be implemented here to deal with the operation
						stateMap.remove(transactionID);
						break;
					case INVALID_FILE_WACL:
						// a callback would be implemented here to deal with the operation
						stateMap.remove(transactionID);
						break;
					case SERIALIZATION_FAILED:
						// a callback would be implemented here to deal with the operation
						stateMap.remove(transactionID);
						break;
				}
            }
        } 
    } 	
	/**
	 * Gets the number of Transaction IDs in the hashmap
	 * @return the number of transaction IDs.
	 */
	public int getCount() 
    { 
        // Only one thread is permitted 
        synchronized(this) 
        {
        	return stateMap.size();
        }
    } 	
}
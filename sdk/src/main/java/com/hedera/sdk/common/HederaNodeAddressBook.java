package com.hedera.sdk.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hederahashgraph.api.proto.java.NodeAddressBook;

/**
 * Gives the node addresses in the address book
 */
public class HederaNodeAddressBook implements Serializable {

	private static final long serialVersionUID = 1;

	/**
	 * array of HederaNodeAddress 
	 */
	public List<HederaNodeAddress> nodeAddresses  = new ArrayList<HederaNodeAddress>();
	
	/**
	 * Default constructor, creates a HederaAccountID with default values
	 */
	public HederaNodeAddressBook() {
	}

	/**
	 * Constructs from a list of {@link HederaNodeAddress}
	 * @param nodeAddresses the list of {@link HederaNodeAddress}
	 */
	public HederaNodeAddressBook(List<HederaNodeAddress> nodeAddresses) {

		for (HederaNodeAddress nodeAddress : nodeAddresses) {
			this.nodeAddresses.add(nodeAddress);
		}
	}

	/**
	 * Adds a {@link HederaNodeAddress} to the list
	 * @param nodeAddress a {@link HederaNodeAddress} object
	 */
	public void add(HederaNodeAddress nodeAddress) {

		this.nodeAddresses.add(nodeAddress);

	}
	/**
	 * Deletes a matching {@link HederaNodeAddress} from the list
	 * @param nodeAddress the {@link HederaNodeAddress} to remove
	 * @return true if successfully found and removed
	 */
	public boolean delete(HederaNodeAddress nodeAddress) {

		return this.nodeAddresses.remove(nodeAddress);
	}

	/**
	 * Gets the protobuf for the {@link HederaNodeAddress} in this list
	 * @return {@link NodeAddressBook}
	 */
	public NodeAddressBook getProtobuf() {

		// Generates the protobuf payload for this class
		NodeAddressBook.Builder nodeAddressBook = NodeAddressBook.newBuilder();
		for (HederaNodeAddress nodeAddress : nodeAddresses) {
			nodeAddressBook.addNodeAddress(nodeAddress.getProtobuf());
		}
		
		return nodeAddressBook.build();
	}
}
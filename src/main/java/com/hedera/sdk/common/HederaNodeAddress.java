package com.hedera.sdk.common;

import java.io.Serializable;
import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.NodeAddress;

/**
 * The information about a node
 */
public class HederaNodeAddress implements Serializable {

	private static final long serialVersionUID = 1;

	/**
	 * the ip address of the Node 
	 */
	byte[] ipAddress = new byte[0];
	/**
	 * the port number of the grpc server for the node
	 */
	int portNo = 0;
	/**
	 * the memo field of the node 
	 */
	byte[] memo = new byte[0];

	/**
	 * Default constructor, creates a HederaAccountID with default values
	 */
	public HederaNodeAddress() {
	}

	/**
	 * Constructor for a HederaNodeAddress from specified parameter values
	 * @param ipAddress the ip address of the Node 
	 * @param portNo the port number of the grpc server for the node
	 */
	public HederaNodeAddress(byte[] ipAddress, int portNo) {
		this(ipAddress, portNo, new byte[0]);
	}
	/**
	 * Constructor for a HederaNodeAddress from specified parameter values
	 * @param ipAddress the ip address of the Node 
	 * @param portNo the port number of the grpc server for the node
	 * @param memo the memo field of the node 
	 */
	public HederaNodeAddress(byte[] ipAddress, int portNo, byte[] memo) {
		this.ipAddress = ipAddress.clone();
		this.portNo = portNo;
		this.memo = memo.clone();
	}

	/**
	 * Constructor for a HederaNodeAddress from protobuf
	 * @param nodeAddress the protobuf from which to create the HederaNodeAddress
	 */
	public HederaNodeAddress(NodeAddress nodeAddress) {
		this.ipAddress = nodeAddress.getIpAddress().toByteArray();
		this.portNo = nodeAddress.getPortno();
		this.memo = nodeAddress.getMemo().toByteArray();
	}

	/**
	 * Generate a protobuf payload for this object
	 * @return a protobuf NodeAddress 
	 */
	public NodeAddress getProtobuf() {
		NodeAddress.Builder nodeAddress = NodeAddress.newBuilder();
		
		if (this.ipAddress != null) {
			if (this.ipAddress.length > 0) {
				nodeAddress.setIpAddress(ByteString.copyFrom(this.ipAddress));
			}
		}
		
		nodeAddress.setPortno(this.portNo);

		if (this.memo != null) {
			if (this.memo.length > 0) {
				nodeAddress.setMemo(ByteString.copyFrom(this.memo));
			}
		}

		return nodeAddress.build();
	}
}
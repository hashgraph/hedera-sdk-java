package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hederahashgraph.api.proto.java.ShardID;

/**
 * Each shard has a nonnegative shard number. Each realm within a given shard has a nonnegative realm number 
 * (that number might be reused in other shards). And each account, file, and smart contract instance within a given realm has a 
 * nonnegative number (which might be reused in other realms). Every account, file, and smart contract instance is within exactly 
 * one realm. So a FileID is a triplet of numbers, like 0.1.2 for entity number 2 within realm 1 within shard 0. 
 * Each realm maintains a single counter for assigning numbers, so if there is a file with ID 0.1.2, then there won't be an 
 * account or smart contract instance with ID 0.1.2.
 * Everything is partitioned into realms so that each Solidity smart contract can access everything in just a single realm, 
 * locking all those entities while it's running, but other smart contracts could potentially run in other realms in parallel. 
 * So realms allow Solidity to be parallelized somewhat, even though the language itself assumes everything is serial.
 */
public class HederaShardID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaShardID.class);
	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;

	/**
	 * Default constructor
	 */
	public HederaShardID() {


	}
	/**
	 * Constructor from a shardNum
	 * @param shardNum the shard number for the shard
	 */
	public HederaShardID(long shardNum) {

 		this.shardNum = shardNum;

	}

	/**
	 * Construct from a {@link ShardID} protobuf stream
	 * @param shardIDProtobuf the protobuf expression of a ShardID
	 */
	public HederaShardID(ShardID shardIDProtobuf) {

		this.shardNum = shardIDProtobuf.getShardNum();

	}

	/**
	 * Generate a protobuf payload for this object
	 * @return {@link ShardID} 
	 */
	public ShardID getProtobuf() {

		
	   	ShardID.Builder shardID = ShardID.newBuilder();
		if (this.shardNum > 0) {
			shardID.setShardNum(this.shardNum);
		}


		return shardID.build();
	}
}
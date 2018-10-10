package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.RealmID;

/**
 * The ID for a realm. Within a given shard, every realm has a unique ID. Each account, file, and contract instance belongs to exactly one realm.
 */
public class HederaRealmID implements Serializable {
	final static Logger logger = LoggerFactory.getLogger(HederaRealmID.class);

	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;

	/**
	 * the realm number (nonnegative)
	 */
	public long realmNum = 0;

	/**
	 * Default constructor
	 */
	public HederaRealmID() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from a shard number and realm number
	 * @param shardNum the shard number for the realm
	 * @param realmNum the realm's number
	 */
	public HederaRealmID(long shardNum, long realmNum) {
	   	logger.trace("Start - Object init in shard {}, realm {}", shardNum, realmNum);
 		this.shardNum = shardNum;
 		this.realmNum = realmNum;
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from a protobuf {@link RealmID}
	 * @param realmIDProtobuf a protobuf expression of a RealmID
	 */
	public HederaRealmID(RealmID realmIDProtobuf) {
	   	logger.trace("Start - Object init realmIDProtobuf {}", realmIDProtobuf);
		this.shardNum = realmIDProtobuf.getShardNum();
		this.realmNum = realmIDProtobuf.getRealmNum();
	   	logger.trace("End - Object init");
	}
	/**
	 * Gets a {@link RealmID} protobuf for this object
	 * @return {@link RealmID}
	 */
	public RealmID getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		
	   	RealmID.Builder realmID = RealmID.newBuilder();
		if (this.shardNum > 0) {
			realmID.setShardNum(this.shardNum);
		}
	   	if (this.realmNum > 0) {
	   		realmID.setRealmNum(this.realmNum);
	   	}
		logger.trace("End - getProtobuf");

		return realmID.build();
	}
}
package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
import com.hederahashgraph.api.proto.java.RealmID;

/**
 * The ID for a realm. Within a given shard, every realm has a unique ID. Each account, file, and contract instance belongs to exactly one realm.
 */
public class HederaRealmID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaRealmID.class);

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


	}
	/**
	 * Constructor from a shard number and realm number
	 * @param shardNum the shard number for the realm
	 * @param realmNum the realm's number
	 */
	public HederaRealmID(long shardNum, long realmNum) {

 		this.shardNum = shardNum;
 		this.realmNum = realmNum;

	}
	/**
	 * Constructor from a protobuf {@link RealmID}
	 * @param realmIDProtobuf a protobuf expression of a RealmID
	 */
	public HederaRealmID(RealmID realmIDProtobuf) {

		this.shardNum = realmIDProtobuf.getShardNum();
		this.realmNum = realmIDProtobuf.getRealmNum();

	}
	/**
	 * Gets a {@link RealmID} protobuf for this object
	 * @return {@link RealmID}
	 */
	public RealmID getProtobuf() {

		
	   	RealmID.Builder realmID = RealmID.newBuilder();
		if (this.shardNum > 0) {
			realmID.setShardNum(this.shardNum);
		}
	   	if (this.realmNum > 0) {
	   		realmID.setRealmNum(this.realmNum);
	   	}


		return realmID.build();
	}
}
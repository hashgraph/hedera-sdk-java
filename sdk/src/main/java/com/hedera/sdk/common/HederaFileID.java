package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.FileID;

/**
 * The ID for a Hedera File which is made of a shard number, realm number and file number
 */
public class HederaFileID implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaFileID.class);
	private static final long serialVersionUID = 1;

	/**
	 * the shard number (nonnegative)
	 */
	public long shardNum = 0;

	/**
	 * the realm number (nonnegative)
	 * note: if set to -1, in future versions it will be automatically assigned by the Hedera network
	 */
	public long realmNum = 0;

	/**
	 * file number a nonnegative number unique within its realm
	 */
	public long fileNum = 0;
	
	/**
	 * Default constructor with default values
	 */
	public HederaFileID() {


	}

	/**
	 * Constructs a HederaFileID from specified values
	 * @param shardNum the shard number
	 * @param realmNum the realm number
	 * @param fileNum the file number
	 */
	public HederaFileID(long shardNum, long realmNum, long fileNum) {

		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;

	}

	/**
	 * Constructs a HederaFileID from a FileID protobuf
	 * @param fileIDProtobuf the protobuf to generate the HederaFileId from
	 */
	public HederaFileID(FileID fileIDProtobuf) {

		this.shardNum = fileIDProtobuf.getShardNum();
		this.realmNum = fileIDProtobuf.getRealmNum();
		this.fileNum = fileIDProtobuf.getFileNum();

	}

	/**
	 * Generates a protobuf FileID object from this object
	 * @return FileID protobuf
	 */
	public FileID getProtobuf() {

		FileID.Builder fileID = FileID.newBuilder();
		
		fileID.setShardNum(this.shardNum);
		if (this.realmNum != -1) {
			// if realmnum is -1, create a new realm
			fileID.setRealmNum(this.realmNum);
		}
		fileID.setFileNum(this.fileNum);
		

		return fileID.build();
	}
}
package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.FileID;

/**
 * The ID for a Hedera File which is made of a shard number, realm number and file number
 */
public class HederaFileID implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaFileID.class);
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
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}

	/**
	 * Constructs a HederaFileID from specified values
	 * @param shardNum the shard number
	 * @param realmNum the realm number
	 * @param fileNum the file number
	 */
	public HederaFileID(long shardNum, long realmNum, long fileNum) {
	   	logger.trace("Start - Object init shardNum {}, realmNum {}, fileNum {}", shardNum, realmNum, fileNum);
		this.shardNum = shardNum;
		this.realmNum = realmNum;
		this.fileNum = fileNum;
	   	logger.trace("End - Object init");
	}

	/**
	 * Constructs a HederaFileID from a FileID protobuf
	 * @param fileIDProtobuf the protobuf to generate the HederaFileId from
	 */
	public HederaFileID(FileID fileIDProtobuf) {
	   	logger.trace("Start - Object from fileIDProtobuf {}", fileIDProtobuf);
		this.shardNum = fileIDProtobuf.getShardNum();
		this.realmNum = fileIDProtobuf.getRealmNum();
		this.fileNum = fileIDProtobuf.getFileNum();
	   	logger.trace("End - Object init");
	}

	/**
	 * Generates a protobuf FileID object from this object
	 * @return FileID protobuf
	 */
	public FileID getProtobuf() {
	   	logger.trace("Start - getProtobuf");
		FileID.Builder fileID = FileID.newBuilder();
		
		fileID.setShardNum(this.shardNum);
		if (this.realmNum != -1) {
			// if realmnum is -1, create a new realm
			fileID.setRealmNum(this.realmNum);
		}
		fileID.setFileNum(this.fileNum);
		
	   	logger.trace("End - getProtobuf");
		return fileID.build();
	}
}
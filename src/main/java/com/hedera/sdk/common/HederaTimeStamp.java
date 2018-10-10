package com.hedera.sdk.common;

import java.io.Serializable;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Timestamp;

/**
 * An exact date and time. This is the same data structure as the protobuf Timestamp.proto 
 * (see the comments in https://github.com/google/protobuf/blob/master/src/google/protobuf/timestamp.proto)
 */
public class HederaTimeStamp implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaTimeStamp.class);
	private static final long serialVersionUID = 1;
	/**
	 * Time value, defaults to Now
	 */
	public Instant time = Instant.now();
	/**
	 * Default constructor
	 */
	public HederaTimeStamp() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}
	/**
	 * Constructor from an {@link Instant}
	 * @param time the instant to construct from
	 */
	public HederaTimeStamp(Instant time) {
	   	logger.trace("Start - Object init time {}", time);
		this.time = time;
	   	logger.trace("End - Object init");
	}
	/** 
	 * Constructor from seconds and nanos
	 * @param seconds the seconds to construct from
	 * @param nanos the nanos to construct from
	 */
	public HederaTimeStamp(long seconds, int nanos) {
	   	logger.trace("Start - Object init time {}", time);
		this.time = Instant.ofEpochMilli(0);
		this.time = this.time.plusSeconds(seconds);
		this.time = this.time.plusNanos(nanos);
	   	logger.trace("End - Object init");
	}
	
	/**
	 * Construct from a {@link Timestamp} protobuf
	 * @param timestampProtobuf the timestamp in protobuf format
	 */
	public HederaTimeStamp(Timestamp timestampProtobuf) {
	   	logger.trace("Start - Object init timestampProtobuf {}", timestampProtobuf);
		this.time = Instant.ofEpochMilli(0);
		this.time = this.time.plusSeconds(timestampProtobuf.getSeconds());
		this.time = this.time.plusNanos(timestampProtobuf.getNanos());
	   	logger.trace("End - Object init");
	}

	/**
	 * Generate a {@link Timestamp} protobuf payload for this object 
	 * @return {@link Timestamp}
	 */
	public Timestamp getProtobuf() {
	   	logger.trace("Start - getProtobuf");
	   	logger.trace("End - getProtobuf");
		return Timestamp.newBuilder().setSeconds(this.time.getEpochSecond())
			    .setNanos(this.time.getNano()).build();
	}
	/**
	 * Returns the seconds element of the timestamp 
	 * @return {@link Long} the number of seconds
	 */
	public long seconds() {
		return this.time.getEpochSecond();
	}
	/**
	 * Returns the nanos element of the timestamp 
	 * @return {@link int} the number of nanos
	 */
	public int nanos() {
		return this.time.getNano();
	}
}
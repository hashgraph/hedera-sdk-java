package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Duration;

/**
 * The length of a period of time. 
 * This is an identical data structure to the protobuf Duration.proto 
 * (see the comments in https://github.com/google/protobuf/blob/master/src/google/protobuf/duration.proto)
 */
public class HederaDuration implements Serializable {
	final Logger logger = LoggerFactory.getLogger(HederaDuration.class);
	private static final long serialVersionUID = 1;

	/**
	 * the number of seconds (defaults to 120s = 2 minutes)
	 */
	public long seconds = 120;

	/**
	 * the number of nanoseconds (defaults to 0)
	 */
	public int nanos = 0;

	/**
	 * Constructor with default values 
	 */
	public HederaDuration() {
	   	logger.trace("Start - Object init");
	   	logger.trace("End - Object init");
	}

	/**
	 * Constructor with specific values
	 * @param seconds the number of seconds
	 * @param nanos the number of nanoseconds 
	 */
	public HederaDuration(long seconds, int nanos) {
	   	logger.trace("Start - Object init seconds {}, nanos {}", seconds, nanos);
		this.seconds = seconds;
		this.nanos = nanos;
	   	logger.trace("End - Object init");
	}

	/**
	 * Constructor from a protobuf duration
	 * @param durationProtobuf the protobuf for the duration
	 */
	public HederaDuration(Duration durationProtobuf) {
	   	logger.trace("Start - Object init durationProtobuf {}", durationProtobuf);
		this.seconds = durationProtobuf.getSeconds();
		this.nanos = durationProtobuf.getNanos();
	   	logger.trace("End - Object init");
	}

	/**
	 * Generate a protobuf for this object
	 * @return Duration protobuf object
	 */
	public Duration getProtobuf() {
	   	logger.trace("getProtobuf");
		return Duration.newBuilder().setSeconds(this.seconds)
			    .setNanos(this.nanos).build();
	}
}
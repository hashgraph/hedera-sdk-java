package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Duration;

/**
 * The length of a period of time. 
 * This is an identical data structure to the protobuf Duration.proto 
 * (see the comments in https://github.com/google/protobuf/blob/master/src/google/protobuf/duration.proto)
 */
public class HederaDuration implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaDuration.class);
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
	}

	/**
	 * Constructor with specific values
	 * @param seconds the number of seconds
	 * @param nanos the number of nanoseconds 
	 */
	public HederaDuration(long seconds, int nanos) {

		this.seconds = seconds;
		this.nanos = nanos;
	}

	/**
	 * Constructor from a protobuf duration
	 * @param durationProtobuf the protobuf for the duration
	 */
	public HederaDuration(Duration durationProtobuf) {

		this.seconds = durationProtobuf.getSeconds();
		this.nanos = durationProtobuf.getNanos();
	}

	/**
	 * Generate a protobuf for this object
	 * @return Duration protobuf object
	 */
	public Duration getProtobuf() {

		return Duration.newBuilder().setSeconds(this.seconds)
			    .setNanos(this.nanos).build();
	}
}
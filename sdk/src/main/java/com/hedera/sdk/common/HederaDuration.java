package com.hedera.sdk.common;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import com.hederahashgraph.api.proto.java.Duration;

/**
 * The length of a period of time. 
 */
public class HederaDuration implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaDuration.class);
	private static final long serialVersionUID = 1;

	/**
	 * the number of seconds (defaults to 2 minutes)
	 */
	public long seconds = 120;

	/**
	 * Constructor with default values 
	 */
	public HederaDuration() {
	}
	
	public static HederaDuration HederaDurationMinute() {
		return new HederaDuration(60);
	}
	public static HederaDuration HederaDurationHour() {
		return new HederaDuration(60 * 60);
	}
	public static HederaDuration HederaDurationDay() {
		return new HederaDuration(60 * 60 * 24);
	}
	public static HederaDuration HederaDurationWeek() {
		return new HederaDuration(60 * 60 * 24 * 7);
	}
	public static HederaDuration HederaDurationMonth() {
		return new HederaDuration(60 * 60 * 24 * 30);
	}
	public static HederaDuration HederaDurationYear() {
		return new HederaDuration(60 * 60 * 24 * 365);
	}

	/**
	 * Constructor with specific values
	 * @param seconds the number of seconds
	 */
	public HederaDuration(long seconds) {

		this.seconds = seconds;
	}

	/**
	 * Constructor from a protobuf duration
	 * @param durationProtobuf the protobuf for the duration
	 */
	public HederaDuration(Duration durationProtobuf) {

		this.seconds = durationProtobuf.getSeconds();
	}

	/**
	 * Generate a protobuf for this object
	 * @return Duration protobuf object
	 */
	public Duration getProtobuf() {

		return Duration.newBuilder().setSeconds(this.seconds)
			    .build();
	}
}
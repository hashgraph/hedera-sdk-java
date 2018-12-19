package com.hedera.sdk.common;

import java.io.Serializable;
import java.time.Instant;
import org.slf4j.LoggerFactory;
import com.hederahashgraph.api.proto.java.Timestamp;

/**
 * An exact date and time. This is the same data structure as the protobuf Timestamp.proto 
 * (see the comments in https://github.com/google/protobuf/blob/master/src/google/protobuf/timestamp.proto)
 */
public class HederaTimeStamp implements Serializable {
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaTimeStamp.class);
	private static final long serialVersionUID = 1;
	/**
	 * Time value, defaults to Now -10s to account for possible minor time differences between client and node.
	 * An earlier time is better than a later one.
	 */
	public Instant time = Instant.now().minusSeconds(10); 
	/**
	 * Default constructor
	 */
	public HederaTimeStamp() {


	}
	/**
	 * Constructor from an {@link Instant}
	 * @param time the instant to construct from
	 */
	public HederaTimeStamp(Instant time) {

		this.time = time;

	}
	/** 
	 * Constructor from seconds and nanos
	 * @param seconds the seconds to construct from
	 * @param nanos the nanos to construct from
	 */
	public HederaTimeStamp(long seconds, int nanos) {

		this.time = Instant.ofEpochMilli(0);
		this.time = this.time.plusSeconds(seconds);
		this.time = this.time.plusNanos(nanos);

	}
	
	/**
	 * Construct from a {@link Timestamp} protobuf
	 * @param timestampProtobuf the timestamp in protobuf format
	 */
	public HederaTimeStamp(Timestamp timestampProtobuf) {

		this.time = Instant.ofEpochMilli(0);
		this.time = this.time.plusSeconds(timestampProtobuf.getSeconds());
		this.time = this.time.plusNanos(timestampProtobuf.getNanos());

	}

	/**
	 * Generate a {@link Timestamp} protobuf payload for this object 
	 * @return {@link Timestamp}
	 */
	public Timestamp getProtobuf() {


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
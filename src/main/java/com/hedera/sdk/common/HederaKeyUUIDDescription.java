package com.hedera.sdk.common;

import java.io.Serializable;
import org.slf4j.LoggerFactory;
/**
 * Description and UUID for a key
 */

public class HederaKeyUUIDDescription implements Serializable {
	private static final long serialVersionUID = 1L;
	final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(HederaKeyUUIDDescription.class);

	/**
	 * Description of the key
	 */
	public String description = "";
	/**
	 * UUID for the key
	 */
	public String uuid = "";
	
	/**
	 * Default constructor
	 */
	public HederaKeyUUIDDescription () {
		
	}
	/**
	 * Constructs from a uuid and description
	 * @param uuid the UUID
	 * @param description the description
	 */
	public HederaKeyUUIDDescription (String uuid, String description) {

		this.description = description;
		this.uuid = uuid;

	}
}

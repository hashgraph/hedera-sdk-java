package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import com.hedera.sdk.common.HederaTimeStamp;
import com.hederahashgraph.api.proto.java.Timestamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HederaTimeStampTest {

	protected static HederaTimeStamp duration1;
	protected static HederaTimeStamp duration2;
	
	@BeforeAll
	static void initAll() {
		duration1 = new HederaTimeStamp(Instant.now().plusMillis(1040));
		
		Timestamp proto = duration1.getProtobuf();
		duration2 = new HederaTimeStamp(proto);
	}

	@Test
	@DisplayName("Checking matching timestamp details")
	void testAccount() {
		assertEquals(duration1.time, duration2.time);
	}

}

package test.hedera.sdk.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.hedera.sdk.common.HederaDuration;
import com.hederahashgraph.api.proto.java.Duration;

class HederaDurationTest {

	protected static HederaDuration duration1;
	protected static HederaDuration duration2;
	
	@BeforeAll
	static void initAll() {
		duration1 = new HederaDuration(10,100);
		
		Duration proto = duration1.getProtobuf();
		duration2 = new HederaDuration(proto);
	}

	@Test
	@DisplayName("Checking matching account details")
	void testAccount() {
		assertEquals(duration1.nanos, duration2.nanos);
		assertEquals(duration1.seconds, duration2.seconds);
	}

}

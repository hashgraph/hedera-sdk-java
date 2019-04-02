package com.hedera.sdk.file

import spock.lang.Specification
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey

class FileDeleteTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
			def tx = new FileDeleteTransaction();
		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  fileDelete {
  }
}
""";
	}
}

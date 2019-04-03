package com.hedera.sdk.account

import spock.lang.Specification

class AccountDeleteClaimTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountDeleteClaimTransaction();

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoDeleteClaim {
  }
}
"""
	}
}

package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import spock.lang.Specification

class AccountRecordsQueryTest extends Specification{
	def "Query can be built with defaults"() {
		when:
		def query = new AccountRecordsQuery()

		then:
		query.inner.build().toString() == """cryptoGetAccountRecords {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new AccountRecordsQuery().with(true, {
			account = new AccountId(5)
		})

		then:
		query.inner.build().toString() == """cryptoGetAccountRecords {
  accountID {
    accountNum: 5
  }
}
"""
	}
}

package com.hedera.sdk.contract

import com.hedera.sdk.ContractId
import spock.lang.Specification

class ContractInfoQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new ContractInfoQuery()

		then:
		query.inner.build().toString() == """contractGetInfo {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new ContractInfoQuery().with(true, {
			contract = new ContractId(10, 14, 14)
		})

		then:
		query.inner.build().toString() == """contractGetInfo {
  contractID {
    shardNum: 10
    realmNum: 14
    contractNum: 14
  }
}
"""
	}
}

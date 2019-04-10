package com.hedera.sdk.contract

import com.hedera.sdk.ContractId
import spock.lang.Specification

class ContractCallQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new ContractCallQuery()

		then:
		query.inner.build().toString() == """contractCallLocal {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new ContractCallQuery().with(true, {
			contract = new ContractId(10, 10, 15)
			gas = 1541
			functionParameters = [1451, 4, 245, 1, 543]
			maxResultSize = 444447
		})

		then:
		query.inner.build().toString() == """contractCallLocal {
  contractID {
    shardNum: 10
    realmNum: 10
    contractNum: 15
  }
  gas: 1541
  functionParameters: "\\253\\004\\365\\001\\037"
  maxResultSize: 444447
}
"""
	}
}

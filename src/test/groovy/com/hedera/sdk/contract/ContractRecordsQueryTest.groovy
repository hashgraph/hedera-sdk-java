package com.hedera.sdk.contract


import spock.lang.Specification

class ContractRecordsQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new ContractRecordsQuery()

		then:
		query.inner.build().toString() == """ContractGetRecords {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new ContractRecordsQuery().with(true, {
			contract = new ContractId(10, 14, 34)
		})

		then:
		query.inner.build().toString() == """ContractGetRecords {
  contractID {
    shardNum: 10
    realmNum: 14
    contractNum: 34
  }
}
"""
	}
}

package com.hedera.sdk.contract

import com.hedera.sdk.ContractId
import spock.lang.Specification

class ContractBytecodeQueryTest extends Specification {
	def "Query can be built with defaults"() {
		when:
		def query = new ContractBytecodeQuery()

		then:
		query.inner.build().toString() == """contractGetBytecode {
}
"""
	}

	def "Query can be built"() {
		when:
		def query = new ContractBytecodeQuery().with(true, {
            contract = new ContractId(14, 11, 4)
		})

		then:
		query.inner.build().toString() == """contractGetBytecode {
  contractID {
    shardNum: 14
    realmNum: 11
    contractNum: 4
  }
}
"""
	}
}

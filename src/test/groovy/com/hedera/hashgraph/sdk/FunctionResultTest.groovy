package com.hedera.hashgraph.sdk

import com.google.protobuf.ByteString
import com.hedera.hashgraph.sdk.proto.ContractFunctionResult
import spock.lang.Specification

class FunctionResultTest extends Specification {
	def 'provides results correctly'() {
		given:
		def callResultHex = """\
00000000000000000000000000000000000000000000000000000000ffffffff\
0000000000000000000000000000000000000000000000000000000000000060\
00000000000000000000000000000000000000000000000000000000000000a0\
000000000000000000000000000000000000000000000000000000000000000d\
48656c6c6f2c20776f726c642100000000000000000000000000000000000000\
0000000000000000000000000000000000000000000000000000000000000014\
48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000\
"""

		when:
		def result = new FunctionResult(ContractFunctionResult.newBuilder().setContractCallResult(
				ByteString.copyFrom(callResultHex.decodeHex())))

		then:
		result.getInt(0) == -1
		result.getString(1) == "Hello, world!"
		result.getString(2) == "Hello, world, again!"
	}
}

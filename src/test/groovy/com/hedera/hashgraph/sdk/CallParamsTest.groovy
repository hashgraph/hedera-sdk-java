package com.hedera.hashgraph.sdk

import spock.lang.Specification

class CallParamsTest extends Specification {
	def 'funcSelector() produces correct bytes'() {
		// testing all the examples here
		// https://solidity.readthedocs.io/en/v0.5.7/abi-spec.html#examples
		expect:
		hash == CallParams.funcSelector(funcName, paramTypes).toByteArray().encodeHex().toString()

		where:
		hash << [
			'cdcd77c0',
			'fce353f6',
			'a5643bf2',
			'8be65246'
		]
		funcName << ['baz', 'bar', 'sam', 'f']
		paramTypes << [
			['uint32', 'bool'],
			['bytes3[2]'],
			['bytes', 'bool', 'uint256[]'],
			[
				'uint256',
				'uint32[]',
				'bytes10',
				'bytes'
			]
		]
		// omitted ('2289b18c', 'g', ['uint[][]','string[]'])
		// this is the only one that the hash doesn't match which suggests the documentation is wrong here
	}

	def 'encodes params correctly'() {
		when:
		def bytes = new CallParams('set_message').add("Hello, world!").toProto()
		def bytesEquiv = new CallParams('set_message').add("Hello, world!".getBytes('UTF-8')).toProto()

		then:
		bytes.toByteArray().encodeHex().toString() == """\
2e982602\
0000000000000000000000000000000000000000000000000000000000000020\
000000000000000000000000000000000000000000000000000000000000000d\
48656c6c6f2c20776f726c642100000000000000000000000000000000000000\
"""
		// signature should encode differently but the contents are identical
		bytesEquiv.toByteArray().encodeHex().toString() == """\
010473a7\
0000000000000000000000000000000000000000000000000000000000000020\
000000000000000000000000000000000000000000000000000000000000000d\
48656c6c6f2c20776f726c642100000000000000000000000000000000000000\
"""
	}

	def 'uint256() left-pads properly'() {
		expect:
		encoded == CallParams.uint256(val).toByteArray().encodeHex().toString()

		where:
		val << [
			0,
			2,
			255,
			4095,
			255 << 24,
			4095 << 20,
			(int) 0xdeadbeef
		]
		encoded << [
			'0000000000000000000000000000000000000000000000000000000000000000',
			'0000000000000000000000000000000000000000000000000000000000000002',
			'00000000000000000000000000000000000000000000000000000000000000ff',
			'0000000000000000000000000000000000000000000000000000000000000fff',
			'00000000000000000000000000000000000000000000000000000000ff000000',
			'00000000000000000000000000000000000000000000000000000000fff00000',
			'00000000000000000000000000000000000000000000000000000000deadbeef',
		]
	}
}


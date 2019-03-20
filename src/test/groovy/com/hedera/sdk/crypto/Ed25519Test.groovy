package com.hedera.sdk.crypto

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

class Ed25519Test extends Specification {
	def "private key generates successfully"() {
		when:
		def key = Ed25519PrivateKey.generate()

		then:
		key != null
		key.toBytes() != null
	}

	def "private key can be recovered from bytes"() {
		when:
		def key1 = Ed25519PrivateKey.generate()
		def key1Bytes = key1.toBytes()
		def key2 = Ed25519PrivateKey.fromBytes(key1Bytes)
		def key2Bytes = key2.toBytes()

		then:
		key1Bytes == key2Bytes
	}

	def "private key can be recovered from string"() {
		when:
		def key1 = Ed25519PrivateKey.generate()
		def key1Bytes = key1.toBytes()
		def key2 = Ed25519PrivateKey.fromBytes(key1Bytes)
		def key2Bytes = key2.toBytes()

		then:
		key1Bytes == key2Bytes
	}

	def "private key can be recovered from external string"() {
		when:
		def key = Ed25519PrivateKey.fromString(keyStr)

		then:
		key != null

		where:
		keyStr << [
			// ASN1 encoded hex
			"302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10",
			// raw hex
			"db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10\
                                  e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
		]
	}
}

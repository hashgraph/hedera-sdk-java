package com.hedera.hashgraph.sdk.crypto.ed25519

import com.hedera.hashgraph.sdk.crypto.Key
import spock.lang.Specification

class Ed25519PublicKeyTest extends Specification {
	def "public key can be recovered from bytes"() {
		when:
		def key1 = Ed25519PrivateKey.generate().getPublicKey()
		def key1Bytes = key1.toBytes()
		def key2 = Ed25519PublicKey.fromBytes(key1Bytes)
		def key2Bytes = key2.toBytes()

		then:
		key1Bytes == key2Bytes
	}

	def "public key can be recovered from string"() {
		when:
		def key1 = Ed25519PrivateKey.generate().getPublicKey()
		def key1Str = key1.toString()
		def key2 = Ed25519PublicKey.fromString(key1Str)
		def key2Str = key2.toString()
		def key3 = Key.fromString(key1Str)
		def key3Str = key3.toString()

		then:
		key3 instanceof Ed25519PublicKey
		key1Str == key2Str
		key1Str == key3Str
	}

	def "public key can be recovered from external string"() {
		when:
		def key = Ed25519PublicKey.fromString(keyStr)
		def key2 = Key.fromString(keyStr)

		then:
		key != null
		key2 instanceof Ed25519PublicKey
		key.toBytes() == ((Ed25519PublicKey) key2).toBytes()

		where:
		keyStr << [
			// ASN1 encoded hex
			'302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7',
			// raw hex
			'e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7',
		]
	}

	def "public key can be encoded to string"() {
		when:
		def key = Ed25519PublicKey.fromString(keyStr)

		then:
		key != null
		key.toString() == keyStr

		where:
		keyStr = '302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7'
	}
}

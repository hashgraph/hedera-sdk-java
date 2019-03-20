package com.hedera.sdk.crypto.ed25519

import org.bouncycastle.util.encoders.Hex
import spock.lang.Specification

class Ed25519PrivateKeyTest extends Specification {
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
			'302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10',
			// raw hex (concatenated private + public key)
			'db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10' +
			'e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7',
			// raw hex (just private key)
			'db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10'
		]
	}

	def "reproducible signature can be computed"() {
		when:
		def keyStr = "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10"
		def messageStr = "This is a message about the world."
		def key = Ed25519PrivateKey.fromString(keyStr)
		def signature = key.sign(messageStr.getBytes())
		def signatureStr = Hex.toHexString(signature)

		then:
		signatureStr == "73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e"
	}
}

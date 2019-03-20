package com.hedera.sdk.crypto.ed25519

import spock.lang.Specification

class Ed25519SignatureTest extends Specification {
	static final messageStr = 'This is a message about the world.'

	static final privKeyStrings = [
		// ASN1 encoded hex
		'302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10',
		// raw hex (concatenated private + public key)
		'db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10' +
		'e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7',
		// raw hex (just private key)
		'db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10'
	]

	def "reproducible signature can be computed"() {
		when:
		def key = Ed25519PrivateKey.fromString(keyStr)
		def signature = Ed25519Signature.forMessage(key, messageStr.getBytes())

		then:
		signature.toString() == sigStr

		where:
		keyStr << privKeyStrings
		sigStr << [
			"73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e",
			"73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e",
			"73bea53f31ca9c42a422ecb7516ec08d0bbd1a6bfd630ccf10ec1872454814d29f4a8011129cd007eab544af01a75f508285b591e5bed24b68f927751e49e30e",
		]
	}

	def "signature can be verified"() {
		when:
		def privKey = Ed25519PrivateKey.fromString(keyStr)
		def signature = Ed25519Signature.forMessage(privKey, messageStr.getBytes())

		then:
		signature.verify(privKey.getPublicKey(), messageStr.getBytes())

		where:
		keyStr << privKeyStrings
	}
}

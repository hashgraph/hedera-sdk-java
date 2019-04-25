package com.hedera.hashgraph.sdk.file


import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.hashgraph.sdk.proto.FileGetInfoResponse
import com.hedera.hashgraph.sdk.proto.KeyList
import com.hedera.hashgraph.sdk.proto.Response
import spock.lang.Specification

class FileInfoTest extends Specification {
	def privateKey = Ed25519PrivateKey.generate()
	def publicKey = privateKey.publicKey

	def "won't deserialize from the wrong kind of response"() {
		when:
		new FileInfo(Response.defaultInstance)
		then:
		thrown(IllegalArgumentException)
	}

	def "requires at least one key"() {
		given:
		def response = Response.newBuilder()
				.setFileGetInfo(FileGetInfoResponse.defaultInstance)
				.build()
		when:
		new FileInfo(response)
		then:
		def e = thrown(IllegalArgumentException)
		e.message == "`FileGetInfoResponse` missing keys"
	}

	def "deserializes from a correct response"() {
		given:
		def response = Response.newBuilder()
				.setFileGetInfo(
				FileGetInfoResponse.newBuilder()
				.setFileInfo(FileGetInfoResponse.FileInfo.newBuilder()
				.setSize(1024)
				.setKeys(KeyList.newBuilder().addKeys(publicKey.toKeyProto())))
				).build()

		when:
		def fileInfo = new FileInfo(response)

		then:
		fileInfo.fileId == new FileId(0, 0, 0)
		!fileInfo.deleted
		fileInfo.size == 1024
		fileInfo.keys.get(0).toKeyProto() == publicKey.toKeyProto()
	}
}

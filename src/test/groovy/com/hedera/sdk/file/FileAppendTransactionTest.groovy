package com.hedera.sdk.file

import com.hedera.sdk.account.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileAppendTransactionTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def "empty builder does not validate"() {
		when:
		new FileAppendTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setFileId() required
.setContents() required"""
	}

	def "complete builder does validate"() {
		when:
		def txn = new FileAppendTransaction()
				.setNodeAccountId(new AccountId(3))
				.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z")))
				.setFileId(new FileId(1, 2, 3))
				.setContents([1, 2, 3, 4] as byte[])
				.sign(privateKey)

		then:
		notThrown(IllegalStateException)
	}

	def "complete builder serializes"() {
		when:
		def txn = new FileAppendTransaction()
				.setNodeAccountId(new AccountId(3))
				.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z")))
				.setFileId(new FileId(1, 2, 3))
				.setContents([1, 2, 3, 4] as byte[])
				.sign(privateKey).toProto()

		then:
		txn.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "i\\366\\002\\236\\202%\\027\\217J=U3\\201\\373\\033\\205P\\315n\\301o\\353\\316\\234A\\353\\315\\265Fh\\275u+\\215W\\002\\230\\\\_\\250<\\306\\342\\262\\212d\\321\\003\\210\\344\\3023@\\3557\\320\\265\\337\\313A\\005\\375l\\000"
  }
}
bodyBytes: "\\n\\r\\n\\006\\b\\300\\206\\235\\345\\005\\022\\003\\030\\322\\t\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bx\\202\\001\\016\\022\\006\\b\\001\\020\\002\\030\\003\\"\\004\\001\\002\\003\\004"
"""
	}
}

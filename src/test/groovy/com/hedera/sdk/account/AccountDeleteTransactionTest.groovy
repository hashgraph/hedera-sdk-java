package com.hedera.sdk.account


import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountDeleteTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new AccountDeleteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setTransferAccountId() required
.setDeleteAccountId() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountDeleteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			transferAccountId = new AccountId(4)
			deleteAccountId = new AccountId(1)
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "CB\\337\\205\\355[\\364\\302\\021d\\355\\000\\371\\306\\026[P\\244\\335@7+\\016\\333\\030M\\276\\274S\\022\\276!\\265\\372\\023\\342\\206\\002\\330H^4\\0206h\\227L\\242A\\016\\342\\025\\303c\\021\\030\\332\\372\\257\\231;o\\242\\b"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bxb\\b\\n\\002\\030\\004\\022\\002\\030\\001"
"""
	}
}

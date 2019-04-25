package com.hedera.hashgraph.sdk.file

import com.hedera.hashgraph.sdk.TransactionId
import com.hedera.hashgraph.sdk.account.AccountId
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileUpdateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new FileUpdateTransaction().build()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setFileId()
.addKey()"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new FileUpdateTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			file = new FileId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158728)
			addKey(key.getPublicKey())
			contents = [1, 2, 3, 4, 5]
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\230\\314\\233T\\261\\257\\254\\330\\030Jyz\\270\\221\\217\\324\\233p\\217\\320\\370Q91\\374\\210\\373\\357\\255\$\\000q\\036\\202\\341\\233\\3055m\\232`\\264\$ \\202g\\037\\377\\372\\326-\\311R\\035>\\223\\004E\\354\\002I\\242\\237\\016"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bx\\232\\001=\\n\\006\\b\\001\\020\\002\\030\\003\\022\\006\\b\\210\\251\\212\\345\\005\\032\$\\n\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\"\\005\\001\\002\\003\\004\\005"
"""
	}
}

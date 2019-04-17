package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.file.FileId
import com.hedera.sdk.account.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractUpdateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractUpdateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setContract() required"""
	}

	// FIXME: Can't set adminKey
	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractUpdateTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158571)
			adminKey = key.getPublicKey()
			proxyAccount = new AccountId(10, 11, 12)
			autoRenewPeriod = Duration.ofHours(10)
			file = new FileId(4, 5, 6)
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\332\\366\\021\\322Y\\323\\030A\\301Vj\\275p\\357W\\210\\356\\034l\\v3\\001\\016\\253\\306\\243\\257U\\032\\345\\371Y&\\245d\\221~\\005\\362^\\3520\\210\\v36\\376\\350l\\377*\\315\\026R\\037\\3425\\324\\251<\\022U]\\n"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bxJJ\\n\\006\\b\\001\\020\\002\\030\\003\\022\\006\\b\\353\\247\\212\\345\\005\\032\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\2162\\006\\b\\n\\020\\v\\030\\f:\\004\\b\\240\\231\\002B\\006\\b\\004\\020\\005\\030\\006"
"""
	}
}

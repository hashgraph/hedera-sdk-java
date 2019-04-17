package com.hedera.sdk.account


import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class AccountUpdateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new AccountUpdateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setAccountForUpdate() required
.setKey() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountUpdateTransaction().with(true, {
			setKey(key.getPublicKey())
			nodeAccountId = new AccountId(3)
			transactionId = txId
			accountForUpdate = new AccountId(2)
			proxyAccount = new AccountId(3)
			sendRecordThreshold = 5
			receiveRecordThreshold = 6
			autoRenewPeriod = Duration.ofHours(10)
			expirationTime = Instant.ofEpochSecond(1554158543)
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\037\\371\\353\\t\\224\\356\\202\\b\\236\\354\\334#\\250\\2341N\\237\\017#\\003u\\361&\\265\\256\\302\\352\\033\\206\\220y\\341V\\351jA\\300\\205\\225\\355?@\\221\\317\\025C\\032\\217\\220\\006\\356\\301\\234\\213\\200\\365Bh\\316\\f}]\\245\\004"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bxzB\\022\\002\\030\\002\\032\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216\\"\\002\\030\\003B\\004\\b\\240\\231\\002J\\006\\b\\317\\247\\212\\345\\005Z\\002\\b\\005b\\002\\b\\006"
"""
	}
}

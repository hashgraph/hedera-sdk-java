package com.hedera.hashgraph.sdk.contract

import com.hedera.hashgraph.sdk.TransactionId
import com.hedera.hashgraph.sdk.account.AccountId
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class ContractExecuteTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractExecuteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setContract() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractExecuteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			gas = 10
			amount = 1000
			functionParameters = [424, 243, 141] as byte[]
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "z\\267f\\023/[6xg\\020\\246>\\356\\f\\002h4V\\360\\243q\\a\\343OV\\0052P\\035\$\\355\\177\\344\\023\\271!\\352\\3661\\370a\\337\\362\\244\\2024\\253{o\\002n\\024f\\371\\266\\ar\\375\\206\\253&\\244\\257\\003"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bx:\\022\\n\\006\\b\\001\\020\\002\\030\\003\\020\\n\\030\\350\\a\\"\\003\\250\\363\\215"
"""
	}
}

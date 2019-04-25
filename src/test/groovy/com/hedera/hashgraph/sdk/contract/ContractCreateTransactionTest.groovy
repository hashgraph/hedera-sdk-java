package com.hedera.hashgraph.sdk.contract

import com.hedera.hashgraph.sdk.TransactionId
import com.hedera.hashgraph.sdk.file.FileId
import com.hedera.hashgraph.sdk.account.AccountId
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractCreateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractCreateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setBytecodeFile() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractCreateTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			bytecodeFile = new FileId(1, 2, 3)
			adminKey = key.getPublicKey()
			gas = 10
			initialBalance = 1000
			proxyAccountId = new AccountId(4)
			autoRenewPeriod = Duration.ofHours(7)
			constructorParams = [10, 11, 12, 13, 425] as byte[]
			shard = 20
			realm = 40
			newRealmAdminKey = key.getPublicKey()
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\366\\374B\\345`Z=e\\266\\021\\303z\\221\\234#\\250\\214\\203;\\243]\\230\\2543\\274\\253F\\262\\026\\036\\372:\\240\\323H6\\f\\315\\315\\357\\027\\267\\341`o\\002\\331\\fA\\352\\223a\\0007\\317\\023\\351u\\022\\f\\033\\024\\211\\b"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bxBn\\n\\006\\b\\001\\020\\002\\030\\003\\032\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216 \\n(\\350\\a2\\002\\030\\004B\\004\\b\\360\\304\\001J\\005\\n\\v\\f\\r\\251R\\002\\b\\024Z\\002\\020(b\\"\\022 \\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
"""
	}
}

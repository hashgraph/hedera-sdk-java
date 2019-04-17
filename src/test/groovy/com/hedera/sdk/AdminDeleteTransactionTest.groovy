package com.hedera.sdk

import com.hedera.sdk.account.AccountId
import com.hedera.sdk.contract.ContractId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.file.FileId
import spock.lang.Specification

import java.time.Instant

class AdminDeleteTransactionTest extends Specification {
	def "empty Transaction does not validate"() {
		when:
		new AdminDeleteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setID() required"""
	}

	def "Transaction can be built with FileId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminDeleteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			ID = new FileId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158643)
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "N\\273\\261\\303\\210\\025\\253\\325\\037G+D\\201\\272VD\\301\\003\\214y_\\267/\\353\\201\\\\\\320\\177\\314\\340\\271b\\365C\\222^\\251X\\0041\\374\\236\\021(\\366\\362\\212\\276}M\\016\\201*\\327pX\\222\\v\\222\\037\\301\\2641\\000"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bx\\242\\001\\020\\n\\006\\b\\001\\020\\002\\030\\003\\032\\006\\b\\263\\250\\212\\345\\005"
"""
	}

	def "Transaction can be built with ContractId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminDeleteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			ID = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158643)
		}).sign(key).toProto()

		then:
		tx.toString() == """\
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\3032\\257:>\\031\\232w\\a\\t\\335\\357iO\\301SP\\311iE\\336dM\\330\\262cQ\\347Uj\\270\\376UB\\310\\247\\334\$\\n\\335\\327\\316\\034\\357\\241\\251\\227|\\370\\222\\205\\243\\206X\\336;\\311\\rrR\\224a@\\n"
  }
}
bodyBytes: "\\n\\f\\n\\006\\b\\316\\247\\212\\345\\005\\022\\002\\030\\002\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bx\\242\\001\\020\\022\\006\\b\\001\\020\\002\\030\\003\\032\\006\\b\\263\\250\\212\\345\\005"
"""
	}
}

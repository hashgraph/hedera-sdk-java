package com.hedera.sdk

import com.hedera.sdk.account.AccountId
import com.hedera.sdk.account.CryptoTransferTransaction
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class TransactionReceiptQueryTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def paymentTxn = new CryptoTransferTransaction(null).with {
		nodeAccountId = new AccountId(3)
		transactionId = new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z"))
		addSender(new AccountId(1234), 10000)
		addRecipient(new AccountId(3), 10000)
		sign(privateKey)
	}

	def query = new TransactionReceiptQuery(null)
	.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T11:00:00Z")))
	.setPayment(paymentTxn)

	def builtQuery = query.inner.build()

	def queryString = """\
transactionGetReceipt {
  header {
    payment {
      sigMap {
        sigPair {
          pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
          ed25519: "\\304B\\017\\242d=\\273\\3439\\305\\034\\224\\203#\\\\\\261\\343fa\\002]\\351\\\\\\036\\326\\327\\v\\037\\324\\317~\\020\\2371O\\020j\\377]\\261\\300\\216\\377n\\210\\264\\204?\\320\\001<\\225\\035E\\263&\\244 \\017\\207/\\332\\355\\017"
        }
      }
      bodyBytes: "\\n\\r\\n\\006\\b\\300\\206\\235\\345\\005\\022\\003\\030\\322\\t\\022\\002\\030\\003\\030\\240\\215\\006\\"\\002\\bxr\\027\\n\\025\\n\\t\\n\\003\\030\\322\\t\\020\\237\\234\\001\\n\\b\\n\\002\\030\\003\\020\\240\\234\\001"
    }
  }
  transactionID {
    transactionValidStart {
      seconds: 1554462000
    }
    accountID {
      accountNum: 1234
    }
  }
}
"""

	def "correct query validates"() {
		when:
		query.validate()

		then:
		notThrown(IllegalArgumentException)
	}

	def "incorrect query does not validate"() {
		given:
		def query = new TransactionReceiptQuery()

		when:
		query.validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == "query builder failed validation:\n.setTransactionId() required"
	}

	def "query builds correctly"() {
		when:
		def serialized = builtQuery.toString()

		then:
		serialized == queryString
	}
}

package com.hedera.sdk

import com.hedera.sdk.account.AccountId
import com.hedera.sdk.account.CryptoTransferTransaction
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class TransactionRecordQueryTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def paymentTxn = new CryptoTransferTransaction().with {
		transactionId = new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z"))
		nodeAccountId = new AccountId(3)
		addSender(new AccountId(1234), 10000)
		addRecipient(new AccountId(3), 10000)
		testSign(privateKey)
	}

	def query = new TransactionRecordQuery().with {
		transaction = new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T11:00:00Z"))
		payment = paymentTxn
		return it
	}

	def queryString = """\
transactionGetRecord {
  header {
    payment {
      body {
        transactionID {
          transactionValidStart {
            seconds: 1554465600
          }
          accountID {
            accountNum: 1234
          }
        }
        nodeAccountID {
          accountNum: 3
        }
        transactionFee: 100000
        transactionValidDuration {
          seconds: 120
        }
        cryptoTransfer {
          transfers {
            accountAmounts {
              accountID {
                accountNum: 1234
              }
              amount: -10000
            }
            accountAmounts {
              accountID {
                accountNum: 3
              }
              amount: 10000
            }
          }
        }
      }
      sigs {
        sigs {
          signatureList {
            sigs {
              ed25519: "\\304B\\017\\242d=\\273\\3439\\305\\034\\224\\203#\\\\\\261\\343fa\\002]\\351\\\\\\036\\326\\327\\v\\037\\324\\317~\\020\\2371O\\020j\\377]\\261\\300\\216\\377n\\210\\264\\204?\\320\\001<\\225\\035E\\263&\\244 \\017\\207/\\332\\355\\017"
            }
          }
        }
      }
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
		def query = new TransactionRecordQuery()

		when:
		query.validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == "query builder failed validation:\n.setPayment() required\n.setTransactionId() required"
	}

	def "query builds correctly"() {
		when:
		def builtQuery = query.toProto()

		then:
		builtQuery.toString() == queryString
	}
}

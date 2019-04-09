package com.hedera.sdk

import com.hedera.sdk.account.CryptoTransferTransaction
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class TransactionRecordQueryTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def paymentTxn = new CryptoTransferTransaction().with {
		transactionId = new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z"))
		nodeAccount = new AccountId(3)
		addSender(new AccountId(1234), 10000)
		addRecipient(new AccountId(3), 10000)
		testSign(privateKey)
	}

	def query = new TransactionRecordQuery().with {
		transaction = new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T11:00:00Z"))
		payment = paymentTxn
	}

	def builtQuery = query.toProto()

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
              ed25519: "Y\\267\\231\\330\\a\\036q\\317\\324x\\177\\226\\t\\v\\320\\003\\306K\\233\\266\\320\\204\\257s\\020U\\226\\250\\275\\251\\036\\237\\306\\277GW\\202\\254\\f\\346\\"C\\334a\\263.M\\354Z\\275\\025G\\232\\256)\\301\\203\\\\\\r\\360\\372C\\317\\003"
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
		e.message == "query builder failed validation:\n.setPayment() required\n.setTransaction() required"
	}

	def "query builds correctly"() {
		when:
		def serialized = builtQuery.toString()

		then:
		serialized == queryString
	}
}

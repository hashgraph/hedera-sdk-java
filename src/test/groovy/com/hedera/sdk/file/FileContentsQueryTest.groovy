package com.hedera.sdk.file

import com.hedera.sdk.AccountId
import com.hedera.sdk.FileId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.account.CryptoTransferTransaction
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileContentsQueryTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def paymentTxn = new CryptoTransferTransaction()
	.setNodeAccount(new AccountId(3))
	.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z")))
	.addSender(new AccountId(1234), 10000)
	.addRecipient(new AccountId(3), 10000)
	.testSign(privateKey)

	def query = new FileContentsQuery()
	.setFileId(new FileId(1, 2, 3))
	.setPayment(paymentTxn)

	def builtQuery = query.toProto()

	def queryString = """\
fileGetContents {
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
  fileID {
    shardNum: 1
    realmNum: 2
    fileNum: 3
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
		def query = new FileContentsQuery()

		when:
		query.validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == "query builder failed validation:\n.setPayment() required\n.setFileId() required"
	}

	def "query builds correctly"() {
		when:
		def serialized = builtQuery.toString()

		then:
		serialized == queryString
	}
}

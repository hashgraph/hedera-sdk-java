package com.hedera.sdk.contract

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.account.CryptoTransferTransaction
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class CryptoTransferTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new CryptoTransferTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoTransfer {
    transfers {
    }
  }
}
"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new CryptoTransferTransaction().with(true, {
			transactionId = txId
			addSender(new AccountId(4), 4400)
			addRecipient(new AccountId(55), 150)
			addTransfer(new AccountId(78), 123)
		}).sign(key)

		then:
		tx.build().toString() == """body {
  transactionID {
    transactionValidStart {
      seconds: 1554158542
    }
    accountID {
      accountNum: 2
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
          accountNum: 4
        }
        amount: -4400
      }
      accountAmounts {
        accountID {
          accountNum: 55
        }
        amount: 150
      }
      accountAmounts {
        accountID {
          accountNum: 78
        }
        amount: 123
      }
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\005\\374\\235=\\252\\005 :\\267n\\270\\363a\\300\\246\\235t2\\211\\305\\337\\366)\\b\\303\\3221\\376\\347\\266\\232\\372P\\312{-\\277\\017\\222\\262\\245\\312E\\324(\\374C\\005a\\241\\355^;\\375\\372eb\\2004\\022,;\\341\\a"
      }
    }
  }
}
"""
	}
}

package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountDeleteTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new AccountDeleteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setTransferAccountId() required
.setDeleteAccountId() required"""
	}

	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountCreateTransaction()

		then:
		tx.toProto().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoCreateAccount {
    sendRecordThreshold: 9223372036854775807
    receiveRecordThreshold: 9223372036854775807
    autoRenewPeriod {
      seconds: 2592000
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
		def tx = new AccountDeleteTransaction().with(true, {
			nodeAccount = new AccountId(3)
			transactionId = txId
			transferAccountId = new AccountId(4)
			deleteAccountId = new AccountId(1)
		}).testSign(key).toProto()

		then:
		tx.toString() == """\
body {
  transactionID {
    transactionValidStart {
      seconds: 1554158542
    }
    accountID {
      accountNum: 2
    }
  }
  nodeAccountID {
    accountNum: 3
  }
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoDelete {
    transferAccountID {
      accountNum: 4
    }
    deleteAccountID {
      accountNum: 1
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "CB\\337\\205\\355[\\364\\302\\021d\\355\\000\\371\\306\\026[P\\244\\335@7+\\016\\333\\030M\\276\\274S\\022\\276!\\265\\372\\023\\342\\206\\002\\330H^4\\0206h\\227L\\242A\\016\\342\\025\\303c\\021\\030\\332\\372\\257\\231;o\\242\\b"
      }
    }
  }
}
"""
	}
}

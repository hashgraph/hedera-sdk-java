package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountDeleteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountCreateTransaction()

		then:
		tx.build().toString() == """body {
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
			transactionId = txId
            transferAccountId = new AccountId(4)
            deleteAccountId = new AccountId(1)
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
        ed25519: "?\$D+npb\\306\\360G\\271\\222K\\f\\253\\262\\024\\247\\277\\202/\\035\\362<]M\\303 \\266\\333\\357~\\003b\\'\\314\\236\\322\\204?;\\363\\300\\304\\224\\006p#w\\231\\345\\336\\224\\202\\332\\230\\\\k\\252[\\373lX\\017"
      }
    }
  }
}
"""
    }
}

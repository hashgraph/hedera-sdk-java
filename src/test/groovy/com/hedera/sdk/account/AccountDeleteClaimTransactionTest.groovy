package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountDeleteClaimTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountDeleteClaimTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoDeleteClaim {
  }
}
"""
	}
	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountDeleteClaimTransaction().with(true, { transactionId = txId }).sign(key)

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
  cryptoDeleteClaim {
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "%\\\\\\336\$\\\\{&\\336\\371\\302\\026Vg\\345\\265\\220\\247\\261\\f\\362\\232f5\\355\\241\\326\\335`\\256\\333\\263G\\'\\337\\242&\\026\\031\\006 \\272g\\203\\211\\326\\257\\017\\212\\310\\261\\365D\\227\\263\\370U/\\242I\\262\\204\\335\\324\\n"
      }
    }
  }
}
"""
	}
}

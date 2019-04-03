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
		def tx = new AccountDeleteClaimTransaction().with(true, {
			transactionId = txId
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
  cryptoDeleteClaim {
    accountIDToDeleteFrom {
      accountNum: 1020
    }
    hashToDelete: "\\001\\002\\003\\004\\005\\006"
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\005hzl\\026\\346\\265\\311\\022\\0212H\\376\\271\\026`\\267\\034\\361\\364\\274\\"\\305\\033E_k$f\\331!\$\\vcK\\323\\325\\032\\241\\257\\237\\b\\003\\307\\344\\377\\241\\325\\342\\245\\212\\222p\\bm\\270;w\\206\\316\\336\\325N\\017"
      }
    }
  }
}"""
	}
}

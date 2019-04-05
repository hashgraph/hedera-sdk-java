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
			accountToDeleteFrom = new AccountId(4)
			hashToDelete = [4, 2, 1, 5]
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
      accountNum: 4
    }
    hashToDelete: "\\004\\002\\001\\005"
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\023\\346\\243\\210\\302\\003\\315h\\355\\030\\271\\364\\364\\204\\265\\212\\b\\021\\311\\f.\\244\\211l\\261\\027\\3123\\247\\324\\330\\323F\\203\\362\\3312\\221I\\033\\032\\347\\267n9\\266\\340\\016~\\247\\210Pj\\267^\\265=do:|!\\235\\v"
      }
    }
  }
}
"""
	}
}

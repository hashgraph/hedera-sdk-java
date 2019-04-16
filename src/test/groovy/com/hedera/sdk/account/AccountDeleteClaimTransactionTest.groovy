package com.hedera.sdk.account


import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountDeleteClaimTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new AccountDeleteClaimTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setAccountToDeleteFrom() required
.setHashToDelete() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountDeleteClaimTransaction().with(true, {
			transactionId = txId
			nodeAccountId = new AccountId(3)
			accountToDeleteFrom = new AccountId(4)
			hashToDelete = [4, 2, 1, 5]
		}).testSign(key)

		then:
		tx.toProto().toString() == """\
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
        ed25519: "\\2167\\344\\314\\366\\213\\271\\274\\267\\207\\337\\222\\371}\\332D\\r\\217k\\351\\200w@\\3353`\\031A\\215h\\352:\\226\\027p]\\313\\216\\024\\225\\373\\337\\330\\024\\033\\214_5\\255\\247\\330`\\247U\\371\\253\\233\\213\\324\\361\\207}\\357\\t"
      }
    }
  }
}
"""
	}
}

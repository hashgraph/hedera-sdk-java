package com.hedera.sdk.account

import com.hedera.sdk.AccountId
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
		e.message == """"""
	}

	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountDeleteClaimTransaction()

		then:
		tx.toProto().toString() == """body {
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
			nodeAccount = new AccountId(3)
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
        ed25519: "\\026\\nq\\314\\212-~\\363\\363?\\210Jj\\314\\256\\214\\244|\\030\\205of8\\226*0\\304\\312x\\242&\\336\\3443\\205Dx\\366\\252\\340\\352\\303\\254\\316]R\\333fU+\\255\\274\\356\\326\\314\\211\\272\\320\\201\\027\\347\\3205\\005"
      }
    }
  }
}
"""
	}
}

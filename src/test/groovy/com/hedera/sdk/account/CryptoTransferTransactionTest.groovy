package com.hedera.sdk.account


import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class CryptoTransferTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new CryptoTransferTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
at least one transfer required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new CryptoTransferTransaction().with(true, {
			transactionId = txId
			nodeAccountId = new AccountId(2)
			addSender(new AccountId(4), 800)
			addRecipient(new AccountId(55), 400)
			addTransfer(new AccountId(78), 400)
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
    accountNum: 2
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
        amount: -800
      }
      accountAmounts {
        accountID {
          accountNum: 55
        }
        amount: 400
      }
      accountAmounts {
        accountID {
          accountNum: 78
        }
        amount: 400
      }
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\223\\347\\326q\\3477\\303\\307g+\\207\\252\\251N=},v\\337\\231i\\r\\201\\276Qn\\277\\227\\261\\257\\332\\201a\\003[\\320\\036\\310\\"\\353\\0338@\\216\\331w\\302\\300\\260\\276\\221\\330g\\300~\\234z\\247\\331\\031\\032\\300\\026\\006"
      }
    }
  }
}
"""
	}
}

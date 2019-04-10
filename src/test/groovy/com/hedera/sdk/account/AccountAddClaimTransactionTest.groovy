package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountAddClaimTransactionTest extends Specification {
	def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def account = new AccountId(1000)

	def hash = [1, 2, 2, 3, 3, 3] as byte[]

	def tx = new AccountAddClaimTransaction().with {
		setNodeAccount(new AccountId(3))
		setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-08T07:04:00Z")))
		setAccount(account)
		setHash(hash)
		addKey(key.publicKey)
	}

	def txString = """\
body {
  transactionID {
    transactionValidStart {
      seconds: 1554707040
    }
    accountID {
      accountNum: 1234
    }
  }
  nodeAccountID {
    accountNum: 3
  }
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoAddClaim {
    accountID {
      accountNum: 1000
    }
    claim {
      accountID {
        accountNum: 1000
      }
      hash: "\\001\\002\\002\\003\\003\\003"
      keys {
        keys {
          ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
        }
      }
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\023\\242\\274\\a\\320I2R\\234\\\\\\253\\230Dr\\230?M\\273Fsf\\322\\001i7\\002m\\376\\331\$vae1\\326+\\267J\\006\\251\\002jh\\271c!\\bG\\213\\374\\225S\\377\\033\\302,\\233\\207\\210\\017n\\314%\\001"
      }
    }
  }
}
"""

	def "correct transaction validates"() {
		when:
		tx.validate()

		then:
		notThrown(IllegalArgumentException)
	}

	def "incorrect transaction does not validate"() {
		given:
		def tx = new AccountAddClaimTransaction()

		when:
		tx.validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setAccount() required
.setHash() required
.addKey() required"""
	}

	def "transaction builds correctly"() {
		when:
		def builtTx = tx.testSign(key).toProto()

		then:
		builtTx.toString() == txString
	}
}

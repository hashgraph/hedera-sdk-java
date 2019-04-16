package com.hedera.sdk.file

import com.hedera.sdk.TransactionId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileCreateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new FileCreateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.addKey() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new FileCreateTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			expirationTime = Instant.ofEpochSecond(1554158728)
			addKey(key.getPublicKey())
			contents = [1, 2, 3, 4, 5]
			newRealmAdminKey = key.getPublicKey()
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
  fileCreate {
    expirationTime {
      seconds: 1554158728
    }
    keys {
      keys {
        ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
      }
    }
    contents: "\\001\\002\\003\\004\\005"
    newRealmAdminKey {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\'\\"j\\340\\340\\270kil\\246\\310\\022gpYH,C\\v\\312\\357oT\\316\\301Q\\326\\251\\020t\\240\\375\\307\\020\\340 \\205d\\262\\210\\240!\\226?\\fw^\\345\\246oA\\311W\\004\\260\\263\\235z\\374\\222lB\\370\\004"
      }
    }
  }
}
"""
	}
}

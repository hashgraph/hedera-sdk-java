package com.hedera.sdk.file

import com.hedera.sdk.TransactionId;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.FileId;
import com.hedera.sdk.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.crypto.ed25519.Ed25519PublicKey
import spock.lang.Specification

import java.time.Instant

class FileCreateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new FileCreateTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  fileCreate {
    keys {
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
		def tx = new FileCreateTransaction().with(true, {
			transactionId = txId
			expirationTime = Instant.ofEpochSecond(1554158728)
			addKey(key.getPublicKey())
			contents = [1, 2, 3, 4, 5]
			newRealmAdminKey = key.getPublicKey()
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
        ed25519: "\\225\\247o\\210(\\341P=\\356\\fy\\217\\004\\367L\\274\\2507\\201\\004\\260\\254\\324\\250\\271s\\367\\r\\347:y\\264\\272\\353RK\\206\\304\\374\\243\\325@\\303\\264E\\327\\rop{k\\215\\205_\\025\\017\\310\\b\\rV\\230\\303v\\f"
      }
    }
  }
}
"""
	}
}

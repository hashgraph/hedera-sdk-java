package com.hedera.sdk.file

import com.hedera.sdk.TransactionId
import com.hedera.sdk.FileId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileUpdateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new FileUpdateTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  fileUpdate {
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
		def tx = new FileUpdateTransaction().with(true, {
			transactionId = txId
			file = new FileId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158728)
			addKey(key.getPublicKey())
			contents = [1, 2, 3, 4, 5]
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
  fileUpdate {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
    expirationTime {
      seconds: 1554158728
    }
    keys {
      keys {
        ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
      }
    }
    contents: "\\001\\002\\003\\004\\005"
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "`xB1K\\362\\316\\265T\\203\\3666\\212S\\"\\215\\210\\201w\\372\\363,\\317\\324U\\355\\215s\\201\\302l?L\\260\\021\\324\\271rE\\321\\247\\253\\251\\2147^#>\\267\\243\\244L\\271\\225\\030\\374\\274\\373W#\\3111+\\a"
      }
    }
  }
}
"""
	}
}

package com.hedera.sdk

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AdminUndeleteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AdminUndeleteTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  adminUndelete {
  }
}
"""
    }

	def "Transaction can be built with FileId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminUndeleteTransaction().with(true, {
			transactionId = txId
            ID = new FileId(1, 2, 3)
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
  adminUndelete {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\317\\262\\375\\311v\\232\\266\\306g\\001\\251p\\035\\234\\252\\260\\a\\004z\\275\\356J\\026\\234\\346[f\\216\\215\\025\\221a\\370%\\035\\006\\226\\261m\\363S\\025\\027\\261\\360\\302C\\2005\\004\\321\\316\\341\\v-O\\373\\255\\354\\370\\021\\030\\021\\002"
      }
    }
  }
}
"""
    }

	def "Transaction can be built with ContractId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminUndeleteTransaction().with(true, {
			transactionId = txId
            ID = new ContractId(1, 2, 3)
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
  adminUndelete {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\231\\a\\230;Mr\\264]\\031J\\345^j\\r\\346\\373B\\326\\211\\034\\031]\\033a\\217e]\\344BA\\030\\270\\275\\303\\314\\005\\337y7\\325l\\253+JV\\367\\304\\361\\372\\357\\212\\347\\346+\\307yP\\"\\204D\\347\\026\\225\\v"
      }
    }
  }
}
"""
    }
}

package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.ContractId
import com.hedera.sdk.FileId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractUpdateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractUpdateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setContract() required"""
	}

	// FIXME: Can't set adminKey
	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractUpdateTransaction().with(true, {
			nodeAccount = new AccountId(3)
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158571)
			adminKey = key.getPublicKey()
			proxyAccount = new AccountId(10, 11, 12)
			autoRenewPeriod = Duration.ofHours(10)
			file = new FileId(4, 5, 6)
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
  contractUpdateInstance {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
    expirationTime {
      seconds: 1554158571
    }
    adminKey {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
    proxyAccountID {
      shardNum: 10
      realmNum: 11
      accountNum: 12
    }
    autoRenewPeriod {
      seconds: 36000
    }
    fileID {
      shardNum: 4
      realmNum: 5
      fileNum: 6
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\332\\366\\021\\322Y\\323\\030A\\301Vj\\275p\\357W\\210\\356\\034l\\v3\\001\\016\\253\\306\\243\\257U\\032\\345\\371Y&\\245d\\221~\\005\\362^\\3520\\210\\v36\\376\\350l\\377*\\315\\026R\\037\\3425\\324\\251<\\022U]\\n"
      }
    }
  }
}
"""
	}
}

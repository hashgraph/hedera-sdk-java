package com.hedera.sdk.file

import spock.lang.Specification
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.account.AccountId
import com.hedera.sdk.TransactionId
import java.time.Instant

class FileDeleteTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new FileDeleteTransaction().validate()
		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setFileId()"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new FileDeleteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			fileId = new FileId(848, 973, 1234)
		}).sign(key).toProto()

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
  fileDelete {
    fileID {
      shardNum: 848
      realmNum: 973
      fileNum: 1234
    }
  }
}
sigMap {
  sigPair {
    pubKeyPrefix: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    ed25519: "\\231\\257\\205\\311\\2437^g\\030/\$\\346\\314\\332V\\\\\\2103\\240+\\252\\275\\263\\250=\\037\\260\\370\\342P^kf\\022ae\\346\\024\\314\\025Z\\374\\267\\301B\\301\\311\\202\\267\\fuWm%\\365\\256\\350P2By\\377o\\t"
  }
}
"""
	}
}

package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdUtilTest {
    @Test
    @DisplayName("parses id string correctly")
    void testParseString() {
        long[] correctArray = {0,0,0};
        long[] returnedArray = IdUtil.parseIdString("0.0.0");
        assertEquals( correctArray[0], returnedArray[0]);
        assertEquals( correctArray[1], returnedArray[1]);
        assertEquals( correctArray[2], returnedArray[2]);
    }

    @Test
    @DisplayName("creates account id from string correctly")
    void testAccountIdFromString() {
        AccountId accountFromString = AccountId.fromString("0.0.400");
        assertEquals( 0, accountFromString.getRealmNum());
        assertEquals( 0, accountFromString.getShardNum());
        assertEquals( 400, accountFromString.getAccountNum());
    }

    @Test
    @DisplayName("creates contract id from string correctly")
    void testContractIdFromString() {
        ContractId contractFromString = ContractId.fromString("0.0.400");
        assertEquals( 0, contractFromString.getRealmNum());
        assertEquals( 0, contractFromString.getShardNum());
        assertEquals( 400, contractFromString.getContractNum());
    }

    @Test
    @DisplayName("creates file id from string correctly")
    void testFileIdFromString() {
        FileId fileFromString = FileId.fromString("0.0.400");
        assertEquals( 0, fileFromString.getRealmNum());
        assertEquals( 0, fileFromString.getShardNum());
        assertEquals( 400, fileFromString.getFileNum());
    }
}

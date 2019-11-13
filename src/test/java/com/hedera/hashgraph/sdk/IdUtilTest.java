package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class IdUtilTest {

    @Test
    @DisplayName("creates account id from string correctly")
    void testAccountIdFromString() {
        AccountId accountFromString = AccountId.fromString("1.2.400");
        assertEquals(1, accountFromString.shard);
        assertEquals(2, accountFromString.realm);
        assertEquals(400, accountFromString.account);
    }

    @Test
    @DisplayName("creates contract id from string correctly")
    void testContractIdFromString() {
        ContractId contractFromString = ContractId.fromString("0.0.400");
        assertEquals(1, contractFromString.shard);
        assertEquals(2, contractFromString.realm);
        assertEquals(400, contractFromString.contract);
    }

    @Test
    @DisplayName("creates file id from string correctly")
    void testFileIdFromString() {
        FileId fileFromString = FileId.fromString("0.0.400");
        assertEquals( 0, fileFromString.getRealmNum());
        assertEquals( 0, fileFromString.getShardNum());
        assertEquals( 400, fileFromString.getFileNum());
    }

    @Test
    @DisplayName("incorrect account id from string should fail")
    void testBadAccountIdFromStringFails() {
        final String message = "Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}";
        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> IdUtil.parseIdString("a.0.400", AccountId::new)).getMessage());
    }

    @Test
    @DisplayName("incorrect contract id from string should fail")
    void testBadContractIdFromStringFails() {
        final String message = "Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}";
        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> IdUtil.parseIdString("0.!.400", ContractId::new)).getMessage());
    }

    @Test
    @DisplayName("incorrect file id from string should fail")
    void testBadFileIdFromStringFails() {
        final String message = "Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}";
        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> IdUtil.parseIdString("0.1", FileId::new)).getMessage());
    }


}

package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolidityUtilTest {

    @Test
    void testAddressFor() {
        assertEquals(
            "0000000000000000000000000000000000000400",
            SolidityUtil.addressForEntity(0, 0, 1024));

        assertEquals(
            "0000000100000000000000ff0000000000000400",
            SolidityUtil.addressForEntity(1, 255, 1024));

        assertEquals(
            "0000000000000000000000000000000000000400",
            new AccountId(0, 0, 1024).toSolidityAddress());

        assertEquals(
            "0000000100000000000000ff0000000000000400",
            new AccountId(1, 255, 1024).toSolidityAddress());

        assertEquals(
            "0000000000000000000000000000000000000400",
            new ContractId(0, 0, 1024).toSolidityAddress());

        assertEquals(
            "0000000100000000000000ff0000000000000400",
            new ContractId(1, 255, 1024).toSolidityAddress());

        assertEquals(
            "0000000000000000000000000000000000000400",
            new FileId(0, 0, 1024).toSolidityAddress());

        assertEquals(
            "0000000100000000000000ff0000000000000400",
            new FileId(1, 255, 1024).toSolidityAddress());

        // shard num will be truncated
        assertThrows(
            IllegalArgumentException.class,
            () -> SolidityUtil.addressForEntity(1L << 33, 0, 0));
    }

    @Test
    void testParseAddress() {
        assertEquals(
            new AccountId(0, 0, 1024),
            AccountId.fromSolidityAddress("0000000000000000000000000000000000000400"));

        assertEquals(
            new AccountId(1, 255, 1024),
            AccountId.fromSolidityAddress("0000000100000000000000ff0000000000000400"));

        assertEquals(
            new ContractId(0, 0, 1024),
            ContractId.fromSolidityAddress("0000000000000000000000000000000000000400"));

        assertEquals(
            new ContractId(1, 255, 1024),
            ContractId.fromSolidityAddress("0000000100000000000000ff0000000000000400"));

        assertEquals(
            new FileId(0, 0, 1024),
            FileId.fromSolidityAddress("0000000000000000000000000000000000000400"));

        assertEquals(
            new FileId(1, 255, 1024),
            FileId.fromSolidityAddress("0000000100000000000000ff0000000000000400"));

        assertThrows(
            IllegalArgumentException.class,
            // incorrect length
            () -> AccountId.fromSolidityAddress("blahblahblah"));

        assertThrows(
            IllegalArgumentException.class,
            // correct length but incorrectly formatted
            () -> AccountId.fromSolidityAddress("blahblahblahzzzzzzzzzzzzzzzzzzzzzzzzzzzz"));

        assertThrows(
            IllegalArgumentException.class,
            // incorrect length
            () -> ContractId.fromSolidityAddress("blahblahblah"));

        assertThrows(
            IllegalArgumentException.class,
            // correct length but incorrectly formatted
            () -> ContractId.fromSolidityAddress("blahblahblahzzzzzzzzzzzzzzzzzzzzzzzzzzzz"));

        assertThrows(
            IllegalArgumentException.class,
            // incorrect length
            () -> FileId.fromSolidityAddress("blahblahblah"));

        assertThrows(
            IllegalArgumentException.class,
            // correct length but incorrectly formatted
            () -> FileId.fromSolidityAddress("blahblahblahzzzzzzzzzzzzzzzzzzzzzzzzzzzz"));
    }
}

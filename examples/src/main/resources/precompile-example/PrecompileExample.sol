// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.5.0 <0.9.0;
pragma experimental ABIEncoderV2;

import "./ExpiryHelper.sol";
import "./PrngSystemContract.sol";

// To alter the behavior of the SolidityPrecompileExample, re-compile this solidity file
// (you will also need the other files in this directory)
// and copy the outputted json file to ./PrecompileExample.json

//[X] prng
//[X] create fungible
//[X] mint fungible
//[ ] associate fungible
//[ ] transfer fungible
//[ ] approve fungible allowance
//[ ] spend fungible allowance
//[ ] pause fungible
//[ ] unpause fungible
//[ ] freeze fungible
//[ ] unfreeze fungible
//[X] create NFT
//[ ] mint NFT
//[ ]transfer NFT
//[ ] approve NFT allowance
//[ ] spend NFT allowance
//
//burn?
//KYC?

// TODO: reorder steps to match this list

contract PrecompileExample is ExpiryHelper, PrngSystemContract {
    address payable owner;
    address payable aliceAccount;
    address payable bobAccount;
    address fungibleToken;
    address nftToken;

    constructor(address payable _owner, address payable _aliceAccount, address payable _bobAccount) {
        owner = _owner;
        aliceAccount = _aliceAccount;
        bobAccount = _bobAccount;
    }

    function step0() external returns (bytes32 result) {
        require(msg.sender == owner);

        result = this.getPseudorandomSeed();
    }

    // In order for some functions (such as createFungibleToken) to work, the contract must possess the funds for
    // the function call.  We are using ContractExecuteTransaction.setPayableAmount() to transfer some Hbar
    // to the contract's account at each step (which means this function must be payable), and then transferring
    // the excess Hbar back to the owner at the end of each step.
    function step1() external payable returns (int responseCode) {
        require(msg.sender == owner);

        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](1);
        // Set the admin key and the supply key to the key of the account that executed function (INHERIT_ACCOUNT_KEY).
        keys[0] = createSingleKey(ADMIN_KEY_TYPE | SUPPLY_KEY_TYPE, INHERIT_ACCOUNT_KEY, bytes(""));

        (responseCode, fungibleToken) = createFungibleToken(
            IHederaTokenService.HederaToken(
                "Example Fungible token", // name
                "E", // symbol
                owner, // treasury
                "memo",
                true, // supply type, false -> INFINITE, true -> FINITE
                1000, // max supply
                false, // freeze default (setting to false means that this token will not be initially frozen on creation)
                keys, // the keys for the new token
            // auto-renew fee paid by aliceAccount every 7,000,000 seconds (approx. 81 days).
            // This is the minimum auto renew period.
                createAutoRenewExpiry(aliceAccount, 7000000)
            ),
            100, // initial supply
            0 // decimals
        );

        // send any excess Hbar back to the owner
        owner.transfer(address(this).balance);
    }

    function step2() external returns (int responseCode) {
        require(msg.sender == owner);

        uint64 mintedCount;
        int64[] memory mintedSerials; // applicable to NFT tokens only
        (responseCode, mintedCount, mintedSerials) = mintToken(
            fungibleToken,
            100, // amount (applicable to fungible tokens only)
            new bytes[](0) // metadatas (applicable to NFT tokens only)
        );
    }

    function step3() external returns (int responseCode) {
        require(msg.sender == owner);

        responseCode = associateToken(aliceAccount, fungibleToken);
    }

    function step4() external returns (int responseCode) {
        require(msg.sender == owner);

        responseCode = transferToken(
            fungibleToken,
            owner, // sender
            aliceAccount, // receiver
            100 // amount to transfer
        );
    }

    function step11(bytes memory keyBytes) external payable returns (int responseCode) {
        require(msg.sender == owner);

        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](1);
        // Set the admin key and the supply key to given ED25519 public key bytes.
        // These must be the key's raw bytes acquired via key.toBytesRaw()
        keys[0] = createSingleKey(ADMIN_KEY_TYPE | SUPPLY_KEY_TYPE, ED25519_KEY, keyBytes);

        IHederaTokenService.FixedFee[] memory fixedFees = new IHederaTokenService.FixedFee[](1);
        // Create a fixed fee of 1 Hbar (100,000,000 tinybar) that is collected by owner
        fixedFees[0] = createFixedFeeForHbars(100000000, owner);

        (responseCode, nftToken) = createNonFungibleTokenWithCustomFees(
            IHederaTokenService.HederaToken(
                "Example NFT token", // name
                "ENFT", // symbol
                owner, // treasury
                "memo",
                true, // supply type, false -> INFINITE, true -> FINITE
                1000, // max supply
                false, // freeze default (setting to false means that this token will not be initially frozen on creation)
                keys, // the keys for the new token
            // auto-renew fee paid by aliceAccount every 7,000,000 seconds (approx. 81 days).
            // This is the minimum auto renew period.
                createAutoRenewExpiry(aliceAccount, 7000000)
            ),
            fixedFees,
            new IHederaTokenService.RoyaltyFee[](0)
        );

        // send any excess Hbar back to the owner
        owner.transfer(address(this).balance);
    }
}


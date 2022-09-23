// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.5.0 <0.9.0;
pragma experimental ABIEncoderV2;

import "./ExpiryHelper.sol";
import "./PrngSystemContract.sol";

// To alter the behavior of the SolidityPrecompileExample, re-compile this solidity file
// (you will also need the other files in this directory)
// and copy the outputted json file to ./PrecompileExample.json

contract PrecompileExample is ExpiryHelper, PrngSystemContract {
    address payable owner;
    address payable aliceAccount;
    address fungibleToken;
    address nftToken;

    constructor(address payable _owner, address payable _aliceAccount) {
        owner = _owner;
        aliceAccount = _aliceAccount;
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
                address(this), // treasury
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

    function step12(bytes[] memory metadatas) external returns (int responseCode) {
        require(msg.sender == owner);
        require(metadatas.length == 3);

        uint64 mintedCount;
        int64[] memory mintedSerials; // applicable to NFT tokens only
        (responseCode, mintedCount, mintedSerials) = mintToken(
            nftToken,
            0, // amount (applicable to fungible tokens only)
            metadatas // (applicable to NFT tokens only)
        );

        require(mintedCount == 3);
        require(mintedSerials.length == 3);
        require(mintedSerials[0] == 1);
        require(mintedSerials[1] == 2);
        require(mintedSerials[2] == 3);
    }

    function step13() external returns (int responseCode) {
        require(msg.sender == owner);

        responseCode = associateToken(aliceAccount, nftToken);
    }

    function step14() external returns (int responseCode) {
        require(msg.sender == owner);

        // You may also use transferNFTs to transfer more than one serial number at a time

        responseCode = transferNFT(
            nftToken,
            address(this), // sender
            aliceAccount, // receiver
            1 // serial number
        );
    }

    function step15() external returns (address ownerAddress) {
        require(msg.sender == owner);

        int responseCode;
        IHederaTokenService.NonFungibleTokenInfo memory info;
        (responseCode, info) = getNonFungibleTokenInfo(nftToken, 1);
        ownerAddress = info.ownerId;
    }
}


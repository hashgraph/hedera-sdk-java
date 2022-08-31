// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.0 <0.9.0;
pragma experimental ABIEncoderV2;

// This file was copied from github.com/hashgraph/hedera-smart-contracts on Aug 31 2022

import "./HederaTokenService.sol";
import "./IHederaTokenService.sol";
import "./HederaResponseCodes.sol";
import "./ExpiryHelper.sol";

/// This is a notional example of how the functions in HIP-358 could be used.
/// It is non-normative.
contract TokenCreateContract is ExpiryHelper {

    using Bits for uint;

    // create a fungible Token with no custom fees, with calling contract as
    // admin key, passed ED25519 key as supply and pause key.
    function createFungible(
        bytes memory ed25519Key,
        address autoRenewAccount,
        uint32 autoRenewPeriod
    ) external payable returns (address createdTokenAddress) {

        // instantiate the list of keys we'll use for token create
        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](2);

        // use the helper methods in KeyHelper to create basic keys
        keys[0] = createSingleKey(HederaTokenService.ADMIN_KEY_TYPE, KeyHelper.INHERIT_ACCOUNT_KEY, "");

        // create TokenKey of types supplyKey and pauseKey with value a contract address passed as function arg
        uint supplyPauseKeyType;
        IHederaTokenService.KeyValue memory supplyPauseKeyValue;
        // turn on bits corresponding to supply and pause key types
        supplyPauseKeyType = supplyPauseKeyType.setBit(4);
        supplyPauseKeyType = supplyPauseKeyType.setBit(6);
        // set the value of the key to the ed25519Key passed as function arg
        supplyPauseKeyValue.ed25519 = ed25519Key;
        keys[1] = IHederaTokenService.TokenKey (supplyPauseKeyType, supplyPauseKeyValue);

        IHederaTokenService.HederaToken memory myToken;
        myToken.name = "MyToken";
        myToken.symbol = "MTK";
        myToken.treasury = address(this);
        myToken.tokenKeys = keys;
        // create the expiry schedule for the token using ExpiryHelper
        myToken.expiry = createAutoRenewExpiry(autoRenewAccount, autoRenewPeriod);

        // call HTS precompiled contract, passing 200 as initial supply and 8 as decimals
        (int responseCode, address token) =
                HederaTokenService.createFungibleToken(myToken, 200, 8);

        if (responseCode != HederaResponseCodes.SUCCESS) {
            revert ();
        }

        createdTokenAddress = token;
    }

    // create fungible token with custom fees, with an ECDSA key as admin key
    function createFungibleWithFees(
        bytes memory ecdsaAdminKey,
        address treasury,
        address feeCollector,
        address existingTokenAddress,
        address autoRenewAccount,
        uint32 autoRenewPeriod
    ) external payable returns (address createdTokenAddress) {

        // create the admin key using KeyHelper method
        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](1);
        keys[0] = createSingleKey(HederaTokenService.ADMIN_KEY_TYPE, KeyHelper.ECDSA_SECPK2561K1_KEY, ecdsaAdminKey);

        // declare custom fees
        IHederaTokenService.FixedFee[] memory fixedFees = new IHederaTokenService.FixedFee[](2);
        // create a fixed fee with hbar as payment using FeeHelper 
        fixedFees[0] = createFixedHbarFee(5, feeCollector);
        // create a fixed fee with existing token as payment using FeeHelper 
        fixedFees[1] = createFixedTokenFee(5, existingTokenAddress, feeCollector);

        IHederaTokenService.FractionalFee[] memory fractionalFees = new IHederaTokenService.FractionalFee[](1);
        // create a fractional fee without limits using FeeHelper
        fractionalFees[0] = createFractionalFee(4, 5, true, feeCollector);

        IHederaTokenService.HederaToken memory myToken;
        myToken.name = "MyToken";
        myToken.symbol = "MTK";
        myToken.treasury = treasury;
        myToken.tokenKeys = keys;
        // create the expiry schedule for the token using ExpiryHelper
        myToken.expiry = createAutoRenewExpiry(autoRenewAccount, autoRenewPeriod);

        (int responseCode, address token) =
                HederaTokenService.createFungibleTokenWithCustomFees(myToken, 200, 8, fixedFees, fractionalFees);

        if (responseCode != HederaResponseCodes.SUCCESS) {
            revert ();
        }

        createdTokenAddress = token;
    }

    // create an NFT without fees with contract as freeze key
    function createNonFungibleToken(
        address contractFreezeKey,
        address autoRenewAccount,
        uint32 autoRenewPeriod
    ) external payable returns (address createdTokenAddress) {

        // instantiate the list of keys we'll use for token create
        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](1);
        // use the helper methods in KeyHelper to create basic key
        keys[0] = createSingleKey(HederaTokenService.FREEZE_KEY_TYPE, KeyHelper.CONTRACT_ID_KEY, contractFreezeKey);

        IHederaTokenService.HederaToken memory myToken;
        myToken.name = "MyNFT";
        myToken.symbol = "MNFT";
        myToken.memo = "memo";
        myToken.treasury = address(this);
        myToken.tokenSupplyType = true; // make the total supply FINITE
        myToken.maxSupply = 10;
        myToken.tokenKeys = keys;
        myToken.freezeDefault = true;
        myToken.expiry = createAutoRenewExpiry(autoRenewAccount, autoRenewPeriod);

        (int responseCode, address token) =
                HederaTokenService.createNonFungibleToken(myToken);

        if (responseCode != HederaResponseCodes.SUCCESS) {
            revert ();
        }

        createdTokenAddress = token;
    }

    // create NFT with royalty fees, contract has the mint and admin key
    function createNonFungibleTokenWithCustomFees(
        address contractIdKey,
        address feeCollectorAndTreasury,
        address existingTokenAddress,
        address autoRenewAccount,
        uint32 autoRenewPeriod
    ) external payable returns (address createdTokenAddress) {

        // TokenKey of type adminKey and supplyKey with value this contract id
        uint adminSupplyKeyType;
        adminSupplyKeyType = adminSupplyKeyType.setBit(0); // turn on bit corresponding to admin key type
        adminSupplyKeyType = adminSupplyKeyType.setBit(4); // turn on bit corresponding to supply key type
        IHederaTokenService.KeyValue memory adminSupplyKeyValue;
        adminSupplyKeyValue.contractId = contractIdKey;

        // instantiate the list of keys we'll use for token create
        IHederaTokenService.TokenKey[] memory keys = new IHederaTokenService.TokenKey[](1);
        keys[0] = IHederaTokenService.TokenKey (adminSupplyKeyType, adminSupplyKeyValue);

        // declare fees
        IHederaTokenService.RoyaltyFee[] memory royaltyFees = new IHederaTokenService.RoyaltyFee[](3);
        royaltyFees[0] = createRoyaltyFeeWithoutFallback(4, 5, feeCollectorAndTreasury);
        royaltyFees[1] = createRoyaltyFeeWithHbarFallbackFee(4, 5, 50, feeCollectorAndTreasury);
        royaltyFees[2] =
                createRoyaltyFeeWithTokenDenominatedFallbackFee(4, 5, 30, existingTokenAddress, feeCollectorAndTreasury);

        IHederaTokenService.HederaToken memory myToken;
        myToken.name = "MyNFT";
        myToken.symbol = "MNFT";
        myToken.treasury = feeCollectorAndTreasury;
        myToken.tokenKeys = keys;
        myToken.expiry = createAutoRenewExpiry(autoRenewAccount, autoRenewPeriod);

        // create the token through HTS with default expiry and royalty fees;
        (int responseCode, address token) =
                HederaTokenService.createNonFungibleTokenWithCustomFees(
                    myToken,
                    new IHederaTokenService.FixedFee[](0),
                    royaltyFees
                );

        if (responseCode != HederaResponseCodes.SUCCESS) {
            revert ();
        }

        createdTokenAddress = token;
    }

     function createTokenWithDefaultExpiryAndEmptyKeys() public payable returns (address createdTokenAddress) {
        IHederaTokenService.HederaToken memory token;
        token.name = "name";
        token.symbol = "symbol";
        token.treasury = address(this);

        (int responseCode, address tokenAddress) =
        HederaTokenService.createFungibleToken(token, 200, 8);

        if (responseCode != HederaResponseCodes.SUCCESS) {
            revert ();
        }

        createdTokenAddress = tokenAddress;
    }
}

library Bits {

    uint constant internal ONE = uint(1);

    // Sets the bit at the given 'index' in 'self' to '1'.
    // Returns the modified value.
    function setBit(uint self, uint8 index) internal pure returns (uint) {
        return self | ONE << index;
    }
}

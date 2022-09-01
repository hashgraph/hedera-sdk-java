// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.5.0 <0.9.0;
pragma experimental ABIEncoderV2;

import "./ExpiryHelper.sol";
import "./PrngSystemContract.sol";

//[X] prng
//[X] create fungible
//[ ] create NFT
//[ ] mint fungible
//[ ] mint NFT
//[ ] associate
//[ ] transfer
//[ ] approve fungible allowance
//[ ] approve NFT allowance
//[ ] pause
//[ ] unpause
//[ ] freeze
//[ ] unfreeze
//
//KYC?

contract PrecompileExample is ExpiryHelper, PrngSystemContract {
    address owner;
    address payable otherAccount;
    address fungibleToken;

    constructor(address payable _otherAccount) {
        owner = msg.sender;
        otherAccount = _otherAccount;
    }

    function step0() external returns (bytes32 result) {
        require(msg.sender == owner);

        result = this.getPseudorandomSeed();
    }

    // In order for some functions (such as createFungibleToken) to work, the function that is calling them must be
    // payable, and payment must be provided via ContractExecuteTransaction.setPayableAmount().
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
                // auto-renew fee paid by otherAccount every 7,000,000 seconds (approx. 81 days).
                // This is the minimum auto renew period.
                createAutoRenewExpiry(otherAccount, 7000000)
            ),
            100, // initial supply
            0 // decimals
        );
    }
}


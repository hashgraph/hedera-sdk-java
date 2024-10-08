// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.5.0 <0.9.0;
pragma experimental ABIEncoderV2;

// This file was copied from github.com/hashgraph/hedera-smart-contracts on Aug 7 2024

import "./HederaTokenService.sol";

abstract contract ExpiryHelper {
    function createAutoRenewExpiry(
        address autoRenewAccount,
        int64 autoRenewPeriod
    ) internal pure returns (IHederaTokenService.Expiry memory expiry) {
        expiry.autoRenewAccount = autoRenewAccount;
        expiry.autoRenewPeriod = autoRenewPeriod;
    }

    function createSecondExpiry(
        int64 second
    ) internal pure returns (IHederaTokenService.Expiry memory expiry) {
        expiry.second = second;
    }
}

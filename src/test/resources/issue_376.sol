pragma solidity >=0.4.22 <0.6.0;
pragma experimental ABIEncoderV2;

contract Issue376 {
    // the contract's owner, set in the constructor
    address owner;

    constructor() public {
        // set the owner of the contract for `kill()`
        owner = msg.sender;
    }

    // return a string
    function giveStrings(string[] memory strings) public pure returns (string memory) {
        return strings[0];
    }

    // recover the funds of the contract
    function kill() public { if (msg.sender == owner) selfdestruct(msg.sender); }
}

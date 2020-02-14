pragma solidity >=0.4.22 <0.7.0;

contract FailConstructor {

    constructor(string memory message) public {
        revert(message);
    }
}

pragma solidity ^0.5.3;

contract SimpleStorage {
    uint storedData = 5;

    function set(uint x) public {
        storedData = x;
    }

    function get() public view returns (uint) {
        return storedData;
    }
}
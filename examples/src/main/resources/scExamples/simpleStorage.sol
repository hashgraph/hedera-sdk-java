pragma solidity ^0.5.3;

contract SimpleStorage {
    uint storedData = 5;

    event valueSet(uint x);

    function set(uint x) public {
        storedData = x;
		emit valueSet(x);        
    }

    function get() public view returns (uint) {
        return storedData;
    }
}
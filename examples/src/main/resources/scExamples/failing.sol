pragma solidity ^0.5.4;

contract Failing {
        
        uint16 x = 10;
        
        function buy() public {
            require(x == 2, "This contract raised an error");
            
        }
}
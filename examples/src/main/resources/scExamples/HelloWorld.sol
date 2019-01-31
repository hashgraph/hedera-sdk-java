pragma solidity ^0.5.3;

contract HelloWorld {
 function getInt () public pure returns (uint) {
   return 42;
 }
 function getString () public pure returns (string memory) {
   return "John says hi";
 }
}
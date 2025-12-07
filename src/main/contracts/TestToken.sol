// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

contract TestToken {
    string public name = "Test Token";
    string public symbol = "TT";
    uint8 public constant decimals = 18;  // 建议使用 constant
    uint256 public totalSupply;
    
    mapping(address => uint256) public balanceOf;
    
    event Transfer(address indexed from, address indexed to, uint256 value);
    
    // 修复：添加 public 可见性
    constructor() public {
        totalSupply = 1000000 * 10**uint256(decimals);  // 修复：添加类型转换
        balanceOf[msg.sender] = totalSupply;
        emit Transfer(address(0), msg.sender, totalSupply);  // 添加 mint 事件
    }
    
    function transfer(address to, uint256 amount) external returns (bool) {
        require(balanceOf[msg.sender] >= amount, "Insufficient balance");
        balanceOf[msg.sender] -= amount;
        balanceOf[to] += amount;
        emit Transfer(msg.sender, to, amount);
        return true;
    }
    
    // 测试用水龙头
    function faucet() external {
        uint256 amount = 1000 * 10**uint256(decimals);  // 修复：添加类型转换
        balanceOf[msg.sender] += amount;
        emit Transfer(address(0), msg.sender, amount);  // 添加 mint 事件
    }
}
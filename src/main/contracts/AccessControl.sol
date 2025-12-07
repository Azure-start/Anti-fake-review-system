// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

/**
 * @title AccessControl
 * @dev 简化的权限管理合约
 */
contract AccessControl {
    address public admin;
    mapping(address => bool) public authorizedContracts;
    
    event AdminChanged(address indexed newAdmin);
    event ContractAuthorized(address indexed contractAddr, bool status);
    
    modifier onlyAdmin() {
        require(msg.sender == admin, "Not admin");
        _;
    }
    
    constructor() public {
        admin = msg.sender;
    }
    
    function setAdmin(address newAdmin) external onlyAdmin {
        require(newAdmin != address(0), "Zero address");
        admin = newAdmin;
        emit AdminChanged(newAdmin);
    }
    
    function authorizeContract(address contractAddr) external onlyAdmin {
        authorizedContracts[contractAddr] = true;
        emit ContractAuthorized(contractAddr, true);
    }
    
    function revokeContract(address contractAddr) external onlyAdmin {
        authorizedContracts[contractAddr] = false;
        emit ContractAuthorized(contractAddr, false);
    }
    
    function isAuthorized(address contractAddr) external view returns (bool) {
        return authorizedContracts[contractAddr];
    }
}

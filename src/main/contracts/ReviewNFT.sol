// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;
//0.6.x版本中要返回结构体，需要启用 ABIEncoderV2 实验特性
pragma experimental ABIEncoderV2; 

/**
 * @title ReviewNFT
 * @dev 简化的评论NFT合约（不使用ERC721标准库）
 */
contract ReviewNFT {
    string public name = "Review NFT";
    string public symbol = "RNFT";
    
    address public accessControl;
    address public reviewCore;
    
    uint256 private _tokenIdCounter;
    
    struct NFT {
        uint256 tokenId;
        address owner;
        uint256 reviewId;
        string productId;
        uint256 timestamp;
    }
    
    mapping(uint256 => NFT) public nfts;
    mapping(address => uint256[]) public userTokens;
    
    event NFTMinted(uint256 indexed tokenId, address indexed owner, uint256 reviewId);
    
    modifier onlyReviewCore() {
        require(msg.sender == reviewCore, "Not ReviewCore");
        _;
    }
    
    modifier onlyAdmin() {
        require(msg.sender == IAccessControl(accessControl).admin(), "Not admin");
        _;
    }
    
    function setAccessControl(address _accessControl) external {
        require(accessControl == address(0), "Already set");
        accessControl = _accessControl;
    }
    
    function setReviewCore(address _reviewCore) external onlyAdmin {
        reviewCore = _reviewCore;
    }
    
    /**
     * @dev 铸造NFT
     */
    function mint(
        address to,
        uint256 reviewId,
        string memory productId
    ) external onlyReviewCore returns (uint256) {
        _tokenIdCounter++;
        uint256 newTokenId = _tokenIdCounter;
        
        nfts[newTokenId] = NFT({
            tokenId: newTokenId,
            owner: to,
            reviewId: reviewId,
            productId: productId,
            timestamp: block.timestamp
        });
        
        userTokens[to].push(newTokenId);
        
        emit NFTMinted(newTokenId, to, reviewId);
        return newTokenId;
    }
    
    /**
     * @dev 获取NFT信息
     */
    function getNFT(uint256 tokenId) external view returns (NFT memory) {
        require(nfts[tokenId].owner != address(0), "NFT not exists");
        return nfts[tokenId];
    }
    
    /**
     * @dev 获取用户的所有NFT
     */
    function getUserTokens(address user) external view returns (uint256[] memory) {
        return userTokens[user];
    }
    
    /**
     * @dev 获取总供应量
     */
    function totalSupply() external view returns (uint256) {
        return _tokenIdCounter;
    }
}

interface IAccessControl {
    function admin() external view returns (address);
}

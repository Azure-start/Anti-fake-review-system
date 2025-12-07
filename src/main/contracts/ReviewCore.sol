// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

//0.6.x版本中要返回结构体，需要启用 ABIEncoderV2 实验特性
pragma experimental ABIEncoderV2; 

/**
 * @title ReviewCore
 * @dev 简化的核心评论合约
 */
contract ReviewCore {
    address public accessControl;
    ICoinDayValidator public validator;
    IRewardPool public rewardPool;
    IReviewNFT public reviewNFT;
    
    uint256 private _reviewIdCounter;
    uint256 public minContentLength = 20;
    
    struct Review {
        uint256 id;
        address reviewer;
        string productId;
        string content;
        uint8 rating;
        uint256 reward;
        uint256 nftId;
        uint256 timestamp;
    }
    
    mapping(uint256 => Review) public reviews;
    mapping(string => uint256[]) public productReviews;
    mapping(address => uint256[]) public userReviews;
    
    event ReviewSubmitted(
        uint256 indexed reviewId,
        address indexed reviewer,
        string productId,
        uint256 nftId
    );
    
    modifier onlyAdmin() {
        require(msg.sender == IAccessControl(accessControl).admin(), "Not admin");
        _;
    }
    
    constructor(
        address _accessControl,
        address _validator,
        address _rewardPool,
        address _reviewNFT
    ) public {
        accessControl = _accessControl;
        validator = ICoinDayValidator(_validator);
        rewardPool = IRewardPool(_rewardPool);
        reviewNFT = IReviewNFT(_reviewNFT);
    }
    
    /**
     * @dev 提交评论
     */
    function submitReview(
        string memory productId,
        string memory content,
        uint8 rating
    ) external returns (uint256) {
        // 验证
        require(bytes(content).length >= minContentLength, "Content too short");
        require(rating >= 1 && rating <= 5, "Invalid rating");
        require(validator.validateCoinDays(msg.sender), "Insufficient coin days");
        
        // 创建评论
        _reviewIdCounter++;
        uint256 reviewId = _reviewIdCounter;
        
        // 计算奖励
        uint256 coinDays = validator.calculateCoinDays(msg.sender);
        uint256 reward = rewardPool.calculateReward(
            coinDays,
            bytes(content).length,
            rating
        );
        
        // 发放奖励
        uint256 actualReward = rewardPool.distributeReward(msg.sender, reward, reviewId);
        
        // 铸造NFT
        uint256 nftId = reviewNFT.mint(msg.sender, reviewId, productId);
        
        // 保存评论
        reviews[reviewId] = Review({
            id: reviewId,
            reviewer: msg.sender,
            productId: productId,
            content: content,
            rating: rating,
            reward: actualReward,
            nftId: nftId,
            timestamp: block.timestamp
        });
        
        productReviews[productId].push(reviewId);
        userReviews[msg.sender].push(reviewId);
        
        // 销毁币天
        validator.burnCoinDays(msg.sender);
        
        emit ReviewSubmitted(reviewId, msg.sender, productId, nftId);
        return reviewId;
    }
    
    /**
     * @dev 获取产品的所有评论
     */
    function getProductReviews(string memory productId) 
        external 
        view 
        returns (Review[] memory) 
    {
        uint256[] memory ids = productReviews[productId];
        Review[] memory result = new Review[](ids.length);
        
        for (uint256 i = 0; i < ids.length; i++) {
            result[i] = reviews[ids[i]];
        }
        
        return result;
    }
    
    /**
     * @dev 获取用户的所有评论
     */
    function getUserReviews(address user) external view returns (Review[] memory) {
        uint256[] memory ids = userReviews[user];
        Review[] memory result = new Review[](ids.length);
        
        for (uint256 i = 0; i < ids.length; i++) {
            result[i] = reviews[ids[i]];
        }
        
        return result;
    }
    
    /**
     * @dev 获取评论详情
     */
    function getReview(uint256 reviewId) external view returns (Review memory) {
        require(reviewId > 0 && reviewId <= _reviewIdCounter, "Invalid review ID");
        return reviews[reviewId];
    }
    
    /**
     * @dev 获取总评论数
     */
    function totalReviews() external view returns (uint256) {
        return _reviewIdCounter;
    }
    
    function setMinContentLength(uint256 _length) external onlyAdmin {
        minContentLength = _length;
    }
}

// 接口定义
interface IAccessControl {
    function admin() external view returns (address);
}

interface ICoinDayValidator {
    function validateCoinDays(address user) external view returns (bool);
    function calculateCoinDays(address user) external view returns (uint256);
    function burnCoinDays(address user) external;
}

interface IRewardPool {
    function calculateReward(uint256 coinDays, uint256 contentLength, uint8 rating) 
        external pure returns (uint256);
    function distributeReward(address user, uint256 amount, uint256 reviewId) 
        external returns (uint256);
}

interface IReviewNFT {
    function mint(address to, uint256 reviewId, string memory productId) 
        external returns (uint256);
}

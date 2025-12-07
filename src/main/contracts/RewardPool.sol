// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

interface IAccessControl {
    function admin() external view returns (address);
}

interface IERC20 {
    function transfer(address to, uint256 amount) external returns (bool);
    function balanceOf(address account) external view returns (uint256);
}

/**
 * @title RewardPool
 * @dev 简化的奖励池合约
 */
contract RewardPool {
    IERC20 public token;
    address public accessControl;
    address public reviewCore;
    
    uint256 public dailyRewardAmount = 10000 * 10**18;
    uint256 public totalDistributed;
    
    event RewardDistributed(address indexed user, uint256 amount, uint256 reviewId);
    event PoolFunded(uint256 amount);
    
    modifier onlyReviewCore() {
        require(msg.sender == reviewCore, "Not ReviewCore");
        _;
    }
    
    modifier onlyAdmin() {
        require(accessControl != address(0), "AccessControl not set");
        require(msg.sender == IAccessControl(accessControl).admin(), "Not admin");
        _;
    }
    
    // 修复：添加 public 可见性
    constructor(address _token) public {
        require(_token != address(0), "Invalid token address");
        token = IERC20(_token);
    }
    
    function setAccessControl(address _accessControl) external {
        require(accessControl == address(0), "Already set");
        require(_accessControl != address(0), "Invalid address");
        accessControl = _accessControl;
    }
    
    function setReviewCore(address _reviewCore) external onlyAdmin {
        require(_reviewCore != address(0), "Invalid address");
        reviewCore = _reviewCore;
    }
    
    function setDailyReward(uint256 _amount) external onlyAdmin {
        dailyRewardAmount = _amount;
    }
    
    /**
     * @dev 计算奖励
     */
    function calculateReward(
        uint256 coinDays,
        uint256 contentLength,
        uint8 rating
    ) public pure returns (uint256) {
        require(rating >= 1 && rating <= 5, "Invalid rating");
        
        // 基础奖励 10个代币
        uint256 baseReward = 10 * 10**18;
        
        // 币天加成（0-100%）
        uint256 coinDayBonus = coinDays > 100 ? 100 : coinDays;
        
        // 内容加成（0-50%）
        uint256 lengthBonus = contentLength > 100 ? 50 : contentLength / 2;
        
        // 评分加成（20%-100%）
        uint256 ratingBonus = uint256(rating) * 20;
        
        uint256 totalBonus = coinDayBonus + lengthBonus + ratingBonus;
        return (baseReward * (100 + totalBonus)) / 100;
    }
    
    /**
     * @dev 发放奖励
     */
    function distributeReward(
        address user,
        uint256 amount,
        uint256 reviewId
    ) external onlyReviewCore returns (uint256) {
        require(user != address(0), "Invalid user address");
        require(amount > 0, "Amount must be greater than 0");
        
        uint256 balance = token.balanceOf(address(this));
        
        if (amount > balance) {
            amount = balance;
        }
        
        if (amount > 0) {
            bool success = token.transfer(user, amount);
            require(success, "Transfer failed");
            totalDistributed += amount;
            emit RewardDistributed(user, amount, reviewId);
        }
        
        return amount;
    }
    
    /**
     * @dev 获取池子余额
     */
    function getPoolBalance() external view returns (uint256) {
        return token.balanceOf(address(this));
    }
    
    /**
     * @dev 提取代票（仅管理员）
     */
    function withdrawTokens(address to, uint256 amount) external onlyAdmin {
        require(to != address(0), "Invalid address");
        require(amount > 0, "Amount must be greater than 0");
        require(token.balanceOf(address(this)) >= amount, "Insufficient balance");
        
        bool success = token.transfer(to, amount);
        require(success, "Transfer failed");
    }
}
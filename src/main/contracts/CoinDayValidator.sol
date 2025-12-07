// SPDX-License-Identifier: MIT
pragma solidity ^0.6.10;

interface IERC20 {
    function balanceOf(address account) external view returns (uint256);
}

/**
 * @title CoinDayValidator
 * @dev 简化的币天验证合约
 */
contract CoinDayValidator {
    IERC20 public token;
    address public accessControl;
    address public reviewCore;
    
    uint256 public minCoinDays = 100;
    
    struct UserData {
        uint256 lastBalance;
        uint256 lastUpdateTime;
    }
    
    mapping(address => UserData) public userData;
    
    event CoinDaysBurned(address indexed user);
    
    modifier onlyReviewCore() {
        require(msg.sender == reviewCore, "Not ReviewCore");
        _;
    }
    
    modifier onlyAdmin() {
        require(msg.sender == IAccessControl(accessControl).admin(), "Not admin");
        _;
    }
    
    constructor(address _token, uint256 _minCoinDays) public {
        token = IERC20(_token);
        minCoinDays = _minCoinDays;
    }
    
    function setAccessControl(address _accessControl) external {
        require(accessControl == address(0), "Already set");
        accessControl = _accessControl;
    }
    
    function setReviewCore(address _reviewCore) external onlyAdmin {
        reviewCore = _reviewCore;
    }
    
    function setMinCoinDays(uint256 _minCoinDays) external onlyAdmin {
        minCoinDays = _minCoinDays;
    }
    
    /**
     * @dev 计算用户币天
     */
    function calculateCoinDays(address user) public view returns (uint256) {
        UserData memory data = userData[user];
        uint256 currentBalance = token.balanceOf(user);
        
        if (data.lastUpdateTime == 0) {
            return 0;
        }
        
        uint256 timeElapsed = block.timestamp - data.lastUpdateTime;
        uint256 dayCount = timeElapsed / 1 days;
        
        return currentBalance * dayCount;
    }
    
    /**
     * @dev 验证币天是否达标
     */
    function validateCoinDays(address user) external view returns (bool) {
        return calculateCoinDays(user) >= minCoinDays;
    }
    
    /**
     * @dev 初始化用户记录
     */
    function initUser(address user) external {
        if (userData[user].lastUpdateTime == 0) {
            userData[user] = UserData({
                lastBalance: token.balanceOf(user),
                lastUpdateTime: block.timestamp
            });
        }
    }
    
    /**
     * @dev 销毁币天（评论后调用）
     */
    function burnCoinDays(address user) external onlyReviewCore {
        userData[user] = UserData({
            lastBalance: token.balanceOf(user),
            lastUpdateTime: block.timestamp
        });
        emit CoinDaysBurned(user);
    }
}

interface IAccessControl {
    function admin() external view returns (address);
}

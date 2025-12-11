-- 添加区块链评论ID字段到reviews表
ALTER TABLE reviews ADD COLUMN blockchain_review_id BIGINT NULL COMMENT '区块链评论ID' AFTER tx_hash;

-- 创建索引以提高查询性能
CREATE INDEX idx_blockchain_review_id ON reviews(blockchain_review_id);
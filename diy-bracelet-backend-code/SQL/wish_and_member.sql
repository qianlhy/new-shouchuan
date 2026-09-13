-- ============================================================
-- 祈愿手作 · 广场心愿众筹功能
-- 会员中心：从订单实付金额动态计算，无需新增表
-- 执行：在 diy_bangle 库执行本脚本
-- ============================================================

-- 心愿众筹记录表：一个用户对一个商品只能「想要」一次
CREATE TABLE IF NOT EXISTS `product_wish_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '想要时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品心愿众筹记录表';

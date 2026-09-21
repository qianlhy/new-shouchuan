-- 灵感广场作品表（购物车保存设计图时提交，后台控制展示）
-- 执行：在 diy_bangle 库执行本脚本

CREATE TABLE IF NOT EXISTS `square_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) DEFAULT NULL COMMENT '设计图URL',
  `diy_data` mediumtext COMMENT 'DIY设计JSON（含珠子顺序，与购物车diyData同结构）',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `bead_count` int(11) DEFAULT '0' COMMENT '珠子数量',
  `hand_size` decimal(5,2) DEFAULT NULL COMMENT '手围cm',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态：0隐藏 1展示',
  `show_scope` tinyint(4) NOT NULL DEFAULT 3 COMMENT '展示范围：1仅灵感广场 2仅推荐设计 3灵感广场+推荐设计',
  `sort` int(11) DEFAULT '0' COMMENT '排序（越大越靠前）',
  `user_id` bigint(20) DEFAULT NULL COMMENT '提交用户ID',
  `cart_item_id` bigint(20) DEFAULT NULL COMMENT '来源购物车项ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_scope_sort` (`status`, `show_scope`, `sort`, `id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵感广场作品表';

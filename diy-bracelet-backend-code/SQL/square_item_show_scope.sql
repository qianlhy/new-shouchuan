-- 广场作品增加展示范围：1仅灵感广场 2仅推荐设计 3灵感广场+推荐设计
-- 已建表环境执行本脚本；全新环境可直接用更新后的 square_item.sql

ALTER TABLE `square_item`
  ADD COLUMN `show_scope` tinyint(4) NOT NULL DEFAULT 3 COMMENT '展示范围：1仅灵感广场 2仅推荐设计 3灵感广场+推荐设计' AFTER `status`;

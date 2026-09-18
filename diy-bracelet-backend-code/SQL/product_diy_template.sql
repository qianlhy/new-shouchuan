-- ============================================================
-- 祈愿手作 · 商品 DIY 设计模板
-- 后台配置搭配后，小程序详情页展示并可带入制作台
-- 执行：在 diy_bangle 库执行本脚本
-- ============================================================

ALTER TABLE `product`
  ADD COLUMN `creator_name` varchar(64) DEFAULT NULL COMMENT '创作者名称' AFTER `description`,
  ADD COLUMN `diy_data` mediumtext COMMENT 'DIY设计模板JSON（与购物车diyData同结构）' AFTER `cover_image`;

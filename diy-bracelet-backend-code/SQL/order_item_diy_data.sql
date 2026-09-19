-- ============================================================
-- 订单明细支持 DIY 设计数据（下单写入 order_item.diy_data）
-- 执行：在 diy_bangle 库执行
-- 若提示 Duplicate column，说明已加过，可忽略
-- ============================================================

ALTER TABLE `order_item`
  ADD COLUMN `diy_data` mediumtext NULL COMMENT 'DIY设计JSON快照' AFTER `product_image`;

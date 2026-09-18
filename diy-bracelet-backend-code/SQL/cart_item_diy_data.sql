-- ============================================================
-- 购物车支持 DIY 设计数据（加购后列表能读出 diy_data）
-- 执行：在 diy_bangle 库执行
-- 若提示 Duplicate column，说明已加过，可忽略
-- ============================================================

ALTER TABLE `cart_item`
  ADD COLUMN `diy_data` mediumtext NULL COMMENT 'DIY设计JSON' AFTER `quantity`;

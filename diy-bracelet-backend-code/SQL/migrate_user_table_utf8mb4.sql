-- 修复：微信用户昵称含 emoji 时 JDBC 报错
-- Incorrect string value: '\xF0\x9F...' for column 'nickname'
-- 原因：MySQL 的 utf8 最多 3 字节，emoji 需要 utf8mb4（4 字节）
--
-- 在已部署的库上执行一次（将 diy_bangle 换成你的库名）：
--   mysql -u用户 -p diy_bangle < migrate_user_table_utf8mb4.sql

USE `diy_bangle`;

ALTER TABLE `user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Aicon 图标库（Lucide Icons）

来源：https://github.com/lucide-icons/lucide  
协议：ISC（可商用）  
风格：线性描边、现代统一

## 目录说明
- lucide-svg/     全量 SVG（1800+）
- png/            已按品牌色导出的 PNG
- tools/          导出脚本（export-icons.js / export-tabbar.js）

## 项目内路径
BraceletUniapp/static/icons/*.png

## 新增图标
1. 在 lucide-svg 找到图标名（如 house、truck）
2. 编辑 tools/export-icons.js 的 ICONS 映射
3. 在 tools 目录执行：node export-icons.js
4. 页面引用：/static/icons/图标名.png

## 品牌色
- purple #6B4EFF（默认）
- gray #666666
- gold #F5C93A

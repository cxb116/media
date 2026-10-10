-- ========================================
-- 测试数据查询SQL
-- ========================================

-- 1. 检查 ssp_slot_info 表中 media_id=22 的广告位
SELECT id, media_id, app_id, name, enable
FROM ssp_slot_info
WHERE media_id = 22;

-- 2. 检查 data_ssp_slot_day_202603 表的数据总量
SELECT COUNT(*) as total_count
FROM data_ssp_slot_day_202603;

-- 3. 检查 data_ssp_slot_day_202603 表中是否有数据
SELECT *
FROM data_ssp_slot_day_202603
LIMIT 10;

-- 4. 使用正确的关联查询：先查广告位ID，再查数据表
-- 假设 ssp_slot_info 中 media_id=22 的广告位ID是 1,2,3
SELECT d.*
FROM data_ssp_slot_day_202603 d
WHERE d.ssp_slot_id IN (
    SELECT id FROM ssp_slot_info WHERE media_id = 22
)
LIMIT 10;

-- 5. 检查特定广告位是否有数据
SELECT COUNT(*) as count, ssp_slot_id
FROM data_ssp_slot_day_202603
GROUP BY ssp_slot_id
ORDER BY count DESC
LIMIT 10;

-- 6. 关联查询：显示广告位名称和数据
SELECT
    s.id as slot_id,
    s.name as slot_name,
    s.media_id,
    COUNT(d.id) as data_count
FROM ssp_slot_info s
LEFT JOIN data_ssp_slot_day_202603 d ON s.id = d.ssp_slot_id
WHERE s.media_id = 22
GROUP BY s.id, s.name, s.media_id;

-- ========================================
-- 常见问题排查
-- ========================================

-- 问题1：表名是否正确
SHOW TABLES LIKE 'data_ssp_slot_day_202603';

-- 问题2：字段名是否正确（注意：表中是 meida_id 不是 media_id）
DESC data_ssp_slot_day_202603;

-- 问题3：检查表结构
SHOW CREATE TABLE data_ssp_slot_day_202603;

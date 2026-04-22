-- ============================================================
-- 大状元高考志愿填报系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `dazhuangyuan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `dazhuangyuan`;

-- ============================================================
-- 1. 院校信息表
-- ============================================================
DROP TABLE IF EXISTS `dy_college`;
CREATE TABLE `dy_college` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `college_name` VARCHAR(200) NOT NULL COMMENT '院校名称',
    `college_code` VARCHAR(20) NOT NULL COMMENT '院校代码',
    `province` VARCHAR(50) NOT NULL COMMENT '所在省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
    `college_type` VARCHAR(50) DEFAULT NULL COMMENT '院校类型: 985/211/双一流/普通本科/专科',
    `college_nature` VARCHAR(50) DEFAULT NULL COMMENT '办学性质: 公办/民办/中外合作办学',
    `college_level` VARCHAR(20) DEFAULT NULL COMMENT '办学层次: 本科/专科',
    `belong` VARCHAR(100) DEFAULT NULL COMMENT '隶属',
    `phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '官网',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '地址',
    `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '校徽URL',
    `intro` TEXT DEFAULT NULL COMMENT '简介',
    `features` TEXT DEFAULT NULL COMMENT '办学特色',
    `employment_rate` VARCHAR(20) DEFAULT NULL COMMENT '就业率',
    `tuition_range` VARCHAR(100) DEFAULT NULL COMMENT '学费区间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_college_code` (`college_code`),
    KEY `idx_province` (`province`),
    KEY `idx_college_type` (`college_type`),
    KEY `idx_college_level` (`college_level`),
    KEY `idx_college_name` (`college_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校信息表';

-- ============================================================
-- 2. 专业信息表
-- ============================================================
DROP TABLE IF EXISTS `dy_major`;
CREATE TABLE `dy_major` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `major_name` VARCHAR(200) NOT NULL COMMENT '专业名称',
    `major_code` VARCHAR(20) NOT NULL COMMENT '专业代码',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '学科门类',
    `sub_category` VARCHAR(100) DEFAULT NULL COMMENT '一级学科',
    `degree_type` VARCHAR(50) DEFAULT NULL COMMENT '授予学位',
    `duration` VARCHAR(20) DEFAULT NULL COMMENT '学制',
    `description` TEXT DEFAULT NULL COMMENT '专业介绍',
    `employment_direction` TEXT DEFAULT NULL COMMENT '就业方向',
    `salary_range` VARCHAR(100) DEFAULT NULL COMMENT '薪资范围',
    `is_hot` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否热门',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_major_code` (`major_code`),
    KEY `idx_category` (`category`),
    KEY `idx_major_name` (`major_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业信息表';

-- ============================================================
-- 3. 院校专业组表（广东新高考核心）
-- ============================================================
DROP TABLE IF EXISTS `dy_college_major_group`;
CREATE TABLE `dy_college_major_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `college_id` BIGINT NOT NULL COMMENT '院校ID',
    `group_code` VARCHAR(30) NOT NULL COMMENT '专业组代码',
    `group_name` VARCHAR(200) DEFAULT NULL COMMENT '专业组名称',
    `require_subjects` JSON DEFAULT NULL COMMENT '选科要求JSON，如["物理","化学"]',
    `batch` VARCHAR(30) DEFAULT NULL COMMENT '录取批次',
    `year` INT NOT NULL COMMENT '年份',
    `plan_count` INT DEFAULT 0 COMMENT '招生人数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_college_id` (`college_id`),
    KEY `idx_year_batch` (`year`, `batch`),
    UNIQUE KEY `uk_college_group` (`college_id`, `group_code`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校专业组表';

-- ============================================================
-- 4. 院校专业关联表
-- ============================================================
DROP TABLE IF EXISTS `dy_college_major`;
CREATE TABLE `dy_college_major` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `college_id` BIGINT NOT NULL COMMENT '院校ID',
    `major_id` BIGINT NOT NULL COMMENT '专业ID',
    `group_id` BIGINT NOT NULL COMMENT '专业组ID',
    `enrollment_year` INT NOT NULL COMMENT '招生年份',
    `plan_count` INT DEFAULT 0 COMMENT '计划招生数',
    PRIMARY KEY (`id`),
    KEY `idx_college_id` (`college_id`),
    KEY `idx_major_id` (`major_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校专业关联表';

-- ============================================================
-- 5. 历年录取数据表（核心数据）
-- ============================================================
DROP TABLE IF EXISTS `dy_admission_data`;
CREATE TABLE `dy_admission_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `college_id` BIGINT NOT NULL COMMENT '院校ID',
    `group_id` BIGINT DEFAULT NULL COMMENT '专业组ID',
    `major_id` BIGINT DEFAULT NULL COMMENT '专业ID',
    `province` VARCHAR(50) NOT NULL COMMENT '招生省份',
    `year` INT NOT NULL COMMENT '年份',
    `batch` VARCHAR(30) NOT NULL COMMENT '批次',
    `category` VARCHAR(20) DEFAULT NULL COMMENT '物理类/历史类',
    `min_score` INT DEFAULT NULL COMMENT '最低分',
    `avg_score` INT DEFAULT NULL COMMENT '平均分',
    `max_score` INT DEFAULT NULL COMMENT '最高分',
    `min_rank` INT DEFAULT NULL COMMENT '最低位次',
    `avg_rank` INT DEFAULT NULL COMMENT '平均位次',
    `max_rank` INT DEFAULT NULL COMMENT '最高位次',
    `enrolled_count` INT DEFAULT 0 COMMENT '录取人数',
    `plan_count` INT DEFAULT 0 COMMENT '计划数',
    `score_line` INT DEFAULT NULL COMMENT '省控线',
    `line_diff` INT DEFAULT NULL COMMENT '线差',
    PRIMARY KEY (`id`),
    KEY `idx_college_year` (`college_id`, `year`, `province`, `batch`),
    KEY `idx_group_year` (`group_id`, `year`),
    KEY `idx_major_year` (`major_id`, `year`),
    KEY `idx_province_year_batch` (`province`, `year`, `batch`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历年录取数据表';

-- ============================================================
-- 6. 各省历年批次线
-- ============================================================
DROP TABLE IF EXISTS `dy_province_score_line`;
CREATE TABLE `dy_province_score_line` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `year` INT NOT NULL COMMENT '年份',
    `batch` VARCHAR(30) NOT NULL COMMENT '批次',
    `category` VARCHAR(20) NOT NULL COMMENT '物理类/历史类',
    `score` INT NOT NULL COMMENT '分数线',
    `rank_count` INT DEFAULT NULL COMMENT '上线人数',
    `total_students` INT DEFAULT NULL COMMENT '考生人数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_province_year_batch_cat` (`province`, `year`, `batch`, `category`),
    KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='各省历年批次线';

-- ============================================================
-- 7. 一分一段表
-- ============================================================
DROP TABLE IF EXISTS `dy_score_rank`;
CREATE TABLE `dy_score_rank` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `year` INT NOT NULL COMMENT '年份',
    `category` VARCHAR(20) NOT NULL COMMENT '物理类/历史类',
    `score` INT NOT NULL COMMENT '分数',
    `rank_count` INT NOT NULL COMMENT '累计人数/位次',
    `score_count` INT DEFAULT 1 COMMENT '本分人数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_province_year_cat_score` (`province`, `year`, `category`, `score`),
    KEY `idx_province_year_cat` (`province`, `year`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一分一段表';

-- ============================================================
-- 8. 用户表
-- ============================================================
DROP TABLE IF EXISTS `dy_user`;
CREATE TABLE `dy_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0未知 1男 2女',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `vip_level` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP等级: 0免费 1基础 2专业 3尊享',
    `vip_expire_at` DATETIME DEFAULT NULL COMMENT 'VIP过期时间',
    `student_score` INT DEFAULT NULL COMMENT '高考分数',
    `student_rank` INT DEFAULT NULL COMMENT '位次',
    `student_subjects` JSON DEFAULT NULL COMMENT '选科JSON',
    `student_category` VARCHAR(20) DEFAULT NULL COMMENT '物理类/历史类',
    `target_province` VARCHAR(50) DEFAULT NULL COMMENT '意向省份',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_vip_level` (`vip_level`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 9. 短信验证码日志
-- ============================================================
DROP TABLE IF EXISTS `dy_sms_log`;
CREATE TABLE `dy_sms_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码',
    `type` VARCHAR(20) DEFAULT 'login' COMMENT '类型: login/register',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未用 1已用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expired_at` DATETIME NOT NULL COMMENT '过期时间',
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码日志';

-- ============================================================
-- 10. 订单表
-- ============================================================
DROP TABLE IF EXISTS `dy_order`;
CREATE TABLE `dy_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `pay_type` VARCHAR(20) DEFAULT NULL COMMENT '支付方式: wechat/alipay',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已退款 3已过期',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expire_at` DATETIME NOT NULL COMMENT '过期时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 11. 产品价格配置表
-- ============================================================
DROP TABLE IF EXISTS `dy_product`;
CREATE TABLE `dy_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_type` VARCHAR(30) NOT NULL COMMENT '产品类型: basic/professional/premium',
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `description` TEXT DEFAULT NULL COMMENT '产品描述',
    `features` JSON DEFAULT NULL COMMENT '功能列表JSON',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_type` (`product_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品价格配置表';

-- ============================================================
-- 12. 用户志愿方案表
-- ============================================================
DROP TABLE IF EXISTS `dy_user_volunteer`;
CREATE TABLE `dy_user_volunteer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT '我的志愿方案' COMMENT '方案名称',
    `total_count` INT DEFAULT 0 COMMENT '志愿总数',
    `rush_count` INT DEFAULT 0 COMMENT '冲',
    `stable_count` INT DEFAULT 0 COMMENT '稳',
    `safe_count` INT DEFAULT 0 COMMENT '保',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认方案',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户志愿方案表';

-- ============================================================
-- 13. 志愿方案明细表
-- ============================================================
DROP TABLE IF EXISTS `dy_volunteer_item`;
CREATE TABLE `dy_volunteer_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL COMMENT '方案ID',
    `college_id` BIGINT NOT NULL COMMENT '院校ID',
    `group_id` BIGINT DEFAULT NULL COMMENT '专业组ID',
    `major_ids` JSON DEFAULT NULL COMMENT '专业ID列表JSON',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `type` VARCHAR(10) DEFAULT NULL COMMENT '冲/稳/保',
    `admission_probability` DECIMAL(5,2) DEFAULT NULL COMMENT '录取概率(%)',
    `is_adjust` TINYINT DEFAULT 1 COMMENT '是否服从调剂',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_college_id` (`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿方案明细表';

-- ============================================================
-- 14. 用户浏览记录
-- ============================================================
DROP TABLE IF EXISTS `dy_user_browse_history`;
CREATE TABLE `dy_user_browse_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `college_id` BIGINT DEFAULT NULL,
    `major_id` BIGINT DEFAULT NULL,
    `action_type` VARCHAR(20) DEFAULT NULL COMMENT 'view/collect/compare',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_college_id` (`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览记录';

-- ============================================================
-- 15. 广告/运营位配置表
-- ============================================================
DROP TABLE IF EXISTS `dy_ad_config`;
CREATE TABLE `dy_ad_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `position` VARCHAR(50) NOT NULL COMMENT '位置标识: home_banner/home_mid/profile_top/about_page',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `sub_title` VARCHAR(200) DEFAULT NULL COMMENT '副标题',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
    `link_url` VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
    `ad_type` VARCHAR(30) DEFAULT NULL COMMENT '升学规划/学业评估/咨询/外部',
    `contact_info` VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
    `sort_order` INT DEFAULT 0,
    `is_active` TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    `start_at` DATETIME DEFAULT NULL,
    `end_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_position` (`position`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告/运营位配置表';

-- ============================================================
-- 16. 系统配置表
-- ============================================================
DROP TABLE IF EXISTS `dy_system_config`;
CREATE TABLE `dy_system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `config_desc` VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 产品配置
INSERT INTO `dy_product` (`product_type`, `name`, `original_price`, `price`, `description`, `features`, `sort_order`, `status`) VALUES
('free', '免费版', 0.00, 0.00, '基础功能免费体验，了解院校专业信息', 
 '["查看院校基本信息","查看历年分数线","查询一分一段表","志愿生成预览(限3所)","查询批次线"]', 0, 1),
('basic', '基础版', 198.00, 98.00, '完整冲稳保方案，轻松填报不踩坑',
 '["全部免费功能","完整冲稳保方案(限10所院校)","院校对比功能","志愿表导出Excel","批次线与一分一段查询"]', 1, 1),
('professional', '专业版', 398.00, 198.00, 'AI智能分析+院校PK+志愿诊断，精准填报',
 '["全部基础版功能","完整冲稳保方案(45所院校)","AI智能分析报告","院校PK深度对比","志愿风险诊断","优先客服支持","多方案对比"]', 2, 1),
('premium', '尊享版', 598.00, 298.00, '全方位护航+一对一审核，志愿填报零失误',
 '["全部专业版功能","一对一志愿方案审核","专家升学规划指导","免费学业评估报告","VIP专属客服","优先匹配最新招生数据","高中课程优惠"]', 3, 1);

-- 系统默认配置
INSERT INTO `dy_system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('site_name', '大状元高考志愿填报系统', '网站名称'),
('site_description', '广东省高考志愿填报智能推荐，让每一位考生都能精准录取', '网站描述'),
('contact_phone', '13800138000', '联系电话'),
('contact_wechat', 'dazhuangyuan_vip', '联系微信'),
('volunteer_max_preview', '3', '免费用户志愿预览数量'),
('basic_max_colleges', '10', '基础版最大推荐院校数'),
('professional_max_colleges', '45', '专业版最大推荐院校数'),
('sms_daily_limit', '5', '每日短信发送上限'),
('rush_ratio_default', '4:25:16', '默认冲稳保比例'),
('db_year', '2024', '当前录取数据年份'),
('current_province', '广东', '默认省份'),
('ad_enabled', '1', '是否启用广告位');

-- 广告位配置
INSERT INTO `dy_ad_config` (`position`, `title`, `sub_title`, `image_url`, `link_url`, `ad_type`, `contact_info`, `sort_order`, `is_active`) VALUES
('home_banner', '高考升学规划', '一对一专家指导，量身定制升学方案', NULL, '/about', '升学规划', '微信: dazhuangyuan_vip', 1, 1),
('home_mid', '免费学科能力评估', '测一测你的优势学科，找到最适合的专业方向', NULL, '/assess', '学业评估', NULL, 2, 1),
('home_mid', '志愿填报一对一咨询', '资深专家团队，为你量身定制志愿方案', NULL, '/about', '咨询', '电话: 13800138000', 3, 1),
('profile_top', '升级VIP享受更多功能', '解锁完整冲稳保方案和AI智能分析', NULL, '/pay', '咨询', NULL, 1, 1),
('about_page', '大朋友教育', '深圳本土教育品牌，专注初升高、高考复读、AI诊断', NULL, '/about', '外部', '电话: 13800138000', 1, 1);

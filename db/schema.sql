-- 农贸市场计量公平秤监管平台 数据库结构
-- 字符集统一 utf8mb4，避免中文乱码

CREATE DATABASE IF NOT EXISTS fairscale
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE fairscale;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(64)           DEFAULT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'viewer',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stalls (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    stall_no      VARCHAR(32)  NOT NULL,
    market_name   VARCHAR(128) NOT NULL,
    merchant_name VARCHAR(64)  NOT NULL,
    category      VARCHAR(32)           DEFAULT NULL,
    contact_phone VARCHAR(32)           DEFAULT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stalls_no (stall_no),
    KEY idx_stalls_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scales (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    stall_id          BIGINT      NOT NULL,
    asset_no          VARCHAR(48) NOT NULL,
    model             VARCHAR(64)          DEFAULT NULL,
    manufacturer      VARCHAR(128)         DEFAULT NULL,
    max_capacity_g    INT                  DEFAULT NULL,
    verified_at       DATE                 DEFAULT NULL,
    verify_cycle_days INT         NOT NULL DEFAULT 365,
    status            VARCHAR(20) NOT NULL DEFAULT 'in_use',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scales_asset (asset_no),
    KEY idx_scales_stall (stall_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS recheck_records (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    stall_id        BIGINT      NOT NULL,
    commodity       VARCHAR(64) NOT NULL,
    claimed_weight_g INT        NOT NULL,
    actual_weight_g  INT        NOT NULL,
    shortage_g       INT        NOT NULL DEFAULT 0,
    result          VARCHAR(16) NOT NULL DEFAULT 'pass',
    handled_by      VARCHAR(64)          DEFAULT NULL,
    remark          VARCHAR(255)         DEFAULT NULL,
    rechecked_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_recheck_stall (stall_id),
    KEY idx_recheck_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 计量信用评分体系 ==========

-- 评分规则配置表
CREATE TABLE IF NOT EXISTS credit_rules (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    rule_code       VARCHAR(64)  NOT NULL COMMENT '规则编码，如 RECHECK_SHORTAGE',
    rule_name       VARCHAR(128) NOT NULL COMMENT '规则名称',
    score_delta     INT          NOT NULL DEFAULT 0 COMMENT '基础加减分（正数加分，负数扣分）',
    has_severity    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否按严重程度分级',
    has_magnitude   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否按幅度计算倍率',
    decay_months    INT          NOT NULL DEFAULT 0 COMMENT '衰减月数，0=不衰减',
    description     VARCHAR(255)          DEFAULT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_credit_rules_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 摊位当前信用分快照
CREATE TABLE IF NOT EXISTS credit_scores (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    stall_id          BIGINT      NOT NULL,
    current_score     INT         NOT NULL DEFAULT 100 COMMENT '当前信用分',
    level_code        VARCHAR(20) NOT NULL DEFAULT 'good' COMMENT '信用等级编码',
    clean_days        INT         NOT NULL DEFAULT 0 COMMENT '连续无违规天数',
    last_event_at     DATETIME             DEFAULT NULL,
    last_recalc_at    DATETIME             DEFAULT NULL,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_credit_scores_stall (stall_id),
    KEY idx_credit_scores_score (current_score),
    KEY idx_credit_scores_level (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 信用事件原始记录（加减分的触发事件）
CREATE TABLE IF NOT EXISTS credit_events (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    stall_id        BIGINT       NOT NULL,
    event_type      VARCHAR(64)  NOT NULL COMMENT '事件类型，对应 rule_code',
    severity        VARCHAR(20)           DEFAULT NULL COMMENT '严重程度: mild/normal/serious',
    magnitude_value INT                   DEFAULT NULL COMMENT '幅度值，如短缺克数、超期天数',
    source_table    VARCHAR(64)           DEFAULT NULL COMMENT '关联源表，如 recheck_records',
    source_id       BIGINT                DEFAULT NULL COMMENT '关联源记录ID',
    description     VARCHAR(255)          DEFAULT NULL,
    rectified       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已整改',
    rectified_at    DATETIME              DEFAULT NULL,
    occurred_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_credit_events_stall (stall_id),
    KEY idx_credit_events_type (event_type),
    KEY idx_credit_events_occurred (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 信用分变动流水（每一笔加减的详细记录，可追溯可重算）
CREATE TABLE IF NOT EXISTS credit_flows (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    stall_id                BIGINT       NOT NULL,
    event_id                BIGINT                DEFAULT NULL,
    rule_code               VARCHAR(64)  NOT NULL,
    rule_name               VARCHAR(128) NOT NULL,
    score_before            INT          NOT NULL,
    score_delta_raw         INT          NOT NULL COMMENT '原始加减分（不考虑衰减）',
    score_delta_applied     INT          NOT NULL COMMENT '实际生效加减分（考虑上下限裁剪）',
    score_after             INT          NOT NULL,
    decay_months            INT          NOT NULL DEFAULT 0 COMMENT '衰减周期（月），0=不衰减',
    is_addition             TINYINT(1)   NOT NULL COMMENT 'true=加分，false=扣分',
    rule_snapshot           VARCHAR(1024)         DEFAULT NULL COMMENT '规则快照JSON，便于规则变更后追溯',
    occurred_at             DATETIME     NOT NULL,
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_credit_flows_stall (stall_id),
    KEY idx_credit_flows_event (event_id),
    KEY idx_credit_flows_occurred (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 红黑榜快照（周期发布，定版留档）
CREATE TABLE IF NOT EXISTS credit_rankings (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    period_type     VARCHAR(20)  NOT NULL DEFAULT 'monthly' COMMENT '周期类型: monthly/quarterly',
    period_label    VARCHAR(32)  NOT NULL COMMENT '周期标签，如 2026-06',
    ranking_type    VARCHAR(16)  NOT NULL COMMENT '榜单类型: red/black',
    stall_id        BIGINT       NOT NULL,
    stall_no        VARCHAR(32)  NOT NULL,
    merchant_name   VARCHAR(64)  NOT NULL,
    market_name     VARCHAR(128) NOT NULL,
    score           INT          NOT NULL,
    level_code      VARCHAR(20)  NOT NULL,
    snapshot_data   TEXT                  DEFAULT NULL COMMENT '快照详情JSON',
    publish_status  VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT 'draft/published',
    published_at    DATETIME              DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rankings_period_stall_type (period_label, ranking_type, stall_id),
    KEY idx_rankings_type_period (ranking_type, period_label),
    KEY idx_rankings_status (publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化评分规则
INSERT INTO credit_rules (rule_code, rule_name, score_delta, has_severity, has_magnitude, decay_months, description) VALUES
('RECHECK_SHORTAGE',    '复称短缺',              -10, 1, 1, 12, '被公平秤复称判定缺斤短两，扣分与短缺幅度挂钩'),
('SCALE_EXPIRED',       '计量器具超期未检',       -15, 0, 1, 6,  '器具超过检定有效期仍在使用'),
('SCALE_UNQUALIFIED',   '计量器具检定不合格',     -20, 1, 0, 12, '强制检定结论为不合格'),
('SCALE_REFUSE_RECTIFY','拒不整改',               -30, 0, 0, 24, '收到整改通知后拒不整改或整改不到位'),
('PERIOD_CLEAN_30',     '连续30天无违规',         +3,  0, 0, 0,  '每连续30天无违规记录自动加分'),
('PERIOD_CLEAN_90',     '连续90天无违规',         +8,  0, 0, 0,  '每连续90天无违规记录额外加分'),
('ACTIVE_RECTIFY',      '主动整改到位',           +5,  0, 0, 0,  '违规后主动整改且通过复核'),
('INSPECTION_PASS',     '抽检合格',               +2,  0, 0, 0,  '日常巡查或抽检计量合格')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

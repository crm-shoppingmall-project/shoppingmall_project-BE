-- 사용 명령어: mysql -u ohgiraffers -p shoppingmalldb < "파일폴더/db_schema.sql"

-- Schema for shoppingmalldb
-- Added DROP TABLE IF EXISTS for idempotent script execution.
-- Foreign key constraints are considered for table drop order.

SET FOREIGN_KEY_CHECKS=0;

-- Drop tables in reverse order of dependency
DROP TABLE IF EXISTS `Review_comment`;
DROP TABLE IF EXISTS `Review`;
DROP TABLE IF EXISTS `purchase_detail`;
DROP TABLE IF EXISTS `Return_request`;
DROP TABLE IF EXISTS `Product`;
DROP TABLE IF EXISTS `CS_ticket_reply`;
DROP TABLE IF EXISTS `CS_ticket`;
DROP TABLE IF EXISTS `Member_grade_history`;
DROP TABLE IF EXISTS `Member_profile`;
DROP TABLE IF EXISTS `Message_send_log`;
DROP TABLE IF EXISTS `Segment`;
DROP TABLE IF EXISTS `Member`;
DROP TABLE IF EXISTS `Member_grade`;
DROP TABLE IF EXISTS `Member_rfm`;
DROP TABLE IF EXISTS `Campaign_target_segment`;
DROP TABLE IF EXISTS `Campaign`;
DROP TABLE IF EXISTS `Segment_member`;
DROP TABLE IF EXISTS `History`;
DROP TABLE IF EXISTS `Payment`;
DROP TABLE IF EXISTS `Cart_item`;
DROP TABLE IF EXISTS `Cart`;
DROP TABLE IF EXISTS `Purchase`;


-- Create tables

CREATE TABLE `Campaign_target_segment` (
    `target_segment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `segment_id`       BIGINT NOT NULL,
    `campaign_id`       BIGINT NOT NULL,
    PRIMARY KEY (`target_segment_id`)
);

CREATE TABLE `CS_ticket_reply` (
    `reply_id`           BIGINT NOT NULL AUTO_INCREMENT,
    `cs_ticket_id`       BIGINT NOT NULL,
    `reply_responder_id` BIGINT NOT NULL,
    `reply_content`      TEXT NOT NULL,
    `reply_created`      TIMESTAMP NOT NULL COMMENT '접수,처리중,처리완료,거절',
    PRIMARY KEY (`reply_id`)
);

CREATE TABLE `Purchase` (
    `purchase_id`        BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`          BIGINT NOT NULL,
    `purchase_status`    ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
    `purchase_processed` TIMESTAMP NULL,
    PRIMARY KEY (`purchase_id`)
);

CREATE TABLE `Cart` (
    `cart_id`   BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    PRIMARY KEY (`cart_id`)
);

CREATE TABLE `Cart_item` (
    `cart_item_id`       BIGINT NOT NULL AUTO_INCREMENT,
    `cart_id`            BIGINT NOT NULL,
    `product_id`         BIGINT NOT NULL,
    `cart_item_updated`  TIMESTAMP NOT NULL,
    `cart_item_status`   ENUM('ACTIVE','REMOVED','UPDATED') NOT NULL,
    `cart_item_quantity` INT NOT NULL,
    PRIMARY KEY (`cart_item_id`)
);

CREATE TABLE `Payment` (
    `payment_id`       BIGINT NOT NULL AUTO_INCREMENT,
    `purchase_id`         BIGINT NOT NULL,
    `payment_status`   ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
    `payment_created`  DATETIME NOT NULL,
    `payment_type`     ENUM('PAYMENT','REFUND') NOT NULL,
    `payment_tid`      VARCHAR(20) NOT NULL,
    `payment_paid_at`  TIMESTAMP NOT NULL,
    PRIMARY KEY (`payment_id`)
);

CREATE TABLE `History` (
    `history_id`         BIGINT NOT NULL AUTO_INCREMENT,
    `history_datetime`   TIMESTAMP NOT NULL,
    `history_action_type` ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','PAID','REFUND') NOT NULL,
    `history_member_id`  BIGINT NOT NULL,
    `history_detail`     TEXT NULL,
    `history_ip_address` VARCHAR(100) NOT NULL,
    `history_ref_tbl`    ENUM('Member','Purchase','Segment','CS_ticket','Product','Return_request','CART_ITEM') NOT NULL,
    PRIMARY KEY (`history_id`)
);

CREATE TABLE `Segment_member` (
    `segment_member_seq`   BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`            BIGINT NOT NULL,
    `segment_id`           BIGINT NOT NULL,
    `segment_member_joined` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`segment_member_seq`)
);

CREATE TABLE `Campaign` (
    `campaign_id`        BIGINT NOT NULL AUTO_INCREMENT,
    `campaign_name`      VARCHAR(200) NOT NULL,
    `campaign_status`    ENUM('SCHEDULED','RUNNING','COMPLETED') NOT NULL,
    `campaign_scheduled` TIMESTAMP NOT NULL,
    `campaign_content`   TEXT NOT NULL,
    PRIMARY KEY (`campaign_id`)
);

CREATE TABLE `Member_rfm` (
    `rfm_id`         BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`      BIGINT NOT NULL,
    `rfm_receny_days` INT NULL,
    `rfm_frequency`   INT NULL,
    `rfm_monetary`    INT NULL,
    `rfm_r_score`     TINYINT NULL,
    `rfm_f_score`     TINYINT NULL,
    `rfm_m_score`     TINYINT NULL,
    `rfm_total_score` TINYINT NULL,
    `rfm_snapshot`    TIMESTAMP NULL,
    PRIMARY KEY (`rfm_id`)
);

CREATE TABLE `Member_grade` (
    `grade_code` INT NOT NULL AUTO_INCREMENT,
    `grade_name` ENUM('BRONZE','SILVER','GOLD','VIP') NOT NULL,
    `grade_desc` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`grade_code`)
);

CREATE TABLE `Member` (
    `member_id`        BIGINT NOT NULL AUTO_INCREMENT,
    `grade_code`       INT NOT NULL,
    `member_name`      VARCHAR(15) NOT NULL,
    `member_gender`    CHAR(1) NOT NULL,
    `member_phone`     VARCHAR(20) NOT NULL,
    `member_birth`     DATE NOT NULL,
    `member_pwd`       VARCHAR(255) NOT NULL,
    `member_email`     VARCHAR(30) NOT NULL,
    `member_status`    ENUM('active','withdrawn') NOT NULL DEFAULT 'active',
    `member_created`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `member_updated`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `member_withdrawn` TIMESTAMP NULL,
    `member_last_at`   TIMESTAMP NOT NULL,
    PRIMARY KEY (`member_id`)
);

CREATE TABLE `Segment` (
    `segment_id`   BIGINT NOT NULL AUTO_INCREMENT,
    `segment_name` VARCHAR(255) NOT NULL,
    `segment_rule` JSON NULL,
    PRIMARY KEY (`segment_id`)
);

CREATE TABLE `Message_send_log` (
    `send_id`      BIGINT NOT NULL AUTO_INCREMENT,
    `campaign_id`  BIGINT NOT NULL,
    `member_id`    BIGINT NOT NULL,
    `send_at`      TIMESTAMP NOT NULL,
    `send_clicked` TIMESTAMP NULL,
    PRIMARY KEY (`send_id`)
);

CREATE TABLE `Member_profile` (
    `profile_seq`           BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`             BIGINT NOT NULL,
    `profile_address`       VARCHAR(200) NOT NULL,
    `profile_detail_address` VARCHAR(200) NOT NULL,
    `profile_preferred`     VARCHAR(200) NULL,
    `profile_interests`     VARCHAR(200) NULL,
    PRIMARY KEY (`profile_seq`)
);

CREATE TABLE `Member_grade_history` (
    `history_id`     BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`      BIGINT NOT NULL,
    `grade_code`     INT NOT NULL,
    `history_before` ENUM('BRONZE','SILVER','GOLD','VIP') NULL,
    `history_after`  ENUM('BRONZE','SILVER','GOLD','VIP') NULL,
    `history_changed` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`history_id`)
);

CREATE TABLE `CS_ticket` (
    `cs_ticket_id`      BIGINT NOT NULL AUTO_INCREMENT,
    `member_id`         BIGINT NOT NULL,
    `cs_ticket_channel` ENUM('WEB','MOBILE','CALL','EMAIL') NOT NULL,
    `cs_ticket_category` VARCHAR(50) NOT NULL,
    `cs_ticket_status`  ENUM('RECEIVED','COMPLETED','REJECTED') NOT NULL,
    `cs_ticket_title`   VARCHAR(200) NOT NULL,
    `cs_ticket_content` TEXT NOT NULL,
    `cs_ticket_created` TIMESTAMP NOT NULL,
    PRIMARY KEY (`cs_ticket_id`)
);

CREATE TABLE `Product` (
    `product_id`       BIGINT NOT NULL AUTO_INCREMENT,
    `product_name`     VARCHAR(50) NOT NULL,
    `product_category` VARCHAR(30) NOT NULL,
    `product_registed` TIMESTAMP NOT NULL,
    `product_quantity` INT NOT NULL,
    `product_price`    INT NOT NULL,
    `product_status`   ENUM('ACTIVE','INACTIVE','DELETED') NOT NULL,
    `product_updated`  TIMESTAMP NOT NULL,
    PRIMARY KEY (`product_id`)
);

CREATE TABLE `Return_request` (
    `request_id`        BIGINT NOT NULL AUTO_INCREMENT,
    `purchase_id`       BIGINT NOT NULL,
    `request_reason`    TEXT NOT NULL,
    `request_status`    ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
    `request_processed` TIMESTAMP NULL,
    PRIMARY KEY (`request_id`)
);

CREATE TABLE `purchase_detail` (
    `purchase_detail_id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id`         BIGINT NOT NULL,
    `purchase_id`        BIGINT NOT NULL,
    `purchase_status`    ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
    `purchase_processed` TIMESTAMP NULL,
    `product_quantity`   INT NOT NULL,
    `purchase_paid_amount` INT NOT NULL,
    PRIMARY KEY (`purchase_detail_id`)
);

CREATE TABLE `Review` (
    `review_id`       BIGINT NOT NULL AUTO_INCREMENT,
    `review_title`    VARCHAR(100) NOT NULL,
    `review_content`  TEXT NOT NULL,
    `review_score`    INT NOT NULL,
    `review_created`  TIMESTAMP NOT NULL,
    `review_updated`  TIMESTAMP NULL,
    `review_deleted`  TIMESTAMP NULL,
    `field`           VARCHAR(255) NULL,
    PRIMARY KEY (`review_id`)
);

CREATE TABLE `Review_comment` (
    `review_comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `review_id`         BIGINT NOT NULL,
    `review_comment`    VARCHAR(100) NOT NULL,
    `review_content`    TEXT NOT NULL,
    `review_score`      INT NOT NULL,
    `review_created`    TIMESTAMP NOT NULL,
    `review_updated`    TIMESTAMP NULL,
    `review_deleted`    TIMESTAMP NULL,
    `field`             VARCHAR(255) NULL,
    PRIMARY KEY (`review_comment_id`)
);

SET FOREIGN_KEY_CHECKS=1;

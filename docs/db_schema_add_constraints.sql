-- 사용 명령어: mysql -u ohgiraffers -p shoppingmalldb < "db_schema_add_constraints.sql"
SET FOREIGN_KEY_CHECKS=0;

-- Drop tables in reverse order of dependency
DROP TABLE IF EXISTS Review_comment;
DROP TABLE IF EXISTS Review;
DROP TABLE IF EXISTS purchase_detail;
DROP TABLE IF EXISTS Return_request;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS CS_ticket_reply;
DROP TABLE IF EXISTS CS_ticket;
DROP TABLE IF EXISTS Member_grade_history;
DROP TABLE IF EXISTS Member_profile;
DROP TABLE IF EXISTS Message_send_log;
DROP TABLE IF EXISTS Segment_member;
DROP TABLE IF EXISTS Member_rfm;
DROP TABLE IF EXISTS Campaign_target_segment;
DROP TABLE IF EXISTS Campaign;
DROP TABLE IF EXISTS Segment;
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS Member_grade;
DROP TABLE IF EXISTS History;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS Cart_item;
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS Purchase;

-- Create tables
CREATE TABLE Campaign_target_segment (
target_segment_id BIGINT NOT NULL AUTO_INCREMENT,
segment_id BIGINT NOT NULL,
campaign_id BIGINT NOT NULL,
PRIMARY KEY (target_segment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE CS_ticket_reply (
reply_id BIGINT NOT NULL AUTO_INCREMENT,
cs_ticket_id BIGINT NOT NULL,
reply_responder_id BIGINT NOT NULL,
reply_content TEXT NOT NULL,
reply_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접수,처리중,처리완료,거절',
PRIMARY KEY (reply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Purchase (
purchase_id BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
purchase_status ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
purchase_processed TIMESTAMP NULL,
PRIMARY KEY (purchase_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Cart (
cart_id BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
PRIMARY KEY (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Cart_item (
cart_item_id BIGINT NOT NULL AUTO_INCREMENT,
cart_id BIGINT NOT NULL,
product_id BIGINT NOT NULL,
cart_item_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
cart_item_status ENUM('ACTIVE','REMOVED','UPDATED') NOT NULL,
cart_item_quantity INT NOT NULL,
PRIMARY KEY (cart_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Payment (
payment_id BIGINT NOT NULL AUTO_INCREMENT,
purchase_id BIGINT NOT NULL,
payment_status ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
payment_created DATETIME NOT NULL,
payment_type ENUM('PAYMENT','REFUND') NOT NULL,
payment_tid VARCHAR(20) NOT NULL,
payment_paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE History (
history_id BIGINT NOT NULL AUTO_INCREMENT,
history_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
history_action_type VARCHAR(50) NOT NULL,
history_action_category ENUM('AUTH','VIEW','ENGAGE','PURCHASE','CS','SYSTEM') NOT NULL,
history_member_id BIGINT NOT NULL,
history_detail JSON NOT NULL,
history_ip_address VARCHAR(100) NOT NULL,
history_ref_tbl ENUM(
    'campaign', 'cart', 'cart_item', 'cs_ticket',
    'member', 'payment', 'product', 'purchase',
    'return_request', 'review', 'segment'
    ) NULL,
history_ref_id BIGINT NULL,
history_user_agent VARCHAR(512) NULL,
PRIMARY KEY (history_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Segment_member (
segment_member_seq BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
segment_id BIGINT NOT NULL,
segment_member_joined TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (segment_member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Campaign (
campaign_id BIGINT NOT NULL AUTO_INCREMENT,
campaign_name VARCHAR(200) NOT NULL,
campaign_status ENUM('SCHEDULED','RUNNING','COMPLETED') NOT NULL,
campaign_scheduled TIMESTAMP NOT NULL,
campaign_content TEXT NOT NULL,
PRIMARY KEY (campaign_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Member_rfm (
rfm_id BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
rfm_receny_days INT NULL,
rfm_frequency INT NULL,
rfm_monetary INT NULL,
rfm_r_score TINYINT NULL,
rfm_f_score TINYINT NULL,
rfm_m_score TINYINT NULL,
rfm_total_score TINYINT NULL,
rfm_snapshot TIMESTAMP NULL,
PRIMARY KEY (rfm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Member_grade (
grade_code INT NOT NULL AUTO_INCREMENT,
grade_name ENUM('BRONZE','SILVER','GOLD','VIP') NOT NULL,
grade_desc VARCHAR(255) NOT NULL,
PRIMARY KEY (grade_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Member (
member_id BIGINT NOT NULL AUTO_INCREMENT,
grade_code INT NOT NULL,
member_name VARCHAR(15) NOT NULL,
member_gender CHAR(1) NOT NULL,
member_phone VARCHAR(20) NOT NULL,
member_birth DATE NOT NULL,
member_pwd VARCHAR(255) NOT NULL,
member_email VARCHAR(30) NOT NULL,
member_status ENUM('active','withdrawn') NOT NULL DEFAULT 'active',
member_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
member_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
member_withdrawn TIMESTAMP NULL,
member_last_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
member_role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
PRIMARY KEY (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Segment (
segment_id BIGINT NOT NULL AUTO_INCREMENT,
segment_name VARCHAR(255) NOT NULL,
segment_rule JSON NULL,
CHECK (segment_rule IS NULL OR JSON_VALID(segment_rule)),
PRIMARY KEY (segment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Message_send_log (
send_id BIGINT NOT NULL AUTO_INCREMENT,
campaign_id BIGINT NOT NULL,
member_id BIGINT NOT NULL,
send_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
send_clicked TIMESTAMP NULL,
PRIMARY KEY (send_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Member_profile (
profile_seq BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
profile_address VARCHAR(200) NOT NULL,
profile_detail_address VARCHAR(200) NOT NULL,
profile_preferred VARCHAR(200) NULL,
profile_interests VARCHAR(200) NULL,
PRIMARY KEY (profile_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Member_grade_history (
history_id BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
grade_code INT NOT NULL,
history_before ENUM('BRONZE','SILVER','GOLD','VIP') NULL,
history_after ENUM('BRONZE','SILVER','GOLD','VIP') NULL,
history_changed TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (history_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE CS_ticket (
cs_ticket_id BIGINT NOT NULL AUTO_INCREMENT,
member_id BIGINT NOT NULL,
cs_ticket_channel ENUM('WEB','MOBILE','CALL','EMAIL') NOT NULL,
cs_ticket_category VARCHAR(50) NOT NULL,
cs_ticket_status ENUM('RECEIVED','COMPLETED','REJECTED') NOT NULL,
cs_ticket_title VARCHAR(200) NOT NULL,
cs_ticket_content TEXT NOT NULL,
cs_ticket_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (cs_ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Product (
product_id BIGINT NOT NULL AUTO_INCREMENT,
product_name VARCHAR(50) NOT NULL,
product_category VARCHAR(30) NOT NULL,
product_registed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
product_quantity INT NOT NULL,
product_price INT NOT NULL,
product_status ENUM('ACTIVE','INACTIVE','DELETED') NOT NULL,
product_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Return_request (
request_id BIGINT NOT NULL AUTO_INCREMENT,
purchase_id BIGINT NOT NULL,
request_reason TEXT NOT NULL,
request_status ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
request_processed TIMESTAMP NULL,
PRIMARY KEY (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase_detail (
purchase_detail_id BIGINT NOT NULL AUTO_INCREMENT,
product_id BIGINT NOT NULL,
purchase_id BIGINT NOT NULL,
purchase_status ENUM('REQUESTED','REJECTED','COMPLETED') NOT NULL,
purchase_processed TIMESTAMP NULL,
product_quantity INT NOT NULL,
purchase_paid_amount INT NOT NULL,
PRIMARY KEY (purchase_detail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Review (
review_id BIGINT NOT NULL AUTO_INCREMENT,
review_title VARCHAR(100) NOT NULL,
review_content TEXT NOT NULL,
review_score INT NOT NULL,
review_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
review_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
review_deleted TIMESTAMP NULL,
field VARCHAR(255) NULL,
PRIMARY KEY (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Review_comment (
review_comment_id BIGINT NOT NULL AUTO_INCREMENT,
review_id BIGINT NOT NULL,
review_comment VARCHAR(100) NOT NULL,
review_content TEXT NOT NULL,
review_score INT NOT NULL,
review_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
review_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
review_deleted TIMESTAMP NULL,
field VARCHAR(255) NULL,
PRIMARY KEY (review_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Foreign Keys
ALTER TABLE Campaign_target_segment ADD CONSTRAINT FK_Segment_TO_Campaign_target_segment_1 FOREIGN KEY (segment_id) REFERENCES Segment (segment_id);
ALTER TABLE Campaign_target_segment ADD CONSTRAINT FK_Campaign_TO_Campaign_target_segment_1 FOREIGN KEY (campaign_id) REFERENCES Campaign (campaign_id);
ALTER TABLE CS_ticket_reply ADD CONSTRAINT FK_CS_ticket_TO_CS_ticket_reply_1 FOREIGN KEY (cs_ticket_id) REFERENCES CS_ticket (cs_ticket_id);
ALTER TABLE Purchase ADD CONSTRAINT FK_Member_TO_Purchase_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Cart ADD CONSTRAINT FK_Member_TO_Cart_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Cart_item ADD CONSTRAINT FK_Cart_TO_Cart_item_1 FOREIGN KEY (cart_id) REFERENCES Cart (cart_id);
ALTER TABLE Cart_item ADD CONSTRAINT FK_Product_TO_Cart_item_1 FOREIGN KEY (product_id) REFERENCES Product (product_id);
ALTER TABLE Payment ADD CONSTRAINT FK_Purchase_TO_Payment_1 FOREIGN KEY (purchase_id) REFERENCES Purchase (purchase_id);
ALTER TABLE Segment_member ADD CONSTRAINT FK_Member_TO_Segment_member_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Segment_member ADD CONSTRAINT FK_Segment_TO_Segment_member_1 FOREIGN KEY (segment_id) REFERENCES Segment (segment_id);
ALTER TABLE Member_rfm ADD CONSTRAINT FK_Member_TO_Member_rfm_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Member ADD CONSTRAINT FK_Member_grade_TO_Member_1 FOREIGN KEY (grade_code) REFERENCES Member_grade (grade_code);
ALTER TABLE Message_send_log ADD CONSTRAINT FK_Campaign_TO_Message_send_log_1 FOREIGN KEY (campaign_id) REFERENCES Campaign (campaign_id);
ALTER TABLE Message_send_log ADD CONSTRAINT FK_Member_TO_Message_send_log_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Member_profile ADD CONSTRAINT FK_Member_TO_Member_profile_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Member_grade_history ADD CONSTRAINT FK_Member_TO_Member_grade_history_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Member_grade_history ADD CONSTRAINT FK_Member_grade_TO_Member_grade_history_1 FOREIGN KEY (grade_code) REFERENCES Member_grade (grade_code);
ALTER TABLE CS_ticket ADD CONSTRAINT FK_Member_TO_CS_ticket_1 FOREIGN KEY (member_id) REFERENCES Member (member_id);
ALTER TABLE Return_request ADD CONSTRAINT FK_Purchase_TO_Return_request_1 FOREIGN KEY (purchase_id) REFERENCES Purchase (purchase_id);
ALTER TABLE purchase_detail ADD CONSTRAINT FK_Product_TO_purchase_detail_1 FOREIGN KEY (product_id) REFERENCES Product (product_id);
ALTER TABLE purchase_detail ADD CONSTRAINT FK_Purchase_TO_purchase_detail_1 FOREIGN KEY (purchase_id) REFERENCES Purchase (purchase_id);
ALTER TABLE Review_comment ADD CONSTRAINT FK_Review_TO_Review_comment_1 FOREIGN KEY (review_id) REFERENCES Review (review_id);

SET FOREIGN_KEY_CHECKS=1;
-- 사용 예: mysql -u ohgiraffers -p shoppingmalldb < "dummy_data_tripled_reset_fix.sql"

-- 데이터베이스 지정(환경에 맞게 조정)
USE shoppingmalldb;

-- =======================================================
-- A) 프로시저 사전 삭제(재실행 시 ERROR 1304 방지)
-- =======================================================
DROP PROCEDURE IF EXISTS seed_products;
DROP PROCEDURE IF EXISTS seed_members;
DROP PROCEDURE IF EXISTS generate_purchase_and_cart_data;
DROP PROCEDURE IF EXISTS generate_return_and_refund_data;
DROP PROCEDURE IF EXISTS generate_review_and_comment_data;
DROP PROCEDURE IF EXISTS generate_cs_ticket_and_reply_data;
DROP PROCEDURE IF EXISTS generate_message_logs;
DROP PROCEDURE IF EXISTS generate_rfm_data;
DROP PROCEDURE IF EXISTS generate_history_logs;

-- =======================================================
-- B) 기존 데이터 전부 삭제(TRUNCATE 시에만 FK OFF)
-- =======================================================
SET FOREIGN_KEY_CHECKS=0;

TRUNCATE TABLE `Review_comment`;
TRUNCATE TABLE `Review`;
TRUNCATE TABLE `CS_ticket_reply`;
TRUNCATE TABLE `CS_ticket`;
TRUNCATE TABLE `Message_send_log`;
TRUNCATE TABLE `Campaign_target_segment`;
TRUNCATE TABLE `Campaign`;
TRUNCATE TABLE `Segment_member`;
TRUNCATE TABLE `Segment`;
TRUNCATE TABLE `Member_rfm`;
TRUNCATE TABLE `Return_request`;
TRUNCATE TABLE `Payment`;
TRUNCATE TABLE `purchase_detail`;
TRUNCATE TABLE `Purchase`;
TRUNCATE TABLE `Cart_item`;
TRUNCATE TABLE `Cart`;
TRUNCATE TABLE `Member_grade_history`;
TRUNCATE TABLE `Member_profile`;
TRUNCATE TABLE `Member`;
TRUNCATE TABLE `Member_grade`;
TRUNCATE TABLE `Product`;
TRUNCATE TABLE `History`;

SET FOREIGN_KEY_CHECKS=1;

-- =======================================================
-- C) 기준 코드/정적 데이터
-- =======================================================
INSERT INTO `Member_grade` (`grade_code`,`grade_name`,`grade_desc`) VALUES
(1,'BRONZE','브론즈 등급'),
(2,'SILVER','실버 등급'),
(3,'GOLD','골드 등급'),
(4,'VIP','VIP 등급');

-- 150개 Product 생성
DELIMITER $$
CREATE PROCEDURE seed_products()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE cat VARCHAR(30);
  WHILE i <= 150 DO
    SET cat = ELT(1 + (i % 12),
      '가전','주방용품','반려동물','스포츠','스킨케어','헤어케어',
      'PC용품','도서','음향기기','여행','생활용품','패션잡화');
    INSERT INTO `Product`
      (`product_name`,`product_category`,`product_registed`,`product_quantity`,`product_price`,`product_status`,`product_updated`)
    VALUES
      (CONCAT('상품-',LPAD(i,3,'0')), cat, NOW(), 50 + (i*5), 5000 + (i*1000), 'ACTIVE', NOW());
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL seed_products();
DROP PROCEDURE seed_products;

-- 90명 Member 생성
DELIMITER $$
CREATE PROCEDURE seed_members()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE g INT;
  DECLARE gender CHAR(1);
  DECLARE role VARCHAR(5);
  WHILE i <= 90 DO
    SET g = ELT(1 + (i % 4), 1,2,3,4);
    SET gender = ELT(1 + (i % 2), 'M','F');
    SET role = CASE WHEN i IN (1,8,17) THEN 'ADMIN' ELSE 'USER' END;
    INSERT INTO `Member`
      (`grade_code`,`member_name`,`member_gender`,`member_phone`,`member_birth`,`member_pwd`,
       `member_email`,`member_status`,`member_role`,`member_created`,`member_updated`,`member_last_at`)
    VALUES
      (g, CONCAT('사용자',LPAD(i,3,'0')), gender, CONCAT('010-1111-',LPAD(i,4,'0')),
       DATE_SUB('1990-01-01', INTERVAL (i*100) DAY), 'hashed_password',
       CONCAT('user',i,'@example.com'), 'active', role,
       NOW() - INTERVAL (i*3) DAY, NOW() - INTERVAL (i*3) DAY, NOW());
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL seed_members();
DROP PROCEDURE seed_members;

-- 파생 데이터
INSERT INTO `Member_profile` (`member_id`,`profile_address`,`profile_detail_address`,`profile_preferred`,`profile_interests`)
SELECT `member_id`, '서울시 강남구 테헤란로', '123-456', '캐주얼, 비비드 컬러', '영화, 음악, 여행' FROM `Member`;

INSERT INTO `Member_grade_history` (`member_id`,`grade_code`,`history_before`,`history_after`,`history_changed`)
SELECT m.member_id, m.grade_code, NULL,
       (SELECT grade_name FROM Member_grade WHERE grade_code=m.grade_code),
       m.member_created
FROM `Member` m;

INSERT INTO `Cart` (`member_id`)
SELECT `member_id` FROM `Member`;

-- =======================================================
-- D) 구매/결제/장바구니
-- =======================================================
DELIMITER $$
CREATE PROCEDURE generate_purchase_and_cart_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE j INT;
  DECLARE num_purchases INT;
  DECLARE random_product_id INT;
  DECLARE random_quantity INT;
  DECLARE product_price_val INT;
  DECLARE total_amount INT;
  DECLARE purchase_date TIMESTAMP;
  DECLARE p_status VARCHAR(16);
  DECLARE v_cart_id BIGINT;

  WHILE i <= 90 DO
    SET num_purchases = FLOOR(3 + RAND()*8); -- 3~10회
    SET j = 1;
    WHILE j <= num_purchases DO
      SET purchase_date = NOW() - INTERVAL FLOOR(1 + RAND()*120) DAY;
      SET p_status = ELT(FLOOR(1 + RAND()*3), 'COMPLETED','REQUESTED','REJECTED');

      INSERT INTO `Purchase` (`member_id`,`purchase_status`,`purchase_processed`)
      VALUES (i, p_status, purchase_date);
      SET @v_purchase_id := LAST_INSERT_ID();

      IF p_status <> 'REJECTED' THEN
        SET random_product_id = FLOOR(1 + RAND()*150);
        SET random_quantity = FLOOR(1 + RAND()*2);
        SELECT `product_price` INTO product_price_val FROM `Product` WHERE `product_id` = random_product_id;
        SET total_amount = product_price_val * random_quantity;

        INSERT INTO `purchase_detail`
          (`product_id`,`purchase_id`,`purchase_status`,`purchase_processed`,`product_quantity`,`purchase_paid_amount`)
        VALUES
          (random_product_id, @v_purchase_id, p_status, purchase_date, random_quantity, total_amount);

        INSERT INTO `Payment`
          (`purchase_id`,`payment_status`,`payment_created`,`payment_type`,`payment_tid`,`payment_paid_at`)
        VALUES
          (@v_purchase_id, p_status, purchase_date, 'PAYMENT', CONCAT('TID', LPAD(@v_purchase_id, 6, '0')), purchase_date);
      END IF;

      SET j = j + 1;
    END WHILE;

    IF RAND() > 0.5 THEN
      SELECT cart_id INTO v_cart_id FROM `Cart` WHERE `member_id` = i LIMIT 1;
      SET j = 1;
      WHILE j <= FLOOR(1 + RAND()*3) DO
        SET random_product_id = FLOOR(1 + RAND()*150);
        INSERT INTO `Cart_item` (`cart_id`,`product_id`,`cart_item_updated`,`cart_item_status`,`cart_item_quantity`)
        VALUES (v_cart_id, random_product_id, NOW() - INTERVAL FLOOR(RAND()*5) DAY, 'ACTIVE', FLOOR(1 + RAND()*2));
        SET j = j + 1;
      END WHILE;
    END IF;

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_purchase_and_cart_data();
DROP PROCEDURE generate_purchase_and_cart_data;

-- =======================================================
-- E) 반품/환불
-- =======================================================
DELIMITER $$
CREATE PROCEDURE generate_return_and_refund_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE completed_purchase_id BIGINT;
  DECLARE return_status VARCHAR(16);
  DECLARE payment_counter BIGINT;
  SELECT COALESCE(MAX(payment_id),0) + 1 INTO payment_counter FROM Payment;

  WHILE i <= 150 DO
    SELECT purchase_id INTO completed_purchase_id
    FROM Purchase WHERE purchase_status='COMPLETED'
    ORDER BY RAND() LIMIT 1;

    IF completed_purchase_id IS NOT NULL THEN
      SET return_status = ELT(FLOOR(1 + RAND()*3), 'REQUESTED','COMPLETED','REJECTED');

      INSERT INTO `Return_request` (`purchase_id`,`request_reason`,`request_status`,`request_processed`)
      VALUES (completed_purchase_id,
              ELT(FLOOR(1 + RAND()*3),'단순 변심','상품 불량','사이즈/색상 불만'),
              return_status,
              NOW() - INTERVAL FLOOR(RAND()*10) DAY);

      IF return_status='COMPLETED' THEN
        INSERT INTO `Payment`
          (`payment_id`,`purchase_id`,`payment_status`,`payment_created`,`payment_type`,`payment_tid`,`payment_paid_at`)
        VALUES
          (payment_counter, completed_purchase_id, 'COMPLETED',
           NOW() - INTERVAL FLOOR(RAND()*9) DAY, 'REFUND',
           CONCAT('TID_R', LPAD(payment_counter,5,'0')),
           NOW() - INTERVAL FLOOR(RAND()*9) DAY);
        SET payment_counter = payment_counter + 1;
      END IF;
    END IF;

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_return_and_refund_data();
DROP PROCEDURE generate_return_and_refund_data;

-- =======================================================
-- F) 리뷰/댓글
-- =======================================================
DELIMITER $$
CREATE PROCEDURE generate_review_and_comment_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_product_id BIGINT;
  DECLARE v_member_id BIGINT;
  DECLARE admin_id BIGINT;

  WHILE i <= 300 DO
    SELECT pd.product_id, p.member_id INTO v_product_id, v_member_id
    FROM purchase_detail pd
    JOIN Purchase p ON pd.purchase_id = p.purchase_id
    WHERE p.purchase_status='COMPLETED'
    ORDER BY RAND() LIMIT 1;

    IF v_product_id IS NOT NULL THEN
      INSERT INTO `Review` (`review_title`,`review_content`,`review_score`,`review_created`)
      VALUES (CONCAT('상품(', v_product_id, ') 후기'),
              '정말 마음에 듭니다. 배송도 빨랐어요.',
              FLOOR(3 + RAND()*3),
              NOW() - INTERVAL FLOOR(RAND()*60) DAY);
      SET @v_review_id := LAST_INSERT_ID();

      IF RAND() > 0.5 THEN
        SELECT member_id INTO admin_id FROM Member WHERE member_role='ADMIN' ORDER BY RAND() LIMIT 1;
        INSERT INTO `Review_comment`
          (`review_id`,`review_comment`,`review_content`,`review_score`,`review_created`)
        VALUES
          (@v_review_id, '소중한 후기 감사합니다!',
           '고객님의 소중한 후기에 감사드립니다. 앞으로도 좋은 상품으로 보답하겠습니다.',
           5, NOW() - INTERVAL FLOOR(RAND()*59) DAY);
      END IF;
    END IF;

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_review_and_comment_data();
DROP PROCEDURE generate_review_and_comment_data;

-- =======================================================
-- G) CS 티켓/답변
-- =======================================================
DELIMITER $$
CREATE PROCEDURE generate_cs_ticket_and_reply_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE admin_id BIGINT;
  DECLARE ticket_status VARCHAR(16);

  WHILE i <= 210 DO
    SET ticket_status = ELT(FLOOR(1 + RAND()*3), 'RECEIVED','COMPLETED','REJECTED');

    INSERT INTO `CS_ticket`
      (`member_id`,`cs_ticket_channel`,`cs_ticket_category`,`cs_ticket_status`,`cs_ticket_title`,`cs_ticket_content`,`cs_ticket_created`)
    VALUES
      (FLOOR(1 + RAND()*90),
       ELT(FLOOR(1 + RAND()*4),'WEB','MOBILE','CALL','EMAIL'),
       ELT(FLOOR(1 + RAND()*5),'배송문의','결제문의','상품문의','반품/환불','기타'),
       ticket_status,
       CONCAT('문의 드립니다 (', i, ')'),
       '자세한 문의 내용입니다.',
       NOW() - INTERVAL FLOOR(RAND()*60) DAY);

    SET @v_ticket_id := LAST_INSERT_ID();

    IF ticket_status <> 'RECEIVED' AND RAND() > 0.2 THEN
      SELECT member_id INTO admin_id FROM Member WHERE member_role='ADMIN' ORDER BY RAND() LIMIT 1;
      INSERT INTO `CS_ticket_reply` (`cs_ticket_id`,`reply_responder_id`,`reply_content`,`reply_created`)
      VALUES (@v_ticket_id, admin_id, '안녕하세요, 고객님. 문의주신 내용에 대해 답변 드립니다.',
              NOW() - INTERVAL FLOOR(RAND()*59) DAY);
    END IF;

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_cs_ticket_and_reply_data();
DROP PROCEDURE generate_cs_ticket_and_reply_data;

-- =======================================================
-- H) 세그먼트/캠페인/메시지/RFM/History
-- =======================================================
INSERT INTO `Segment` (`segment_name`,`segment_rule`) VALUES
('VIP 고객',       '{"grade":"VIP"}'),
('신규 가입 30일', '{"join_date":"last_30_days"}'),
('최근 60일 미구매','{"last_purchase":"over_60_days"}'),
('마케팅 동의',     '{"agreement":"marketing_email"}'),
('20대 여성',       '{"age_range":"20-29","gender":"F"}'),
('30대 남성',       '{"age_range":"30-39","gender":"M"}'),
('반려동물 구매',   '{"purchase_category":"반려동물"}');

INSERT INTO `Segment_member` (`member_id`,`segment_id`) SELECT member_id, (SELECT segment_id FROM Segment WHERE segment_name='VIP 고객') FROM Member WHERE grade_code=4;
INSERT INTO `Segment_member` (`member_id`,`segment_id`) SELECT member_id, (SELECT segment_id FROM Segment WHERE segment_name='신규 가입 30일') FROM Member WHERE member_created >= NOW() - INTERVAL 30 DAY;
INSERT INTO `Segment_member` (`member_id`,`segment_id`) SELECT member_id, (SELECT segment_id FROM Segment WHERE segment_name='마케팅 동의') FROM Member;
INSERT INTO `Segment_member` (`member_id`,`segment_id`)
SELECT m.member_id, (SELECT segment_id FROM Segment WHERE segment_name='20대 여성')
FROM Member m WHERE m.member_gender='F' AND (YEAR(CURDATE())-YEAR(m.member_birth)) BETWEEN 20 AND 29;
INSERT INTO `Segment_member` (`member_id`,`segment_id`)
SELECT m.member_id, (SELECT segment_id FROM Segment WHERE segment_name='30대 남성')
FROM Member m WHERE m.member_gender='M' AND (YEAR(CURDATE())-YEAR(m.member_birth)) BETWEEN 30 AND 39;
INSERT INTO `Segment_member` (`member_id`,`segment_id`)
SELECT DISTINCT pu.member_id, (SELECT segment_id FROM Segment WHERE segment_name='반려동물 구매')
FROM Purchase pu
JOIN purchase_detail pd ON pu.purchase_id=pd.purchase_id
JOIN Product p ON pd.product_id=p.product_id
WHERE p.product_category='반려동물';

INSERT INTO `Segment_member` (`member_id`,`segment_id`)
SELECT m.member_id, (SELECT segment_id FROM Segment WHERE segment_name='최근 60일 미구매')
FROM Member m
LEFT JOIN (SELECT member_id, MAX(purchase_processed) AS last_purchase_date FROM Purchase GROUP BY member_id) x
  ON m.member_id=x.member_id
WHERE x.last_purchase_date IS NULL OR x.last_purchase_date < NOW() - INTERVAL 60 DAY;

INSERT INTO `Campaign` (`campaign_name`,`campaign_status`,`campaign_scheduled`,`campaign_content`) VALUES
('가을맞이 15% 할인 캠페인', 'COMPLETED', '2023-09-15 09:00:00', '전 상품 15% 할인 쿠폰 증정'),
('VIP 고객 감사 이벤트',     'RUNNING',   '2023-10-25 09:00:00', 'VIP 고객님께만 드리는 특별 사은품 증정'),
('신규 고객 웰컴 쿠폰',      'SCHEDULED', '2023-11-01 09:00:00', '가입 후 첫 구매 시 10,000원 할인'),
('휴면 고객 활성화 프로모션','SCHEDULED', '2023-11-10 09:00:00', '복귀 시 20% 할인 쿠폰');

INSERT INTO `Campaign_target_segment` (`campaign_id`,`segment_id`)
SELECT (SELECT MIN(campaign_id) FROM Campaign WHERE campaign_name='가을맞이 15% 할인 캠페인'),
       (SELECT segment_id FROM Segment WHERE segment_name='마케팅 동의')
UNION ALL
SELECT (SELECT MIN(campaign_id) FROM Campaign WHERE campaign_name='VIP 고객 감사 이벤트'),
       (SELECT segment_id FROM Segment WHERE segment_name='VIP 고객')
UNION ALL
SELECT (SELECT MIN(campaign_id) FROM Campaign WHERE campaign_name='신규 고객 웰컴 쿠폰'),
       (SELECT segment_id FROM Segment WHERE segment_name='신규 가입 30일')
UNION ALL
SELECT (SELECT MIN(campaign_id) FROM Campaign WHERE campaign_name='휴면 고객 활성화 프로모션'),
       (SELECT segment_id FROM Segment WHERE segment_name='최근 60일 미구매');

-- 발송 로그 450건
DELIMITER $$
CREATE PROCEDURE generate_message_logs()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 450 DO
    INSERT INTO `Message_send_log` (`campaign_id`,`member_id`,`send_at`,`send_clicked`)
    VALUES (
      FLOOR(1 + RAND()*4),
      FLOOR(1 + RAND()*90),
      NOW() - INTERVAL FLOOR(RAND()*30) DAY,
      CASE WHEN RAND() > 0.7 THEN NOW() - INTERVAL FLOOR(RAND()*29) DAY ELSE NULL END
    );
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_message_logs();
DROP PROCEDURE generate_message_logs;

-- RFM(90명)
DELIMITER $$
CREATE PROCEDURE generate_rfm_data()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE r_score, f_score, m_score TINYINT;
  WHILE i <= 90 DO
    SET r_score = FLOOR(1 + RAND()*5);
    SET f_score = FLOOR(1 + RAND()*5);
    SET m_score = FLOOR(1 + RAND()*5);
    INSERT INTO `Member_rfm`
      (`member_id`,`rfm_receny_days`,`rfm_frequency`,`rfm_monetary`,`rfm_r_score`,`rfm_f_score`,`rfm_m_score`,`rfm_total_score`,`rfm_snapshot`)
    VALUES
      (i, FLOOR(1 + RAND()*90), FLOOR(5 + RAND()*20), FLOOR(100000 + RAND()*1000000),
       r_score, f_score, m_score, r_score+f_score+m_score, NOW());
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_rfm_data();
DROP PROCEDURE generate_rfm_data;

-- History 600건
DELIMITER $$
CREATE PROCEDURE generate_history_logs()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 600 DO
    INSERT INTO `History` (`history_datetime`,`history_action_type`,`history_member_id`,`history_detail`,`history_ip_address`,`history_ref_tbl`)
    VALUES (
      NOW() - INTERVAL FLOOR(RAND()*10000) MINUTE,
      ELT(FLOOR(1 + RAND()*7),'CREATE','UPDATE','DELETE','LOGIN','LOGOUT','PAID','REFUND'),
      FLOOR(1 + RAND()*90),
      CONCAT('Action log entry #', i),
      CONCAT('192.168.1.', FLOOR(1 + RAND()*254)),
      ELT(FLOOR(1 + RAND()*7),'Member','Purchase','Segment','CS_ticket','Product','Return_request','CART_ITEM')
    );
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;
CALL generate_history_logs();
DROP PROCEDURE generate_history_logs;

-- =======================================================
-- I) 임포트 직후 점검(필요 시 주석 해제; 결과 0 기대)
-- =======================================================
-- SHOW PROCEDURE STATUS WHERE Db = DATABASE();
-- SELECT COUNT(*) FROM Payment p LEFT JOIN Purchase pu ON p.purchase_id=pu.purchase_id WHERE pu.purchase_id IS NULL;
-- SELECT COUNT(*) FROM purchase_detail d LEFT JOIN Purchase pu ON d.purchase_id=pu.purchase_id WHERE pu.purchase_id IS NULL;
-- SELECT COUNT(*) FROM purchase_detail d LEFT JOIN Product pr ON d.product_id=pr.product_id WHERE pr.product_id IS NULL;
-- SELECT COUNT(*) FROM Return_request r LEFT JOIN Purchase pu ON r.purchase_id=pu.purchase_id WHERE pu.purchase_id IS NULL;
-- SELECT COUNT(*) FROM CS_ticket_reply r LEFT JOIN CS_ticket t ON r.cs_ticket_id=t.cs_ticket_id WHERE t.cs_ticket_id IS NULL;
-- SELECT COUNT(*) FROM Review_comment c LEFT JOIN Review r ON c.review_id=r.review_id WHERE r.review_id IS NULL;
-- SELECT COUNT(*) FROM Segment_member sm LEFT JOIN Member m ON sm.member_id=m.member_id WHERE m.member_id IS NULL;
-- SELECT COUNT(*) FROM Segment_member sm LEFT JOIN Segment s ON sm.segment_id=s.segment_id WHERE s.segment_id IS NULL;
-- SELECT COUNT(*) FROM Segment WHERE segment_rule IS NOT NULL AND JSON_VALID(segment_rule)=0;
-- SELECT MIN(cnt), MAX(cnt) FROM (SELECT member_id, COUNT(*) cnt FROM Purchase GROUP BY member_id) x;
-- SELECT COUNT(*) FROM CS_ticket_reply;
-- 끝.

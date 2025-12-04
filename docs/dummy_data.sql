-- 사용 명령어:  mysql -u ohgiraffers -p shoppingmalldb < "파일폴더/dummy_data.sql"

-- SQL DUMMY DATA FOR shoppingmalldb
-- version 2.2
-- last updated: 2023-10-28
-- Fixed "Unknown column 'p.member_id'" error in Segment_member insert statement.

-- To avoid issues with foreign key constraints during the import,
-- we disable the checks at the beginning and re-enable them at the end.
SET FOREIGN_KEY_CHECKS=0;

-- It's good practice to clear existing data to prevent conflicts.
TRUNCATE TABLE `Member_grade`;
TRUNCATE TABLE `Member`;
TRUNCATE TABLE `Member_profile`;
TRUNCATE TABLE `Member_grade_history`;
TRUNCATE TABLE `Product`;
TRUNCATE TABLE `Cart`;
TRUNCATE TABLE `Cart_item`;
TRUNCATE TABLE `Purchase`;
TRUNCATE TABLE `purchase_detail`;
TRUNCATE TABLE `Payment`;
TRUNCATE TABLE `History`;
TRUNCATE TABLE `CS_ticket`;
TRUNCATE TABLE `CS_ticket_reply`;
TRUNCATE TABLE `Review`;
TRUNCATE TABLE `Review_comment`;
TRUNCATE TABLE `Segment`;
TRUNCATE TABLE `Segment_member`;
TRUNCATE TABLE `Campaign`;
TRUNCATE TABLE `Campaign_target_segment`;
TRUNCATE TABLE `Message_send_log`;
TRUNCATE TABLE `Member_rfm`;
TRUNCATE TABLE `Return_request`;

-- 1. Member_grade: Static data for membership levels.
INSERT INTO `Member_grade` (`grade_code`, `grade_name`, `grade_desc`) VALUES
(1, 'BRONZE', 'Bronze level membership'),
(2, 'SILVER', 'Silver level membership'),
(3, 'GOLD', 'Gold level membership'),
(4, 'VIP', 'VIP level membership');

-- 2. Member: 30 members as requested.
-- Passwords are set to a placeholder 'hashed_password'.
INSERT INTO `Member` (`member_id`, `grade_code`, `member_name`, `member_gender`, `member_phone`, `member_birth`, `member_pwd`, `member_email`, `member_status`, `member_created`, `member_updated`, `member_last_at`) VALUES
(1, 1, '김철수', 'M', '010-1111-0001', '1990-01-15', 'hashed_password', 'user1@example.com', 'active', '2023-01-10 10:00:00', '2023-01-10 10:00:00', '2023-10-27 09:00:00'),
(2, 2, '이영희', 'F', '010-1111-0002', '1992-05-20', 'hashed_password', 'user2@example.com', 'active', '2023-02-15 11:30:00', '2023-02-15 11:30:00', '2023-10-27 10:00:00'),
(3, 3, '박민준', 'M', '010-1111-0003', '1988-11-30', 'hashed_password', 'user3@example.com', 'active', '2023-03-20 14:00:00', '2023-03-20 14:00:00', '2023-10-26 18:30:00'),
(4, 4, '최지우', 'F', '010-1111-0004', '1995-02-25', 'hashed_password', 'user4@example.com', 'active', '2023-04-05 09:00:00', '2023-04-05 09:00:00', '2023-10-27 11:00:00'),
(5, 1, '정수빈', 'F', '010-1111-0005', '1998-07-12', 'hashed_password', 'user5@example.com', 'active', '2023-05-12 16:45:00', '2023-05-12 16:45:00', '2023-10-25 20:00:00'),
(6, 2, '윤도현', 'M', '010-1111-0006', '1991-09-01', 'hashed_password', 'user6@example.com', 'active', '2023-06-18 18:00:00', '2023-06-18 18:00:00', '2023-10-27 12:10:00'),
(7, 1, '강하나', 'F', '010-1111-0007', '1993-04-18', 'hashed_password', 'user7@example.com', 'active', '2023-07-21 20:10:00', '2023-07-21 20:10:00', '2023-10-26 15:00:00'),
(8, 3, '송중기', 'M', '010-1111-0008', '1985-09-19', 'hashed_password', 'user8@example.com', 'active', '2023-08-01 12:00:00', '2023-08-01 12:00:00', '2023-10-27 13:00:00'),
(9, 2, '임윤아', 'F', '010-1111-0009', '1990-05-30', 'hashed_password', 'user9@example.com', 'active', '2023-08-15 13:20:00', '2023-08-15 13:20:00', '2023-10-27 14:20:00'),
(10, 1, '조인성', 'M', '010-1111-0010', '1981-07-28', 'hashed_password', 'user10@example.com', 'active', '2023-09-01 10:00:00', '2023-09-01 10:00:00', '2023-10-27 09:30:00'),
(11, 1, '한지민', 'F', '010-1111-0011', '1982-11-05', 'hashed_password', 'user11@example.com', 'active', NOW(), NOW(), NOW()),
(12, 2, '유재석', 'M', '010-1111-0012', '1972-08-14', 'hashed_password', 'user12@example.com', 'active', NOW(), NOW(), NOW()),
(13, 3, '전지현', 'F', '010-1111-0013', '1981-10-30', 'hashed_password', 'user13@example.com', 'active', NOW(), NOW(), NOW()),
(14, 1, '이광수', 'M', '010-1111-0014', '1985-07-14', 'hashed_password', 'user14@example.com', 'active', NOW(), NOW(), NOW()),
(15, 2, '송지효', 'F', '010-1111-0015', '1981-08-15', 'hashed_password', 'user15@example.com', 'active', NOW(), NOW(), NOW()),
(16, 1, '하동훈', 'M', '010-1111-0016', '1979-08-20', 'hashed_password', 'user16@example.com', 'active', NOW(), NOW(), NOW()),
(17, 4, '김태희', 'F', '010-1111-0017', '1980-03-29', 'hashed_password', 'user17@example.com', 'active', NOW(), NOW(), NOW()),
(18, 1, '정우성', 'M', '010-1111-0018', '1973-03-20', 'hashed_password', 'user18@example.com', 'active', NOW(), NOW(), NOW()),
(19, 2, '이효리', 'F', '010-1111-0019', '1979-05-10', 'hashed_password', 'user19@example.com', 'active', NOW(), NOW(), NOW()),
(20, 3, '원빈', 'M', '010-1111-0020', '1977-11-10', 'hashed_password', 'user20@example.com', 'active', NOW(), NOW(), NOW()),
(21, 1, '손예진', 'F', '010-1111-0021', '1982-01-11', 'hashed_password', 'user21@example.com', 'active', NOW(), NOW(), NOW()),
(22, 2, '현빈', 'M', '010-1111-0022', '1982-09-25', 'hashed_password', 'user22@example.com', 'active', NOW(), NOW(), NOW()),
(23, 1, '공유', 'M', '010-1111-0023', '1979-07-10', 'hashed_password', 'user23@example.com', 'active', NOW(), NOW(), NOW()),
(24, 2, '박보검', 'M', '010-1111-0024', '1993-06-16', 'hashed_password', 'user24@example.com', 'active', NOW(), NOW(), NOW()),
(25, 3, '아이유', 'F', '010-1111-0025', '1993-05-16', 'hashed_password', 'user25@example.com', 'active', NOW(), NOW(), NOW()),
(26, 1, '수지', 'F', '010-1111-0026', '1994-10-10', 'hashed_password', 'user26@example.com', 'active', NOW(), NOW(), NOW()),
(27, 2, '차은우', 'M', '010-1111-0027', '1997-03-30', 'hashed_password', 'user27@example.com', 'active', NOW(), NOW(), NOW()),
(28, 1, '제니', 'F', '010-1111-0028', '1996-01-16', 'hashed_password', 'user28@example.com', 'active', NOW(), NOW(), NOW()),
(29, 4, '뷔', 'M', '010-1111-0029', '1995-12-30', 'hashed_password', 'user29@example.com', 'active', NOW(), NOW(), NOW()),
(30, 2, '정국', 'M', '010-1111-0030', '1997-09-01', 'hashed_password', 'user30@example.com', 'active', NOW(), NOW(), NOW());

-- 3. Member_profile: Address information for each member.
INSERT INTO `Member_profile` (`member_id`, `profile_address`, `profile_detail_address`)
SELECT `member_id`, '서울시 강남구 테헤란로', '123-456' FROM `Member`;

-- 4. Member_grade_history: Initial grade assignment history.
INSERT INTO `Member_grade_history` (`member_id`, `grade_code`, `history_before`, `history_after`, `history_changed`)
SELECT `member_id`, `grade_code`, 'BRONZE', (SELECT grade_name FROM Member_grade WHERE grade_code = Member.grade_code), `member_created` FROM `Member`;

-- 5. Product: A variety of 50 products.
INSERT INTO `Product` (`product_id`, `product_name`, `product_category`, `product_registed`, `product_quantity`, `product_price`, `product_status`, `product_updated`) VALUES
(1, '프리미엄 유기농 샴푸', '헤어케어', NOW(), 100, 25000, 'ACTIVE', NOW()),
(2, '수분가득 보습 로션', '스킨케어', NOW(), 150, 18000, 'ACTIVE', NOW()),
(3, '스마트 LED TV 55인치', '가전', NOW(), 50, 750000, 'ACTIVE', NOW()),
(4, '고성능 게이밍 마우스', 'PC용품', NOW(), 200, 45000, 'ACTIVE', NOW()),
(5, '클래식 디자인 손목시계', '패션잡화', NOW(), 80, 120000, 'ACTIVE', NOW()),
(6, '친환경 텀블러', '주방용품', NOW(), 300, 15000, 'ACTIVE', NOW()),
(7, '강아지 영양 간식', '반려동물', NOW(), 250, 9900, 'ACTIVE', NOW()),
(8, '초경량 등산화', '스포츠', NOW(), 120, 89000, 'ACTIVE', NOW()),
(9, '어린이용 비타민 젤리', '건강식품', NOW(), 180, 22000, 'ACTIVE', NOW()),
(10, '베스트셀러 소설', '도서', NOW(), 400, 13500, 'ACTIVE', NOW()),
(11, '저소음 무선 청소기', '가전', NOW(), 60, 280000, 'ACTIVE', NOW()),
(12, '천연 아로마 오일', '생활용품', NOW(), 220, 17000, 'ACTIVE', NOW()),
(13, '데일리 백팩', '패션잡화', NOW(), 130, 59000, 'ACTIVE', NOW()),
(14, '기계식 키보드 (청축)', 'PC용품', NOW(), 90, 110000, 'ACTIVE', NOW()),
(15, '고양이 자동 급식기', '반려동물', NOW(), 70, 68000, 'ACTIVE', NOW()),
(16, '홈트레이닝 요가매트', '스포츠', NOW(), 350, 25000, 'ACTIVE', NOW()),
(17, '프로폴리스 치약', '생활용품', NOW(), 500, 7000, 'ACTIVE', NOW()),
(18, '여행용 캐리어 24인치', '여행', NOW(), 100, 95000, 'ACTIVE', NOW()),
(19, '블루투스 이어폰', '음향기기', NOW(), 250, 78000, 'ACTIVE', NOW()),
(20, '캡슐 커피 머신', '주방용품', NOW(), 80, 150000, 'ACTIVE', NOW()),
(21, '자외선 차단 선크림', '스킨케어', NOW(), 300, 16000, 'ACTIVE', NOW()),
(22, '탈모 완화 기능성 샴푸', '헤어케어', NOW(), 120, 32000, 'ACTIVE', NOW()),
(23, '4K UHD 모니터 32인치', 'PC용품', NOW(), 70, 450000, 'ACTIVE', NOW()),
(24, '스마트 워치', '가전', NOW(), 110, 290000, 'ACTIVE', NOW()),
(25, '소가죽 남성 벨트', '패션잡화', NOW(), 150, 48000, 'ACTIVE', NOW()),
(26, '에어프라이어 5L', '주방용품', NOW(), 90, 89000, 'ACTIVE', NOW()),
(27, '캣타워', '반려동물', NOW(), 40, 130000, 'ACTIVE', NOW()),
(28, '캠핑용 접이식 의자', '스포츠', NOW(), 200, 35000, 'ACTIVE', NOW()),
(29, '홍삼 스틱', '건강식품', NOW(), 160, 55000, 'ACTIVE', NOW()),
(30, '경제 경영 베스트셀러', '도서', NOW(), 300, 18000, 'ACTIVE', NOW()),
(31, '공기청정기', '가전', NOW(), 80, 220000, 'ACTIVE', NOW()),
(32, '디퓨저 세트', '생활용품', NOW(), 250, 23000, 'ACTIVE', NOW()),
(33, '크로스백', '패션잡화', NOW(), 180, 67000, 'ACTIVE', NOW()),
(34, '웹캠', 'PC용품', NOW(), 200, 52000, 'ACTIVE', NOW()),
(35, '강아지 배변패드', '반려동물', NOW(), 400, 19000, 'ACTIVE', NOW()),
(36, '덤벨 세트', '스포츠', NOW(), 100, 49000, 'ACTIVE', NOW()),
(37, '전동 칫솔', '생활용품', NOW(), 150, 88000, 'ACTIVE', NOW()),
(38, '목베개', '여행', NOW(), 300, 12000, 'ACTIVE', NOW()),
(39, '헤드셋', '음향기기', NOW(), 120, 115000, 'ACTIVE', NOW()),
(40, '전기포트', '주방용품', NOW(), 200, 38000, 'ACTIVE', NOW()),
(41, '마스크팩 100매', '스킨케어', NOW(), 500, 30000, 'ACTIVE', NOW()),
(42, '헤어 에센스', '헤어케어', NOW(), 180, 19000, 'ACTIVE', NOW()),
(43, '노트북 거치대', 'PC용품', NOW(), 250, 28000, 'ACTIVE', NOW()),
(44, '스마트 체중계', '가전', NOW(), 130, 42000, 'ACTIVE', NOW()),
(45, '선글라스', '패션잡화', NOW(), 90, 75000, 'ACTIVE', NOW()),
(46, '믹서기', '주방용품', NOW(), 100, 65000, 'ACTIVE', NOW()),
(47, '고양이 모래', '반려동물', NOW(), 300, 21000, 'ACTIVE', NOW()),
(48, '캠핑용 텐트', '스포츠', NOW(), 50, 180000, 'ACTIVE', NOW()),
(49, '오메가3', '건강식품', NOW(), 200, 28000, 'ACTIVE', NOW()),
(50, '유아용 그림책', '도서', NOW(), 350, 9000, 'ACTIVE', NOW());

-- 6. Cart: One cart for each member.
INSERT INTO `Cart` (`cart_id`, `member_id`)
SELECT `member_id`, `member_id` FROM `Member`;

-- 7. Purchase, purchase_detail, Payment, and Cart_item data generation
-- This section generates 10 purchases for each of the 30 members (300 total).
DELIMITER $$
CREATE PROCEDURE generate_dummy_purchases_initial()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 1;
    DECLARE purchase_counter INT DEFAULT 1;
    DECLARE detail_counter INT DEFAULT 1;
    DECLARE payment_counter INT DEFAULT 1;
    DECLARE cart_item_counter INT DEFAULT 1;
    DECLARE random_product_id INT;
    DECLARE random_quantity INT;
    DECLARE product_price_val INT;
    DECLARE total_amount INT;
    DECLARE purchase_date TIMESTAMP;

    WHILE i <= 30 DO
        SET j = 1;
        WHILE j <= 10 DO
            SET purchase_date = NOW() - INTERVAL FLOOR(1 + RAND() * 60) DAY;
            SET random_product_id = FLOOR(1 + (RAND() * 49));
            SET random_quantity = FLOOR(1 + (RAND() * 2));
            SELECT `product_price` INTO product_price_val FROM `Product` WHERE `product_id` = random_product_id;
            SET total_amount = product_price_val * random_quantity;

            INSERT INTO `Purchase` (`purchase_id`, `member_id`, `purchase_status`, `purchase_processed`)
            VALUES (purchase_counter, i, 'COMPLETED', purchase_date);

            INSERT INTO `purchase_detail` (`purchase_detail_id`, `product_id`, `purchase_id`, `purchase_status`, `product_quantity`, `purchase_paid_amount`)
            VALUES (detail_counter, random_product_id, purchase_counter, 'COMPLETED', random_quantity, total_amount);

            INSERT INTO `Payment` (`payment_id`, `purchase_id`, `payment_status`, `payment_created`, `payment_type`, `payment_tid`, `payment_paid_at`)
            VALUES (payment_counter, purchase_counter, 'COMPLETED', purchase_date, 'PAYMENT', CONCAT('TID', LPAD(payment_counter, 6, '0')), purchase_date);

            INSERT INTO `Cart_item` (`cart_item_id`, `cart_id`, `product_id`, `cart_item_updated`, `cart_item_status`, `cart_item_quantity`)
            VALUES (cart_item_counter, i, random_product_id, purchase_date - INTERVAL 5 MINUTE, 'REMOVED', random_quantity);

            SET purchase_counter = purchase_counter + 1;
            SET detail_counter = detail_counter + 1;
            SET payment_counter = payment_counter + 1;
            SET cart_item_counter = cart_item_counter + 1;
            SET j = j + 1;
        END WHILE;
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL generate_dummy_purchases_initial();
DROP PROCEDURE generate_dummy_purchases_initial;


-- 8. Other sample data (initial set)
INSERT INTO `CS_ticket` (`cs_ticket_id`, `member_id`, `cs_ticket_channel`, `cs_ticket_category`, `cs_ticket_status`, `cs_ticket_title`, `cs_ticket_content`, `cs_ticket_created`) VALUES
(1, 1, 'WEB', '배송문의', 'COMPLETED', '배송이 너무 늦어요', '주문한지 3일이 지났는데 아직도 상품 준비중이네요. 언제쯤 받을 수 있나요?', '2023-09-04 10:00:00'),
(2, 2, 'MOBILE', '결제문의', 'COMPLETED', '이중 결제가 된 것 같아요', '어플에서 결제했는데 오류가 나서 다시 시도했더니 두 번 결제된 것 같습니다. 확인 부탁드립니다.', '2023-09-06 11:00:00'),
(3, 3, 'CALL', '상품문의', 'RECEIVED', '상품 재입고 문의', '55인치 스마트 TV 재입고 예정이 있나요?', '2023-10-27 10:00:00');

INSERT INTO `CS_ticket_reply` (`reply_id`, `cs_ticket_id`, `reply_responder_id`, `reply_content`, `reply_created`) VALUES
(1, 1, 1, '고객님, 안녕하세요. 주문하신 상품은 현재 출고 준비 중이며, 내일 출고될 예정입니다. 불편을 드려 죄송합니다.', '2023-09-04 11:00:00'),
(2, 2, 1, '고객님, 확인 결과 중복 결제가 확인되어 한 건은 즉시 취소 처리해드렸습니다. 이용에 불편을 드려 죄송합니다.', '2023-09-06 11:30:00');

INSERT INTO `Review` (`review_id`, `review_title`, `review_content`, `review_score`, `review_created`) VALUES
(1, '샴푸 정말 좋네요', '유기농이라 믿고 샀는데, 거품도 잘 나고 향도 은은해서 만족합니다. 재구매 의사 있어요!', 5, '2023-09-05 10:00:00'),
(2, '로션 보습력 최고', '건성 피부인데 이 로션 쓰고 많이 좋아졌어요. 촉촉함이 오래가네요.', 5, '2023-09-06 12:00:00'),
(3, '소설책 잘 읽었습니다', '시간 가는 줄 모르고 읽었네요. 좋은 책 감사합니다.', 4, '2023-09-10 15:00:00');

INSERT INTO `History` (`history_datetime`, `history_action_type`, `history_member_id`, `history_detail`, `history_ip_address`, `history_ref_tbl`) VALUES
('2023-10-27 09:00:00', 'LOGIN', 1, 'Member logged in', '192.168.0.1', 'Member'),
('2023-10-27 09:30:00', 'LOGIN', 10, 'Member logged in', '192.168.0.10', 'Member'),
('2023-09-01 10:00:00', 'PAID', 1, 'Purchase ID 1 paid', '192.168.0.1', 'Purchase');

-- ===================================================================================
-- == START OF ADDITIONAL DUMMY DATA (v2.0)
-- ===================================================================================

-- 9. Segment & Segment_member: Define customer segments and assign members.
INSERT INTO `Segment` (`segment_id`, `segment_name`, `segment_rule`) VALUES
(1, 'VIP 고객', '{"grade": "VIP"}'),
(2, '신규 가입 고객 (최근 30일)', '{"join_date": "last_30_days"}'),
(3, '최근 60일 미구매 고객', '{"last_purchase": "over_60_days"}'),
(4, '마케팅 수신 동의 고객', '{"agreement": "marketing_email"}'),
(5, '20대 여성', '{"age_range": "20-29", "gender": "F"}'),
(6, '30대 남성', '{"age_range": "30-39", "gender": "M"}'),
(7, '반려동물 용품 구매 고객', '{"purchase_category": "반려동물"}');

-- Assign members to segments
-- VIP 고객 (grade_code = 4)
INSERT INTO `Segment_member` (`member_id`, `segment_id`) SELECT member_id, 1 FROM `Member` WHERE grade_code = 4;
-- 신규 가입 고객 (가입일이 최근 30일 이내)
INSERT INTO `Segment_member` (`member_id`, `segment_id`) SELECT member_id, 2 FROM `Member` WHERE member_created >= NOW() - INTERVAL 30 DAY;
-- 최근 60일 미구매 고객 (마지막 구매일이 60일 이전) - Stored Procedure로 구매내역 생성 후 채워넣기
-- 마케팅 수신 동의 고객 (모든 회원을 대상으로 가정)
INSERT INTO `Segment_member` (`member_id`, `segment_id`) SELECT member_id, 4 FROM `Member`;
-- 20대 여성
INSERT INTO `Segment_member` (`member_id`, `segment_id`) SELECT member_id, 5 FROM `Member` WHERE member_gender = 'F' AND (YEAR(CURDATE()) - YEAR(member_birth)) BETWEEN 20 AND 29;
-- 30대 남성
INSERT INTO `Segment_member` (`member_id`, `segment_id`) SELECT member_id, 6 FROM `Member` WHERE member_gender = 'M' AND (YEAR(CURDATE()) - YEAR(member_birth)) BETWEEN 30 AND 39;
-- 반려동물 용품 구매 고객 (구매내역 기반)
INSERT INTO `Segment_member` (`member_id`, `segment_id`)
SELECT DISTINCT pu.member_id, 7
FROM Purchase pu
JOIN purchase_detail pd ON pu.purchase_id = pd.purchase_id
JOIN Product p ON pd.product_id = p.product_id
WHERE p.product_category = '반려동물';


-- 10. Campaign, Campaign_target_segment, Message_send_log: Marketing data.
INSERT INTO `Campaign` (`campaign_id`, `campaign_name`, `campaign_status`, `campaign_scheduled`, `campaign_content`) VALUES
(1, '가을맞이 15% 할인 캠페인', 'COMPLETED', '2023-09-15 09:00:00', '전 상품 15% 할인 쿠폰 증정'),
(2, 'VIP 고객 감사 이벤트', 'RUNNING', '2023-10-25 09:00:00', 'VIP 고객님께만 드리는 특별 사은품 증정'),
(3, '신규 고객 웰컴 쿠폰', 'SCHEDULED', '2023-11-01 09:00:00', '가입 후 첫 구매 시 사용 가능한 10,000원 할인 쿠폰'),
(4, '휴면 고객 활성화 프로모션', 'SCHEDULED', '2023-11-10 09:00:00', '지금 복귀하시면 20% 할인 쿠폰을 드립니다!');

-- Link campaigns to segments
INSERT INTO `Campaign_target_segment` (`campaign_id`, `segment_id`) VALUES
(1, 4), -- 가을맞이 -> 모든 마케팅 동의 고객
(2, 1), -- VIP 이벤트 -> VIP 고객
(3, 2), -- 웰컴 쿠폰 -> 신규 가입 고객
(4, 3); -- 휴면 고객 프로모션 -> 최근 미구매 고객

-- Message send logs (over 30)
DELIMITER $$
CREATE PROCEDURE generate_message_logs()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 35 DO
        INSERT INTO `Message_send_log` (`campaign_id`, `member_id`, `send_at`, `send_clicked`)
        VALUES (
            FLOOR(1 + (RAND() * 4)),  -- campaign_id 1-4
            FLOOR(1 + (RAND() * 30)), -- member_id 1-30
            NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
            CASE WHEN RAND() > 0.7 THEN NOW() - INTERVAL FLOOR(RAND() * 29) DAY ELSE NULL END
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_message_logs();
DROP PROCEDURE generate_message_logs;

-- 11. CS_ticket & CS_ticket_reply: Add 30+ tickets and replies.
DELIMITER $$
CREATE PROCEDURE generate_cs_tickets()
BEGIN
    DECLARE i INT DEFAULT 4; -- Start from ID 4
    DECLARE responder_id INT;
    WHILE i <= 35 DO
        INSERT INTO `CS_ticket` (`cs_ticket_id`, `member_id`, `cs_ticket_channel`, `cs_ticket_category`, `cs_ticket_status`, `cs_ticket_title`, `cs_ticket_content`, `cs_ticket_created`)
        VALUES (
            i,
            FLOOR(1 + (RAND() * 30)),
            ELT(FLOOR(1 + RAND() * 4), 'WEB', 'MOBILE', 'CALL', 'EMAIL'),
            ELT(FLOOR(1 + RAND() * 5), '배송문의', '결제문의', '상품문의', '반품/환불', '기타'),
            ELT(FLOOR(1 + RAND() * 3), 'RECEIVED', 'COMPLETED', 'REJECTED'),
            CONCAT('문의 드립니다 (', i, ')'),
            CONCAT('문의 내용입니다. 티켓 번호: ', i),
            NOW() - INTERVAL FLOOR(RAND() * 60) DAY
        );
        -- Add a reply for about 70% of tickets
        IF RAND() > 0.3 THEN
            SET responder_id = FLOOR(1 + (RAND() * 5)); -- Assume first 5 members are staff
            INSERT INTO `CS_ticket_reply` (`cs_ticket_id`, `reply_responder_id`, `reply_content`, `reply_created`)
            VALUES (i, responder_id, CONCAT('답변 드립니다. 문의주신 내용 (', i, ')에 대해 확인 후 다시 연락드리겠습니다.'), NOW() - INTERVAL FLOOR(RAND() * 59) DAY);
        END IF;
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_cs_tickets();
DROP PROCEDURE generate_cs_tickets;

-- 12. Return_request: Add 30+ return requests.
DELIMITER $$
CREATE PROCEDURE generate_return_requests()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 35 DO
        INSERT INTO `Return_request` (`purchase_id`, `request_reason`, `request_status`, `request_processed`)
        VALUES (
            FLOOR(1 + (RAND() * 300)), -- Link to one of the 300 purchases
            ELT(FLOOR(1 + RAND() * 3), '단순 변심', '상품 불량', '사이즈/색상 불만'),
            ELT(FLOOR(1 + RAND() * 3), 'REQUESTED', 'COMPLETED', 'REJECTED'),
            CASE WHEN RAND() > 0.5 THEN NOW() - INTERVAL FLOOR(RAND() * 10) DAY ELSE NULL END
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_return_requests();
DROP PROCEDURE generate_return_requests;

-- 13. Review & Review_comment: Add 30+ reviews and comments.
DELIMITER $$
CREATE PROCEDURE generate_reviews()
BEGIN
    DECLARE i INT DEFAULT 4; -- Start from ID 4
    WHILE i <= 35 DO
        INSERT INTO `Review` (`review_id`, `review_title`, `review_content`, `review_score`, `review_created`)
        VALUES (
            i,
            CONCAT('상품 후기 (', i, ')'),
            CONCAT('아주 만족합니다. 제품 번호: ', FLOOR(1 + (RAND() * 50))),
            FLOOR(3 + (RAND() * 3)), -- Score 3, 4, or 5
            NOW() - INTERVAL FLOOR(RAND() * 60) DAY
        );
        -- Add a comment for about 50% of reviews
        IF RAND() > 0.5 THEN
            INSERT INTO `Review_comment` (`review_id`, `review_comment`, `review_content`, `review_score`, `review_created`)
            VALUES (i, 'Re: 상품 후기', '소중한 후기 감사합니다!', 5, NOW() - INTERVAL FLOOR(RAND() * 59) DAY);
        END IF;
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_reviews();
DROP PROCEDURE generate_reviews;

-- 14. Member_rfm: Generate RFM data for all 30 members.
DELIMITER $$
CREATE PROCEDURE generate_rfm_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE r_score, f_score, m_score TINYINT;
    WHILE i <= 30 DO
        SET r_score = FLOOR(1 + (RAND() * 5));
        SET f_score = FLOOR(1 + (RAND() * 5));
        SET m_score = FLOOR(1 + (RAND() * 5));
        INSERT INTO `Member_rfm` (`member_id`, `rfm_receny_days`, `rfm_frequency`, `rfm_monetary`, `rfm_r_score`, `rfm_f_score`, `rfm_m_score`, `rfm_total_score`, `rfm_snapshot`)
        VALUES (
            i,
            FLOOR(1 + (RAND() * 90)),
            FLOOR(5 + (RAND() * 20)),
            FLOOR(100000 + (RAND() * 1000000)),
            r_score,
            f_score,
            m_score,
            r_score + f_score + m_score,
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_rfm_data();
DROP PROCEDURE generate_rfm_data;

-- 15. History: Add 30+ more history logs.
DELIMITER $$
CREATE PROCEDURE generate_history_logs()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 35 DO
        INSERT INTO `History` (`history_datetime`, `history_action_type`, `history_member_id`, `history_detail`, `history_ip_address`, `history_ref_tbl`)
        VALUES (
            NOW() - INTERVAL FLOOR(RAND() * 10000) MINUTE,
            ELT(FLOOR(1 + RAND() * 7), 'CREATE','UPDATE','DELETE','LOGIN','LOGOUT','PAID','REFUND'),
            FLOOR(1 + (RAND() * 30)),
            CONCAT('Action log entry #', i),
            CONCAT('192.168.1.', FLOOR(1 + (RAND() * 254))),
            ELT(FLOOR(1 + RAND() * 7), 'Member','Purchase','Segment','CS_ticket','Product','Return_request','CART_ITEM')
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;
CALL generate_history_logs();
DROP PROCEDURE generate_history_logs;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;

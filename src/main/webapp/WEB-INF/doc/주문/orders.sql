-- C:/kd/ws_java/team7_v2sbm3c/src/main/webapp/WEB-INF/doc/주문

DROP TABLE orders;

DROP TABLE orders CASCADE ORDERS; -- 자식 무시하고 삭제 가능

-- 주문 기본 정보 테이블
CREATE TABLE orders (
  orderno       NUMBER(10) PRIMARY KEY,                 -- 주문 번호 (시퀀스 orders_seq 사용)
  memberno      NUMBER(10) NOT NULL,                    -- 주문한 회원 번호 (member 테이블 FK)
  rname         VARCHAR2(50) NOT NULL,                  -- 수령자 이름
  rtel          VARCHAR2(20) NOT NULL,                  -- 수령자 전화번호
  rzipcode      VARCHAR2(10) NOT NULL,                  -- 우편번호
  raddress1     VARCHAR2(100) NOT NULL,                 -- 기본 주소
  raddress2     VARCHAR2(100),                          -- 상세 주소
  message       VARCHAR2(200),                          -- 배송 요청사항 (예: 부재시 문앞에)
  payment       VARCHAR2(30) NOT NULL,                  -- 결제 수단 (예: 신용카드, 무통장입금)
  total         NUMBER(10) DEFAULT 0,                   -- 주문 총 금액
  point         NUMBER(10) DEFAULT 0,                   -- 포인트 사용 또는 적립 값
  order_state   VARCHAR2(30) DEFAULT '결제완료',        -- 주문 상태 (예: 결제완료, 배송중, 배송완료)
  rdate         DATE DEFAULT SYSDATE,                   -- 주문 날짜 및 시간
  
  CONSTRAINT fk_orders_member FOREIGN KEY (memberno)
    REFERENCES member(memberno)
);

DROP SEQUENCE orders_seq;

-- 주문 번호 생성을 위한 시퀀스
CREATE SEQUENCE orders_seq
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  NOCACHE
  NOCYCLE;

-- 테스트 INSERT
INSERT INTO orders (
  orderno, memberno, rname, rtel, rzipcode, raddress1, raddress2,
  message, payment, total, point, order_state
) VALUES (
  orders_seq.NEXTVAL, 1, '홍길동', '010-1234-5678', '04520', 
  '서울시 강남구 테헤란로 123', '502호',
  '부재시 경비실에 맡겨주세요', '신용카드', 15200, 0, '결제완료'
);

DELETE FROM orders
WHERE orderno = 12;  -- 여기에 삭제하려는 주문 번호 입력

SELECT * FROM orders;

-- 최신 주문 순으로 보기:
SELECT * FROM orders
ORDER BY rdate DESC;


--
-- 상품이 하나도 없는 주문 제거
DELETE FROM orders
WHERE orderno IN (
  SELECT o.orderno
  FROM orders o
  LEFT JOIN order_item i ON o.orderno = i.orderno
  WHERE i.orderno IS NULL
);

SELECT o.orderno, o.rdate, o.total, o.status
FROM orders o
WHERE NOT EXISTS (
  SELECT 1 FROM order_item i WHERE i.orderno = o.orderno
);


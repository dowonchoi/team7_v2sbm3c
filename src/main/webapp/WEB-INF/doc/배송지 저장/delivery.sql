-- delivery 테이블 생성 (기본 배송지 저장용)
CREATE TABLE delivery (
  deliveryno   NUMBER(10) PRIMARY KEY,                 -- 배송지 고유번호
  memberno     NUMBER(10) NOT NULL,                    -- 회원번호 (FK 형태로 관리 가능)
  rname        VARCHAR2(50) NOT NULL,                  -- 수령인 이름
  rtel         VARCHAR2(20) NOT NULL,                  -- 연락처
  rzipcode     VARCHAR2(10) NOT NULL,                  -- 우편번호
  raddress1    VARCHAR2(100) NOT NULL,                 -- 기본 주소
  raddress2    VARCHAR2(100),                          -- 상세 주소
  message      VARCHAR2(200),                          -- 배송 메시지
  rdate        DATE DEFAULT SYSDATE                    -- 등록일
);

-- deliveryno 시퀀스 생성
CREATE SEQUENCE delivery_seq
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  NOCACHE
  NOCYCLE;
  
commit;

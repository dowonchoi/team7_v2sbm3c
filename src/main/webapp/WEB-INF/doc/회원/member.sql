-- 테이블 구조
-- member 삭제전에 FK가 선언된 blog 테이블 먼저 삭제합니다.
DROP TABLE qna;
DROP TABLE reply;
DROP TABLE member;
-- 제약 조건과 함께 삭제(제약 조건이 있어도 삭제됨, 권장하지 않음.)
DROP TABLE member CASCADE CONSTRAINTS; 
 
CREATE TABLE member (
  memberno NUMBER(10) PRIMARY KEY,                 -- 회원 번호
  id VARCHAR(30) NOT NULL UNIQUE,                  -- 아이디(이메일)
  email ,                             -- 이메일
  passwd VARCHAR(200) NOT NULL,                    -- 암호화된 비밀번호
  mname VARCHAR(30) NOT NULL,                      -- 성명
  tel VARCHAR(14) NOT NULL,                        -- 전화번호
  zipcode VARCHAR2(10),                            -- 우편번호
  address1 VARCHAR(80),                            -- 주소 1
  address2 VARCHAR(50),                            -- 주소 2
  mdate DATE NOT NULL,                             -- 가입일
  grade NUMBER(2) NOT NULL,                        -- 등급 (1~4: 관리자, 5~15: 공급자, 16~39: 소비자, 40~59: 탈퇴)
  supplier_approved CHAR(1) DEFAULT 'N',           -- 공급자 승인 여부 ('Y' 또는 'N')
  business_file VARCHAR2(200),                     -- 사업자 등록증 파일명
  is_visible CHAR(1) DEFAULT 'Y'                   -- 회원 노출 여부 ('Y': 보임, 'N': 숨김)
);

COMMENT ON TABLE member IS '회원';
COMMENT ON COLUMN member.memberno IS '회원 번호';
COMMENT ON COLUMN member.id IS '아이디';
COMMENT ON COLUMN member.email IS '이메일';
COMMENT ON COLUMN member.passwd IS '패스워드';
COMMENT ON COLUMN member.mname IS '성명';
COMMENT ON COLUMN member.tel IS '전화번호';
COMMENT ON COLUMN member.zipcode IS '우편번호';
COMMENT ON COLUMN member.address1 IS '주소1';
COMMENT ON COLUMN member.address2 IS '주소2';
COMMENT ON COLUMN member.mdate IS '가입일';
COMMENT ON COLUMN member.grade IS '등급';
COMMENT ON COLUMN member.supplier_approved IS '공급자 승인 여부';
COMMENT ON COLUMN member.business_file IS '사업자 등록증 파일명';
COMMENT ON COLUMN member.is_visible IS '회원 노출 여부';

DROP SEQUENCE member_seq;

CREATE SEQUENCE member_seq
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  NOCYCLE
  CACHE 2;
  
ALTER TABLE member MODIFY supplier_approved VARCHAR2(1) DEFAULT 'N';
  
ALTER TABLE member RENAME COLUMN business_file_name TO business_file;

ALTER TABLE member ADD business_file_origin VARCHAR2(100);

UPDATE member
SET passwd = 'fS/kjO+fuEKk06Zl7VYMhg==' 
WHERE id = 'test';
  
SELECT memberno, business_file
FROM member 
WHERE business_file IS NOT NULL;

-- 5. 샘플 데이터 삽입
-- 관리자
INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'admin', 'tteoliMall@gmail.com', '1234', '떨이몰 관리자',
        '010-0000-0000', '00000', '서울특별시 종로구', '청운동', SYSDATE, 1);

-- 공급자
INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'shop', 'seller1@gmall.com', '1234', '떨이상점1',
        '010-1234-1111', '12345', '부산 해운대구', '우동', SYSDATE, 5);

INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'sale', 'seller2@gmall.com', '1234', '남는재고마트',
        '010-1234-2222', '12345', '인천 계양구', '계산동', SYSDATE, 6);

-- 소비자
INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'sobi', 'buyer1@naver.com', '1234', '김소비',
        '010-2345-6789', '04567', '서울 강남구', '역삼동', SYSDATE, 16);

INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'park', 'buyer2@daum.net', '1234', '박떨이',
        '010-9876-5432', '12345', '대구 수성구', '범어동', SYSDATE, 17);

INSERT INTO member (memberno, id, email, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.NEXTVAL, 'choi', 'buyer3@gmail.com', '1234', '최할인',
        '010-1122-3344', '67890', '부산 진구', '전포동', SYSDATE, 18);

COMMIT;

SELECT * FROM member WHERE id = 'user';

SELECT id, passwd FROM member WHERE id = 'admin';

-- 6. 주요 쿼리

-- 아이디 중복 확인
SELECT COUNT(*) FROM member WHERE id = 'user1';

-- 로그인 검증
SELECT COUNT(*) FROM member WHERE id = 'user1@gmail.com' AND passwd = '1234';

-- 전체 회원 목록
SELECT memberno, id, email, mname, tel, grade, is_visible FROM member ORDER BY grade, id;

-- 탈퇴 회원 확인
SELECT memberno, id, email, is_visible FROM member WHERE memberno = 9;

-- 승인 대기 공급자 목록
SELECT memberno, id, email, mname, tel, grade, supplier_approved, business_file
FROM member
WHERE grade BETWEEN 5 AND 15 AND supplier_approved = 'N';

-- 회원 정보 수정
UPDATE member
SET mname = '조인성', tel = '111-1111-1111', zipcode = '00000',
    address1 = '강원도', address2 = '홍천군', grade = 14
WHERE memberno = 12;

-- 회원 탈퇴 처리
UPDATE member
SET grade = 40, is_visible = 'N'
WHERE memberno = 9;

-- 비밀번호 암호화 후 변경 (예: '1234')
UPDATE member
SET passwd = 'fS/kjO+fuEKk06Zl7VYMhg=='  -- ← 정확히 security.aesEncode("1234") 값
WHERE id = 'admin';

SELECT passwd FROM member;

COMMIT;



-- 테이블 구조
-- member 삭제전에 FK가 선언된 blog 테이블 먼저 삭제합니다.
DROP TABLE qna;
DROP TABLE reply;
DROP TABLE member;
-- 제약 조건과 함께 삭제(제약 조건이 있어도 삭제됨, 권장하지 않음.)
DROP TABLE member CASCADE CONSTRAINTS; 
 
CREATE TABLE member (
  memberno NUMBER(10) NOT NULL, -- 회원 번호, 레코드를 구분하는 컬럼 
  id         VARCHAR(30)   NOT NULL UNIQUE, -- 이메일(아이디), 중복 안됨, 레코드를 구분 
  passwd     VARCHAR(200)   NOT NULL, -- 패스워드, 영숫자 조합, 암호화
  mname      VARCHAR(30)   NOT NULL, -- 성명, 한글 10자 저장 가능
  tel            VARCHAR(14)   NOT NULL, -- 전화번호
  zipcode     VARCHAR(5)        NULL, -- 우편번호, 12345
  address1    VARCHAR(80)       NULL, -- 주소 1
  address2    VARCHAR(50)       NULL, -- 주소 2
  mdate       DATE             NOT NULL, -- 가입일    
  grade        NUMBER(2)     NOT NULL, -- 등급(1~10: 관리자, 11~20: 회원, 40~49: 정지 회원, 99: 탈퇴 회원)
  PRIMARY KEY (memberno)               -- 한번 등록된 값은 중복 안됨
);
 
COMMENT ON TABLE MEMBER is '회원';
COMMENT ON COLUMN MEMBER.MEMBERNO is '회원 번호';
COMMENT ON COLUMN MEMBER.ID is '아이디';
COMMENT ON COLUMN MEMBER.PASSWD is '패스워드';
COMMENT ON COLUMN MEMBER.MNAME is '성명';
COMMENT ON COLUMN MEMBER.TEL is '전화번호';
COMMENT ON COLUMN MEMBER.ZIPCODE is '우편번호';
COMMENT ON COLUMN MEMBER.ADDRESS1 is '주소1';
COMMENT ON COLUMN MEMBER.ADDRESS2 is '주소2';
COMMENT ON COLUMN MEMBER.MDATE is '가입일';
COMMENT ON COLUMN MEMBER.GRADE is '등급';

DROP SEQUENCE member_seq;

CREATE SEQUENCE member_seq
  START WITH 1              -- 시작 번호
  INCREMENT BY 1          -- 증가값
  MAXVALUE 9999999999 -- 최대값: 9999999 --> NUMBER(7) 대응
  CACHE 2                       -- 2번은 메모리에서만 계산
  NOCYCLE;                     -- 다시 1부터 생성되는 것을 방지
 
1. 등록
 
1) id 중복 확인(null 값을 가지고 있으면 count에서 제외됨)
SELECT COUNT(id)
FROM member
WHERE id='user1';

SELECT COUNT(id) as cnt
FROM member
WHERE id='user1';
 
 cnt
 ---
   0   ← 중복 되지 않음.
   
2) 등록
-- 관리자 계정 1개만 생성
INSERT INTO member(memberno, id, passwd, mname, tel, zipcode,
                   address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'admin@gmall.com', 'admin1234', '떨이몰 관리자', 
        '010-0000-0000', '00000', '서울특별시 종로구', '청운동', sysdate, 1);
 
-- 공급자 회원 예시
INSERT INTO member(memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'seller1@gmall.com', '1234', '떨이상점1', '010-1234-1111', '12345', '부산시 해운대구', '우동', sysdate, 11);

INSERT INTO member(memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'seller2@gmall.com', '1234', '남는재고마트', '010-1234-2222', '12345', '인천시 계양구', '계산동', sysdate, 12);
 
-- 소비자 회원 예시
INSERT INTO member(memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'buyer1@naver.com', 'pass1234', '김소비', '010-2345-6789', '04567', '서울특별시 강남구', '역삼동', sysdate, 21);

INSERT INTO member(memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'buyer2@daum.net', 'pass5678', '박떨이', '010-9876-5432', '12345', '대구광역시 수성구', '범어동', sysdate, 22);

INSERT INTO member(memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'buyer3@gmail.com', 'pass9012', '최할인', '010-1122-3344', '67890', '부산광역시 진구', '전포동', sysdate, 23);

COMMIT;

2. 목록
- 검색을 하지 않는 경우, 전체 목록 출력
 
SELECT memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade
FROM member
ORDER BY grade ASC, id ASC;
     
     
3. 조회
 
1) 회원 정보 조회
SELECT memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade
FROM member
WHERE grade = 13;

SELECT memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade
FROM member
WHERE id = 'seller1@gmall.com';

4. 수정, PK: 변경 못함, UNIQUE: 변경을 권장하지 않음(id)
UPDATE member 
SET mname='총각네', tel='010-1231-1111', zipcode='12345',
    address1='경기도 고양시', address2='일산동', grade=13
WHERE grade=11;

COMMIT;

5. 삭제
1) 모두 삭제
DELETE FROM member;
 
2) 특정 회원 삭제
DELETE FROM member
WHERE grade=12;

COMMIT;

8. 회원 등급 변경
-- 정지 회원
UPDATE member
SET grade = 30
WHERE grade = 23;

-- 탈퇴 회원
UPDATE member
SET grade = 40
WHERE memberno=9;

commit;

INSERT INTO member(memberno, id, passwd, mname, tel, zipcode,
                                address1, address2, mdate, grade)
VALUES (member_seq.nextval, 'admin', 'fS/kjO+fuEKk06Zl7VYMhg==', '통합 관리자', '000-0000-0000', '12345',
             '서울시 종로구', '관철동', sysdate, 1);

COMMIT;

SELECT memberno, id, passwd, mname, tel, zipcode, address1, address2, mdate, grade
FROM member
ORDER BY grade ASC, id ASC;
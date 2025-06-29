-- 1. categrp 테이블 생성
CREATE TABLE categrp (
  grp       VARCHAR2(30) PRIMARY KEY,   -- cate.grp 와 JOIN될 컬럼
  name      VARCHAR2(100) NOT NULL,     -- 대분류 이름
  seqno     NUMBER(5) DEFAULT 1 NOT NULL, -- 출력 순서
  visible   CHAR(1) DEFAULT 'Y' NOT NULL, -- Y/N
  rdate     DATE DEFAULT SYSDATE NOT NULL -- 등록일
);

-- 2. 초기 데이터 삽입
INSERT INTO categrp (grp, name, seqno, visible, rdate)
VALUES ('즉석조리', '냉동', 305, 'Y', SYSDATE);

-- 3. 커밋
COMMIT;

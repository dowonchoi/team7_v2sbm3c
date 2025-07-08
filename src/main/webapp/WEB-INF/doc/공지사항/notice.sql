-- ✅ 1. 기존 테이블 삭제 (테스트용)
DROP TABLE notice CASCADE CONSTRAINTS;

-- ✅ 2. 공지사항 테이블 생성
CREATE TABLE notice (
    notice_id      NUMBER(10)     NOT NULL,              -- 공지사항 번호 (PK)
    title          VARCHAR2(200)  NOT NULL,              -- 제목
    content        CLOB           NOT NULL,              -- 내용
    cate       VARCHAR2(50)   DEFAULT '공지',        -- 카테고리
    is_fixed       CHAR(1)        DEFAULT 'N',           -- 상단 고정 여부 ('Y'/'N')
    view_count     NUMBER(10)     DEFAULT 0,             -- 조회수
    writer_id      VARCHAR2(50)   NOT NULL,              -- 작성자 ID
    writer_name    VARCHAR2(50),                         -- 작성자 이름
    reg_date       DATE           DEFAULT SYSDATE,       -- 등록일
    upd_date       DATE,                                 -- 수정일
    PRIMARY KEY (notice_id)
);

-- ✅ 3. 시퀀스 생성 (PK용)
DROP SEQUENCE notice_seq;

CREATE SEQUENCE notice_seq
START WITH 1
INCREMENT BY 1
MAXVALUE 9999999999
CACHE 2
NOCYCLE;

ALTER TABLE notice ADD image VARCHAR2(200);

COMMIT;

SELECT image FROM notice;

-- 소비자용
SELECT * FROM qna WHERE user_type = 'user';

-- 공급자용
SELECT * FROM qna WHERE user_type = 'supplier';

-- ✅ 4. 샘플 데이터 삽입
INSERT INTO notice (notice_id, title, content, cate, is_fixed, writer_id, writer_name)
VALUES (notice_seq.nextval, '사이트 점검 안내', '2025년 7월 5일 사이트 점검이 예정되어 있습니다.', '공지', 'Y', 'admin', '관리자');

INSERT INTO notice (notice_id, title, content, cate, is_fixed, writer_id, writer_name)
VALUES (notice_seq.nextval, '여름 휴가 일정 공지', '2025년 여름 휴가 일정 안내입니다.', '공지', 'N', 'admin', '관리자');

COMMIT;

-- ✅ 5. 전체 목록 조회 (최신순)
SELECT notice_id, title, cate, is_fixed, view_count, writer_name, reg_date
FROM notice
ORDER BY notice_id DESC;

-- ✅ 6. 특정 공지사항 조회 (PK로 조회)
SELECT *
FROM notice
WHERE notice_id = 1;

-- ✅ 7. 특정 카테고리 공지사항 목록 조회
SELECT notice_id, title, writer_name, reg_date
FROM notice
WHERE cate = '공지'
ORDER BY notice_id DESC;

-- ✅ 8. 조회수 증가 예제 (공지사항 조회 시)
UPDATE notice
SET view_count = view_count + 1,
    upd_date = SYSDATE
WHERE notice_id = 1;

-- 공지사항 수정
UPDATE notice
SET title = '수정된 공지 제목',
    content = '수정된 내용입니다.',
    cate = '공지',
    is_fixed = 'N',
    upd_date = SYSDATE
WHERE notice_id = 1;

COMMIT;

-- 삭제
DELETE FROM notice
WHERE notice_id = 1;

COMMIT;

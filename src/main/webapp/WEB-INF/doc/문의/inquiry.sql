DROP TABLE inquiry CASCADE CONSTRAINTS;

CREATE TABLE inquiry (
    inquiry_id     NUMBER(10)     NOT NULL, -- 문의글 번호
    title          VARCHAR2(200)  NOT NULL, -- 제목
    content        CLOB           NOT NULL, -- 문의 내용
    writer_id      VARCHAR2(50)   NOT NULL, -- 작성자 ID
    writer_name    VARCHAR2(50),            -- 작성자 이름
    reg_date       DATE           DEFAULT SYSDATE, -- 등록일
    view_count     NUMBER         DEFAULT 0,       -- 조회수
    answer         CLOB,                         -- 관리자 답변
    answer_date    DATE,                         -- 답변일
    answer_admin   VARCHAR2(50),                 -- 답변한 관리자 ID
    memberno       NUMBER(10),                   -- 회원 번호 (FK 연결 가능)
    PRIMARY KEY (inquiry_id)
);

DROP SEQUENCE inquiry_seq;

CREATE SEQUENCE inquiry_seq
START WITH 1
INCREMENT BY 1
MAXVALUE 9999999999
CACHE 2
NOCYCLE;

ALTER TABLE inquiry ADD user_type VARCHAR2(20);

-- 소비자 문의
INSERT INTO inquiry (inquiry_id, title, content, writer_id, writer_name, memberno)
VALUES (inquiry_seq.NEXTVAL, '환불 문의', '환불 절차를 알고 싶어요.', 'user1', '홍길동', 4);

-- 관리자 답변 추가
UPDATE inquiry
SET answer = '환불은 3일 내 처리됩니다.', answer_date = SYSDATE, answer_admin = 'admin1'
WHERE inquiry_id = 1;

COMMIT;

SELECT COUNT(*) FROM inquiry WHERE memberno = 4;

SELECT inquiry_id, title, memberno FROM inquiry ORDER BY reg_date DESC;

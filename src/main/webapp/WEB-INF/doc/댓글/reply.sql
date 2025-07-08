-- ✅ 1. 기존 댓글 테이블 삭제 (테스트용)
DROP TABLE notice_reply CASCADE CONSTRAINTS;

-- ✅ 2. 댓글 테이블 생성
CREATE TABLE notice_reply (
    reply_id      NUMBER(10)     NOT NULL,         -- 댓글 번호 (PK)
    notice_id     NUMBER(10)     NOT NULL,         -- 공지사항 번호 (FK)
    content       VARCHAR2(1000) NOT NULL,         -- 댓글 내용
    writer_id     VARCHAR2(50)   NOT NULL,         -- 작성자 ID
    writer_name   VARCHAR2(50),                    -- 작성자 이름
    reg_date      DATE           DEFAULT SYSDATE,  -- 등록일
    upd_date      DATE,                            -- 수정일
    is_deleted    CHAR(1)        DEFAULT 'N',      -- 삭제 여부 ('Y'/'N')
    PRIMARY KEY (reply_id),
    FOREIGN KEY (notice_id) REFERENCES notice(notice_id)
);

-- ✅ 3. 시퀀스 생성 (PK용)
DROP SEQUENCE notice_reply_seq;

CREATE SEQUENCE notice_reply_seq
START WITH 1
INCREMENT BY 1
MAXVALUE 9999999999
CACHE 2
NOCYCLE;

-- ✅ 4. 샘플 데이터 삽입 (공지사항 ID 필수)
INSERT INTO notice_reply (reply_id, notice_id, content, writer_id, writer_name)
VALUES (notice_reply_seq.nextval, 1, '첫 번째 댓글입니다.', 'user1', '홍길동');

INSERT INTO notice_reply (reply_id, notice_id, content, writer_id, writer_name)
VALUES (notice_reply_seq.nextval, 1, '두 번째 댓글입니다.', 'user2', '이몽룡');

INSERT INTO notice_reply (reply_id, notice_id, content, writer_id, writer_name)
VALUES (notice_reply_seq.nextval, 2, '다른 공지의 댓글입니다.', 'user3', '성춘향');

COMMIT;

-- ✅ 5. 전체 댓글 목록 조회 (최신순)
SELECT reply_id, notice_id, content, writer_name, reg_date, is_deleted
FROM notice_reply
ORDER BY reply_id DESC;

AND is_deleted = 'N'

-- ✅ 6. 특정 공지사항의 댓글 조회
SELECT reply_id, content, writer_name, reg_date
FROM notice_reply
WHERE notice_id = 1 AND is_deleted = 'N'
ORDER BY reply_id ASC;

-- ✅ 7. 댓글 수정
UPDATE notice_reply
SET content = '수정된 댓글 내용입니다.',
    upd_date = SYSDATE
WHERE reply_id = 1;

COMMIT;

-- ✅ 8. 댓글 삭제 (소프트 딜리트)
UPDATE notice_reply
SET is_deleted = 'Y',
    upd_date = SYSDATE
WHERE reply_id = 2;

COMMIT;

-- ✅ 9. 댓글 완전 삭제 (진짜 삭제할 때만!)
DELETE FROM notice_reply
WHERE reply_id = 3;

COMMIT;

CREATE TABLE mms_img (
    mimgno            NUMBER PRIMARY KEY,           -- MMS 이미지 PK
    memberno          NUMBER NOT NULL,             -- 관리자 회원 번호
    prompt            VARCHAR2(1000) NOT NULL,     -- OpenAI 프롬프트
    message_text      VARCHAR2(1000),              -- 합성 텍스트
    original_filename VARCHAR2(255) NOT NULL,      -- 원본 이미지 파일명
    final_filename    VARCHAR2(255),               -- 텍스트 합성된 이미지 파일명
    filepath          VARCHAR2(500),               -- 저장 경로
    status            VARCHAR2(20) DEFAULT 'created' NOT NULL, -- 상태
    rdate             DATE DEFAULT SYSDATE         -- 등록일
);

CREATE SEQUENCE mms_img_seq START WITH 1 INCREMENT BY 1;

-- 최신 이미지 조회
SELECT mimgno, original_filename, final_filename, status
FROM mms_img
ORDER BY mimgno DESC;

commit;
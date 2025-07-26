CREATE TABLE mms_send_log (
    mslogno       NUMBER PRIMARY KEY,        -- 발송 로그 PK
    mimgno        NUMBER NOT NULL,           -- MMS 이미지 FK
    memberno      NUMBER NOT NULL,           -- 관리자 회원 번호
    phone_number  VARCHAR2(20) NOT NULL,     -- 수신자 번호
    send_status   VARCHAR2(20) NOT NULL,     -- success/fail
    send_time     DATE DEFAULT SYSDATE,      -- 발송 시간
    CONSTRAINT fk_mms_send_log_img FOREIGN KEY (mimgno)
        REFERENCES mms_img(mimgno)
        
);

CREATE SEQUENCE mms_send_log_seq START WITH 1 INCREMENT BY 1;

-- 발송 내역 확인
SELECT mslogno, mimgno, phone_number, send_status
FROM mms_send_log
ORDER BY mslogno DESC;

commit;

ALTER TABLE mms_send_log
ADD CONSTRAINT fk_mms_send_log_member
FOREIGN KEY (memberno)
REFERENCES member(memberno);
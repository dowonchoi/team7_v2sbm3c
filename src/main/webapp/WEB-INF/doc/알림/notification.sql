DROP TABLE notification;

CREATE TABLE notification (
  notification_id NUMBER PRIMARY KEY,           -- 알림 번호 (시퀀스)
  memberno NUMBER NOT NULL,                    -- 대상 회원 번호
  type VARCHAR2(20),                            -- 알림 유형 ('order', 'qna', 'event' 등)
  message VARCHAR2(500),                        -- 알림 내용
  url VARCHAR2(500),                            -- 클릭 시 이동할 URL
  is_read CHAR(1) DEFAULT 'N',                  -- 읽음 여부 (Y/N)
  created_at DATE DEFAULT SYSDATE               -- 생성일시
);

DROP SEQUENCE notification_seq;

CREATE SEQUENCE notification_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

INSERT INTO notification (notification_id, memberno, type, message, url)
VALUES (notification_seq.NEXTVAL, 101, 'order', '주문이 완료되었습니다.', '/order/12345');

INSERT INTO notification (notification_id, memberno, type, message, url)
VALUES (notification_seq.NEXTVAL, 101, 'qna', '문의하신 글에 답변이 등록되었습니다.', '/qna/9876');

COMMIT;

SELECT COUNT(*)
FROM notification
WHERE memberno = #{memberno}
AND is_read = 'N';
  
SELECT *
FROM notification
WHERE memberno = #{memberno}
ORDER BY created_at DESC;
  
UPDATE notification
SET is_read = 'Y'
WHERE notification_id = #{notification_id};

SELECT * FROM notification ORDER BY created_at DESC;

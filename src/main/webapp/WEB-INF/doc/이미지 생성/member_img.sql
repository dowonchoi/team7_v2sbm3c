CREATE TABLE member_img (
    member_imgno NUMBER PRIMARY KEY,
    memberno NUMBER NOT NULL,
    prompt VARCHAR2(1000) NOT NULL,
    filename VARCHAR2(255) NOT NULL,
    rdate DATE DEFAULT SYSDATE
);

CREATE SEQUENCE member_img_seq START WITH 1 INCREMENT BY 1;

SELECT filename FROM member_img ORDER BY member_imgno DESC;

UPDATE member_img
SET filename = SUBSTR(filename, LENGTH(filename) - INSTR(REVERSE(filename), '/') + 2);

UPDATE member_img
SET filename = REGEXP_SUBSTR(filename, '[^/\\]+$');

DELETE FROM member_img
WHERE filename IN (
  '20250718180134_598.jpg',
  '20250718180019_291.jpg',
  '20250718175912_106.jpg',
  '20250718175818_687.jpg'
);

commit;
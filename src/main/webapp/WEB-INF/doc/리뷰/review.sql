CREATE TABLE review (
  reviewno   NUMBER PRIMARY KEY,
  productsno NUMBER NOT NULL,
  memberno   NUMBER NOT NULL,
  content    CLOB NOT NULL,
  emotion    NUMBER,               -- 1: 긍정, 0: 부정
  summary    VARCHAR2(500),
  rdate      DATE DEFAULT SYSDATE
);

-- ✅ (1) 첫 번째 이미지 파일 정보
ALTER TABLE review ADD file1 VARCHAR2(200);       -- 사용자가 업로드한 원래 파일명
ALTER TABLE review ADD file1saved VARCHAR2(200);  -- 서버에 저장된 고유 파일명
ALTER TABLE review ADD size1 NUMBER;              -- 파일 크기 (바이트)

-- ✅ (2) 두 번째 이미지 파일 정보
ALTER TABLE review ADD file2 VARCHAR2(200);
ALTER TABLE review ADD file2saved VARCHAR2(200);
ALTER TABLE review ADD size2 NUMBER;

-- ✅ (3) 세 번째 이미지 파일 정보
ALTER TABLE review ADD file3 VARCHAR2(200);
ALTER TABLE review ADD file3saved VARCHAR2(200);
ALTER TABLE review ADD size3 NUMBER;


SELECT reviewno, memberno FROM review ORDER BY reviewno DESC;


CREATE SEQUENCE review_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE review ADD CONSTRAINT fk_review_products FOREIGN KEY (productsno) REFERENCES products(productsno);
ALTER TABLE review ADD CONSTRAINT fk_review_member FOREIGN KEY (memberno) REFERENCES member(memberno);

commit;
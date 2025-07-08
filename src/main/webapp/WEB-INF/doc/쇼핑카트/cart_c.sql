-- 장바구니 테이블
CREATE TABLE cart (
  cartno      NUMBER(10) PRIMARY KEY,
  memberno    NUMBER(10) NOT NULL,
  productsno  NUMBER(10) NOT NULL,
  cnt         NUMBER(5) DEFAULT 1 NOT NULL,
  rdate       DATE DEFAULT SYSDATE NOT NULL,
  
  FOREIGN KEY (memberno) REFERENCES member(memberno),
  FOREIGN KEY (productsno) REFERENCES products(productsno)
);
-- 'Y'는 구매 예정, 'N'은 장바구니에만 보관
ALTER TABLE cart ADD selected CHAR(1) DEFAULT 'Y';
-- Y / N 등으로 장바구니 체크 여부 구분하는 필드
ALTER TABLE cart ADD selected VARCHAR2(1) DEFAULT 'Y';

ALTER TABLE cart ADD point NUMBER(5) DEFAULT 0;



CREATE SEQUENCE cart_seq START WITH 1 INCREMENT BY 1;


-- 장바구니 시퀀스
CREATE SEQUENCE cart_seq
  START WITH 1
  INCREMENT BY 1
  NOCACHE;



-- 장바구니 기본 데이터 삽입
INSERT INTO cart(cartno, memberno, productsno, cnt, rdate)
VALUES (cart_seq.nextval, 4, 11, 4, SYSDATE);

INSERT INTO cart(cartno, memberno, productsno, cnt, rdate)
VALUES (cart_seq.nextval, 4, 12, 3, SYSDATE);

commit;

SELECT *
FROM cart
WHERE memberno = 4;


-- 주문 상세 항목 테이블 (N:1 관계에서 N쪽)
CREATE TABLE order_item (
  order_itemno   NUMBER(10) PRIMARY KEY,              -- 주문 상세 고유 번호
  orderno        NUMBER(10) NOT NULL,                 -- 주문 번호 (orders 테이블 FK)
  productsno     NUMBER(10) NOT NULL,                 -- 상품 번호 (products 테이블 FK)
  pname          VARCHAR2(100) NOT NULL,              -- 상품명 (주문 시점 기준 복사)
  thumb1         VARCHAR2(200),                       -- 상품 썸네일 이미지
  price          NUMBER(10) DEFAULT 0,                -- 원가
  dc             NUMBER(3) DEFAULT 0,                 -- 할인율 (예: 10 = 10%)
  saleprice      NUMBER(10) DEFAULT 0,                -- 할인가 (원가 - 할인)
  cnt            NUMBER(5) DEFAULT 1,                 -- 주문 수량
  totalprice     NUMBER(10) DEFAULT 0,                -- 수량 × 할인가
  point          NUMBER(5) DEFAULT 0,                 -- 개당 적립 포인트
  totalpoint     NUMBER(10) DEFAULT 0,                -- 수량 × 포인트

  CONSTRAINT fk_order_item_orderno FOREIGN KEY (orderno)
    REFERENCES orders(orderno),
    
  CONSTRAINT fk_order_item_productsno FOREIGN KEY (productsno)
    REFERENCES products(productsno)
);

CREATE SEQUENCE order_item_seq
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  NOCACHE
  NOCYCLE;

INSERT INTO order_item (
  order_itemno, orderno, productsno, pname, thumb1,
  price, dc, saleprice, cnt, totalprice,
  point, totalpoint
) VALUES (
  order_item_seq.NEXTVAL, 1001, 38, '상큼 아삭한 피망', '피망2_t.jpg',
  10000, 10, 9000, 2, 18000,
  100, 200
);

commit;
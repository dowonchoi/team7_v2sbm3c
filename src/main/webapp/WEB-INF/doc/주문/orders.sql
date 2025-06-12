-- C:/kd/ws_java/team7_v2sbm3c/src/main/webapp/WEB-INF/doc/주문

DROP TABLE orders;

DROP TABLE orders CASCADE ORDERS; -- 자식 무시하고 삭제 가능

CREATE TABLE orders (
  orderno        NUMBER(10)     NOT NULL,
  total_price    NUMBER(10)     NOT NULL,
  payment_method VARCHAR(20)    NULL,
  order_date     DATE           NULL,
  status         VARCHAR(20)    DEFAULT '주문완료',
  zipcode        VARCHAR(5)     NULL,
  address1       VARCHAR(80)    NULL,
  address2       VARCHAR(50)    NULL,
  memberno       NUMBER(10)     NOT NULL,
  CONSTRAINT PK_ORDERS PRIMARY KEY (orderno),
  CONSTRAINT FK_ORDERS_MEMBER FOREIGN KEY (memberno) REFERENCES member(memberno)
);

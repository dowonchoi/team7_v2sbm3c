-- 배송 현황 테이블 생성 (운송장 번호 기반 조회용)
DROP TABLE shipments;

CREATE TABLE shipments (
  shipmentno   NUMBER(10) PRIMARY KEY,         -- 배송 번호 (시퀀스 사용 추천)
  ship_method  VARCHAR2(50) NOT NULL,          -- 배송 방식 (택배, 퀵, 방문 등)
  trackingno   VARCHAR2(100) NOT NULL UNIQUE,  -- 운송장 번호 (조회용, UNIQUE 설정 권장)
  sdate        DATE,                           -- 배송 시작일
  adate        DATE,                           -- 도착 예정일
  del_status   VARCHAR2(30),                   -- 배송 상태 (배송중, 배송완료 등)
  memberno     NUMBER(10)                      -- 회원 번호 (필요시 FK 연동 가능)
);

DROP SEQUENCE shipments_seq;

-- 시퀀스 생성 (자동 증가용)
CREATE SEQUENCE shipments_seq
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;
  
ALTER TABLE shipments ADD orderno NUMBER(10);

-- FK 추가 (배송 → 주문 연결)
ALTER TABLE shipments
ADD CONSTRAINT fk_shipments_orders
FOREIGN KEY (orderno) REFERENCES orders(orderno);
  
INSERT INTO shipments(shipmentno, ship_method, trackingno, sdate, adate, del_status, memberno, orderno)
VALUES(shipments_seq.NEXTVAL, '택배', 'T1234567890', SYSDATE, SYSDATE + 3, '배송중', 101, 1001);

INSERT INTO shipments (shipmentno, ship_method, trackingno, sdate, adate, del_status, memberno)
VALUES (shipments_seq.NEXTVAL, '퀵서비스', 'Q9876543210', SYSDATE, SYSDATE + 1, '배송완료', 102);

COMMIT;
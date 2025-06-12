DROP TABLE productsgood;

CREATE TABLE productsgood (
    productsgoodno    NUMBER(10)        NOT NULL,
    rdate            DATE        NOT NULL,
    productsno        NUMBER(10)        NOT NULL,
    memberno        NUMBER(10)        NOT NULL,
    PRIMARY KEY (productsgoodno),
    FOREIGN KEY (productsno) REFERENCES products(productsno),
    FOREIGN KEY (memberno) REFERENCES member(memberno)
);


DROP SEQUENCE productsgood_seq;

CREATE SEQUENCE productsgood_seq
START WITH 1         -- 시작 번호
INCREMENT BY 1       -- 증가값
MAXVALUE 9999999999  -- 최대값: 9999999999 --> NUMBER(10) 대응
CACHE 2              -- 2번은 메모리에서만 계산
NOCYCLE;             -- 다시 1부터 생성되는 것을 방지


-- 데이터 삽입
INSERT INTO productsgood(productsgoodno, rdate, productsno, memberno)
VALUES (productsgood_seq.nextval, sysdate, 43, 1);

INSERT INTO productsgood(productsgoodno, rdate, productsno, memberno)
VALUES (productsgood_seq.nextval, sysdate, 43., 1);

INSERT INTO productsgood(productsgoodno, rdate, productsno, memberno)
VALUES (productsgood_seq.nextval, sysdate, 43, 1);

INSERT INTO productsgood(productsgoodno, rdate, productsno, memberno)
VALUES (productsgood_seq.nextval, sysdate, 43, 1);

COMMIT;

-- 전체 목록
SELECT productsgoodno, rdate, productsno, memberno
FROM productsgood
ORDER BY productsgoodno DESC;

productsgoodNO RDATE               productsNO   MEMBERNO
-------------- ------------------- ---------- ----------
             5 2025-01-07 10:51:32          3          5
             3 2025-01-07 10:50:51          1          4
             2 2025-01-07 10:50:34          1          3
             1 2025-01-07 10:50:17          1          1

-- PK 조회
SELECT productsgoodno, rdate, productsno, memberno
FROM productsgood
WHERE productsgoodno = 5;

-- productsno, memberno로 조회
SELECT productsgoodno, rdate, productsno, memberno
FROM productsgood
WHERE productsno=43 AND memberno=1;

-- 삭제
DELETE FROM productsgood
WHERE productsgoodno = 5;

commit;

-- 특정 컨텐츠의 특정 회원 추천 갯수 산출
SELECT COUNT(*) as cnt
FROM productsgood
WHERE productsno=1 AND memberno=1;

       CNT
----------
         1 <-- 이미 추천을 함
         
SELECT COUNT(*) as cnt
FROM productsgood
WHERE productsno=5 AND memberno=1;

       CNT
----------
         0 <-- 추천 안됨
         
-- JOIN, 어느 배우를 누가 추천 했는가?
SELECT productsgoodno, rdate, productsno, memberno
FROM productsgood
ORDER BY productsgoodno DESC;

-- 테이블 2개 join
SELECT r.productsgoodno, r.rdate, r.productsno, c.title, r.memberno
FROM products c, productsgood r
WHERE c.productsno = r.productsno
ORDER BY productsgoodno DESC;

-- 테이블 3개 join, as 사용시 컴럼명 변경 가능: c.title as c_title
SELECT r.productsgoodno, r.rdate, r.productsno, c.title as c_title, r.memberno, m.id, m.mname
FROM products c, productsgood r, member m
WHERE c.productsno = r.productsno AND r.memberno = m.memberno
ORDER BY productsgoodno DESC;

   
 
 
-- /src/main/webapp/WEB-INF/doc/상품/products_c.sql
DROP TABLE products;

DROP TABLE products CASCADE PRODUCTS; -- 자식 무시하고 삭제 가능
SELECT MAX(productsno) FROM products;

CREATE TABLE products(
        productsno                            NUMBER(10)         NOT NULL,
        memberno                              NUMBER(10)     NOT NULL , -- FK
        cateno                                NUMBER(10)         NOT NULL , -- FK
        title                                 VARCHAR2(200)         NOT NULL,
        content                               CLOB                  NOT NULL,
        recom                                 NUMBER(7)         DEFAULT 0         NOT NULL,
        cnt                                   NUMBER(7)         DEFAULT 0         NOT NULL,
        replycnt                              NUMBER(7)         DEFAULT 0         NOT NULL,
        passwd                                VARCHAR2(100)         NOT NULL,
        word                                  VARCHAR2(200)         NULL ,
        rdate                                 DATE               NOT NULL,
        file1                                   VARCHAR(100)          NULL,  -- 원본 파일명 image
        file1saved                            VARCHAR(100)          NULL,  -- 저장된 파일명, image
        thumb1                              VARCHAR(100)          NULL,   -- preview image
        size1                                 NUMBER(10)      DEFAULT 0 NULL,  -- 파일 사이즈
        price                                 NUMBER(10)      DEFAULT 0 NULL,  
        dc                                    NUMBER(10)      DEFAULT 0 NULL,  
        saleprice                            NUMBER(10)      DEFAULT 0 NULL,  
        point                                 NUMBER(10)      DEFAULT 0 NULL,  
        salecnt                               NUMBER(10)      DEFAULT 0 NULL,
        map                                   VARCHAR2(1000)            NULL,
        youtube                               VARCHAR2(1000)            NULL,
        mp4                                  VARCHAR2(100)            NULL,
        visible                                CHAR(1)         DEFAULT 'Y' NOT NULL,
        file2                                   VARCHAR(100)          NULL,  -- 원본 파일명 image
        file2saved                            VARCHAR(100)          NULL,  -- 저장된 파일명, image
        size2                                 NUMBER(10)      DEFAULT 0 NULL,  -- 파일 사이즈
        file3                                   VARCHAR(100)          NULL,  -- 원본 파일명 image
        file3saved                            VARCHAR(100)          NULL,  -- 저장된 파일명, image
        size3                                 NUMBER(10)      DEFAULT 0 NULL,  -- 파일 사이즈
        expdate                               DATE
        PRIMARY KEY (productsno),
        FOREIGN KEY (memberno) REFERENCES member (memberno),
        FOREIGN KEY (cateno) REFERENCES cate (cateno)
);
ALTER TABLE products ADD item VARCHAR2(200);       -- 품목 또는 명칭
ALTER TABLE products ADD maker VARCHAR2(200);      -- 생산자(수입자)
ALTER TABLE products ADD makedate VARCHAR2(200);   -- 제조연월일
ALTER TABLE products ADD importyn VARCHAR2(200);   -- 수입식품 문구 여부
ALTER TABLE products ADD keep VARCHAR2(200);       -- 보관방법, 취급방법
ALTER TABLE products ADD counsel_tel VARCHAR2(200);  -- 소비자상담 전화번호
ALTER TABLE products ADD sizeinfo VARCHAR2(200);   -- 용량/수량/크기
ALTER TABLE products ADD origin VARCHAR2(200);     -- 원산지
ALTER TABLE products ADD detail VARCHAR2(200);     -- 품목군별 표시사항
ALTER TABLE products ADD pack VARCHAR2(200);       -- 상품 구성
ALTER TABLE products ADD safe VARCHAR2(200);       -- 소비자 안전 주의사항
ALTER TABLE products ADD is_best CHAR(1) DEFAULT 'N';
ALTER TABLE products ADD is_new CHAR(1) DEFAULT 'N';
ALTER TABLE products ADD is_event CHAR(1) DEFAULT 'N';
ALTER TABLE products ADD expire_date DATE;

COMMENT ON TABLE products is '상품';
COMMENT ON COLUMN products.productsno is '상품 번호';
COMMENT ON COLUMN products.memberno is '관리자 번호';
COMMENT ON COLUMN products.cateno is '카테고리 번호';
COMMENT ON COLUMN products.title is '제목';
COMMENT ON COLUMN products.content is '내용';
COMMENT ON COLUMN products.recom is '추천수';
COMMENT ON COLUMN products.cnt is '조회수';
COMMENT ON COLUMN products.replycnt is '댓글수';
COMMENT ON COLUMN products.passwd is '패스워드';
COMMENT ON COLUMN products.word is '검색어';
COMMENT ON COLUMN products.rdate is '등록일';
COMMENT ON COLUMN products.file1 is '메인 이미지';
COMMENT ON COLUMN products.file1saved is '실제 저장된 메인 이미지';
COMMENT ON COLUMN products.thumb1 is '메인 이미지 Preview';
COMMENT ON COLUMN products.size1 is '메인 이미지 크기';
COMMENT ON COLUMN products.price is '정가';
COMMENT ON COLUMN products.dc is '할인률';
COMMENT ON COLUMN products.saleprice is '판매가';
COMMENT ON COLUMN products.point is '포인트';
COMMENT ON COLUMN products.salecnt is '재고 수량';
COMMENT ON COLUMN products.map is '지도';
COMMENT ON COLUMN products.youtube is 'Youtube 영상';
COMMENT ON COLUMN products.mp4 is '영상';
COMMENT ON COLUMN products.visible is '출력 모드';
COMMENT ON COLUMN products.file2 is '메인 이미지2';
COMMENT ON COLUMN products.file2saved is '실제 저장된 메인 이미지2';
COMMENT ON COLUMN products.thumb2 is '메인 이미지 Preview2';
COMMENT ON COLUMN products.size2 is '메인 이미지 크기2';
COMMENT ON COLUMN products.file3 is '메인 이미지3';
COMMENT ON COLUMN products.file3saved is '실제 저장된 메인 이미지3';
COMMENT ON COLUMN products.thumb3 is '메인 이미지 Preview3';
COMMENT ON COLUMN products.size3 is '메인 이미지 크기3';
-- 📝 컬럼 주석
COMMENT ON COLUMN products.item IS '품목 또는 명칭';
COMMENT ON COLUMN products.maker IS '생산자(수입자)';
COMMENT ON COLUMN products.makedate IS '제조연월일, 소비기한';
COMMENT ON COLUMN products.importyn IS '수입식품 문구 여부';
COMMENT ON COLUMN products.keep IS '보관방법, 취급방법';
COMMENT ON COLUMN products.counsel_tel IS '소비자상담 전화번호';
COMMENT ON COLUMN products.sizeinfo IS '용량, 수량, 크기';
COMMENT ON COLUMN products.origin IS '원산지';
COMMENT ON COLUMN products.detail IS '세부 품목군별 표시사항';
COMMENT ON COLUMN products.pack IS '상품 구성';
COMMENT ON COLUMN products.safe IS '소비자 안전 주의사항';

DROP SEQUENCE products_seq;

CREATE SEQUENCE products_seq
  START WITH 10                -- 시작 번호 원래는 1이었는데....
  INCREMENT BY 1            -- 증가값
  MAXVALUE 9999999999  -- 최대값: 9999999999 --> NUMBER(10) 대응
  CACHE 2                        -- 2번은 메모리에서만 계산
  NOCYCLE;                      -- 다시 1부터 생성되는 것을 방지

SELECT productsno, title, price_before, saleprice, dc
FROM products
WHERE title LIKE '%토마토%';

-- 판매자 2번, 제철/채소
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (1, 2, 2, '제철 봄동 1kg', '아삭하고 단맛이 나는 제철 봄동입니다.', '1234', SYSDATE, 4000, 10, 3600, 100, 40, 'Y');

-- 판매자 3번, 제철/과일
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (2, 3, 3, '제철 딸기 500g', '달콤한 국산 딸기, 바로 수확해 발송합니다.', '1234', SYSDATE, 7000, 20, 5600, 150, 60, 'Y');

-- 판매자 2번, 신선식품/채소
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (3, 2, 8, '무농약 시금치 300g', '건강을 위한 신선한 무농약 시금치입니다.', '1234', SYSDATE, 3000, 10, 2700, 80, 30, 'Y');

-- 판매자 3번, 신선식품/축산
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (4, 3, 7, '1등급 한우 불고기 300g', '부드럽고 육즙 가득한 국내산 한우입니다.', '1234', SYSDATE, 15000, 15, 12750, 250, 20, 'Y');

-- 판매자 2번, 가공식품/견과
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (5, 2, 11, '믹스 견과 세트 1kg', '아몬드, 호두, 캐슈넛이 들어간 프리미엄 견과 세트.', '1234', SYSDATE, 20000, 20, 16000, 300, 50, 'Y');

-- 판매자 3번, 가공식품/제과제빵
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (6, 3, 13, '수제 오트밀 쿠키', '고소하고 부드러운 오트밀 쿠키입니다.', '1234', SYSDATE, 6000, 10, 5400, 120, 40, 'Y');

-- 판매자 2번, 즉석조리/반찬
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (7, 2, 15, '집반찬 멸치볶음', '달콤짭짤한 멸치볶음 반찬입니다.', '1234', SYSDATE, 5000, 5, 4750, 90, 35, 'Y');

-- 판매자 3번, 즉석조리/간편식
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (8, 3, 16, '오븐에 구운 닭가슴살', '전자레인지 2분 조리 간편한 닭가슴살.', '1234', SYSDATE, 8000, 20, 6400, 140, 70, 'Y');

-- 판매자 3번, 신선식품/수산물
INSERT INTO products (productsno, memberno, cateno, title, content, passwd, rdate, price, dc, saleprice, point, salecnt, visible)
VALUES (9, 3, 9, '자숙 문어 다리 300g', '손질된 자숙 문어 다리, 샐러드에 딱!', '1234', SYSDATE, 18000, 20, 14400, 180, 15, 'Y');

COMMIT;

-- 전체 목록
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, passwd, word, rdate,
           file1, file1saved, thumb1, size1, map, youtube, mp4, expdate
FROM products
ORDER BY productsno DESC;

PRODUCTSNO   MEMBERNO     CATENO TITLE                                                                                                                                                                                                    CONTENT                                                                               RECOM        CNT   REPLYCNT PASSWD                                                                                               WORD                                                                                                                                                                                                     RDATE             FILE1                                                                                                FILE1SAVED                                                                                           THUMB1                                                                                                    SIZE1 MAP
---------- ---------- ---------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------- ---------- ---------- ---------- ---------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
YOUTUBE                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  MP4                                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------------------------------------------------------------------------------------------
         9          3          9 자숙 문어 다리 300g                                                                                                                                                                                      손질된 자숙 문어 다리, 샐러드에 딱!                                                       0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         8          3         16 오븐에 구운 닭가슴살                                                                                                                                                                                     전자레인지 2분 조리 간편한 닭가슴살.                                                      0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         7          2         15 집반찬 멸치볶음                                                                                                                                                                                          달콤짭짤한 멸치볶음 반찬입니다.                                                           0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             


PRODUCTSNO   MEMBERNO     CATENO TITLE                                                                                                                                                                                                    CONTENT                                                                               RECOM        CNT   REPLYCNT PASSWD                                                                                               WORD                                                                                                                                                                                                     RDATE             FILE1                                                                                                FILE1SAVED                                                                                           THUMB1                                                                                                    SIZE1 MAP
---------- ---------- ---------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------- ---------- ---------- ---------- ---------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
YOUTUBE                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  MP4                                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------------------------------------------------------------------------------------------
         6          3         13 수제 오트밀 쿠키                                                                                                                                                                                         고소하고 부드러운 오트밀 쿠키입니다.                                                      0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         5          2         11 믹스 견과 세트 1kg                                                                                                                                                                                       아몬드, 호두, 캐슈넛이 들어간 프리미엄 견과 세트.                                         0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         4          3          7 1등급 한우 불고기 300g                                                                                                                                                                                   부드럽고 육즙 가득한 국내산 한우입니다.                                                   0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             


PRODUCTSNO   MEMBERNO     CATENO TITLE                                                                                                                                                                                                    CONTENT                                                                               RECOM        CNT   REPLYCNT PASSWD                                                                                               WORD                                                                                                                                                                                                     RDATE             FILE1                                                                                                FILE1SAVED                                                                                           THUMB1                                                                                                    SIZE1 MAP
---------- ---------- ---------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------- ---------- ---------- ---------- ---------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------------------------------------------------------------------------------------------------- ---------- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
YOUTUBE                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  MP4                                                                                                 
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ----------------------------------------------------------------------------------------------------
         3          2          8 무농약 시금치 300g                                                                                                                                                                                       건강을 위한 신선한 무농약 시금치입니다.                                                   0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         2          3          3 제철 딸기 500g                                                                                                                                                                                           달콤한 국산 딸기, 바로 수확해 발송합니다.                                                 0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

         1          2          2 제철 봄동 1kg                                                                                                                                                                                            아삭하고 단맛이 나는 제철 봄동입니다.                                                     0          0          0 1234                                                                                                                                                                                                                                                                                                          25/06/12 03:33:15                                                                                                                                                                                                                                                                                                                         0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
                       

-- 1번 cateno 만 출력
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, passwd, word, rdate,
        LOWER(file1) as file1, file1saved, thumb1, size1, map, youtube, mp4
FROM products
WHERE cateno=2
ORDER BY productsno DESC;


-- 모든 레코드 삭제
DELETE FROM products;
commit;

-- 삭제
DELETE FROM products
WHERE productsno = 1;
commit;


DELETE FROM products
WHERE cateno=12 AND productsno <= 41;

commit;


-- ----------------------------------------------------------------------------------------------------
-- 검색, cateno별 검색 목록
-- ----------------------------------------------------------------------------------------------------
-- 모든글
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, word, rdate,
       file1, file1saved, thumb1, size1, map, youtube
FROM products
ORDER BY productsno ASC;

-- 카테고리별 목록
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, word, rdate,
       file1, file1saved, thumb1, size1, map, youtube, file2, file2saved, thumb2, file3, file3saved, thumb3
FROM products
--WHERE cateno=2
ORDER BY productsno ASC;

-- 1) 검색
-- ① cateno별 검색 목록
-- word 컬럼의 존재 이유: 검색 정확도를 높이기 위하여 중요 단어를 명시
-- 글에 'swiss'라는 단어만 등장하면 한글로 '스위스'는 검색 안됨.
-- 이런 문제를 방지하기위해 'swiss,스위스,스의스,수의스,유럽' 검색어가 들어간 word 컬럼을 추가함.
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, word, rdate,
           file1, file1saved, thumb1, size1, map, youtube
FROM products
WHERE cateno=8 AND word LIKE '%부대찌게%'
ORDER BY productsno DESC;

-- title, content, word column search
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, word, rdate,
           file1, file1saved, thumb1, size1, map, youtube
FROM products
WHERE cateno=8 AND (title LIKE '%부대찌게%' OR content LIKE '%부대찌게%' OR word LIKE '%부대찌게%')
ORDER BY productsno DESC;

-- ② 검색 레코드 갯수
-- 전체 레코드 갯수, 집계 함수
SELECT COUNT(*)
FROM products
WHERE cateno=8;

  COUNT(*)  <- 컬럼명
----------
         5
         
SELECT COUNT(*) as cnt -- 함수 사용시는 컬럼 별명을 선언하는 것을 권장
FROM products
WHERE cateno=8;

       CNT <- 컬럼명
----------
         5

-- cateno 별 검색된 레코드 갯수
SELECT COUNT(*) as cnt
FROM products
WHERE cateno=8 AND word LIKE '%부대찌게%';

SELECT COUNT(*) as cnt
FROM products
WHERE cateno=8 AND (title LIKE '%부대찌게%' OR content LIKE '%부대찌게%' OR word LIKE '%부대찌게%');

-- SUBSTR(컬럼명, 시작 index(1부터 시작), 길이), 부분 문자열 추출
SELECT productsno, SUBSTR(title, 1, 4) as title
FROM products
WHERE cateno=8 AND (content LIKE '%부대%');

-- SQL은 대소문자를 구분하지 않으나 WHERE문에 명시하는 값은 대소문자를 구분하여 검색
SELECT productsno, title, word
FROM products
WHERE cateno=8 AND (word LIKE '%FOOD%');

SELECT productsno, title, word
FROM products
WHERE cateno=8 AND (word LIKE '%food%'); 

SELECT productsno, title, word
FROM products
WHERE cateno=8 AND (LOWER(word) LIKE '%food%'); -- 대소문자를 일치 시켜서 검색

SELECT productsno, title, word
FROM products
WHERE cateno=8 AND (UPPER(word) LIKE '%' || UPPER('FOOD') || '%'); -- 대소문자를 일치 시켜서 검색 ★

SELECT productsno, title, word
FROM products
WHERE cateno=8 AND (LOWER(word) LIKE '%' || LOWER('Food') || '%'); -- 대소문자를 일치 시켜서 검색

SELECT productsno || '. ' || title || ' 태그: ' || word as title -- 컬럼의 결합, ||
FROM products
WHERE cateno=8 AND (LOWER(word) LIKE '%' || LOWER('Food') || '%'); -- 대소문자를 일치 시켜서 검색


SELECT UPPER('한글') FROM dual; -- dual: 오라클에서 SQL 형식을 맞추기위한 시스템 테이블

-- ----------------------------------------------------------------------------------------------------
-- 검색 + 페이징 + 메인 이미지
-- ----------------------------------------------------------------------------------------------------
-- step 1
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube
FROM products
WHERE cateno=1 AND (title LIKE '%단풍%' OR content LIKE '%단풍%' OR word LIKE '%단풍%')
ORDER BY productsno DESC;

-- step 2
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, rownum as r
FROM (
          SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                     file1, file1saved, thumb1, size1, map, youtube
          FROM products
          WHERE cateno=1 AND (title LIKE '%단풍%' OR content LIKE '%단풍%' OR word LIKE '%단풍%')
          ORDER BY productsno DESC
);

-- step 3, 1 page
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1 AND (title LIKE '%단풍%' OR content LIKE '%단풍%' OR word LIKE '%단풍%')
                     ORDER BY productsno DESC
           )          
)
WHERE r >= 1 AND r <= 3;

-- step 3, 2 page
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1 AND (title LIKE '%단풍%' OR content LIKE '%단풍%' OR word LIKE '%단풍%')
                     ORDER BY productsno DESC
           )          
)
WHERE r >= 4 AND r <= 6;

-- 대소문자를 처리하는 페이징 쿼리
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1 AND (UPPER(title) LIKE '%' || UPPER('단풍') || '%' 
                                         OR UPPER(content) LIKE '%' || UPPER('단풍') || '%' 
                                         OR UPPER(word) LIKE '%' || UPPER('단풍') || '%')
                     ORDER BY productsno DESC
           )          
)
WHERE r >= 1 AND r <= 3;

-- ----------------------------------------------------------------------------
-- 조회
-- ----------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, passwd, word, rdate,
           file1, file1saved, thumb1, size1, map, youtube, emotion, summary
FROM products
WHERE productsno = 1;

-- ----------------------------------------------------------------------------
-- 다음 지도, MAP, 먼저 레코드가 등록되어 있어야함.
-- map                                   VARCHAR2(1000)         NULL ,
-- ----------------------------------------------------------------------------
-- MAP 등록/수정
UPDATE products SET map='카페산 지도 스크립트' WHERE productsno=1;

-- MAP 삭제
UPDATE products SET map='' WHERE productsno=1;

commit;

-- ----------------------------------------------------------------------------
-- Youtube, 먼저 레코드가 등록되어 있어야함.
-- youtube                                   VARCHAR2(1000)         NULL ,
-- ----------------------------------------------------------------------------
-- youtube 등록/수정
UPDATE products SET youtube='Youtube 스크립트' WHERE productsno=1;

-- youtube 삭제
UPDATE products SET youtube='' WHERE productsno=1;

commit;

-- 패스워드 검사, id="password_check", 0 or 1
SELECT COUNT(*) as cnt 
FROM products
WHERE productsno=30 AND passwd='fS/kjO+fuEKk06Zl7VYMhg==';

       CNT
----------
         1

-- 텍스트 수정: 예외 컬럼: 추천수, 조회수, 댓글 수
UPDATE products
SET title='기차를 타고', content='계획없이 여행 출발',  word='나,기차,생각' 
WHERE productsno = 2;

-- ERROR, " 사용 에러
UPDATE products
SET title='기차를 타고', content="계획없이 '여행' 출발",  word='나,기차,생각'
WHERE productsno = 1;

-- ERROR, \' 에러
UPDATE products
SET title='기차를 타고', content='계획없이 \'여행\' 출발',  word='나,기차,생각'
WHERE productsno = 1;

-- SUCCESS, '' 한번 ' 출력됨.
UPDATE products
SET title='기차를 타고', content='계획없이 ''여행'' 출발',  word='나,기차,생각'
WHERE productsno = 1;

-- SUCCESS
UPDATE products
SET title='기차를 타고', content='계획없이 "여행" 출발',  word='나,기차,생각'
WHERE productsno = 1;

UPDATE products
SET title='기차를 타고', content='계획없이 "여행" 출발',  word='나,기차,생각', emotion=1, summary='여행'
WHERE productsno = 1;

commit;

-- 파일 수정
UPDATE products
SET file1='train.jpg', file1saved='train.jpg', thumb1='train_t.jpg', size1=5000
WHERE productsno = 1;

-- 삭제
DELETE FROM products
WHERE productsno = 42;

commit;

DELETE FROM products
WHERE productsno >= 7;

commit;

-- cateno FK 특정 그룹에 속한 레코드 갯수 산출
SELECT COUNT(*) as cnt 
FROM products 
WHERE cateno=1;

-- memberno FK 특정 관리자에 속한 레코드 갯수 산출
SELECT COUNT(*) as cnt 
FROM products 
WHERE memberno=1;

-- cateno FK 특정 그룹에 속한 레코드 모두 삭제
DELETE FROM products
WHERE cateno=1;

-- memberno FK 특정 관리자에 속한 레코드 모두 삭제
DELETE FROM products
WHERE memberno=1;

commit;

-- 다수의 카테고리에 속한 레코드 갯수 산출: IN
SELECT COUNT(*) as cnt
FROM products
WHERE cateno IN(1,2,3);

-- 다수의 카테고리에 속한 레코드 모두 삭제: IN
SELECT productsno, memberno, cateno, title
FROM products
WHERE cateno IN(1,2,3);

CONTENTSNO    ADMINNO     CATENO TITLE                                                                                                                                                                                                                                                                                                       
---------- ---------- ---------- ------------------------
         3             1                   1           인터스텔라                                                                                                                                                                                                                                                                                                  
         4             1                   2           드라마                                                                                                                                                                                                                                                                                                      
         5             1                   3           컨저링                                                                                                                                                                                                                                                                                                      
         6             1                   1           마션       
         
SELECT productsno, memberno, cateno, title
FROM products
WHERE cateno IN('1','2','3');

CONTENTSNO    ADMINNO     CATENO TITLE                                                                                                                                                                                                                                                                                                       
---------- ---------- ---------- ------------------------
         3             1                   1           인터스텔라                                                                                                                                                                                                                                                                                                  
         4             1                   2           드라마                                                                                                                                                                                                                                                                                                      
         5             1                   3           컨저링                                                                                                                                                                                                                                                                                                      
         6             1                   1           마션       

-- ----------------------------------------------------------------------------------------------------
-- cate + products INNER JOIN
-- ----------------------------------------------------------------------------------------------------
-- 모든글
SELECT c.name,
       t.productsno, t.memberno, t.cateno, t.title, t.content, t.recom, t.cnt, t.replycnt, t.word, t.rdate,
       t.file1, t.file1saved, t.thumb1, t.size1, t.map, t.youtube
FROM cate c, products t
WHERE c.cateno = t.cateno
ORDER BY t.productsno DESC;

-- products, member INNER JOIN
SELECT t.productsno, t.memberno, t.cateno, t.title, t.content, t.recom, t.cnt, t.replycnt, t.word, t.rdate,
       t.file1, t.file1saved, t.thumb1, t.size1, t.map, t.youtube,
       a.mname
FROM member a, products t
WHERE a.memberno = t.memberno
ORDER BY t.productsno DESC;

SELECT t.productsno, t.memberno, t.cateno, t.title, t.content, t.recom, t.cnt, t.replycnt, t.word, t.rdate,
       t.file1, t.file1saved, t.thumb1, t.size1, t.map, t.youtube,
       a.mname
FROM member a INNER JOIN products t ON a.memberno = t.memberno
ORDER BY t.productsno DESC;

-- ----------------------------------------------------------------------------------------------------
-- View + paging
-- ----------------------------------------------------------------------------------------------------
CREATE OR REPLACE VIEW vproducts
AS
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, word, rdate,
        file1, file1saved, thumb1, size1, map, youtube
FROM products
ORDER BY productsno DESC;
                     
-- 1 page
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
       file1, file1saved, thumb1, size1, map, youtube, r
FROM (
     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
            file1, file1saved, thumb1, size1, map, youtube, rownum as r
     FROM vproducts -- View
     WHERE cateno=14 AND (title LIKE '%야경%' OR content LIKE '%야경%' OR word LIKE '%야경%')
)
WHERE r >= 1 AND r <= 3;

-- 2 page
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
       file1, file1saved, thumb1, size1, map, youtube, r
FROM (
     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
            file1, file1saved, thumb1, size1, map, youtube, rownum as r
     FROM vproducts -- View
     WHERE cateno=14 AND (title LIKE '%야경%' OR content LIKE '%야경%' OR word LIKE '%야경%')
)
WHERE r >= 4 AND r <= 6;


-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 좋아요(recom) 기준, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, thumb1, r
FROM (
           SELECT productsno, memberno, cateno, title, thumb1, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, thumb1
                     FROM products
                     WHERE cateno=1
                     ORDER BY recom DESC
           )          
)
WHERE r >= 1 AND r <= 7;

-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 평점(score) 기준, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1
                     ORDER BY score DESC
           )          
)
WHERE r >= 1 AND r <= 7;

-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 최신 상품 기준, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1
                     ORDER BY rdate DESC
           )          
)
WHERE r >= 1 AND r <= 7;

-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 조회수 높은 상품기준, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1
                     ORDER BY cnt DESC
           )          
)
WHERE r >= 1 AND r <= 7;

-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 낮은 가격 상품 추천, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1
                     ORDER BY price ASC
           )          
)
WHERE r >= 1 AND r <= 7;

-- ----------------------------------------------------------------------------------------------------
-- 관심 카테고리의 높은 가격 상품 추천, 1번 회원이 1번 카테고리를 추천 받는 경우, 추천 상품이 7건일 경우
-- ----------------------------------------------------------------------------------------------------
SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
           file1, file1saved, thumb1, size1, map, youtube, r
FROM (
           SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                      file1, file1saved, thumb1, size1, map, youtube, rownum as r
           FROM (
                     SELECT productsno, memberno, cateno, title, content, recom, cnt, replycnt, rdate,
                                file1, file1saved, thumb1, size1, map, youtube
                     FROM products
                     WHERE cateno=1
                     ORDER BY price DESC
           )          
)
WHERE r >= 1 AND r <= 7;

-----------------------------------------------------------
-- FK cateno 컬럼에 대응하는 필수 SQL
-----------------------------------------------------------
-- 특정 카테고리에 속한 레코드 갯수를 리턴
SELECT COUNT(*) as cnt 
FROM products 
WHERE cateno=1;
  
-- 특정 카테고리에 속한 모든 레코드 삭제
DELETE FROM products
WHERE cateno=1;

-----------------------------------------------------------
-- FK memberno 컬럼에 대응하는 필수 SQL
-----------------------------------------------------------
-- 특정 회원에 속한 레코드 갯수를 리턴
SELECT COUNT(*) as cnt 
FROM products 
WHERE memberno=1;
  
-- 특정 회원에 속한 모든 레코드 삭제
DELETE FROM products
WHERE memberno=1;

-----------------------------------------------------------
-- 추천 관련 SQL
-----------------------------------------------------------
-- 추천
UPDATE products
SET recom = recom + 1
WHERE productsno = 1;

-- 비추천
UPDATE products
SET recom = recom - 1
WHERE productsno = 1;


-----------------------------------------------------------
-- 댓글 관련 SQL
-----------------------------------------------------------
1) 댓글수 증가
UPDATE products
SET replycnt = replycnt + 1
WHERE productsno = 3;

2) 댓글수 감소
UPDATE products
SET replycnt = replycnt - 1
WHERE productsno = 3;   

commit;

-----------------------------------------------------------
-- 이미지 3장까지 지원하는 슬라이드용 컬럼 추가 SQL
-----------------------------------------------------------
-- 이미지 2
ALTER TABLE products ADD file2 VARCHAR2(100);
ALTER TABLE products ADD file2saved VARCHAR2(100);
ALTER TABLE products ADD thumb2 VARCHAR2(100);
ALTER TABLE products ADD size2 NUMBER(10) DEFAULT 0;

-- 이미지 3
ALTER TABLE products ADD file3 VARCHAR2(100);
ALTER TABLE products ADD file3saved VARCHAR2(100);
ALTER TABLE products ADD thumb3 VARCHAR2(100);
ALTER TABLE products ADD size3 NUMBER(10) DEFAULT 0;

-- 소비기한
ALTER TABLE products ADD expdate DATE;
--

-- 상품 상세 정보용 컬럼 추가
ALTER TABLE products ADD item VARCHAR2(200);       -- 품목 또는 명칭
ALTER TABLE products ADD maker VARCHAR2(200);      -- 생산자(수입자)
ALTER TABLE products ADD makedate VARCHAR2(200);   -- 제조연월일
ALTER TABLE products ADD importyn VARCHAR2(200);   -- 수입식품 문구 여부
ALTER TABLE products ADD keep VARCHAR2(200);       -- 보관방법, 취급방법
ALTER TABLE products ADD counsel_tel VARCHAR2(200);  -- 소비자상담 전화번호
ALTER TABLE products ADD sizeinfo VARCHAR2(200);   -- 용량/수량/크기
ALTER TABLE products ADD origin VARCHAR2(200);     -- 원산지
ALTER TABLE products ADD detail VARCHAR2(200);     -- 품목군별 표시사항
ALTER TABLE products ADD pack VARCHAR2(200);       -- 상품 구성
ALTER TABLE products ADD safe VARCHAR2(200);       -- 소비자 안전 주의사항



SELECT productsno, title, expdate FROM products WHERE productsno = 38; -- 테스트용

commit;
DESC products;

SELECT * FROM products WHERE productsno = 11;


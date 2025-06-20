/**********************************/
/* Table Name: 카테고리 */
/**********************************/
-- 장르를 카테고리 그룹 테이블로 처리하지 않고 컬럼으로 처리함 ★
DROP TABLE CATE;
DROP TABLE CATE CASCADE CONSTRAINTS; 

CREATE TABLE CATE(
    CATENO                            NUMBER(10)     NOT NULL     PRIMARY KEY,
    GRP                               VARCHAR2(30)  NOT NULL,  
    NAME                              VARCHAR2(30)  NOT NULL,
    CNT                               NUMBER(7)     DEFAULT 0     NOT NULL,
    SEQNO                             NUMBER(5)     DEFAULT 1     NOT NULL,
    VISIBLE                           CHAR(1)      DEFAULT 'N'    NOT NULL,
    RDATE                             DATE          NOT NULL
);

COMMENT ON TABLE cate is '카테고리';
COMMENT ON COLUMN cate.CATENO is '카테고리 번호';
COMMENT ON COLUMN cate.GRP is '그룹 이름';
COMMENT ON COLUMN cate.NAME is '카테고리 이름';
COMMENT ON COLUMN cate.CNT is '관련 자료수';
COMMENT ON COLUMN cate.SEQNO is '출력 순서';
COMMENT ON COLUMN cate.VISIBLE is '출력 모드';
COMMENT ON COLUMN cate.RDATE is '등록일';

DROP SEQUENCE CATE_SEQ;

CREATE SEQUENCE CATE_SEQ
START WITH 1         -- 시작 번호
INCREMENT BY 1       -- 증가값
MAXVALUE 9999999999  -- 최대값: 9999999999 --> NUMBER(10) 대응
CACHE 2              -- 2번은 메모리에서만 계산
NOCYCLE;             -- 다시 1부터 생성되는 것을 방지


--> CREATE
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '제철', '--', 0, 1, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '제철', '채소', 0, 2, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '제철', '과일', 0, 3, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '제철', '해산물', 0, 4, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '신선식품', '--', 0, 100, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '신선식품', '과일', 0, 101, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '신선식품', '축산/계란', 0, 102, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '신선식품', '채소', 0, 103, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '신선식품', '수산물/건어물', 0, 103, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '가공식품', '--', 0, 200, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '가공식품', '견과/건과', 0, 201, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '가공식품', '장/소스/드레싱/식초', 0, 202, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '가공식품', '제과제빵/시리얼', 0, 203, 'Y', SYSDATE);

-- 이것 외 가공 식품
-- 식품 선물세트, 건강식품(홍삼, 비타민 등), 생수/음료, 커피/원두/차, 가루/조미료/오일, 면/통조림

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '즉석조리', '--', 0, 300, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '즉석조리', '반찬', 0, 301, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '즉석조리', '간편식', 0, 302, 'Y', SYSDATE);


-- 삭제 코드

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '못난이', '--', 0, 400, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '못난이', '과일', 0, 400, 'Y', SYSDATE);

INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '못난이', '채소', 0, 400, 'Y', SYSDATE);

-- 이것 외 즉석 조리 식품
-- 냉장, 대용식

-- Test
INSERT INTO cate(cateno, grp, name, cnt, seqno, visible, rdate)
VALUES(CATE_SEQ.nextval, '즉석조리', '냉장', 0, 304, 'Y', SYSDATE);

--> SELECT 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
ORDER BY cateno ASC;

--    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
--         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41
        10 가공식품                       --                                      0        200 Y 25/06/11 06:13:41
        11 가공식품                       견과/건과                               0        201 Y 25/06/11 06:13:41

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        12 가공식품                       장/소스/드레싱/식초                     0        202 Y 25/06/11 06:13:41
        13 가공식품                       제과제빵/시리얼                         0        203 Y 25/06/11 06:15:09
        14 즉석조리                       --                                      0        300 Y 25/06/11 06:15:44
        15 즉석조리                       반찬                                    0        301 Y 25/06/11 06:15:44
        16 즉석조리                       간편식                                  0        302 Y 25/06/11 06:15:44
        17 즉석조리                       냉동                                    0        303 Y 25/06/11 06:15:44
        18 즉석조리                       냉장                                    0        304 Y 25/06/11 06:20:39
        19 즉석조리                       밀키트                                  0        305 Y 25/06/11 06:20:48
        20 못난이                         --                                      0        400 Y 25/06/11 06:36:38
        21 못난이                         과일                                    0        400 Y 25/06/11 06:36:38
        22 못난이                         채소                                    0        400 Y 25/06/11 06:36:38

--> SELECT 조회
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno = 1;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41

--> UPDATE 
UPDATE cate 
SET grp='즉석식품', name='냉장,', seqno=1, visible='Y', rdate=SYSDATE 
WHERE cateno=18;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        18 즉석식품                       냉장,                                   0          1 Y 25/06/11 06:42:53

--> DELETE 
DELETE FROM cate WHERE cateno=18;


--> COUNT(*)
SELECT COUNT(*) as cnt FROM cate;

       CNT
----------
        21

         
COMMIT;


DELETE FROM cate;
COMMIT;


-- 목록 변경됨
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
        18 즉석식품                       냉장,                                   0          1 Y 25/06/11 06:42:53
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41
        10 가공식품                       --                                      0        200 Y 25/06/11 06:13:41

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        11 가공식품                       견과/건과                               0        201 Y 25/06/11 06:13:41
        12 가공식품                       장/소스/드레싱/식초                     0        202 Y 25/06/11 06:13:41
        13 가공식품                       제과제빵/시리얼                         0        203 Y 25/06/11 06:15:09
        14 즉석조리                       --                                      0        300 Y 25/06/11 06:15:44
        15 즉석조리                       반찬                                    0        301 Y 25/06/11 06:15:44
        16 즉석조리                       간편식                                  0        302 Y 25/06/11 06:15:44
        17 즉석조리                       냉동                                    0        303 Y 25/06/11 06:15:44
        19 즉석조리                       밀키트                                  0        305 Y 25/06/11 06:20:48
        20 못난이                         --                                      0        400 Y 25/06/11 06:36:38
        21 못난이                         과일                                    0        400 Y 25/06/11 06:36:38

-- 출력 우선순위 낮춤
UPDATE cate SET seqno=seqno+1 WHERE cateno=21;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;

UPDATE cate SET seqno=seqno+1 WHERE cateno=21;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;
    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE              
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
        21 못난이                         과일                                    0        402 Y 25/06/11 06:36:38

         
-- 출력 우선순위 높임
UPDATE cate SET seqno=seqno-1 WHERE cateno=10;
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;

COMMIT;

SELECT cateno, grp, name, cnt, seqno, visible, rdate 
FROM cate 
ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        18 즉석식품                       냉장,                                   0          1 Y 25/06/11 06:42:53
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41
        10 가공식품                       --                                      0        200 Y 25/06/11 06:13:41

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        11 가공식품                       견과/건과                               0        201 Y 25/06/11 06:13:41
        12 가공식품                       장/소스/드레싱/식초                     0        202 Y 25/06/11 06:13:41
        13 가공식품                       제과제빵/시리얼                         0        203 Y 25/06/11 06:15:09
        14 즉석조리                       --                                      0        300 Y 25/06/11 06:15:44
        15 즉석조리                       반찬                                    0        301 Y 25/06/11 06:15:44
        16 즉석조리                       간편식                                  0        302 Y 25/06/11 06:15:44
        17 즉석조리                       냉동                                    0        303 Y 25/06/11 06:15:44

-- 카테고리 공개 설정
UPDATE cate SET visible='Y' WHERE cateno=1;

-- 카테고리 비공개 설정 
UPDATE cate SET visible='N' WHERE cateno=1;  


COMMIT;


--------------------------------------------------------------------------------
-- 대분류, 중분류 처리
--------------------------------------------------------------------------------
-- 회원/비회원에게 공개할 카테고리 그룹(대분류) 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41
        10 가공식품                       --                                      0        200 Y 25/06/11 06:13:41
        11 가공식품                       견과/건과                               0        201 Y 25/06/11 06:13:41

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
        12 가공식품                       장/소스/드레싱/식초                     0        202 Y 25/06/11 06:13:41
        13 가공식품                       제과제빵/시리얼                         0        203 Y 25/06/11 06:15:09
        14 즉석조리                       --                                      0        300 Y 25/06/11 06:15:44
        15 즉석조리                       반찬                                    0        301 Y 25/06/11 06:15:44
        16 즉석조리                       간편식                                  0        302 Y 25/06/11 06:15:44


SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate WHERE name='--' ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
        10 가공식품                       --                                      0        200 Y 25/06/11 06:13:41
        14 즉석조리                       --                                      0        300 Y 25/06/11 06:15:44


-- 공개된 대분류만 출력(*)
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate WHERE name='--' AND visible='Y' ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE              
---------- ------------------------------ ------------------------------ ---------- ---------- - -------------------
        31 개발                           --                                      0          1 Y 2025-03-27 12:56:03
        32 여행                           --                                      0        101 Y 2025-03-27 12:54:01

-- 회원/비회원에게 공개할 카테고리(중분류) 목록
SELECT cateno, grp, name, cnt, seqno, visible, rdate FROM cate WHERE grp='제철' AND visible='Y' ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         1 제철                           --                                      0          1 Y 25/06/11 06:13:41
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41

-- 제철 그룹의 중분류 출력(*)
SELECT cateno, grp, name, cnt, seqno, visible, rdate 
FROM cate 
WHERE grp='제철' AND name != '--' AND visible = 'Y' 
ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41

-- 제철 그룹의 중분류 출력
SELECT cateno, grp, name, cnt, seqno, visible, rdate 
FROM cate 
WHERE grp='제철' AND name != '--' AND visible = 'Y' 
ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         2 제철                           채소                                    0          2 Y 25/06/11 06:13:41
         3 제철                           과일                                    0          3 Y 25/06/11 06:13:41
         4 제철                           해산물                                  0          4 Y 25/06/11 06:13:41

-- 카테고리 목록 출력
SELECT cateno, grp FROM cate WHERE name = '--' ORDER BY seqno ASC;

    CATENO GRP                           
---------- ------------------------------
         1 제철                          
         5 신선식품                      
        10 가공식품                      
        14 즉석조리                      


SELECT grp FROM cate WHERE name = '--' ORDER BY seqno ASC; -- 권장
GRP                           
------------------------------
제철
신선식품
가공식품
즉석조리

-- SELECT DISTINCT cateno, grp FROM cate ORDER BY seqno ASC;  X

-- FWGHSRO
-- SELECT DISTINCT grp FROM cate ORDER BY seqno ASC; X        
SELECT DISTINCT grp FROM cate;         



-- 검색
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%')
ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41


-- '카테고리 그룹'을 제외한 경우        
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (name != '--') AND ((UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%'))
ORDER BY seqno ASC;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE            
---------- ------------------------------ ------------------------------ ---------- ---------- - -----------------
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41



-- -----------------------------------------------------------------------------
-- 페이징: 정렬 -> ROWNUM -> 분할
-- -----------------------------------------------------------------------------
-- ① 정렬
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE (UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%')
ORDER BY seqno ASC;

-- ② 정렬 -> ROWNUM
SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
FROM (
    SELECT cateno, grp, name, cnt, seqno, visible, rdate
    FROM cate
    WHERE (UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%')
    ORDER BY seqno ASC
);

-- ③ 정렬 -> ROWNUM -> 분할
SELECT cateno, grp, name, cnt, seqno, visible, rdate, r
FROM (
    SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
    FROM (
        SELECT cateno, grp, name, cnt, seqno, visible, rdate
        FROM cate
        WHERE (UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%')
        ORDER BY seqno ASC
    )
)
WHERE r >= 1 AND r <= 3;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE                      R
---------- ------------------------------ ------------------------------ ---------- ---------- - ----------------- ----------
         5 신선식품                       --                                      0        100 Y 25/06/11 06:13:41          1
         6 신선식품                       과일                                    0        101 Y 25/06/11 06:13:41          2
         7 신선식품                       축산/계란                               0        102 Y 25/06/11 06:13:41          3


SELECT cateno, grp, name, cnt, seqno, visible, rdate, r
FROM (
    SELECT cateno, grp, name, cnt, seqno, visible, rdate, rownum as r
    FROM (
        SELECT cateno, grp, name, cnt, seqno, visible, rdate
        FROM cate
        WHERE (UPPER(grp) LIKE '%' || UPPER('신선식품') || '%') OR (UPPER(name) LIKE '%' || UPPER('신선식품') || '%')
        ORDER BY seqno ASC
    )
)
WHERE r >= 4 AND r <= 6;

    CATENO GRP                            NAME                                  CNT      SEQNO V RDATE                      R
---------- ------------------------------ ------------------------------ ---------- ---------- - ----------------- ----------
         8 신선식품                       채소                                    0        103 Y 25/06/11 06:13:41          4
         9 신선식품                       수산물/건어물                           0        103 Y 25/06/11 06:13:41          5

-- 대분류 증가, cateno 1번의 대분류명을 찾음
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno=6;

UPDATE cate SET cnt = cnt + 1 WHERE grp='신선 식품' and name='--'; 

-- 중분류 증가
UPDATE cate SET cnt = cnt + 1 WHERE cateno=5 

-- 대분류 감소, cateno 1번의 대분류명을 찾음
SELECT cateno, grp, name, cnt, seqno, visible, rdate
FROM cate
WHERE cateno=5;

UPDATE cate SET cnt = cnt - 1 WHERE grp='신선 식품' and name='--'; 

-- 중분류 감소
UPDATE cate SET cnt = cnt - 1 WHERE cateno=1 

-- 갯수 전달받아 대분류 감소
UPDATE cate SET cnt = cnt - 5 WHERE grp='신선 식품' and name='--'; 
rollback;




--------------------------------------------------------------------
-- 자료수 갱신 2025 - 06 -19
--------------------------------------------------------------------
-- 1. 모든 cate 테이블의 cnt 초기화
UPDATE cate SET cnt = 0;

-- 2. 중분류: products → cate 직접 매핑
MERGE INTO cate c
USING (
  SELECT cateno, COUNT(*) AS cnt
  FROM products
  GROUP BY cateno
) p
ON (c.cateno = p.cateno)
WHEN MATCHED THEN
  UPDATE SET c.cnt = p.cnt;

-- 3. 대분류: 중분류들의 CNT 합산하여 상위 GRP '--' 카테고리에 반영
MERGE INTO cate parent
USING (
  SELECT grp AS grp_name, SUM(cnt) AS total_cnt
  FROM cate
  WHERE name != '--'  -- 중분류만 집계
  GROUP BY grp
) summary
ON (parent.grp = summary.grp_name AND parent.name = '--') -- 대분류 조건
WHEN MATCHED THEN
  UPDATE SET parent.cnt = summary.total_cnt;













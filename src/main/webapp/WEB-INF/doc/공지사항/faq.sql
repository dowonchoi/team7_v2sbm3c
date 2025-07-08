-- ✅ 기존 FAQ 테이블 삭제 (있으면 삭제)
DROP TABLE faq CASCADE CONSTRAINTS;

-- ✅ FAQ 테이블 생성 (카테고리 + 질문/답변 포함)
CREATE TABLE faq (
    faq_id      NUMBER(10)     NOT NULL,          -- FAQ 번호 (PK)
    cate        VARCHAR2(100)  NOT NULL,          -- 카테고리 (배송, 반품 등)
    question    VARCHAR2(1000) NOT NULL,          -- 질문
    answer      CLOB           NOT NULL,          -- 답변 (긴 글 가능)
    writer_id   VARCHAR2(50),                     -- 작성자 ID (관리자)
    reg_date    DATE           DEFAULT SYSDATE,   -- 등록일
    PRIMARY KEY (faq_id)
);

-- ✅ 시퀀스 생성 (FAQ 번호용)
DROP SEQUENCE faq_seq;

CREATE SEQUENCE faq_seq
START WITH 1
INCREMENT BY 1
MAXVALUE 9999999999
CACHE 2
NOCYCLE;

-- ✅ 샘플 FAQ 데이터 (너가 보내준 이미지 참고)
-- [배송문의] 주문한 상품은 언제 배송되나요?
INSERT INTO faq (faq_id, cate, question, answer, writer_id)
VALUES (faq_seq.nextval, '배송문의', '주문한 상품은 언제 배송되나요?', '상품은 보통 결제 후 2~3일 내 배송됩니다.', 'admin');

-- [교환/반품] 상품을 교환/반품하고 싶어요.
INSERT INTO faq (faq_id, cate, question, answer, writer_id)
VALUES (faq_seq.nextval, '교환/반품', '상품을 교환/반품하고 싶어요.', '고객센터로 문의하시거나 마이페이지 > 주문 내역에서 직접 신청 가능합니다.', 'admin');

-- [회원서비스] 와우 멤버십을 해지하고 싶어요.
INSERT INTO faq (faq_id, cate, question, answer, writer_id)
VALUES (faq_seq.nextval, '회원서비스', '와우 멤버십을 해지하고 싶어요.', '마이페이지 > 멤버십 관리에서 해지 가능합니다.', 'admin');

COMMIT;

-- ✅ 카테고리별 FAQ 조회
-- 배송문의
SELECT * FROM faq WHERE cate = '배송문의';

-- 교환/반품
SELECT * FROM faq WHERE cate = '교환/반품';

-- 전체 FAQ 목록
SELECT * FROM faq ORDER BY cate, faq_id DESC;

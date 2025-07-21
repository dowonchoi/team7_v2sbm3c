-- 기존 테이블 삭제
DROP TABLE cancel;

-- Cancel 테이블 생성 (수정본)
CREATE TABLE cancel (
  cancel_id NUMBER PRIMARY KEY,               -- 신청 ID (시퀀스 사용)
  orderno NUMBER NOT NULL,                    -- 주문 번호
  memberno NUMBER NOT NULL,                   -- 회원 번호
  mname VARCHAR2(100),                        -- 회원 이름 (추가된 필드)
  type VARCHAR2(20),                          -- 유형: '취소', '교환', '반품'
  reason VARCHAR2(1000),                      -- 사유
  status VARCHAR2(20) DEFAULT '대기중',       -- 처리 상태: '대기중', '처리중', '완료'
  is_read CHAR(1) DEFAULT 'N',                -- 관리자 확인 여부 (Y/N)
  created_at DATE DEFAULT SYSDATE             -- 신청 일시
);

-- 시퀀스 삭제 및 생성
DROP SEQUENCE cancel_seq;

CREATE SEQUENCE cancel_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

-- 테스트용 데이터 삽입
INSERT INTO cancel (cancel_id, orderno, memberno, mname, type, reason)
VALUES (cancel_seq.NEXTVAL, 2024001, 101, '홍길동', '취소', '상품을 잘못 주문했습니다.');

INSERT INTO cancel (cancel_id, orderno, memberno, mname, type, reason)
VALUES (cancel_seq.NEXTVAL, 2024002, 101, '홍길동', '반품', '상품이 파손되어 도착했습니다.');

COMMIT;

-- 읽지 않은 요청 개수 조회 (관리자용)
SELECT COUNT(*)
FROM cancel
WHERE is_read = 'N';

-- 특정 회원의 신청 목록 조회
SELECT *
FROM cancel
WHERE memberno = #{memberno}
ORDER BY created_at DESC;

-- 단일 신청 읽음 처리
UPDATE cancel
SET is_read = 'Y'
WHERE cancel_id = #{cancel_id};

-- 전체 신청 목록 조회 (관리자용)
SELECT *
FROM cancel
ORDER BY created_at DESC;

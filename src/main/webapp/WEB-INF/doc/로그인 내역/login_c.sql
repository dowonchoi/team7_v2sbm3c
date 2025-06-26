/**********************************/
/* Table Name: 로그인 내역 */
/**********************************/
CREATE TABLE login(
  loginno                        NUMBER(10) NOT NULL PRIMARY KEY,
  ip                               VARCHAR2(15) NOT NULL,
  id                               VARCHAR(30) NOT NULL,
  sw                              VARCHAR(1) DEFAULT 'N' NOT NULL,
  logindate                     DATE NOT NULL
);

COMMENT ON TABLE login is '로그인 내역';
COMMENT ON COLUMN login.loginno is '로그인 번호';
COMMENT ON COLUMN login.ip is '접속 IP';
COMMENT ON COLUMN login.id is '회원 아이디';
COMMENT ON COLUMN login.sw is '로그인 성공 여부(Y/N)';
COMMENT ON COLUMN login.logindate is '로그인 날짜';


DROP SEQUENCE LOGIN_SEQ;

CREATE SEQUENCE LOGIN_SEQ
  START WITH 1              -- 시작 번호
  INCREMENT BY 1            -- 증가값
  MAXVALUE 9999999999  -- 최대값: 9999999999 --> NUMBER(10) 대응
  CACHE 2                   -- 2번은 메모리에서만 계산
  NOCYCLE;                  -- 다시 1부터 생성되는 것을 방지

TRUNCATE TABLE login;
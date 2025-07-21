CREATE TABLE search_log (
    searchno     NUMBER PRIMARY KEY,
    keyword      VARCHAR2(200) NOT NULL,
    memberno     NUMBER,
    search_date  DATE DEFAULT SYSDATE
);

CREATE SEQUENCE search_log_seq START WITH 1 INCREMENT BY 1;

commit;

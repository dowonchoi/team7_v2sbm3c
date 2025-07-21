CREATE TABLE recipe (
    recipeno NUMBER PRIMARY KEY,
    memberno NUMBER NOT NULL,
    prompt VARCHAR2(500) NOT NULL,
    content CLOB NOT NULL,
    rdate DATE DEFAULT SYSDATE,
    FOREIGN KEY (memberno) REFERENCES member(memberno)
);

CREATE SEQUENCE recipe_seq START WITH 1 INCREMENT BY 1;

commit;

DROP TABLE recipe;

ALTER TABLE recipe
ADD foodBinary VARCHAR2(200);

ALTER TABLE recipe DROP COLUMN prompt;

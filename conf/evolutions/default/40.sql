# --- !Ups

CREATE TABLE PUBLISHER_DEPARTMENT (
  PUBLISHER_ID    NVARCHAR2(100) NOT NULL,
  DEPARTMENT_CODE CHAR(2)        NOT NULL
);

CREATE INDEX PUB_DEPT_PUB_IDX ON PUBLISHER_DEPARTMENT (PUBLISHER_ID);

# --- !Downs

DROP INDEX PUB_DEPT_PUB_IDX;

DROP TABLE PUBLISHER_DEPARTMENT;
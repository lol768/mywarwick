# --- !Ups

CREATE TABLE PUBLISHER (
  ID   NVARCHAR2(100) NOT NULL,
  NAME NVARCHAR2(255) NOT NULL,
  CONSTRAINT PUBLISHER_PK PRIMARY KEY (ID)
);

CREATE TABLE PUBLISHER_PERMISSION (
  PUBLISHER_ID NVARCHAR2(100) NOT NULL,
  USERCODE     NVARCHAR2(100) NOT NULL,
  ROLE         NVARCHAR2(100) NOT NULL,
  CONSTRAINT PUB_PERM_PUB_FK FOREIGN KEY (PUBLISHER_ID) REFERENCES PUBLISHER (ID)
);

CREATE INDEX PUB_PERM_PUB_IDX ON PUBLISHER_PERMISSION (PUBLISHER_ID);

INSERT INTO PUBLISHER (ID, NAME) VALUES ('315d3e23-614e-4d5a-b27f-703cdb834c9b', 'Default Publisher');

ALTER TABLE NEWS_ITEM ADD PUBLISHER_ID NVARCHAR2(100) NULL;
ALTER TABLE NEWS_ITEM ADD UPDATED_BY NVARCHAR2(100) NULL;
ALTER TABLE NEWS_ITEM ADD CREATED_BY NVARCHAR2(100) NULL;

UPDATE NEWS_ITEM SET PUBLISHER_ID = '315d3e23-614e-4d5a-b27f-703cdb834c9b';
UPDATE NEWS_ITEM SET CREATED_BY = 'nobody';

ALTER TABLE NEWS_ITEM MODIFY PUBLISHER_ID NOT NULL;
ALTER TABLE NEWS_ITEM MODIFY CREATED_BY NOT NULL;

# --- !Downs

ALTER TABLE NEWS_ITEM DROP COLUMN CREATED_BY;
ALTER TABLE NEWS_ITEM DROP COLUMN UPDATED_BY;
ALTER TABLE NEWS_ITEM DROP COLUMN PUBLISHER_ID;

DROP INDEX PUB_PERM_PUB_IDX;

DROP TABLE PUBLISHER_PERMISSION;
DROP TABLE PUBLISHER;

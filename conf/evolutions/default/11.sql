# --- !Ups
ALTER TABLE TILE ADD "ICON" NVARCHAR2(100);
ALTER TABLE TILE ADD "TITLE" NVARCHAR2(100) DEFAULT '' NOT NULL;

# --- !Downs
ALTER TABLE TILE DROP COLUMN ICON;
ALTER TABLE TILE DROP COLUMN TITLE;


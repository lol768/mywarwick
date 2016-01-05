# --- !Ups
ALTER TABLE TILE ADD "ICON" NVARCHAR2(100);
ALTER TABLE TILE ADD "TITLE" NVARCHAR2(100);

UPDATE TILE SET title = 'Untitled' WHERE title IS NULL;
ALTER TABLE TILE ALTER TITLE SET NOT NULL;

# --- !Downs
ALTER TABLE TILE DROP COLUMN ICON;
ALTER TABLE TILE DROP COLUMN TITLE;
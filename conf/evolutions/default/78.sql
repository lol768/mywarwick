# --- !Ups
ALTER TABLE TILE ADD TIMEOUT INT DEFAULT 5000 NOT NULL;

# --- !Downs
ALTER TABLE TILE DROP COLUMN TIMEOUT;
# --- !Ups
ALTER TABLE USER_PREFERENCE ADD COLOUR_SCHEME_HIGH_CONTRAST NUMBER(1, 0) DEFAULT 0 NOT NULL;

# --- !Downs
ALTER TABLE USER_PREFERENCE DROP COLUMN COLOUR_SCHEME_HIGH_CONTRAST;

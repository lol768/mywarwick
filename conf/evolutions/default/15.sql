# --- !Ups
CREATE UNIQUE INDEX PUSH_REG_TOKEN_INDEX ON PUSH_REGISTRATION ( USERCODE, TOKEN );

# --- !Downs
DROP INDEX PUSH_REG_TOKEN_INDEX;

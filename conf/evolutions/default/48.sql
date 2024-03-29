# --- !Ups

ALTER SESSION SET DDL_LOCK_TIMEOUT = 120;
DROP INDEX ACTIVITY_PUBLISHED_AT_INDEX;
CREATE INDEX ACTIVITY_PUBLISHER_TIME_INDEX ON ACTIVITY (PUBLISHER_ID, PUBLISHED_AT DESC);

# --- !Downs

ALTER SESSION SET DDL_LOCK_TIMEOUT = 120;
DROP INDEX ACTIVITY_PUBLISHER_TIME_INDEX;
CREATE INDEX ACTIVITY_PUBLISHED_AT_INDEX ON ACTIVITY (PUBLISHED_AT DESC);

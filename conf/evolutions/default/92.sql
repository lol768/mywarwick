# --- !Ups

ALTER SESSION SET DDL_LOCK_TIMEOUT = 120; -- per original evolution
DROP INDEX ACTIVITY_PUBLISHER_TIME_INDEX;
CREATE INDEX ACTIVITY_PUBLISHER_TIME_INDEX ON ACTIVITY (PUBLISHER_ID, PUBLISHED_AT);

# --- !Downs

ALTER SESSION SET DDL_LOCK_TIMEOUT = 120; -- per original evolution
DROP INDEX ACTIVITY_PUBLISHER_TIME_INDEX;
CREATE INDEX ACTIVITY_PUBLISHER_TIME_INDEX ON ACTIVITY (PUBLISHER_ID, PUBLISHED_AT DESC);
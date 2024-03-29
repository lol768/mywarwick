# --- !Ups
DROP INDEX MESSAGE_SEND_STATE;
ALTER TABLE MESSAGE_SEND ADD SEND_AT TIMESTAMP(6);
CREATE INDEX MESSAGE_SEND_STATE_SEND_AT_IDX ON MESSAGE_SEND(STATE, SEND_AT);

# --- !Downs
DROP INDEX MESSAGE_SEND_STATE_SEND_AT_IDX;
ALTER TABLE MESSAGE_SEND DROP COLUMN SEND_AT;
CREATE INDEX MESSAGE_SEND_STATE ON MESSAGE_SEND(STATE);

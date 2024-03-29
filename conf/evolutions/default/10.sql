
# --- !Ups
CREATE TABLE MESSAGE_SEND (
  "ID" NVARCHAR2(100) NOT NULL,
  "ACTIVITY_ID" NVARCHAR2(100) NOT NULL,
  "USERCODE" NVARCHAR2(100) NOT NULL,
  "OUTPUT" NVARCHAR2(10) NOT NULL,
  "STATE" NCHAR(1) DEFAULT 'A' NOT NULL,
  "UPDATED_AT" TIMESTAMP NOT NULL,
  CONSTRAINT "MESSAGE_SEND_PK" PRIMARY KEY("ID")
);

-- output:
-- email
-- sms
-- mobile

-- states:
-- A : available
-- T : taken
-- S : successful
-- F : failed

CREATE INDEX MESSAGE_SEND_STATE ON MESSAGE_SEND(STATE);

# --- !Downs

DROP TABLE MESSAGE_SEND;
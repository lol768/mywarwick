# --- !Ups
CREATE TABLE ACTIVITY_RECIPIENT_READ (
  "USERCODE" nvarchar2 (100) NOT NULL,
  "NOTIFICATIONS_LAST_READ" TIMESTAMP NOT NULL,
  "ACTIVITIES_LAST_READ" TIMESTAMP,
  CONSTRAINT "ACTIVITY_RECIPIENT_READ_PK" PRIMARY KEY ("USERCODE")
);

-- can't alter the column type of ACTIVITY_RECIPIENT.USERCODE without dropping all the indices first - lame!
ALTER TABLE ACTIVITY_RECIPIENT DROP CONSTRAINT ACTIVITY_RECIPIENT_PK;
DROP INDEX ACTIVITY_USERCODE_DATE_INDEX;
ALTER TABLE ACTIVITY_RECIPIENT MODIFY USERCODE nvarchar2 (100);
ALTER TABLE ACTIVITY_RECIPIENT ADD CONSTRAINT "ACTIVITY_RECIPIENT_PK" PRIMARY KEY ("ACTIVITY_ID", "USERCODE");
CREATE INDEX ACTIVITY_USERCODE_DATE_INDEX ON ACTIVITY_RECIPIENT (USERCODE ASC, GENERATED_AT DESC);

# --- !Downs

DROP TABLE ACTIVITY_RECIPIENT_READ;
ALTER TABLE ACTIVITY_RECIPIENT DROP CONSTRAINT ACTIVITY_RECIPIENT_PK;
DROP INDEX ACTIVITY_USERCODE_DATE_INDEX;
ALTER TABLE ACTIVITY_RECIPIENT MODIFY USERCODE nvarchar2 (10);
ALTER TABLE ACTIVITY_RECIPIENT ADD CONSTRAINT "ACTIVITY_RECIPIENT_PK" PRIMARY KEY ("ACTIVITY_ID", "USERCODE");
CREATE INDEX ACTIVITY_USERCODE_DATE_INDEX ON ACTIVITY_RECIPIENT (USERCODE ASC, GENERATED_AT DESC);
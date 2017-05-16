# --- !Ups

CREATE TABLE ACTIVITY_MUTE (
  "ID"            NVARCHAR2(100) NOT NULL,
  "USERCODE"      NVARCHAR2(100) NOT NULL,
  "CREATED_AT"    TIMESTAMP(6) NOT NULL,
  "EXPIRES_AT"    TIMESTAMP(6) NULL,
  "ACTIVITY_TYPE" NVARCHAR2 (100) NULL,
  "PROVIDER_ID"   NVARCHAR2(100) NULL,
  "TAGS"          CLOB,
  CONSTRAINT "ACTIVITY_MUTE_PK" PRIMARY KEY ("ID")
);

# --- !Downs

DROP TABLE ACTIVITY_MUTE;



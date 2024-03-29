# --- !Ups
CREATE TABLE ACTIVITY_TYPE (
  NAME         NVARCHAR2(100) NOT NULL PRIMARY KEY,
  DISPLAY_NAME NVARCHAR2(255) NOT NULL
);

CREATE TABLE ACTIVITY_TAG_TYPE (
  NAME                   NVARCHAR2(255) NOT NULL PRIMARY KEY,
  DISPLAY_NAME           NVARCHAR2(255) NOT NULL,
  VALUE_VALIDATION_REGEX NVARCHAR2(255)
);

# --- !Downs
DROP TABLE ACTIVITY_TYPE;
DROP TABLE ACTIVITY_TAG_TYPE;


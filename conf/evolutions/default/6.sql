# --- !Ups
CREATE TABLE TILE (
  "ID"           NVARCHAR2(100) NOT NULL,
  "DEFAULT_SIZE" NVARCHAR2(100) NOT NULL,
  "FETCH_URL"    NVARCHAR2(100) NOT NULL,
  CONSTRAINT "TILE_PK" PRIMARY KEY ("ID")
);

CREATE TABLE USER_TILE (
  "USERCODE"      NVARCHAR2(100),
  "TILE_ID"       NVARCHAR2(100),
  "TILE_POSITION" INT          NOT NULL,
  "TILE_SIZE"     NVARCHAR2(100),
  "CREATED_AT"    TIMESTAMP(6) NOT NULL,
  "UPDATED_AT"    TIMESTAMP(6) NOT NULL,
  CONSTRAINT "USER_TILE_PREF_FK" FOREIGN KEY ("TILE_ID") REFERENCES TILE ("ID"),
  CONSTRAINT "USER_TILE_PREF_PK" PRIMARY KEY ("USERCODE", "TILE_ID")
);

# --- !Downs
DROP TABLE USER_TILE;
DROP TABLE TILE;

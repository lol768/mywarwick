# --- !Ups
CREATE TABLE TILE (
  "ID"                NVARCHAR2(100),
  "TYPE"              NVARCHAR2(100) NOT NULL,
  "DEFAULT_SIZE"      NVARCHAR2(100) NOT NULL,
  "DEFAULT_FETCH_URL" NVARCHAR2(100) NOT NULL,
  CONSTRAINT "TILE_PK" PRIMARY KEY ("ID")
);

CREATE TABLE USER_TILE_PREF (
  "USERCODE"      NVARCHAR2(100),
  "TILE_ID"       NVARCHAR2(100),
  "TILE_POSITION" INT          NOT NULL,
  "TILE_SIZE"     NVARCHAR2(100),
  "FETCH_URL"     NVARCHAR2(100),
  "CREATED_AT"    TIMESTAMP(6) NOT NULL,
  "LAST_CHANGED"  TIMESTAMP(6) NOT NULL,
  CONSTRAINT "USER_TILE_PREF_FK" FOREIGN KEY ("TILE_ID") REFERENCES TILE ("ID"),
  CONSTRAINT "USER_TILE_PREF_PK" PRIMARY KEY ("USERCODE", "TILE_ID")
);

INSERT INTO TILE VALUES ('mail', 'list', 'large', 'https://localhost/api/tile/ids?tileIds=mail');
INSERT INTO TILE VALUES ('tabula', 'bignumber', 'small', 'https://localhost/api/tile/ids?tileIds=tabula');
INSERT INTO TILE VALUES ('live-departures', 'list', 'small', 'https://localhost/api/tile/ids?tileIds=live-departures');
INSERT INTO TILE VALUES ('modules', 'bignumber', 'small', 'https://localhost/api/tile/ids?tileIds=modules');

INSERT INTO USER_TILE_PREF
VALUES ('cusjau', 'mail', '1', 'large', 'https://localhost/api/tile/ids?tileIds=mail', SYSDATE, SYSDATE);
INSERT INTO USER_TILE_PREF
VALUES ('cusjau', 'tabula', '2', 'small', 'https://localhost/api/tile/ids?tileIds=tabula', SYSDATE, SYSDATE);
INSERT INTO USER_TILE_PREF
VALUES ('cusjau', 'modules', '3', 'small', 'https://localhost/api/tile/ids?tileIds=modules', SYSDATE, SYSDATE);
INSERT INTO USER_TILE_PREF VALUES
  ('cusjau', 'live-departures', '4', 'small', 'https://localhost/api/tile/ids?tileIds=live-departures', SYSDATE,
   SYSDATE);

# --- !Downs
DROP TABLE USER_TILE_PREF;
DROP TABLE TILE;

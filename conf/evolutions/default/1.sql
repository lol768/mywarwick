# --- !Ups
CREATE TABLE objectcache (
    "KEY" nvarchar2 (100) NOT NULL,
    "OBJECTDATA" BLOB,
    "CREATEDDATE" TIMESTAMP (6),
    CONSTRAINT "OBJECTCACHE_PK" PRIMARY KEY ("KEY")
);


# --- !Downs
DROP TABLE objectcache;
# --- !Ups
CREATE TABLE DO_NOT_DISTURB(
  USERCODE NVARCHAR2(100) NOT NULL,
  START_TIME NVARCHAR2(5),
  END_TIME NVARCHAR2(5),
  CONSTRAINT DO_NOT_DISTURB_USERCODE_FK FOREIGN KEY (USERCODE) REFERENCES USER_PREFERENCE(USERCODE),
  CONSTRAINT DO_NOT_DISTURB_PK PRIMARY KEY (USERCODE)
)

# --- !Downs
DROP TABLE DO_NOT_DISTURB;


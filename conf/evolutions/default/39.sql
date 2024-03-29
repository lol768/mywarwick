# --- !Ups

CREATE TABLE PUBLISHED_NOTIFICATION (
  ACTIVITY_ID NVARCHAR2(100) NOT NULL,
  PUBLISHER_ID NVARCHAR2(100) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  CREATED_BY NVARCHAR2(100) NOT NULL,
  CONSTRAINT PUB_NOTIF_ACTIVITY_FK FOREIGN KEY (ACTIVITY_ID) REFERENCES ACTIVITY (ID),
  CONSTRAINT PUB_NOTIF_PUBLISHER_FK FOREIGN KEY (PUBLISHER_ID) REFERENCES PUBLISHER (ID)
);

CREATE INDEX PUB_NOTIF_PUB_IDX ON PUBLISHED_NOTIFICATION (PUBLISHER_ID);

# --- !Downs

DROP INDEX PUB_NOTIF_PUB_IDX;

DROP TABLE PUBLISHED_NOTIFICATION;

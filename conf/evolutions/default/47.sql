# --- !Ups

ALTER TABLE TILE MODIFY FETCH_URL NULL;

INSERT INTO TILE (ID, FETCH_URL, TILE_TYPE, ICON, TITLE, COLOUR) VALUES ('news', NULL, 'news', 'newspaper-o', 'News', 0);
INSERT INTO TILE (ID, FETCH_URL, TILE_TYPE, ICON, TITLE, COLOUR) VALUES ('activity', NULL, 'activity', 'dashboard', 'Activity', 1);
INSERT INTO TILE (ID, FETCH_URL, TILE_TYPE, ICON, TITLE, COLOUR) VALUES ('notifications', NULL, 'notifications', 'inbox', 'Notifications', 2);

INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('news', 'staff');
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('news', 'student');
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('news', 'anonymous');

INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('activity', 'staff');
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('activity', 'student');

INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('notifications', 'staff');
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('notifications', 'student');

# --- !Downs

DELETE FROM TILE_GROUP WHERE TILE_ID IN ('news', 'activity', 'notifications');
DELETE FROM TILE_GROUP_LAYOUT WHERE TILE_ID IN ('news', 'activity', 'notifications');
DELETE FROM USER_TILE WHERE TILE_ID IN ('news', 'activity', 'notifications');
DELETE FROM USER_TILE_LAYOUT WHERE TILE_ID IN ('news', 'activity', 'notifications');
DELETE FROM TILE WHERE ID IN ('news', 'activity', 'notifications');

ALTER TABLE TILE MODIFY FETCH_URL NOT NULL;


# --- !Ups
INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES ('account', 'wbs');
INSERT INTO TILE_GROUP_LAYOUT (GROUP_ID, TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT) VALUES ('wbs', 'account', 2, 0, 0, 2, 2);
INSERT INTO TILE_GROUP_LAYOUT (GROUP_ID, TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT) VALUES ('wbs', 'account', 5, 0, 0, 2, 2);

# --- !Downs
DELETE FROM TILE_GROUP_LAYOUT WHERE GROUP_ID = 'wbs';
DELETE FROM TILE_GROUP WHERE "GROUP" = 'wbs';

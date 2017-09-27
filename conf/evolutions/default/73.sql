# --- !Ups
UPDATE TILE SET TILE_TYPE = 'mail' WHERE ID = 'mail';

# --- !Downs
UPDATE TILE SET TILE_TYPE = 'list' WHERE ID = 'mail';


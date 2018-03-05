# --- !Ups
UPDATE TILE SET TILE_TYPE = 'calendar' WHERE ID = 'calendar';

# --- !Downs
UPDATE TILE SET TILE_TYPE = 'agenda' WHERE ID = 'calendar';


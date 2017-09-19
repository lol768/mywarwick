# --- !Ups
UPDATE TILE SET TITLE = 'Campus', ICON = 'sun-o' WHERE ID = 'weather';

# --- !Downs
UPDATE TILE SET TITLE = 'Weather', ICON = 'fw' WHERE ID = 'weather';


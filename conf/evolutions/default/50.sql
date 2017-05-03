# --- !Ups
UPDATE TILE SET ICON = 'file-text-o' WHERE ID = 'coursework';

# --- !Downs
UPDATE TILE SET ICON = 'cog' WHERE ID = 'coursework';

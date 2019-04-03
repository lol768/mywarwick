# --- !Ups

UPDATE TILE SET ICON = 'utensils' WHERE ICON = 'cutlery';
UPDATE TILE SET ICON = 'chart-pie' WHERE ICON = 'pie-chart';
UPDATE TILE SET ICON = 'file-alt' WHERE ICON = 'file-text-o';

# --- !Downs

UPDATE TILE SET ICON = 'cutlery' WHERE ICON = 'utensils';
UPDATE TILE SET ICON = 'pie-chart' WHERE ICON = 'chart-pie';
UPDATE TILE SET ICON = 'file-text-o' WHERE ICON = 'file-alt';

# --- !Ups

UPDATE PROVIDER SET ICON = 'utensils' WHERE ICON = 'cutlery';
UPDATE PROVIDER SET ICON = 'chart-pie' WHERE ICON = 'pie-chart';
UPDATE PROVIDER SET ICON = 'file-alt' WHERE ICON = 'file-text-o';

# --- !Downs

UPDATE PROVIDER SET ICON = 'cutlery' WHERE ICON = 'utensils';
UPDATE PROVIDER SET ICON = 'pie-chart' WHERE ICON = 'chart-pie';
UPDATE PROVIDER SET ICON = 'file-text-o' WHERE ICON = 'file-alt';

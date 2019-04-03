# --- !Ups
UPDATE TILE SET ICON = 'calendar-alt' WHERE ICON = 'calendar';
UPDATE TILE SET ICON = 'calendar' WHERE ICON = 'calendar-o';
UPDATE TILE SET ICON = 'bus-alt' WHERE ICON = 'bus';
UPDATE TILE SET ICON = 'file-alt' WHERE ICON = 'file-text-o';
UPDATE TILE SET ICON = 'utensils' WHERE ICON = 'cutlery';
UPDATE TILE SET ICON = 'sun' WHERE ICON = 'sun-o';
UPDATE TILE SET ICON = 'envelope' WHERE ICON = 'envelope-o';
UPDATE TILE SET ICON = 'user' WHERE ICON = 'user-o';
UPDATE TILE SET ICON = 'check-square' WHERE ICON = 'check-square-o';
UPDATE TILE SET ICON = 'handshake' WHERE ICON = 'handshake-o';

# --- !Downs
UPDATE TILE SET ICON = 'calendar-o' WHERE ICON = 'calendar';
UPDATE TILE SET ICON = 'calendar' WHERE ICON = 'calendar-alt';
UPDATE TILE SET ICON = 'bus' WHERE ICON = 'bus-alt';
UPDATE TILE SET ICON = 'file-text-o' WHERE ICON = 'file-alt';
UPDATE TILE SET ICON = 'cutlery' WHERE ICON = 'utensils';
UPDATE TILE SET ICON = 'sun-o' WHERE ICON = 'sun';
UPDATE TILE SET ICON = 'envelope-o' WHERE ICON = 'envelope';
UPDATE TILE SET ICON = 'user-o' WHERE ICON = 'user';
UPDATE TILE SET ICON = 'check-square-o' WHERE ICON = 'check-square';
UPDATE TILE SET ICON = 'handshake-o' WHERE ICON = 'handshake';

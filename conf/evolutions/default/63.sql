# --- !Ups
delete from user_tile where tile_id in ('activity', 'news', 'notifications');
delete from user_tile_layout where tile_id in ('activity', 'news', 'notifications');

delete from tile_group_layout where tile_id in ('activity', 'news', 'notifications');
delete from tile_group where tile_id in ('activity', 'news', 'notifications');
delete from tile where id in ('activity', 'news', 'notifications');

# --- !Downs

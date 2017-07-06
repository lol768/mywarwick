# --- !Ups
delete from tile where id in ('activity', 'news', 'notifications');

# --- !Downs

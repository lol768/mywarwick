# --- !Ups
UPDATE ACTIVITY SET TYPE = 'mywarwick-user-publish-notification' WHERE TYPE = 'news';

# --- !Downs
UPDATE ACTIVITY SET TYPE = 'news' WHERE TYPE = 'mywarwick-user-publish-notification';
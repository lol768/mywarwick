# --- !Ups

ALTER TABLE NEWS_ITEM_CATEGORY DROP CONSTRAINT NEWS_ITEM_PUBLISH_CAT_FK;

ALTER TABLE PUBLISH_CATEGORY RENAME TO NEWS_CATEGORY;
ALTER TABLE NEWS_ITEM_CATEGORY RENAME COLUMN PUBLISH_CATEGORY_ID TO NEWS_CATEGORY_ID;

ALTER TABLE NEWS_ITEM_CATEGORY
  ADD CONSTRAINT NEWS_ITEM_NEWS_CAT_FK FOREIGN KEY (NEWS_CATEGORY_ID) REFERENCES NEWS_CATEGORY (ID);

# --- !Downs

ALTER TABLE NEWS_ITEM_CATEGORY DROP CONSTRAINT NEWS_ITEM_NEWS_CAT_FK;

ALTER TABLE NEWS_ITEM_CATEGORY RENAME COLUMN NEWS_CATEGORY_ID TO PUBLISH_CATEGORY_ID;
ALTER TABLE NEWS_CATEGORY RENAME TO PUBLISH_CATEGORY;

ALTER TABLE NEWS_ITEM_CATEGORY
  ADD CONSTRAINT NEWS_ITEM_PUBLISH_CAT_FK FOREIGN KEY (PUBLISH_CATEGORY_ID) REFERENCES PUBLISH_CATEGORY (ID);



# --- !Ups
alter table USER_PREFERENCE add SMS_VERIFICATION NVARCHAR2(6) null;

# --- !Downs
alter table USER_PREFERENCE drop column SMS_VERIFICATION;


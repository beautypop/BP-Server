alter table GcmToken change column regId token VARCHAR(255);
alter table GcmToken rename PushNotificationToken;

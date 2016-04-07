alter table GcmToken change column regId token VARCHAR(255);
alter table GcmToken rename PushNotificationToken;

-- after server starts up
update PushNotificationToken set appVersion='';
update PushNotificationToken set deviceType='ANDROID';
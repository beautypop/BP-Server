alter table GcmToken change column regId token VARCHAR(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
alter table GcmToken rename PushNotificationToken;
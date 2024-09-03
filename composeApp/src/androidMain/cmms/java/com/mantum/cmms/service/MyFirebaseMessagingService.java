package com.mantum.cmms.service;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mantum.component.service.Notification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() == null) {
            return;
        }

        if (remoteMessage.getNotification().getTitle() != null
                && remoteMessage.getNotification().getBody() != null) {

            Notification notification = new Notification(this);
            notification.show(new Notification.Model(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            ));
        }
    }
}
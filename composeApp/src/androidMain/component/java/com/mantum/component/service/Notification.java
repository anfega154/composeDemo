package com.mantum.component.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mantum.demo.R;

import java.util.Random;

public class Notification {

    public final static String CHANNEL_ID = "com.mantum";

    private final Context context;

    private final int id;

    private final String channelId;

    private final NotificationManager notificationManager;

    public Notification(@NonNull Context context) {
        this(context, CHANNEL_ID);
    }

    public Notification(@NonNull Context context, @NonNull String channelId) {
        this(context, channelId, new Random().nextInt(99999) + 1);
    }

    public Notification(@NonNull Context context, @NonNull String channelId, @NonNull Integer id) {
        this.id = id;
        this.context = context;
        this.channelId = channelId;
        this.notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getChannelId() {
        return channelId;
    }

    public void show(@NonNull Notification.Model model) {
        if (notificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setContentTitle(model.getTitle())
                    .setContentText(model.getMessage())
                    .setAutoCancel(model.isAutoCancel())
                    .setSubText(model.getSubMessage())
                    .setSmallIcon(model.getIcon())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(model.getMessage()))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            if (model.getPendingIntent() != null) {
                builder.setContentIntent(model.getPendingIntent());
            }

            if (model.getAction() != null) {
                PendingIntent broadcast = PendingIntent.getBroadcast(
                        context, 1, model.getAction().getIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setOngoing(false);
                builder.addAction(
                        model.getAction().getIcon(),
                        model.getAction().getTitle(),
                        broadcast);
            }

            notificationManager.notify(id, builder.build());
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                channelId, "Mantum push local channel", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel.getId())
                .setContentTitle(model.getTitle())
                .setContentText(model.getMessage())
                .setAutoCancel(model.isAutoCancel())
                .setSubText(model.getSubMessage())
                .setSmallIcon(model.getIcon())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(model.getMessage()))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (model.getPendingIntent() != null) {
            builder.setContentIntent(model.getPendingIntent());
        }

        if (model.getAction() != null) {
            PendingIntent broadcast;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                broadcast = PendingIntent.getBroadcast(
                        context, 1, model.getAction().getIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            } else {
                broadcast = PendingIntent.getBroadcast(
                        context, 1, model.getAction().getIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            builder.setOngoing(false);
            builder.addAction(
                    model.getAction().getIcon(),
                    model.getAction().getTitle(),
                    broadcast);
        }
        notificationManager.notify(id, builder.build());
    }

    public static void cancel(@NonNull Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static void cancelAll(@NonNull Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static class Model {

        private final String title;

        private final String message;

        private int icon;

        private String subMessage;

        private boolean autoCancel;

        private Action action;

        private PendingIntent pendingIntent;

        public Model(String title, String message) {
            this.title = title;
            this.message = message;
            this.icon = R.mipmap.ic_notification;
            this.autoCancel = true;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getSubMessage() {
            return subMessage;
        }

        public void setSubMessage(String subMessage) {
            this.subMessage = subMessage;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(@DrawableRes int icon) {
            this.icon = icon;
        }

        public boolean isAutoCancel() {
            return autoCancel;
        }

        public void setAutoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
        }

        @Nullable
        public Action getAction() {
            return action;
        }

        public void setAction(@Nullable Action action) {
            this.action = action;
        }

        public PendingIntent getPendingIntent() {
            return pendingIntent;
        }

        public void setPendingIntent(PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
        }
    }

    public static class Action {

        private final int icon;

        private final String title;

        private final Intent intent;

        public Action(@DrawableRes int icon, String title, Intent intent) {
            this.icon = icon;
            this.title = title;
            this.intent = intent;
        }

        public int getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public Intent getIntent() {
            return intent;
        }
    }
}
package com.locart;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import java.util.Objects;


public class Locart extends Application {

    public static final String CHANNEL_NEW_CONNECTION = "New Connection";
    public static final String CHANNEL_NEW_CHAT = "New Chat";
    public static final String CHANNEL_NEW_GROUP_CHAT = "New Group Chat";
    public static final String CHANNEL_ALL_Bank = "All Bank";

    public static NotificationManagerCompat notificationManagerCompat;
    public static boolean appRunning = false;


    @Override
    public void onCreate() {
        super.onCreate();

        appRunning = true;
        createNotificationChannels();

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel newConnection = new NotificationChannel(
                    CHANNEL_NEW_CONNECTION,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            newConnection.setDescription("This is Channel 1");

            NotificationChannel newChat = new NotificationChannel(
                    CHANNEL_NEW_CHAT,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            newChat.setDescription("This is Channel 2");

            NotificationChannel newGroupChat = new NotificationChannel(
                    CHANNEL_NEW_GROUP_CHAT,
                    "Channel 3",
                    NotificationManager.IMPORTANCE_HIGH
            );
            newGroupChat.setDescription("This is Channel 3");

            NotificationChannel allBank = new NotificationChannel(
                    CHANNEL_ALL_Bank,
                    "Channel 4",
                    NotificationManager.IMPORTANCE_HIGH
            );
            allBank.setDescription("This is Channel 4");

            NotificationManager manager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(manager).createNotificationChannel(newConnection);
            manager.createNotificationChannel(newChat);
            manager.createNotificationChannel(newGroupChat);
            manager.createNotificationChannel(allBank);
        }
    }

}

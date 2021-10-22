package com.locart.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import com.locart.Locart;
import com.locart.Chats.ChatUserClass;
import com.locart.Fragments.BroadcastFragment;
import com.locart.Fragments.CardFragment;
import com.locart.Fragments.FeedFragment;
import com.locart.Fragments.LocationFragment;
import com.locart.Fragments.MatchesFragment;
import com.locart.Fragments.NotificationsFragment;
import com.locart.Fragments.PostFragment;
import com.locart.Fragments.UserFragment;
import com.locart.Message.MessageActivity;
import com.locart.Utils.Constants;
import com.locart.Utils.CustomViewPager;
import com.locart.Utils.DataProccessor;
import com.locart.Utils.GpsTracker;
import com.locart.model.AlertMatch;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.locart.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import pub.devrel.easypermissions.EasyPermissions;

import static com.locart.Locart.CHANNEL_NEW_CHAT;
import static com.locart.Locart.CHANNEL_NEW_CONNECTION;

/**
 * This Activity controls the display of main fragments of the app:
 * -UserFragment
 * -CardFragment
 * -ChatFragment
 * <p>
 * It is also responsible for initializing the one signal API for the current user
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        CardFragment.Callbacks
        , UserFragment.CallbackUser
        , MatchesFragment.CallbackMatch {


    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    DataProccessor dataProccessor;
    private static final String TAG = "MainActivity";
    MainAdapter adapter;
    public static NotificationManagerCompat notificationManagerCompat;

    private TabLayout tabLayout;
    private CustomViewPager viewPager;
    private GpsTracker gpsTracker;
    /* location permission code */
    private static final int RC_LOCATION_PERM = 101;
    String latitude, longitude, string_city, string_state, string_country, string_location, userId;



    /**
     * A flag that marks whether current Activity has saved its instance state
     */
    private boolean mHasSaveInstanceState;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        dataProccessor = new DataProccessor(this);

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        /* disable swipe in tab */
        viewPager.setOnTouchListener((v, event) -> true);

        View viewTab0 = getLayoutInflater().inflate(R.layout.main_tab_icon, null);
        ImageView tabIcon0 = viewTab0.findViewById(R.id.tab_icon);
        Picasso.get().load(R.drawable.grey_user).into(tabIcon0);
        tabLayout.addTab(tabLayout.newTab().setCustomView(viewTab0));

        View viewTab1 = getLayoutInflater().inflate(R.layout.main_tab_icon, null);
        ImageView tabIcon1 = viewTab1.findViewById(R.id.tab_icon);
        Picasso.get().load(R.drawable.transparent).into(tabIcon1);
        tabLayout.addTab(tabLayout.newTab().setCustomView(viewTab1));

        View viewTab2 = getLayoutInflater().inflate(R.layout.main_tab_icon, null);
        ImageView tabIcon2 = viewTab2.findViewById(R.id.tab_icon);
        Picasso.get().load(R.drawable.grey_nexus).into(tabIcon2);
        tabLayout.addTab(tabLayout.newTab().setCustomView(viewTab2));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new MainAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());


                if (tab.getPosition() == 0) {
                    String user_image = dataProccessor.getStr(Constants.IMAGE);
                    /* get image string and display on tab icon for zero */
                    Glide.with(getApplicationContext())
                            .load(user_image)
                            .placeholder(R.drawable.grey_user)
                            .into(tabIcon0);

                }
                if (tab.getPosition() == 1) {
                    Picasso.get().load(R.drawable.transparent).placeholder(R.drawable.transparent).into(tabIcon1);
                    /* show real icon color for middle icon*/
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                    Intent i = new Intent("REFRESH_CARD_FRAGMENT");
                    lbm.sendBroadcast(i);
                }
                if (tab.getPosition() == 2) {
                    Picasso.get().load(R.drawable.nexus).placeholder(R.drawable.grey_nexus).into(tabIcon2);
                    /* refresh matches fragment */
                    LocalBroadcastManager match = LocalBroadcastManager.getInstance(MainActivity.this);
                    Intent i = new Intent("REFRESH_MATCH_FRAGMENT");
                    match.sendBroadcast(i);
                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0) {
                    Picasso.get().load(R.drawable.grey_user).placeholder(R.drawable.grey_user).into(tabIcon0);
                }
                if (tab.getPosition() == 1) {
                    Picasso.get().load(R.drawable.transparent).placeholder(R.drawable.transparent).into(tabIcon1);
                }
                if (tab.getPosition() == 2) {
                    Picasso.get().load(R.drawable.grey_nexus).placeholder(R.drawable.nexus).into(tabIcon2);
                }


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        /* show swipe card tab by default */
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();

        /* get intent and show necessary tab */
        String tabShow;
        tabShow = getIntent().getStringExtra("tab_show");
        if (tabShow != null) {
            switch (tabShow) {
                case "tab_profile":
                    tabLayout.getTabAt(0).select();
                    break;
                case "tab_swipe":
                    tabLayout.getTabAt(1).select();
                    break;
                case "tab_fragment":
                    tabLayout.getTabAt(2).select();
                    break;

            }
        }

    }

    /**
     * Get Permissions needed to get location
     */
    public void locationAccessTask() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            /* Have permissions, do the thing! check if location is enabled */
            if (isLocationEnabled()) {

                getLocation();
            } else {
                /* show alert dialog to turn on location */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Enable Location");
                builder.setMessage("You need to turn on your location to updated");
                /* add a negative button */
                builder.setNegativeButton("DISMISS", (dialog, which) -> {
                    dialog.dismiss();
                });

                /* add a positive button */
                builder.setPositiveButton("Turn On", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                });

                /* create and show the alert dialog */
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            }

        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_storage),
                    RC_LOCATION_PERM, perms);
        }
    }

    /**
     * if gps location is disabled then ask user to enable it with a dialog
     */
    /* method to check if location is enabled */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Get Current User Location if location is enabled,
     * if it is available and the app is able to find a valid location
     * then update the user location's database with the most updated location
     */
    public void getLocation() {
        gpsTracker = new GpsTracker(this);
        if (gpsTracker.canGetLocation()) {
            double lat = gpsTracker.getLatitude();
            double longi = gpsTracker.getLongitude();
            latitude = String.valueOf(lat);
            longitude = String.valueOf(longi);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(lat, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0) {
                    string_location = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    string_city = addresses.get(0).getLocality();
                    string_state = addresses.get(0).getAdminArea();
                    string_country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                    Log.d(TAG, "Your Location2: " + string_location);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Your Location: " + latitude + " " + longitude);
            /* save new location to fire store */
            saveNewLocation();

        }
    }

    private void saveNewLocation() {
        DocumentReference docRef = firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION).document(userId);
        Map<String, Object> users = new HashMap<>();
        users.put(Constants.USER_STATE, string_state);
        users.put(Constants.USER_COUNTRY, string_country);
        users.put(Constants.USER_LOCATION, string_location);
        users.put(Constants.USER_LATITUDE, latitude);
        users.put(Constants.USER_LONGITUDE, longitude);

        /* add user to database */
        docRef.update(users)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "New location details saved"));

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {

            case R.id.navigation_feed:
                fragment = new FeedFragment();
                break;

            case R.id.navigation_broadcast:
                fragment = new BroadcastFragment();
                break;

            case R.id.navigation_post:
                fragment = new PostFragment();
                break;

            case R.id.navigation_location:
                fragment = new LocationFragment();
                break;

            case R.id.navigation_notifications:
                fragment = new NotificationsFragment();
                break;

        }

        return true;
    }

    @Override
    public void onCardFragmentResume() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_layout, new CardFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onUserFragmentResume() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_layout, new UserFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onMatchFragmentResume() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_layout, new MatchesFragment())
                .commitAllowingStateLoss();
    }


    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        Locart.appRunning = true;
        locationAccessTask();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void OnlineUser() {
        String currentUser = firebaseUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .update(arrayOnlineUser);
    }

    private void UserStatus(String status) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String currentUser = firebaseUser.getUid();

            Map<String, Object> arrayUserStatus = new HashMap<>();
            arrayUserStatus.put("user_status", status);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .update(arrayUserStatus);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }

        /* refresh matches fragment */
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
        Intent i = new Intent("REFRESH_MATCH_FRAGMENT");
        lbm.sendBroadcast(i);

//        isTransactionSafe = true;
//        /* Here after the activity is restored we check if there is any transaction pending from
//            the last restoration */
//        if (isTransactionPending) {
//            commitFragment();
//        }
        mHasSaveInstanceState = false;

        startNotificationMatch();
//        StartNotificationChats();


    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatus("paused");
        Locart.appRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserStatus("offline");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mHasSaveInstanceState = true;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!mHasSaveInstanceState) {
            // avoid FragmentManager.checkStateLoss()'s throwing IllegalStateException
            super.onBackPressed();
        }
    }

    private void startNotificationMatch() {
        AtomicReference<String> swipedUserName = new AtomicReference<>("");
        AtomicReference<String> swipedUserImage = new AtomicReference<>("");
        AtomicReference<String> alertUserId = new AtomicReference<>("");

        if (firebaseUser != null) {
            String currentUser = firebaseUser.getUid();
            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .collection(Constants.MATCH)
                    .whereEqualTo("alert_done", "no")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                AlertMatch alertMatch = doc.getDocument().toObject(AlertMatch.class);
                                alertUserId.set(alertMatch.getUser_matches());

                                if (currentUser != alertUserId.toString()) {
                                    Log.d(TAG, "Alert user id: " + alertUserId.toString());

                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(currentUser)
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                .document(String.valueOf(alertUserId))
                                                                .get()
                                                                .addOnCompleteListener(task2 -> {
                                                                    if (task2.isSuccessful()) {
                                                                        swipedUserImage.set(task2.getResult().getString("z_image"));
                                                                        swipedUserName.set(task2.getResult().getString("name"));

                                                                        Log.d(TAG, "Alert user name: " + swipedUserName);

                                                                        if (Objects.equals(task.getResult().getString("alert_match"), "yes")) {
//                                                                            Log.d(TAG, "Alert ooooooo: "+swipedUserName);
                                                                            notificationNewConnection(swipedUserName, swipedUserImage);
                                                                            /* update match collection */
                                                                            updateAlertNotify(alertUserId);

                                                                        }
                                                                    }
                                                                });


                                                    }
                                                });
                                    }
                                    if (doc.getType() == DocumentChange.Type.MODIFIED) {
                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(currentUser)
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        if (Objects.equals(task.getResult().getString("alert_match"), "yes")) {
                                                            notificationNewConnection(swipedUserName, swipedUserImage);
                                                            /* update match collection */
                                                            updateAlertNotify(alertUserId);
                                                        }
                                                    }
                                                });
                                    }
                                }


                            }
                        }
                    });
        }
    }

    private void notificationNewConnection(AtomicReference<String> swipedName1, AtomicReference<String> swipedImage1) {
        String swipedName = String.valueOf(swipedName1);
        String swipedImage = String.valueOf(swipedImage1);

        Intent broadcastIntent = new Intent(this, MainActivity.class);
        broadcastIntent.putExtra("tab_show", "tab_fragment");
        broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), broadcastIntent, 0);

        /* get image string url and display */
        Bitmap largeIcon = null;

        try {
            URL url = new URL(swipedImage);
            largeIcon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }


        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.getPackageName() + "/" + R.raw.new_connection);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("1", CHANNEL_NEW_CONNECTION, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Description");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        //General code:
        NotificationCompat.Builder status = new NotificationCompat.Builder(this, "1");
        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.uni_foreground)
                //.setOnlyAlertOnce(true)
                .setContentTitle(this.getString(R.string.new_connection))
                .setContentText(swipedName)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(swipedName)
                        .setBigContentTitle("New Connection")
                        .setSummaryText(this.getString(R.string.new_connection)))
                .setLargeIcon(largeIcon)
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSound(soundUri)
                .setContentIntent(contentIntent);


        mNotificationManager.notify(1, status.build());
    }

    private void updateAlertNotify(AtomicReference<String> alertUserId1) {
        String alertUserId = String.valueOf(alertUserId1);
        String currentUser = firebaseUser.getUid();
        /* update notification done in fire store for current user match */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.MATCH)
                .document(alertUserId)
                .update("alert_done", "yes")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "notification done and set to yes: ");
                    }
                });
    }


    private void StartNotificationChats() {

        if (firebaseUser != null) {

            String currentUser = firebaseUser.getUid();

            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .collection(Constants.CHATS)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    ChatUserClass chatUserClass = doc.getDocument().toObject(ChatUserClass.class);

                                    if (chatUserClass.getUser_unread().equals("0")) {

                                        String user_message = chatUserClass.getUser_message();
                                        String user_receiver = chatUserClass.getUser_receiver();

                                        Log.d(TAG, "111111111: " + user_receiver);
                                        Log.d(TAG, "222222222: " + currentUser);

//                                        if (currentUser.equals(user_receiver)){
//                                            Log.d(TAG, "3333332222: ");
//                                            CheckNotificationsChats(currentUser, user_receiver);
//                                        }
                                        CheckNotificationsChats(currentUser, user_receiver);

                                    }
                                }
                                if (doc.getType() == DocumentChange.Type.MODIFIED) {
                                    ChatUserClass chatUserClass = doc.getDocument().toObject(ChatUserClass.class);

                                    if (chatUserClass.getUser_unread().equals("0")) {

                                        String user_message = chatUserClass.getUser_message();
                                        String user_receiver = chatUserClass.getUser_receiver();
//                                        if (currentUser.equals(user_receiver)){
//                                            CheckNotificationsChats(currentUser, user_receiver);
//                                        }

                                        CheckNotificationsChats(currentUser, user_receiver);
                                    }


                                }
                            }
                        }
                    });
        }

    }

    private void CheckNotificationsChats(String currentUser, String user_receiver) {

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(user_receiver)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicReference<String> SenderName = new AtomicReference<>(task.getResult().getString("name"));
                        AtomicReference<String> senderImage = new AtomicReference<>(task.getResult().getString("z_image"));

                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (task1.getResult().getString("alert_chats").equals("yes")) {
                                            notificationNewChat(SenderName, senderImage, user_receiver);
                                        }
                                    }
                                });


                    }
                });
    }

    private void notificationNewChat(AtomicReference<String> senderName1, AtomicReference<String> senderImage1,
                                     String receiver) {
        String senderName = String.valueOf(senderName1);
        String senderImage = String.valueOf(senderImage1);

        Intent broadcastIntent = new Intent(this, MessageActivity.class);
        broadcastIntent.putExtra("user_uid", receiver);
//        broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), broadcastIntent, 0);

        /* get image string url and display */
        Bitmap largeIcon = null;

        try {
            URL url = new URL(senderImage);
            largeIcon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }


        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.getPackageName() + "/" + R.raw.new_chat_message);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("2", CHANNEL_NEW_CHAT, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Description");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        //General code:
        NotificationCompat.Builder status = new NotificationCompat.Builder(this, "2");
        status.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.uni_foreground)
                //.setOnlyAlertOnce(true)
                .setContentTitle(this.getString(R.string.new_chat))
                .setContentText(senderName)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(senderName)
                        .setBigContentTitle("New Chat")
                        .setSummaryText(this.getString(R.string.new_chat)))
                .setLargeIcon(largeIcon)
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSound(soundUri)
                .setContentIntent(contentIntent);


        mNotificationManager.notify(2, status.build());
    }
}

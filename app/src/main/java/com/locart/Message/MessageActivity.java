package com.locart.Message;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locart.Activity.CameraActivity;
import com.locart.Locart;
import com.locart.Chats.ChatsClass;
import com.locart.Utils.DataProccessor;
import com.locart.profile.ProfileActivity;
import com.locart.model.ProfileClass;
import com.locart.R;
import com.locart.Utils.Constants;
import com.locart.Utils.MyRecorder;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import pub.devrel.easypermissions.EasyPermissions;

public class MessageActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    TextView toolbarTextViewDistanceAway, toolbarTextViewName, tvCancelVoiceMessage,
            tvRecordedMessageDuration, toolbarTextViewUsername;

    Toolbar toolbar;
    CircularImageView toolbarImageViewUserImage;
    MessageAdapter messageAdapter;
    ArrayList<ChatsClass> listChatsClass;
    RecyclerView recyclerViewMessageView;
    EmojiconEditText editTextMessageText;
    ImageView cameraButton, galleryButton, gifButton, addButton, sendButton, recordButton, stopAudioButton;
    ListenerRegistration chatsListenerRegistration, seenListenerRegistration;

    Bitmap bitmapMessageImage, convertedImage;
    /* Image request code */
    public int MESSAGE_IMAGE = 1;
    /* storage permission code */
    private static final int RC_STORAGE_PERM = 101;
    /* storage permission code */
    private static final int RC_CAMERA_STORAGE_PERM = 112;
    /* audio record permission code */
    private static final int RC_AUDIO_PERM = 111;
    /* start for activity request code */
    public static final int REQUEST_CODE_CAMERA = 12;
    /* Uri to store the image uri */
    Uri imageUri;
    LinearLayout layoutBottomChat, layoutBottomAudio;
    String genderCurrent, imageCurrent, verifiedCurrent, premiumCurrent, countryCurrent,
            editTextChatHide = "no", stringUnread, stringUnreadz, profileUser, currentUser,
            stringText = "TEXT", stringImage = "IMAGE", stringAudio = "AUDIO",
            sName, sUsername, sJob, sNexusCount, sCity, sWebsite, sAbout, sEducation, sImage,
            sCompany, sLatitude, sLongitude, sCheckOfflineAfter24Hours = "no",
            sLookingFor, sDonate, sDonateHow, sSex;

    int intUnread;
    private static final String TAG = "MessageActivity";
    private EmojIconActions emojIcon;
    private View contentRoot;

    CountDownTimer countDownTimer;
    int second = -1, minute, hour;
    private MyRecorder myRecorder;
    File myDirectory = new File(Environment.getExternalStorageDirectory() +
            File.separator + "Locart" + File.separator +
            File.separator + "Audio");
    File imageDirectory = new File(Environment.getExternalStorageDirectory() +
            File.separator + "Locart" + File.separator +
            File.separator + "Image");
    String fileName = "record.3gp";
    private ArrayList<ProfileClass> arrayUserClass;
    ImageView addContacts, addLocation, attachFile;
    boolean isFABOpen = false;
    DataProccessor dataProccessor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);

        initUi();
    }

    private void initUi() {
        toolbarTextViewName = findViewById(R.id.toolbarTextViewName);
        toolbarTextViewUsername = findViewById(R.id.toolbarTextViewUsername);
        toolbarTextViewDistanceAway = findViewById(R.id.toolbarTextViewDistanceAway);
        toolbarImageViewUserImage = findViewById(R.id.toolbarImageViewUserImage);
        editTextMessageText = findViewById(R.id.editTextMessageText);

        cameraButton = findViewById(R.id.cameraButton);
        recordButton = findViewById(R.id.micButton);
        galleryButton = findViewById(R.id.galleryButton);
        gifButton = findViewById(R.id.gifButton);
        addButton = findViewById(R.id.addButton);
        sendButton = findViewById(R.id.sendButton);
        stopAudioButton = findViewById(R.id.stopAudioButton);

        tvCancelVoiceMessage = findViewById(R.id.tvCancelVoiceMessage);
        tvRecordedMessageDuration = findViewById(R.id.record_view_duration);

        addContacts = findViewById(R.id.add_contacts);
        addLocation = findViewById(R.id.add_location);
        attachFile = findViewById(R.id.attach_file);
        dataProccessor = new DataProccessor(this);

//        myRecorder = new MyRecorder();


        recordButton.setOnClickListener(v -> {
            //check for permission first
            audioAndStorage();
        });


        emojIcon = new EmojIconActions(this, contentRoot, editTextMessageText, gifButton);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.d("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.d("Keyboard", "close");
            }
        });
        emojIcon.addEmojiconEditTextList(editTextMessageText);
        emojIcon.setUseSystemEmoji(true);
//        editTextMessageText.setUseSystemEmoji(true);

        listChatsClass = new ArrayList<>();

        layoutBottomChat = findViewById(R.id.layout_bottom_chat);
        layoutBottomAudio = findViewById(R.id.layout_bottom_audio);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        arrayUserClass = new ArrayList<>();

        recyclerViewMessageView = findViewById(R.id.recyclerViewMessageView);
        recyclerViewMessageView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessageView.setLayoutManager(linearLayoutManager);

        currentUser = firebaseAuth.getCurrentUser().getUid();
        profileUser = getIntent().getStringExtra("user_uid");

        /* limit edit text to 10,000 character */
        int maxTextLength = 10000;
        editTextMessageText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxTextLength)});

        editTextMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() == 0) {
                    showSendButton();
                    emojIcon.setUseSystemEmoji(true);

                    sendButton.setOnClickListener(view -> {
                        String chatMessage = editTextMessageText.getText().toString();

                        sendMessage(currentUser, profileUser, chatMessage, stringText);

                        editTextMessageText.setText("");

                    });

                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    showVoiceButton();

                }
            }

        });

        cameraButton.setOnClickListener(view -> {
            checkCameraAndStoragePermission();

        });

        galleryButton.setOnClickListener(view -> {
            /* select image from storage */
            selectImage();
        });

        gifButton.setOnClickListener(view -> {
//            emojiPopup.toggle();

        });

        addButton.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        readMessage(currentUser, profileUser);

        toolbarTextViewName.setOnClickListener(v -> chatProfile());
        toolbarTextViewDistanceAway.setOnClickListener(v -> chatProfile());

        toolbarImageViewUserImage.setOnClickListener(v -> chatProfile());
    }

    private void showFABMenu() {
        isFABOpen = true;
        addContacts.setVisibility(View.VISIBLE);
        addLocation.setVisibility(View.VISIBLE);
        attachFile.setVisibility(View.VISIBLE);
        addContacts.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        addLocation.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        attachFile.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        addContacts.animate().translationY(0);
        addLocation.animate().translationY(0);
        attachFile.animate().translationY(0);
        addContacts.setVisibility(View.GONE);
        addLocation.setVisibility(View.GONE);
        attachFile.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (!isFABOpen) {
            super.onBackPressed();
        } else {
            closeFABMenu();
        }
    }

    private void checkCameraAndStoragePermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(MessageActivity.this, perms)) {
            // Have permission, do the thing!
            Intent camera = new Intent(MessageActivity.this, CameraActivity.class);
            startActivityForResult(camera, REQUEST_CODE_CAMERA);
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(MessageActivity.this, getString(R.string.rationale_camera_storage),
                    RC_CAMERA_STORAGE_PERM, perms);
        }
    }

    private void chatProfile() {
        Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
        intent.putExtra("user_uid", profileUser);
        intent.putExtra("name", sName);
        intent.putExtra("nexus", sNexusCount);
        intent.putExtra("about", sAbout);
        intent.putExtra("city", sCity);
        intent.putExtra("job", sJob);
        intent.putExtra("education", sEducation);
        intent.putExtra("image", sImage);
        intent.putExtra("username", sUsername);
        intent.putExtra("website", sWebsite);
        intent.putExtra("company", sCompany);

        intent.putExtra("lookingFor", sLookingFor);
        intent.putExtra("donate", sDonate);
        intent.putExtra("donateHow", sDonateHow);

        startActivity(intent);
    }

    private void sendMessage(final String chatSender, final String chatReceiver,
                             final String chatMessage, final String chatType) {

        /* get current time */
        DateFormat dateFormat = new SimpleDateFormat("H:mm", Locale.US);
        String chatTime = dateFormat.format(new Date());

        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("chat_date_sent", Timestamp.now());
        hashMap.put("chat_date_seen", Timestamp.now());
        hashMap.put("chat_sender", chatSender);
        hashMap.put("chat_receiver", chatReceiver);
        hashMap.put("chat_message", chatMessage);
        hashMap.put("chat_seen_chat", "no");
        hashMap.put("delete_sender", "delete");
        hashMap.put("delete_receiver", "delete");
        hashMap.put("message_type", chatType);
        hashMap.put("chat_time", chatTime);

        /* send message */
        db.collection(Constants.CHATS)
                .add(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        final HashMap<String, Object> userChatHashMap = new HashMap<>();
                        userChatHashMap.put("user_date_sent", Timestamp.now());
                        userChatHashMap.put("user_sender", chatSender);
                        userChatHashMap.put("user_receiver", chatReceiver);
                        userChatHashMap.put("user_message", chatMessage);
                        userChatHashMap.put("message_type", chatType);
                        userChatHashMap.put("user_unread", "0");
                        userChatHashMap.put("chat_time", chatTime);


                        final HashMap<String, Object> chatSetHashMap = new HashMap<>();
                        chatSetHashMap.put("user_date_sent", Timestamp.now());
                        chatSetHashMap.put("user_sender", chatReceiver);
                        chatSetHashMap.put("user_receiver", chatSender);
                        chatSetHashMap.put("user_message", chatMessage);
                        chatSetHashMap.put("message_type", chatType);
                        chatSetHashMap.put("user_unread", "0");
                        chatSetHashMap.put("chat_time", chatTime);

                        final HashMap<String, Object> chatUpdateHashMap = new HashMap<>();
                        chatUpdateHashMap.put("user_date_sent", Timestamp.now());
                        chatUpdateHashMap.put("user_sender", chatReceiver);
                        chatUpdateHashMap.put("user_receiver", chatSender);
                        chatUpdateHashMap.put("user_message", chatMessage);

                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .collection(Constants.CHATS)
                                .document(profileUser)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.getResult().exists()) {
                                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(currentUser)
                                                .collection(Constants.CHATS)
                                                .document(profileUser)
                                                .update(userChatHashMap)
                                                .addOnCompleteListener(task112 -> {
                                                    if (task112.isSuccessful()) {

                                                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                                                .document(profileUser)
                                                                .collection(Constants.CHATS)
                                                                .document(currentUser)
                                                                .update(chatUpdateHashMap)
                                                                .addOnCompleteListener(task11 -> {
                                                                    if (task11.isSuccessful()) {
                                                                        /* Update match collection and field */
                                                                        updateMatchCollection();

                                                                    } else {
                                                                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                .document(profileUser)
                                                                                .collection(Constants.CHATS)
                                                                                .document(currentUser)
                                                                                .set(chatSetHashMap)
                                                                                .addOnCompleteListener(task2 -> {
                                                                                    /* Update match collection and field */
                                                                                    updateMatchCollection();
                                                                                });
                                                                    }
                                                                });

                                                    }
                                                });


                                    } else {

                                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(currentUser)
                                                .collection(Constants.CHATS)
                                                .document(profileUser)
                                                .set(userChatHashMap)
                                                .addOnCompleteListener(task113 -> {
                                                    if (task113.isSuccessful()) {

                                                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                                                .document(profileUser)
                                                                .collection(Constants.CHATS)
                                                                .document(currentUser)
                                                                .set(chatSetHashMap)
                                                                .addOnCompleteListener(task22 -> {
                                                                    /* Update match collection and field */
                                                                    updateMatchCollection();
                                                                });

                                                    }
                                                });
                                    }
                                });
                    }
                });

        /* update message as read */
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .collection(Constants.CHATS)
                .document(currentUser)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.getResult().exists()) {

                        stringUnread = task.getResult().getString("user_unread");
                        intUnread = Integer.parseInt(stringUnread) + 1;
                        stringUnreadz = String.valueOf(intUnread);

                        final HashMap<String, Object> chatUnreadHashMap = new HashMap<>();
                        chatUnreadHashMap.put("user_unread", stringUnreadz);

                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(profileUser)
                                .collection(Constants.CHATS)
                                .document(currentUser)
                                .update(chatUnreadHashMap)
                                .addOnCompleteListener(task12 -> {
                                    //done
                                });

                    } else {

                    }

                });
    }

    /* update match, user_chat_start as yes for both chat user */
    private void updateMatchCollection() {
        Map<String, Object> mapMatchesChat = new HashMap<>();
        mapMatchesChat.put("user_chat_start", "yes");

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.MATCH)
                .document(profileUser)
                .update(mapMatchesChat)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
//                        Log.d(TAG, "Done for current user: "+profileUser);
                    }
                });

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .collection(Constants.MATCH)
                .document(currentUser)
                .update(mapMatchesChat)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
//                        Log.d(TAG, "Done for profile user: "+currentUser);
                    }
                });
    }

    private void readMessage(final String myid, final String userid) {

        chatsListenerRegistration = db.collection(Constants.CHATS)
                .orderBy("chat_date_sent", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        ChatsClass chatsClass = doc.getDocument().toObject(ChatsClass.class);
                                        if (
                                                (chatsClass.getChat_receiver().equals(myid) &&
                                                        chatsClass.getChat_sender().equals(userid) ||
                                                        chatsClass.getChat_receiver().equals(userid) &&
                                                                chatsClass.getChat_sender().equals(myid)) &&
                                                        (!chatsClass.getDelete_sender().equals(myid) &&
                                                                !chatsClass.getDelete_receiver().equals(myid))
                                        ) {
                                            listChatsClass.add(chatsClass);
                                        }
                                    }

                                    if (doc.getType() == DocumentChange.Type.MODIFIED) {

                                        ChatsClass chatsClassMod = doc.getDocument().toObject(ChatsClass.class);
                                        for (int i = 0; i < listChatsClass.size(); i++) {
                                            if (doc.getDocument().getDate("chat_date_sent").equals(listChatsClass.get(i).getChat_date_sent())) {
                                                if (doc.getDocument().getString("chat_message").equals(listChatsClass.get(i).getChat_message())) {
                                                    listChatsClass.set(i, chatsClassMod);
                                                }
                                            }
                                        }

                                    }
                                }

                                messageAdapter = new MessageAdapter(listChatsClass, MessageActivity.this);
                                recyclerViewMessageView.setAdapter(messageAdapter);
                            }
                        }
                );
    }

    private void ChatControl() {

//        if (editTextChatHide.equals("yes")) {
//            LayoutBottomChat.setVisibility(View.GONE);
//        } else {
//            LayoutBottomChat.setVisibility(View.VISIBLE);
//        }

    }

    private void SeenMessage(final String userid) {

        currentUser = firebaseAuth.getCurrentUser().getUid();
        profileUser = getIntent().getStringExtra("user_uid");

        db.collection(Constants.CHATS)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot querySnapshot : task.getResult()) {

                        ChatsClass chatsClass = querySnapshot.toObject(ChatsClass.class);

                        if (chatsClass.getChat_receiver() != null && chatsClass.getChat_sender() != null
                                && chatsClass.getChat_receiver().equals(firebaseUser.getUid()) &&
                                chatsClass.getChat_sender().equals(userid)) {

                            HashMap<String, Object> seenHashMap = new HashMap<>();
                            seenHashMap.put("chat_seen_chat", "yes");
                            seenHashMap.put("chat_date_seen", Timestamp.now());

                            if (chatsClass.getChat_seen_chat() != null && chatsClass.getChat_seen_chat().equals("no")) {

                                querySnapshot.getReference().update(seenHashMap);
                            }
                        }
                    }
                });

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.CHATS)
                .document(profileUser)
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        final HashMap<String, Object> chatUnresetHashMap = new HashMap<>();
                        chatUnresetHashMap.put("user_unread", "0");

                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .collection(Constants.CHATS)
                                .document(profileUser)
                                .update(chatUnresetHashMap);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }

        /* intent data */
        profileUser = getIntent().getStringExtra("user_uid");

        UserStatus("online");

        UserCurrent();
        UserProfile();

    }

    private void UserCurrent() {
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {
                        genderCurrent = documentSnapshot.getString("user_gender");
                        imageCurrent = documentSnapshot.getString("z_image");
                        verifiedCurrent = documentSnapshot.getString("user_verified");
                        premiumCurrent = documentSnapshot.getString("user_premium");
                        countryCurrent = documentSnapshot.getString("user_country");

                        String matchCurrent = documentSnapshot.getString("show_match");

                        if (matchCurrent != null && matchCurrent.equals("yes")) {

                            db.collection(Constants.FIRE_STORE_COLLECTION)
                                    .document(currentUser)
                                    .collection(Constants.MATCH)
                                    .document(profileUser)
                                    .addSnapshotListener((documentSnapshot1, e1) -> {
                                        if (documentSnapshot1 != null && !documentSnapshot1.exists()) {

                                            editTextChatHide = "yes";

                                            ChatControl();

                                        }
                                    });

                        }


                    }
                });

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .collection(Constants.CHATS)
                .document(currentUser)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserChats("yes");
                    } else {
                        UserChats("no");

                    }
                });
    }

    private void UserChats(String stringChats) {

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {

                        if (stringChats.equals("no")) {
                            editTextChatHide = "yes";
                            ChatControl();
                        }
                    }
                });


    }

    private void UserProfile() {

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {

                        sName = documentSnapshot.getString("name");
                        sUsername = documentSnapshot.getString("user_username");
                        String user_status = documentSnapshot.getString("user_status");
                        String user_sex = documentSnapshot.getString("sex");
                        sImage = documentSnapshot.getString("z_image");
                        String user_country = documentSnapshot.getString("user_country");
                        String ghost_mode = documentSnapshot.getString("alert_ghost_mode");
                        String show_status = documentSnapshot.getString("show_status");

                        sJob = documentSnapshot.getString("user_job");
                        sCity = documentSnapshot.getString("user_city");
                        sWebsite = documentSnapshot.getString("user_website");
                        sAbout = documentSnapshot.getString("user_about");
                        sEducation = documentSnapshot.getString("user_education");
                        sCompany = documentSnapshot.getString("user_company");
                        sLatitude = documentSnapshot.getString("user_latitude");
                        sLongitude = documentSnapshot.getString("user_longitude");

                        sLookingFor = documentSnapshot.getString("looking_for");
                        sDonate = documentSnapshot.getString("user_donate");
                        sDonateHow = documentSnapshot.getString("user_donate_how");

                        sSex = user_sex;

                        ProfileClass profileClass = documentSnapshot.toObject(ProfileClass.class);
                        arrayUserClass.add(profileClass);

                        /* check to see if ghost mode is enabled */
                        if (ghost_mode != null && ghost_mode.equals("yes")) {

                            db.collection(Constants.FIRE_STORE_COLLECTION)
                                    .document(currentUser)
                                    .collection(Constants.MATCH)
                                    .document(profileUser)
                                    .addSnapshotListener((documentSnapshot1, e1) -> {
                                        if (documentSnapshot1 != null && !documentSnapshot1.exists()) {
                                            editTextChatHide = "yes";

                                            ChatControl();
                                        }
                                    });
                        }

                        /* check for nexus count*/
                        db.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(profileUser)
                                .collection(Constants.MATCH)
                                .addSnapshotListener((queryDocumentSnapshots, e2) -> {
                                    if (queryDocumentSnapshots != null) {
                                        sNexusCount = String.valueOf(queryDocumentSnapshots.size());
                                    }
                                });

                        /* check if username is not empty then attach it to the name */
                        if (sUsername != null && !sUsername.equals("")) {
                            toolbarTextViewName.setText(sName);
                            toolbarTextViewUsername.setText(String.format(" @%s", sUsername));
                        } else {
                            toolbarTextViewName.setText(sName);
                        }

                        /* manipulation for calculating and showing km away */
                        double lat1 = 1.11, long1 = 1.11, lat2 = 1.11, long2 = 1.11;
                        try {
                            lat1 = Double.parseDouble(dataProccessor.getStr(Constants.LATITUDE));
                            long1 = Double.parseDouble(dataProccessor.getStr(Constants.LONGITUDE));
                            lat2 = Double.parseDouble(sLatitude);
                            long2 = Double.parseDouble(sLongitude);
                        } catch (Exception ex) {
                            Log.d(TAG, "Error for input: " + ex.getMessage());
                        }

                        /* get the distance between current user and swipe cards */
                        double distanceInMeter, distanceInKm;

                        Location locationA = new Location("");
                        locationA.setLatitude(lat1);
                        locationA.setLongitude(long1);
                        Location locationB = new Location("");
                        locationB.setLatitude(lat2);
                        locationB.setLongitude(long2);
                        /* get the difference in km */
                        distanceInMeter = locationA.distanceTo(locationB);
                        distanceInKm = locationA.distanceTo(locationB) / 1000;

                        int distanceBetween = Double.valueOf(distanceInKm).intValue();
                        int distanceBetweenTwo = Double.valueOf(distanceInMeter).intValue();
                        if (distanceBetween == 0){
                            /* convert int to string for meter */
                            String m = String.valueOf(distanceBetweenTwo);
                            toolbarTextViewDistanceAway.setText(String.format("%s M away", m));
                        }else {
                            /* convert int to string for km */
                            String km = String.valueOf(distanceBetween);
                            toolbarTextViewDistanceAway.setText(String.format("%s Km away", km));
                        }


                        Date online = documentSnapshot.getDate("user_online");
                        assert online != null;
                        long SavedMilliseconds = online.getTime();

                        /* Check time elapsed */
                        if (System.currentTimeMillis() >= SavedMilliseconds + 24 * 60 * 60 * 1000) {
                            /* time has elapsed */
                            sCheckOfflineAfter24Hours = "yes";
                        }

                        /* check if user is a Shop and set ring as silver else check user status */
                        assert user_sex != null;
                        if (user_sex.equals("Shop")) {
                            toolbarImageViewUserImage.setBorderColorStart(Color.TRANSPARENT);
                            toolbarImageViewUserImage.setBorderColorEnd(R.color.argent);
                        } else {
                            if (user_status != null && user_status.equals("online")) {
                                toolbarImageViewUserImage.setBorderColorStart(Color.GREEN);
                                toolbarImageViewUserImage.setBorderColorEnd(R.color.avacado);
                            } else if (sCheckOfflineAfter24Hours.equals("yes")) {
                                toolbarImageViewUserImage.setBorderColorStart(R.color.amber);
                                toolbarImageViewUserImage.setBorderColorEnd(Color.YELLOW);
                            } else if (user_status != null && user_status.equals("offline")) {
                                toolbarImageViewUserImage.setBorderColorStart(R.color.crimson);
                                toolbarImageViewUserImage.setBorderColorEnd(Color.RED);
                            } else if (user_status != null && user_status.equals("paused")) {
                                /* app is in background */
                                toolbarImageViewUserImage.setBorderColorStart(Color.BLUE);
                                toolbarImageViewUserImage.setBorderColorEnd(R.color.azure);
                            }
                        }

                        /* get image url and display */
                        Glide.with(getApplicationContext())
                                .load(sImage)
                                .placeholder(R.drawable.no_image)
                                .into(toolbarImageViewUserImage);
                    }
                });


    }

    private void UserStatus(String status) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = firebaseUser.getUid();

        Map<String, Object> arrayUserStatus = new HashMap<>();
        arrayUserStatus.put("user_status", status);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .update(arrayUserStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserStatus("online");
        profileUser = getIntent().getStringExtra("user_uid");
        SeenMessage(profileUser);
        currentUser = firebaseAuth.getCurrentUser().getUid();

        checkProfileUserStatus();

    }

    private void checkProfileUserStatus() {
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(profileUser)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    String user_status = documentSnapshot.getString("user_status");
                    Date online = documentSnapshot.getDate("user_online");
                    assert online != null;
                    long SavedMilliseconds = online.getTime();

                    /* Check time elapsed */
                    if (System.currentTimeMillis() >= SavedMilliseconds + 24 * 60 * 60 * 1000) {
                        /* time has elapsed */
                        sCheckOfflineAfter24Hours = "yes";
                    }

                    Log.d(TAG, "User Status: " + user_status);
                    /* check if user is a Shop and set ring as silver else check user status */
                    if (sSex.equals("Shop")) {
                        toolbarImageViewUserImage.setBorderColorStart(Color.TRANSPARENT);
                        toolbarImageViewUserImage.setBorderColorEnd(R.color.argent);
                    } else {
                        if (user_status != null && user_status.equals("online")) {
                            toolbarImageViewUserImage.setBorderColorStart(Color.GREEN);
                            toolbarImageViewUserImage.setBorderColorEnd(R.color.avacado);
                        } else if (sCheckOfflineAfter24Hours.equals("yes")) {
                            toolbarImageViewUserImage.setBorderColorStart(R.color.amber);
                            toolbarImageViewUserImage.setBorderColorEnd(Color.YELLOW);
                        } else if (user_status != null && user_status.equals("offline")) {
                            toolbarImageViewUserImage.setBorderColorStart(R.color.crimson);
                            toolbarImageViewUserImage.setBorderColorEnd(Color.RED);
                        } else if (user_status != null && user_status.equals("paused")) {
                            /* app is in background */
                            toolbarImageViewUserImage.setBorderColorStart(Color.BLUE);
                            toolbarImageViewUserImage.setBorderColorEnd(R.color.azure);
                        }
                    }

                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatus("paused");
        Locart.appRunning = false;
        checkProfileUserStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* stop and release voice note player */
        if (messageAdapter != null) {
            messageAdapter.stopPlaying();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserStatus("offline");
    }

    private void showSendButton() {
        gifButton.setVisibility(View.GONE);
        recordButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        galleryButton.setVisibility(View.GONE);
        sendButton.setVisibility(View.VISIBLE);
    }

    private void showVoiceButton() {
        gifButton.setVisibility(View.VISIBLE);
        recordButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        galleryButton.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.GONE);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_message, menu);
//        return true;
//    }
//
//    @SuppressLint("NonConstantResourceId")
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_audio_call:
//                Toast.makeText(getApplicationContext(), "Audio", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.action_video_call:
//                Toast.makeText(getApplicationContext(), "Video", Toast.LENGTH_SHORT).show();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void selectImage() {
        /* Check for storage permission */
        storageTask();
    }

    private void storageTask() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(MessageActivity.this, perms)) {
            // Have permission, do the thing!
            showFileChooser();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(MessageActivity.this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERM, perms);
        }
    }

    /* method to show file chooser */
    private void showFileChooser() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, MESSAGE_IMAGE);
    }

    /**
     * Get the uri of the image the user picked if the result is successful
     *
     * @param requestCode - code of the request ( for the image request is 1)
     * @param resultCode  - if the result was successful
     * @param data        - data of the image fetched
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (requestCode == MESSAGE_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();

            String imagePath = getFullPathFromContentUri(getApplicationContext(), imageUri);
            final String PATH = "/storage/emulated/0/Locart/Image/picture.jpg";

            try {
                bitmapMessageImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                /* check if bitmap is not empty */
                if (bitmapMessageImage != null) {
                    /* resize image to smaller size in phone storage
                    set bitmap to image view for user*/
                    compressImage(imagePath);

                    convertedImage = BitmapFactory.decodeFile(PATH);

                    UploadTask uploadTask;
                    StorageReference imageRef = storageReference.child("chat_images").child(firebaseUser.getUid()).child(timeStamp + ".jpg");
                    uploadTask = imageRef.putFile(Uri.fromFile(new File(PATH)));

                    Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        /* Continue with the task to get the download URL */
                        return imageRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String downloadUri = String.valueOf(task.getResult());

                            Log.d(TAG, "onSuccess 2..." + downloadUri);
                            /* send image to user and save to fireStore as url */
                            sendMessage(currentUser, profileUser, downloadUri, stringImage);
                            /* delete image from storage */
                            File file2 = new File(imageDirectory, "picture" + ".jpg");
                            if (file2.exists()) {
                                file2.delete();
                            }

                        } else {
                            // Handle failures
                            Log.d(TAG, "onFailure()...");
                        }
                    });


                } else {
                    Toast.makeText(MessageActivity.this, "Failed to load image from memory", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //get image from camera activity and send image
        if (requestCode == REQUEST_CODE_CAMERA) {

            if (resultCode == Activity.RESULT_OK) {
                String messageType = data.getStringExtra("messageType");
                String result = data.getStringExtra("file");
                // do something with the result
                UploadTask uploadTask;
                StorageReference imageRef = storageReference.child("chat_images").child(firebaseUser.getUid()).child(timeStamp + ".jpg");
                uploadTask = imageRef.putFile(Uri.fromFile(new File(result)));

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    /* Continue with the task to get the download URL */
                    return imageRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String downloadUri = String.valueOf(task.getResult());

                        Log.d(TAG, "onSuccess 2..." + downloadUri);
                        /* send image to user and save to fireStore as url */
                        sendMessage(currentUser, profileUser, downloadUri, messageType);
                        /* delete image from storage */
                        File file2 = new File(imageDirectory, "capture" + ".jpg");
                        if (file2.exists()) {
                            file2.delete();
                        }

                    } else {
                        // Handle failures
                        Log.d(TAG, "onFailure()...");
                    }
                });


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // some stuff that will happen if there's no result
                Log.d(TAG, "onActivityResult: upload cancelled");
            }
        }

    }

    /* get image uri to string */
    public static String getFullPathFromContentUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.external.storage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                Cursor cursor = null;
                final String column = "_data";
                final String[] projection = {
                        column
                };

                try {
                    cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int column_index = cursor.getColumnIndexOrThrow(column);
                        return cursor.getString(column_index);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
                return null;
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /* resize image to smaller size */
    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    /* get file name and new storage path */
    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Locart/Image");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + "picture" + ".jpg");
//        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    /* get real path using cursor */
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    /* do image maths for resizing */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public void audioAndStorage() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            myRecorder = new MyRecorder();
            startRecording();

        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_audio_storage),
                    RC_AUDIO_PERM, perms);
        }
    }

    private void startRecording() {

        editTextMessageText.setVisibility(View.GONE);
        cameraButton.setVisibility(View.GONE);
        layoutBottomAudio.setVisibility(View.GONE);
        sendButton.setVisibility(View.VISIBLE);
        tvCancelVoiceMessage.setVisibility(View.VISIBLE);
        tvRecordedMessageDuration.setVisibility(View.VISIBLE);

        myRecorder.startRecording();
        showTimer();

        /* Start Recording.. */
        Log.d("RecordView", "Recording started");


        tvCancelVoiceMessage.setOnClickListener(v -> {
            /* stop audio recording and delete the audio file */
            myRecorder.stopRecording();

            File file = new File(myDirectory, fileName);
            if (file.exists()) {
                file.delete();
            }
            editTextMessageText.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            layoutBottomAudio.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.GONE);
            tvCancelVoiceMessage.setVisibility(View.GONE);
            tvRecordedMessageDuration.setVisibility(View.GONE);

            Log.d("RecordView", "Recording stopped and deleted");

        });

        sendButton.setOnClickListener(v -> {
            //send audio
            myRecorder.stopRecording();

            uploadAudio();

            Log.d("RecordView", "Recording finished and saved");
            editTextMessageText.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            layoutBottomAudio.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.GONE);
            tvCancelVoiceMessage.setVisibility(View.GONE);
            tvRecordedMessageDuration.setVisibility(View.GONE);

        });

    }

    private void uploadAudio() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        Uri file = Uri.fromFile(new File(myDirectory + File.separator + fileName));

        UploadTask uploadTask;
        StorageReference audioRef = storageReference.child("chat_audio").child(firebaseUser.getUid()).child(timeStamp + ".3gp");
        uploadTask = audioRef.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            /* Continue with the task to get the download URL */
            return audioRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String downloadUri = String.valueOf(task.getResult());

                Log.d(TAG, "onSuccess 2..." + downloadUri);
                /* send image to user and save to fireStore as url */
                sendMessage(currentUser, profileUser, downloadUri, stringAudio);
                /* delete audio from storage */
                File file2 = new File(myDirectory, fileName);
                if (file2.exists()) {
                    file2.delete();
                }

            } else {
                // Handle failures
                Log.d(TAG, "onFailure()...");
            }
        });


    }

    private void showTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                second++;
                tvRecordedMessageDuration.setText(recorderTime());
            }

            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    /* recorder time */
    public String recorderTime() {
        if (second == 60) {
            minute++;
            second = 0;
        }
        if (minute == 60) {
            hour++;
            minute = 0;
        }
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

}

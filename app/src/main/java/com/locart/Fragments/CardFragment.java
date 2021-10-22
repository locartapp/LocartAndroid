package com.locart.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.locart.Locart;
import com.locart.model.LaterClass;
import com.locart.model.ProfileClass;
import com.locart.SwipeAdapter;
import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.locart.model.UnlinkClass;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skyfishjy.library.RippleBackground;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.locart.Matches.MatchesClass;
import com.locart.model.LinkClass;

import com.locart.R;

/**
 * Activity that displays the cards to the user
 * <p>
 * It displays them in a way that is within the search params of the current logged in user
 */
public class CardFragment extends Fragment implements CardStackListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    CardStackLayoutManager manager;
    private ArrayList<ProfileClass> arrayUserClass;
    private List<String> arrayUserRemove;

    CardStackView cardStackView;
    private static final String TAG = "CardFragment";

    int intSwipePositionFirst, intSwipePositionLast;

    String stringCheckGender, stringCheckAgesMin, stringCheckAgesMax, stringCheckLocation,
            currentUser, stringDonate, stringDonateHow, stringFetch, stringFetchHow,
            stringDistanceMin, stringDistanceMax, checkNexusCount;

    LinearLayout linearLayoutSwipeImageGroup, linearLayoutSwipeEmptyGroup;

    RippleBackground rippleSwipeAnimation;
    ImageView imageRippleSwipeUser;
    DataProccessor dataProccessor;
    private Activity mActivity;
    private OneMinuteCountDownTimer countDownTimer;
    private final long startTime = 60 * 1000;
    private final long interval = 1 * 1000;
    private boolean allowRefresh = false;
    private Callbacks mCallbacks;
    MyReceiver myReceiver;
    FrameLayout cardLayout;

    View view;

    public CardFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_card, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        dataProccessor = new DataProccessor(getActivity());

        cardLayout = view.findViewById(R.id.card_layout);

        currentUser = firebaseUser.getUid();

        arrayUserClass = new ArrayList<>();
        arrayUserRemove = new ArrayList<>();
        arrayUserRemove.add("demoUserWhenZero");

        linearLayoutSwipeImageGroup = view.findViewById(R.id.linearLayoutSwipeImageGroup);
        linearLayoutSwipeEmptyGroup = view.findViewById(R.id.linearLayoutSwipeEmptyGroup);

        manager = new CardStackLayoutManager(getContext(), this);
        cardStackView = view.findViewById(R.id.cardStackViewNavigationSwipe);
        cardStackView.setLayoutManager(manager);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(2);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(60.0f);
        manager.setDirections(Arrays.asList(Direction.Left, Direction.Right));
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(false);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);

        recyclerViewForUser();

        rippleSwipeAnimation = view.findViewById(R.id.rippleSwipeAnimation);
        imageRippleSwipeUser = view.findViewById(R.id.imageRippleSwipeUser);
        rippleSwipeAnimation.startRippleAnimation();

        initUi();

//        FirebaseFirestore.setLoggingEnabled(true);

        return view;
    }

    private void recyclerViewForUser() {
        /* one minute count down timer */
        countDownTimer = new OneMinuteCountDownTimer(startTime, interval);
        countDownTimer.start();

//        Log.d(TAG, "Starting the query new now");

        stringCheckAgesMin = dataProccessor.getStr(Constants.AGE_MINIMUM);
        stringCheckAgesMax = dataProccessor.getStr(Constants.AGE_MAXIMUM);
        stringDistanceMin = dataProccessor.getStr(Constants.DISTANCE_MINIMUM);
        stringDistanceMax = dataProccessor.getStr(Constants.DISTANCE_MAXIMUM);

        stringCheckLocation = dataProccessor.getStr(Constants.STATE);
        stringDonate = dataProccessor.getStr(Constants.DONATE);
        stringDonateHow = dataProccessor.getStr(Constants.DONATE_HOW);
        stringFetch = dataProccessor.getStr(Constants.FETCH);
        stringFetchHow = dataProccessor.getStr(Constants.FETCH_HOW);

        /* Query link collection for current user and add to array that won't be displayed */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.LINK)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                LinkClass linkClass = doc.getDocument().toObject(LinkClass.class);
                                arrayUserRemove.add(linkClass.getUser_links());

                            }
                        }


                    }
                });

        /* Query match collection for current user and add to array that won't be displayed */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.MATCH)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                MatchesClass matchesClass = doc.getDocument().toObject(MatchesClass.class);
                                arrayUserRemove.add(matchesClass.getUser_matches());

                            }
                        }


                    }
                });

        /* Query later collection for current user and add to array that won't be displayed */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.LATER)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                LaterClass laterClass = doc.getDocument().toObject(LaterClass.class);
                                arrayUserRemove.add(laterClass.getUser_nopes());

                            }
                        }

                    }
                });

        /* Query unlink collection for current user and add to array that won't be displayed */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.UNLINK)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                UnlinkClass unlinkClass = doc.getDocument().toObject(UnlinkClass.class);
                                arrayUserRemove.add(unlinkClass.getUser_unlink());

                            }
                        }

                    }
                });

        /* Query all users in the database collection */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    String stringShowAges = documentSnapshot.getString("user_age");
                    if (stringShowAges != null) {
                        stringCheckAgesMin = documentSnapshot.getString("age_min");
                        stringCheckAgesMax = documentSnapshot.getString("age_max");
                    } else {
                        stringCheckAgesMin = "18";
                        stringCheckAgesMax = "100";
                    }

                    String stringShowLocation = documentSnapshot.getString("user_city");
                    if (stringShowLocation != null) {
                        stringCheckLocation = stringShowLocation;
                    } else {
                        String stringUserState = documentSnapshot.getString("user_state");

                        Map<String, Object> mapShowLocation = new HashMap<>();
                        mapShowLocation.put("user_city", stringUserState);
                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .update(mapShowLocation);

                        stringCheckLocation = stringUserState;
                    }

                    String stringShowGender = documentSnapshot.getString("user_looking");
                    if (stringShowGender != null) {

                        usersDisplay(stringCheckAgesMin, stringCheckAgesMax);

                    }

                });

    }

    private void usersDisplay(final String stringCheckAgesMin, final String stringCheckAgesMax) {

        /* get current user id */
        final String currentUser = firebaseUser.getUid();

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .orderBy("user_online", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot querySnapshot : Objects.requireNonNull(task.getResult())) {

                        ProfileClass profileClass = querySnapshot.toObject(ProfileClass.class);
//                        Log.d(TAG, "One: "+ profileClass.getName());
//                        Log.d(TAG, "Email: "+ profileClass.getEmail());

                        if (profileClass.getUser_id() != null && !profileClass.getUser_id().equals(currentUser)) {
//                            Log.d(TAG, "Remove Current User: "+ profileClass.getName());

                            String age = profileClass.getUser_age();
                            int intBirthAge = 0, intAgeMin = 0, intAgeMax = 0;

                            try {
                                intBirthAge = Integer.parseInt(age);
                                intAgeMin = Integer.parseInt(stringCheckAgesMin);
                                intAgeMax = Integer.parseInt(stringCheckAgesMax);
//                                Log.d(TAG, String.valueOf(intBirthAge));

                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Unable to convert input string :" + age + " to int");
                            }

                            /* check for maximum and minimum age selected in settings to match displayed swipe cards */
                            if (intBirthAge >= intAgeMin && intBirthAge <= intAgeMax) {
                                String findSex = profileClass.getUser_sex();
                                String lookingPrefs = dataProccessor.getStr(Constants.LOOKING);

//                                Log.d(TAG, "Age restriction :" + profileClass.getName()+"Age is: "+profileClass.getUser_age());
                                double lat1 = 1.11, long1 = 1.11, lat2 = 1.11, long2 = 1.11;

                                try {
                                    lat1 = Double.parseDouble(dataProccessor.getStr(Constants.LATITUDE));
                                    long1 = Double.parseDouble(dataProccessor.getStr(Constants.LONGITUDE));
                                    lat2 = Double.parseDouble(profileClass.getUser_latitude());
                                    long2 = Double.parseDouble(profileClass.getUser_longitude());
                                } catch (Exception ex) {
                                    Log.d(TAG, "Error for input: " + ex.getMessage());
                                }

                                /* get the distance between current user and swipe cards */
                                double distance;
                                Location locationA = new Location("");
                                locationA.setLatitude(lat1);
                                locationA.setLongitude(long1);
                                Location locationB = new Location("");
                                locationB.setLatitude(lat2);
                                locationB.setLongitude(long2);
                                /* get the difference in km */
                                distance = locationA.distanceTo(locationB) / 1000;

                                int distanceBetween = Double.valueOf(distance).intValue();
                                int maxUserRange = 100;
                                try {
                                    maxUserRange = Integer.parseInt(dataProccessor.getStr(Constants.DISTANCE_MAXIMUM));
                                } catch (Exception e) {
                                    Log.d(TAG, "Error for input: " + e.getMessage());
                                }

//                                Log.d(TAG, "Available swipe with age: " + profileClass.getUser_country());

//                                Log.d(TAG, "Got the distance: "+ distanceBetween);
                                // check the distance set in settings against location
                                if (distanceBetween <= maxUserRange) {

                                    String donate = dataProccessor.getStr(Constants.DONATE);
                                    String donateHow = dataProccessor.getStr(Constants.DONATE_HOW);
                                    String lookingFor = dataProccessor.getStr(Constants.LOOKING_FOR);
                                    if (donate.equals("None") || donate.equals("")) {
//                                        Log.d(TAG, "Donate How None: " + profileClass.getName());
                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                    } else if (donate.equals("Ready To Donate")) {
                                        if (profileClass.getUser_donate().equals("Ready To Accept")) {
                                            switch (donateHow) {
                                                case "A+":
                                                    if (profileClass.getUser_donate_how().equals("A+") || profileClass.getUser_donate_how().equals("AB+")) {
                                                        Log.d(TAG, "Donate How 111: " + profileClass.getName());
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "A-":
                                                    if (profileClass.getUser_donate_how().equals("A-") || profileClass.getUser_donate_how().equals("A+")
                                                            || profileClass.getUser_donate_how().equals("AB-") || profileClass.getUser_donate_how().equals("AB+")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "B+":
                                                    if (profileClass.getUser_donate_how().equals("B+") || profileClass.getUser_donate_how().equals("AB+")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "B-":
                                                    if (profileClass.getUser_donate_how().equals("B-") || profileClass.getUser_donate_how().equals("B+")
                                                            || profileClass.getUser_donate_how().equals("AB-") || profileClass.getUser_donate_how().equals("AB+")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "O+":
                                                    if (profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("A+")
                                                            || profileClass.getUser_donate_how().equals("B+") || profileClass.getUser_donate_how().equals("AB+")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "O-":
                                                    if (profileClass.getUser_donate_how().equals("A+") || profileClass.getUser_donate_how().equals("A-")
                                                            || profileClass.getUser_donate_how().equals("B+") || profileClass.getUser_donate_how().equals("B-")
                                                            || profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("O-")
                                                            || profileClass.getUser_donate_how().equals("AB+") || profileClass.getUser_donate_how().equals("AB-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "AB+":
                                                    if (profileClass.getUser_donate_how().equals("AB+")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "AB-":
                                                    if (profileClass.getUser_donate_how().equals("AB-") || profileClass.getUser_donate_how().equals("A-")
                                                            || profileClass.getUser_donate_how().equals("B-") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Intestine":
                                                    if (profileClass.getUser_donate_how().equals("Intestine")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Kidney":
                                                    if (profileClass.getUser_donate_how().equals("Kidney")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Lung":
                                                    if (profileClass.getUser_donate_how().equals("Lung")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Liver":
                                                    if (profileClass.getUser_donate_how().equals("Liver")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Pancreas":
                                                    if (profileClass.getUser_donate_how().equals("Pancreas")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Plasma":
                                                    if (profileClass.getUser_donate_how().equals("Plasma")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Platelets":
                                                    if (profileClass.getUser_donate_how().equals("Platelets")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                            }
                                        }


                                    } else if (donate.equals("Ready To Accept")) {
                                        if (profileClass.getUser_donate().equals("Ready To Donate")) {
                                            switch (donateHow) {
                                                case "A+":
                                                    if (profileClass.getUser_donate_how().equals("A+") || profileClass.getUser_donate_how().equals("A-")
                                                            || profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("O-")) {
//                                                        Log.d(TAG, "Donate How 222: " + profileClass.getName());
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "A-":
                                                    if (profileClass.getUser_donate_how().equals("A-") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "B+":
                                                    if (profileClass.getUser_donate_how().equals("B+") || profileClass.getUser_donate_how().equals("B-")
                                                            || profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "B-":
                                                    if (profileClass.getUser_donate_how().equals("B-") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "O+":
                                                    if (profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "O-":
                                                    if (profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "AB+":
                                                    if (profileClass.getUser_donate_how().equals("A+") || profileClass.getUser_donate_how().equals("A-")
                                                            || profileClass.getUser_donate_how().equals("B+") || profileClass.getUser_donate_how().equals("B-")
                                                            || profileClass.getUser_donate_how().equals("O+") || profileClass.getUser_donate_how().equals("O-")
                                                            || profileClass.getUser_donate_how().equals("AB+") || profileClass.getUser_donate_how().equals("AB-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "AB-":
                                                    if (profileClass.getUser_donate_how().equals("AB-") || profileClass.getUser_donate_how().equals("A-")
                                                            || profileClass.getUser_donate_how().equals("B-") || profileClass.getUser_donate_how().equals("O-")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Intestine":
                                                    if (profileClass.getUser_donate_how().equals("Intestine")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Kidney":
                                                    if (profileClass.getUser_donate_how().equals("Kidney")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Lung":
                                                    if (profileClass.getUser_donate_how().equals("Lung")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Liver":
                                                    if (profileClass.getUser_donate_how().equals("Liver")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Pancreas":
                                                    if (profileClass.getUser_donate_how().equals("Pancreas")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Plasma":
                                                    if (profileClass.getUser_donate_how().equals("Plasma")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                                case "Platelets":
                                                    if (profileClass.getUser_donate_how().equals("Platelets")) {
                                                        loadData(lookingFor, profileClass, lookingPrefs, findSex);
                                                    }
                                                    break;
                                            }
                                        }

                                    }

                                }

                            }

                        }

                        SwipeAdapter adapter = new SwipeAdapter(arrayUserClass, getActivity());
                        cardStackView.setAdapter(adapter);
                        /* stop count down timer */
//                        Log.d(TAG, "count down stopped, data retrieved");
                        countDownTimer.cancel();

                        if (arrayUserClass.size() == 0) {
                            linearLayoutSwipeImageGroup.setVisibility(View.GONE);
                            linearLayoutSwipeEmptyGroup.setVisibility(View.VISIBLE);
                        } else {
                            linearLayoutSwipeImageGroup.setVisibility(View.VISIBLE);
                            linearLayoutSwipeEmptyGroup.setVisibility(View.GONE);
                        }
                        rippleSwipeAnimation.stopRippleAnimation();
                        rippleSwipeAnimation.setVisibility(View.GONE);

                    }
                });
    }

    private void loadData(String lookingFor, ProfileClass profileClass, String lookingPrefs, String findSex) {
        if (lookingFor.equals(profileClass.getLooking_for())) {
//                                        Log.d(TAG, "Looking For: " + profileClass.getName());
//                                    Log.d(TAG, "Available swipe with distance: " + profileClass.getUser_country());
//                                    Log.d(TAG, "Available swipe with distance: " + profileClass.getName());
            /* if gender is all then show all users except current user */
            switch (lookingPrefs) {
                case "All":
                    if (!arrayUserRemove.contains(profileClass.getUser_id())) {
                        arrayUserClass.add(profileClass);
                    }
                    break;
                case "Buyer":
                    if (!arrayUserRemove.contains(profileClass.getUser_id()) && findSex.equals("Buyer")) {
                        arrayUserClass.add(profileClass);
                    }
                    break;
                case "Seller":
                    if (!arrayUserRemove.contains(profileClass.getUser_id()) && findSex.equals("Seller")) {
                        arrayUserClass.add(profileClass);
                    }
                    break;
                case "Renter":
                    if (!arrayUserRemove.contains(profileClass.getUser_id()) && findSex.equals("Renter")) {
                        arrayUserClass.add(profileClass);
                    }
                    break;
                case "Shop":
                    if (!arrayUserRemove.contains(profileClass.getUser_id()) && findSex.equals("Shop")) {
                        arrayUserClass.add(profileClass);
                    }
                    break;
            }
        }
    }

    private void initUi() {
        if (mActivity == null) {
            return;
        }
        /* check for nexus count and update, update user verified to yes */
        updateNexusCount();

        /* load user image and use it for ripples while loading users */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot != null) {
                        String user_image = documentSnapshot.getString("z_image");

                        if (getContext() != null) {
                            Glide.with(getContext())
                                    .load(user_image) // Uri of the picture
                                    .placeholder(R.drawable.no_image)
                                    .into(imageRippleSwipeUser);
                        }
                        /* get user details from fire store */
                        String user_id = documentSnapshot.getString("user_id");
                        String name = documentSnapshot.getString("name");
                        String user_email = documentSnapshot.getString("email");
                        String user_company = documentSnapshot.getString("user_company");
                        String user_job = documentSnapshot.getString("user_job");
                        String user_education = documentSnapshot.getString("user_education");
                        String user_city = documentSnapshot.getString("user_city");
                        String user_website = documentSnapshot.getString("user_website");
                        String user_nexus = documentSnapshot.getString("user_nexus");
                        String user_sex = documentSnapshot.getString("sex");
                        String user_looking = documentSnapshot.getString("user_looking");
                        String date_of_birth = documentSnapshot.getString("date_of_birth");
                        String user_age = documentSnapshot.getString("user_age");
                        String user_username = documentSnapshot.getString("user_username");

                        String user_donate = documentSnapshot.getString("user_donate");
                        String user_fetch = documentSnapshot.getString("user_fetch");
                        String user_fetch_how = documentSnapshot.getString("fetch_how");
                        String user_donate_how = documentSnapshot.getString("user_donate_how");
                        String alert_ghost_mode = documentSnapshot.getString("alert_ghost_mode");
                        String alert_location_mode = documentSnapshot.getString("alert_location_mode");
                        String age_min = documentSnapshot.getString("age_min");
                        String age_max = documentSnapshot.getString("age_max");
                        String distance_min = documentSnapshot.getString("distance_min");
                        String distance_max = documentSnapshot.getString("distance_max");
                        String joined_date = documentSnapshot.getString("date_registration");
                        String user_latitude = documentSnapshot.getString("user_latitude");
                        String user_longitude = documentSnapshot.getString("user_longitude");
                        String looking_for = documentSnapshot.getString("looking_for");
                        String mobile_number = documentSnapshot.getString("user_phone");
                        String user_about = documentSnapshot.getString("user_about");


                        /* save user details to shared preference */
                        dataProccessor.setStr(Constants.USER_UID, user_id);
                        dataProccessor.setStr(Constants.NAME, name);
                        dataProccessor.setStr(Constants.EMAIL, user_email);
                        dataProccessor.setStr(Constants.SEX, user_sex);
                        dataProccessor.setStr(Constants.COMPANY, user_company);
                        dataProccessor.setStr(Constants.DATE_OF_BIRTH, date_of_birth);
                        dataProccessor.setStr(Constants.AGE, user_age);
                        dataProccessor.setStr(Constants.JOB, user_job);
                        dataProccessor.setStr(Constants.EDUCATION, user_education);
                        dataProccessor.setStr(Constants.IMAGE, user_image);
                        dataProccessor.setStr(Constants.LOOKING, user_looking);
                        dataProccessor.setStr(Constants.CITY, user_city);
                        dataProccessor.setStr(Constants.WEBSITE, user_website);
                        dataProccessor.setStr(Constants.NEXUS, user_nexus);
                        dataProccessor.setStr(Constants.USERNAME, user_username);

                        dataProccessor.setStr(Constants.DONATE, user_donate);
                        dataProccessor.setStr(Constants.FETCH, user_fetch);
                        dataProccessor.setStr(Constants.FETCH_HOW, user_fetch_how);
                        dataProccessor.setStr(Constants.DONATE_HOW, user_donate_how);
                        dataProccessor.setStr(Constants.GHOST_MODE, alert_ghost_mode);
                        dataProccessor.setStr(Constants.LOCATION_MODE, alert_location_mode);
                        dataProccessor.setStr(Constants.AGE_MINIMUM, age_min);
                        dataProccessor.setStr(Constants.AGE_MAXIMUM, age_max);
                        dataProccessor.setStr(Constants.DISTANCE_MINIMUM, distance_min);
                        dataProccessor.setStr(Constants.DISTANCE_MAXIMUM, distance_max);
                        dataProccessor.setStr(Constants.JOINED_DATE, joined_date);

                        dataProccessor.setStr(Constants.LATITUDE, user_latitude);
                        dataProccessor.setStr(Constants.LONGITUDE, user_longitude);
                        dataProccessor.setStr(Constants.LOOKING_FOR, looking_for);
                        dataProccessor.setStr(Constants.MOBILE_NUMBER, mobile_number);
                        dataProccessor.setStr(Constants.ABOUT, user_about);
//                        Log.d(TAG, "User Looking: "+user_looking);
                    }
                });

    }

    private void swipeUserMatch(String swipedUser) {

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.LIKE)
                .document(swipedUser)
                .get()
                .addOnSuccessListener(task -> {
                    Map<String, Object> mapMatchesSwipe = new HashMap<>();
                    mapMatchesSwipe.put("user_matched", Timestamp.now());
                    mapMatchesSwipe.put("user_matches", swipedUser);
                    mapMatchesSwipe.put("user_chat_start", "no");
                    mapMatchesSwipe.put("alert_done", "no");

                    firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                            .document(currentUser)
                            .collection(Constants.MATCH)
                            .document(swipedUser)
                            .set(mapMatchesSwipe)
                            .addOnSuccessListener(task1 -> {
                                Map<String, Object> mapMatchesCurrent = new HashMap<>();
                                mapMatchesCurrent.put("user_matched", Timestamp.now());
                                mapMatchesCurrent.put("user_matches", currentUser);
                                mapMatchesCurrent.put("user_chat_start", "no");
                                mapMatchesCurrent.put("alert_done", "no");

                                firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                        .document(swipedUser)
                                        .collection(Constants.MATCH)
                                        .document(currentUser)
                                        .set(mapMatchesCurrent)
                                        .addOnSuccessListener(task11 -> {
                                            /* show notification for new connection */
//                                                        Toast.makeText(getContext(), "New Connection!", Toast.LENGTH_SHORT).show();
                                            /* update nexus count for current user */
                                            updateNexusCount();

                                            /* check for nexus count and update for swiped user*/
                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                    .document(swipedUser)
                                                    .collection(Constants.MATCH)
                                                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                                        if (queryDocumentSnapshots != null) {
                                                            if (queryDocumentSnapshots.size() == 0) {
                                                                checkNexusCount = "0";
                                                            } else {
                                                                checkNexusCount = String.valueOf(queryDocumentSnapshots.size());
                                                            }
                                                            /* update nexus count in fire store */
                                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                    .document(swipedUser)
                                                                    .update("user_nexus", checkNexusCount)
                                                                    .addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            Log.d(TAG, "Nexus count updated for swiped user to: " + checkNexusCount);
                                                                        }
                                                                    });
                                                        }
                                                    });

                                        });
                            });
                });

    }

    private void updateNexusCount() {
        /* check for nexus count and update*/
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.MATCH)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() == 0) {
                            checkNexusCount = "0";
                        } else {
                            checkNexusCount = String.valueOf(queryDocumentSnapshots.size());
                        }

                        Map<String, Object> mapUpdate = new HashMap<>();
                        mapUpdate.put("user_nexus", checkNexusCount);
                        mapUpdate.put("user_verified", "yes");
                        /* update nexus count in fire store */
                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .update(mapUpdate)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Nexus count updated for current user to: " + checkNexusCount);
                                    }
                                });
                    }
                });
    }

    public void swipeUserLink() {
        final String swipedUser = arrayUserClass.get(intSwipePositionFirst).getUser_id();
//        final String swipedUserName = arrayUserClass.get(intSwipePositionFirst).getName();
//        final String swipedUserImage = arrayUserClass.get(intSwipePositionFirst).getZ_image();
        final String swipedUserSex = arrayUserClass.get(intSwipePositionFirst).getUser_sex();

        if (swipedUserSex != null && swipedUserSex.equals("Shop")) {
            Log.d(TAG, "createMatchForBothUser");
            createMatchForBothUser(swipedUser);
        } else {
            /* normal user swipe */
            Map<String, Object> mapLovesUser = new HashMap<>();
            mapLovesUser.put("user_links", swipedUser);
            mapLovesUser.put("user_linked", Timestamp.now());
            mapLovesUser.put("user_super", "no");

            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .collection(Constants.LINK)
                    .document(swipedUser)
                    .set(mapLovesUser)
                    .addOnSuccessListener(task -> {

                        Map<String, Object> mapLikesUser = new HashMap<>();
                        mapLikesUser.put("user_likes", currentUser);
                        mapLikesUser.put("user_liked", Timestamp.now());
                        mapLovesUser.put("user_super", "no");

                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(swipedUser)
                                .collection(Constants.LIKE)
                                .document(currentUser)
                                .set(mapLikesUser)
                                .addOnSuccessListener(task1 -> {
                                    swipeUserMatch(swipedUser);

                                });

                    });
        }


    }

    private void createMatchForBothUser(String swipedUser) {
        Map<String, Object> mapLinkSwipeUser = new HashMap<>();
        mapLinkSwipeUser.put("user_links", swipedUser);
        mapLinkSwipeUser.put("user_linked", Timestamp.now());
        mapLinkSwipeUser.put("user_super", "no");

        Map<String, Object> mapLinkCurrentUser = new HashMap<>();
        mapLinkCurrentUser.put("user_links", currentUser);
        mapLinkCurrentUser.put("user_linked", Timestamp.now());
        mapLinkCurrentUser.put("user_super", "no");

        Map<String, Object> mapLikeSwipeUser = new HashMap<>();
        mapLikeSwipeUser.put("user_likes", swipedUser);
        mapLikeSwipeUser.put("user_liked", Timestamp.now());
        mapLikeSwipeUser.put("user_super", "no");

        Map<String, Object> mapLikeCurrentUser = new HashMap<>();
        mapLikeCurrentUser.put("user_likes", currentUser);
        mapLikeCurrentUser.put("user_liked", Timestamp.now());
        mapLikeCurrentUser.put("user_super", "no");

        Map<String, Object> mapMatchSwipeUser = new HashMap<>();
        mapMatchSwipeUser.put("user_matched", Timestamp.now());
        mapMatchSwipeUser.put("user_matches", swipedUser);
        mapMatchSwipeUser.put("user_chat_start", "no");
        mapMatchSwipeUser.put("alert_done", "no");

        Map<String, Object> mapMatchCurrentUser = new HashMap<>();
        mapMatchCurrentUser.put("user_matched", Timestamp.now());
        mapMatchCurrentUser.put("user_matches", currentUser);
        mapMatchCurrentUser.put("user_chat_start", "no");
        mapMatchCurrentUser.put("alert_done", "no");

        /* create like, link and match for current user */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.LINK)
                .document(swipedUser)
                .set(mapLinkSwipeUser)
                .addOnSuccessListener(task ->
                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(currentUser)
                                .collection(Constants.LIKE)
                                .document(swipedUser)
                                .set(mapLikeSwipeUser)
                                .addOnSuccessListener(task1 ->
                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(currentUser)
                                                .collection(Constants.MATCH)
                                                .document(swipedUser)
                                                .set(mapMatchSwipeUser)
                                                .addOnSuccessListener(task2 -> {
                                                            /* check for nexus count and update for current user*/
                                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                    .document(currentUser)
                                                                    .collection(Constants.MATCH)
                                                                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                                                        if (queryDocumentSnapshots != null) {
                                                                            if (queryDocumentSnapshots.size() == 0) {
                                                                                checkNexusCount = "0";
                                                                            } else {
                                                                                checkNexusCount = String.valueOf(queryDocumentSnapshots.size());
                                                                            }
                                                                            /* update nexus count in fire store */
                                                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                    .document(currentUser)
                                                                                    .update("user_nexus", checkNexusCount)
                                                                                    .addOnSuccessListener(task3 ->
                                                                                            Log.d(TAG, "Nexus count updated for current user to: " + checkNexusCount));
                                                                        }
                                                                    });

                                                        }
                                                )
                                )
                );

        /* create like, link and match for Shop */
        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(swipedUser)
                .collection(Constants.LINK)
                .document(currentUser)
                .set(mapLinkCurrentUser)
                .addOnSuccessListener(task ->
                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                .document(swipedUser)
                                .collection(Constants.LIKE)
                                .document(currentUser)
                                .set(mapLikeCurrentUser)
                                .addOnSuccessListener(task1 ->
                                        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                .document(swipedUser)
                                                .collection(Constants.MATCH)
                                                .document(currentUser)
                                                .set(mapMatchCurrentUser)
                                                .addOnSuccessListener(task2 -> {
                                                            /* check for nexus count and update for current user*/
                                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                    .document(swipedUser)
                                                                    .collection(Constants.MATCH)
                                                                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                                                        if (queryDocumentSnapshots != null) {
                                                                            if (queryDocumentSnapshots.size() == 0) {
                                                                                checkNexusCount = "0";
                                                                            } else {
                                                                                checkNexusCount = String.valueOf(queryDocumentSnapshots.size());
                                                                            }
                                                                            /* update nexus count in fire store */
                                                                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                                                                    .document(swipedUser)
                                                                                    .update("user_nexus", checkNexusCount)
                                                                                    .addOnSuccessListener(task3 ->
                                                                                            Log.d(TAG, "Nexus count updated for swipe user to: " + checkNexusCount));
                                                                        }
                                                                    });

                                                        }
                                                )
                                )
                );

    }

    public void swipeUserLater() {
        final String swipedUser = arrayUserClass.get(intSwipePositionFirst).getUser_id();

        Map<String, Object> mapNopesUser = new HashMap<>();
        mapNopesUser.put("user_nopes", swipedUser);
        mapNopesUser.put("user_noped", Timestamp.now());

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .collection(Constants.LATER)
                .document(swipedUser)
                .set(mapNopesUser);
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
    }

    @Override
    public void onCardSwiped(Direction direction) {

        if (direction == Direction.Right) {
            swipeUserLink();
        } else if (direction == Direction.Left) {
            swipeUserLater();
        }

    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {
        intSwipePositionFirst = position;
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        intSwipePositionLast = position;
    }

    private void OnlineUser() {
        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .update(arrayOnlineUser);
    }

    private void userStatus(String status) {
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
    public void onResume() {
        super.onResume();
        userStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }
        /* reload fragment */
        if (allowRefresh) {
            allowRefresh = false;
//            Log.d(TAG, "onResume called");
            mCallbacks.onCardFragmentResume();
        }
        /* refresh on tab selected */
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(myReceiver,
                new IntentFilter("REFRESH_CARD_FRAGMENT"));

    }

    @Override
    public void onPause() {
        super.onPause();
        userStatus("paused");
        Locart.appRunning = false;
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(myReceiver);
        /* refresh set to true */
        if (!allowRefresh)
//            Log.d(TAG, "onPause called");
            allowRefresh = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userStatus("offline");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        /* Activity containing this fragment must implement its callbacks */
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public interface Callbacks {
        /* Callback for when fragment resumes. */
        void onCardFragmentResume();
    }

    /* count down inner timer class */
    public class OneMinuteCountDownTimer extends CountDownTimer {

        public OneMinuteCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
//            Log.d(TAG, "count down finished, no data retrieved");
            if (arrayUserClass.size() == 0) {
                linearLayoutSwipeImageGroup.setVisibility(View.GONE);
                linearLayoutSwipeEmptyGroup.setVisibility(View.VISIBLE);
                rippleSwipeAnimation.stopRippleAnimation();
                rippleSwipeAnimation.setVisibility(View.GONE);
            } else {
                linearLayoutSwipeImageGroup.setVisibility(View.VISIBLE);
                linearLayoutSwipeEmptyGroup.setVisibility(View.GONE);
                rippleSwipeAnimation.stopRippleAnimation();
                rippleSwipeAnimation.setVisibility(View.GONE);
            }

        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            CardFragment.this.refresh();
        }

    }

    public void refresh() {
        /* your code in refresh. */
//        Log.d(TAG, "YES");
        mCallbacks.onCardFragmentResume();
    }
}
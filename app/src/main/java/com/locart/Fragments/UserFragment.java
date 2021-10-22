package com.locart.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.locart.Locart;
import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.locart.Activity.EditProfileActivity;

import com.locart.R;
import com.locart.Activity.SettingsActivity;

/**
 * Activity responsible for displaying the current user and the buttons to go
 * to the settingsActivity and EditProfileActivity
 */
public class UserFragment extends Fragment {

    private TextView mName, mJob, mEducation, mCity, mWebsite,
            mAbout, mSettings, mEditProfile, mNexus, mDonate, mLookingFor, mUsername;

    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String name, about, company, job, education, city, website, nexus, image, userId, dateOfBirth,
            username, donate, donateHow, lookingFor;
    DataProccessor dataProccessor;
    View view;
    private boolean allowRefresh = false;
    private Activity mActivity;
    private CallbackUser callbackUser;
    FrameLayout userLayout;
    LinearLayout linearLayoutShareProfile;


    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_user, container, false);

        initializeObjects();

        return view;
    }

    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects() {
        mName = view.findViewById(R.id.name);
        mUsername = view.findViewById(R.id.username);
        mJob = view.findViewById(R.id.job);
        mEducation = view.findViewById(R.id.education);
        mCity = view.findViewById(R.id.city);
        mWebsite = view.findViewById(R.id.website);
        mAbout = view.findViewById(R.id.about);
        mNexus = view.findViewById(R.id.nexus);
        mProfileImage = view.findViewById(R.id.profileImage);
        mSettings = view.findViewById(R.id.tvSettings);
        mEditProfile = view.findViewById(R.id.tvEditProfile);
        mDonate = view.findViewById(R.id.donate);
        userLayout = view.findViewById(R.id.card_layout);
        linearLayoutShareProfile = view.findViewById(R.id.linearLayoutShareProfile);
        mLookingFor = view.findViewById(R.id.looking_for);

        dataProccessor = new DataProccessor(getActivity());
        Linkify.addLinks(mAbout, Linkify.ALL);
        mAbout.setMovementMethod(LinkMovementMethod.getInstance());
        mAbout.setText(Html.fromHtml(String.valueOf(mAbout)));

        mEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        mSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });
        linearLayoutShareProfile.setOnClickListener(v -> {
            //logic to share user profile
            String username = dataProccessor.getStr(Constants.USERNAME);
            if (!username.equals("")){
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Locart.me/"+username);
                startActivity(sendIntent);
            }else {
                Toast.makeText(getActivity(), "Username is not set", Toast.LENGTH_SHORT).show();
            }

        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        /* check for nexus count*/
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser.getUid())
                .collection(Constants.MATCH)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null) {
                        if (queryDocumentSnapshots.size() == 0) {
//                            Log.d(TAG, " Size: " + String.valueOf(queryDocumentSnapshots.size()));
                            mNexus.setVisibility(View.GONE);
                        } else {

//                            Log.d(TAG, " Size 2: " + String.valueOf(queryDocumentSnapshots.size()));
                            String nexusCount = String.valueOf(queryDocumentSnapshots.size());
                            mNexus.setText(String.format("Nexus: %s", nexusCount));
                        }
                    }
                });
    }

    /**
     * Fetches current user's info from the database
     */
    private void getUserInfo() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        /* getting unique user id */
        final String current = user.getUid();


        db.collection(Constants.FIRE_STORE_COLLECTION)
                .whereEqualTo("user_id", current)/* looks for the corresponding value with the field in the database */
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                            String user_about = document.getString("user_about");
                            String name = document.getString("name");
                            String user_company = document.getString("user_company");
                            String user_job = document.getString("user_job");
                            String user_education = document.getString("user_education");
                            String user_city = document.getString("user_city");
                            String user_website = document.getString("user_website");
                            String user_nexus = document.getString("user_nexus");

                            if (name != null && !name.isEmpty()) {
                                mName.setText(name);
                            } else {
                                mName.setVisibility(View.GONE);
                            }
                            if (user_about != null && !user_about.isEmpty()) {
                                mAbout.setText(user_about);
                            } else {
                                mAbout.setVisibility(View.GONE);
                            }


                            if (user_job != null && !user_job.isEmpty() && !user_company.isEmpty()) {
                                mJob.setText(String.format("%s at %s", user_job, user_company));
                            } else if (Objects.requireNonNull(user_job).isEmpty() && !user_company.isEmpty()){
                                mJob.setText(user_company);
                            } else if (!user_job.isEmpty() && user_company.isEmpty()){
                                mJob.setText(user_job);
                            }else {
                                mJob.setVisibility(View.GONE);
                            }

                            if (user_education != null && !user_education.isEmpty()) {
                                mEducation.setText(user_education);
                            } else {
                                mEducation.setVisibility(View.GONE);
                            }
                            if (user_city != null && !user_city.isEmpty()) {
                                mCity.setText(user_city);
                            } else {
                                mCity.setVisibility(View.GONE);
                            }
                            if (user_website != null && !user_website.isEmpty()) {
                                mCity.setText(user_website);
                            } else {
                                mCity.setVisibility(View.GONE);
                            }
                            if (user_nexus != null && !user_nexus.isEmpty()) {
                                mNexus.setText(new StringBuilder().append(user_nexus).append(" Nexus").toString());
                            } else {
                                mNexus.setVisibility(View.GONE);
                            }

                            /* get image string and decode with base64 */
                            String imageBytes = String.valueOf(document.get("z_image"));
                            byte[] imageByteArray = Base64.decode(imageBytes, Base64.DEFAULT);

                            Glide.with(getActivity())
                                    .load(imageByteArray)
                                    .placeholder(R.drawable.no_image)
                                    .into(mProfileImage);

                        }
                    }
                });

    }

    private void getUserInfoFromPrefs() {
        /* get user details from shared preference */
        userId = dataProccessor.getStr(Constants.USER_UID);
        name = dataProccessor.getStr(Constants.NAME);
        nexus = dataProccessor.getStr(Constants.NEXUS);
        about = dataProccessor.getStr(Constants.ABOUT);
        company = dataProccessor.getStr(Constants.COMPANY);
        dateOfBirth = dataProccessor.getStr(Constants.DATE_OF_BIRTH);
        username = dataProccessor.getStr(Constants.USERNAME);
        job = dataProccessor.getStr(Constants.JOB);
        education = dataProccessor.getStr(Constants.EDUCATION);
        image = dataProccessor.getStr(Constants.IMAGE);
        city = dataProccessor.getStr(Constants.CITY);
        website = dataProccessor.getStr(Constants.WEBSITE);
        donate = dataProccessor.getStr(Constants.DONATE);
        donateHow = dataProccessor.getStr(Constants.DONATE_HOW);
        lookingFor = dataProccessor.getStr(Constants.LOOKING_FOR);

        if (username != null && !username.isEmpty()) {
            mName.setText(name);
            mUsername.setText(String.format(" @%s", username));
        } else {
            mName.setText(name);
        }
//        if (nexus != null && !nexus.isEmpty()) {
//            mNexus.setText(String.format("Nexus: %s", nexus));
//        } else {
//            mNexus.setVisibility(View.GONE);
//        }
        if (about != null && !about.isEmpty()) {
            mAbout.setText(about);
        } else {
            mAbout.setVisibility(View.GONE);
        }

        if (job != null && !job.isEmpty() && !company.isEmpty()) {
            mJob.setText(String.format("%s at %s", job, company));
        } else if (Objects.requireNonNull(job).isEmpty() && !company.isEmpty()){
            mJob.setText(company);
        } else if (!job.isEmpty() && company.isEmpty()){
            mJob.setText(job);
        }else {
            mJob.setVisibility(View.GONE);
        }
        if (education != null && !education.isEmpty()) {
            mEducation.setText(education);
        } else {
            mEducation.setVisibility(View.GONE);
        }
        if (city != null && !city.isEmpty()) {
            mCity.setText(city);
        } else {
            mCity.setVisibility(View.GONE);
        }
        if (website != null && !website.isEmpty()) {
            mWebsite.setText(website);
        } else {
            mWebsite.setVisibility(View.GONE);
        }

        if (lookingFor != null && !lookingFor.equals("None")) {
            mLookingFor.setText(String.format("Looking For: %s", lookingFor));
        } else {
            mLookingFor.setVisibility(View.GONE);
        }

        if (donateHow != null && !donateHow.isEmpty() && !donate.equals("None")) {
            mDonate.setText(String.format("%s: %s", donate, donateHow));
        } else {
            mDonate.setVisibility(View.GONE);
        }

        /* get image url and display */
        Glide.with(getActivity())
                .load(image)
                .placeholder(R.drawable.no_image)
                .into(mProfileImage);
    }

    @Override
    public void onStart() {
        super.onStart();
//        getUserInfo();
        getUserInfoFromPrefs();
    }

    private void OnlineUser() {
        String currentUserId = currentUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserId)
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
    public void onResume() {
        super.onResume();
        UserStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }
        /* reload fragment */
        if (allowRefresh) {
            allowRefresh = false;
            callbackUser.onUserFragmentResume();
        }
        getUserInfoFromPrefs();
    }

    @Override
    public void onPause() {
        super.onPause();
        UserStatus("paused");
        Locart.appRunning = false;
        /* refresh set to true */
        if (!allowRefresh)
            allowRefresh = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserStatus("offline");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        /* Activity containing this fragment must implement its callbacks */
        callbackUser = (CallbackUser) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public interface CallbackUser {
        /* Callback for when fragment resumes. */
        void onUserFragmentResume();
    }

}
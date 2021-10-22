package com.locart.Activity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.locart.Locart;
import com.locart.Login.ForgotPasswordActivity;
import com.locart.Login.WelcomeActivity;
import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.bumptech.glide.Glide;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.locart.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that control the search settings of the user:
 * -Search interest
 * -Search Distance
 * <p>
 * Also has option to log out the user
 */
public class SettingsActivity extends AppCompatActivity {

    RadioGroup mRadioGroup, mRadioGroupDonate;

    private TextView mChangePasswordButton, mUpdateEmailButton,
            mDeleteButton, mLogOut, mInformationButton, mShowEmail, mShowName,
            mAgeRange, mDistanceRange, mJoinedDated, mFetching;
    private CircularImageView mUserImage;
    private ImageView mSubscriptionImage;
    private EditText etMobileNumber;
    CountryCodePicker countryCodePicker;

    private FirebaseUser currentUser;
    private String stringLooking = "", mPhone, stringDonate = "", stringFetch = "None",
            stringDonateHow = "", userId, stringGhostMode = "", stringLocationMode = "",
            ageFrom = "", ageTo = "", distanceFrom = "0", distanceTo = "100", stringLookingFor = "",
            stringPhone = "";

    private FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    DataProccessor dataProccessor;
    private Spinner spinner, spinnerLookingFor;
    ToggleButton toggleGhostMode, toggleLocationMode;
    RangeSlider sliderSetAge;
    Slider sliderSearchDistance;
    private ProgressBar progressBar;
    private static final String TAG = "SettingsActivity";
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUi();
    }

    private void initUi() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRadioGroup = findViewById(R.id.radio);
        mSubscriptionImage = findViewById(R.id.subscriptionButton);
        mChangePasswordButton = findViewById(R.id.changePasswordButton);
        mUpdateEmailButton = findViewById(R.id.updateEmailButton);
        mDeleteButton = findViewById(R.id.deleteButton);
        mLogOut = findViewById(R.id.logOut);
        mInformationButton = findViewById(R.id.informationButton);
        mRadioGroupDonate = findViewById(R.id.radioDonation);
        toggleGhostMode = findViewById(R.id.toggleGhostMode);
        toggleLocationMode = findViewById(R.id.toggleLocationMode);
        sliderSetAge = findViewById(R.id.fluidSliderAge);
        sliderSearchDistance = findViewById(R.id.fluidSliderDistance);
        progressBar = findViewById(R.id.progressBar);
        mAgeRange = findViewById(R.id.ageRange);
        mDistanceRange = findViewById(R.id.distanceRange);

        mShowEmail = findViewById(R.id.showEmail);
        mShowName = findViewById(R.id.showName);
        spinner = findViewById(R.id.spinner);
        spinnerLookingFor = findViewById(R.id.spinner_looking);
        mUserImage = findViewById(R.id.userImage);
        mJoinedDated = findViewById(R.id.joinedDate);
        mFetching = findViewById(R.id.fetching);
        etMobileNumber = findViewById(R.id.edit_text_mobile_number);
        countryCodePicker = findViewById(R.id.country_code_picker);

        context = this;

        stringDonateHow = "";
        spinner.setVisibility(View.GONE);

        dataProccessor = new DataProccessor(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        mChangePasswordButton.setOnClickListener(v -> {
            /* go to forgot password activity */
            Intent forgotPassword = new Intent(SettingsActivity.this, ForgotPasswordActivity.class);
            forgotPassword.putExtra("forgot_pass", "settings");
            startActivity(forgotPassword);

        });

        mUpdateEmailButton.setOnClickListener(view -> {
            Intent updateEmail = new Intent(SettingsActivity.this, UpdateEmailActivity.class);
            startActivity(updateEmail);

        });

        mDeleteButton.setOnClickListener(v -> {
            Intent deleteAccount = new Intent(SettingsActivity.this, DeleteAccountActivity.class);
            startActivity(deleteAccount);
        });

        mLogOut.setOnClickListener(v -> logOut(context));

        mInformationButton.setOnClickListener(v -> {
            Intent information = new Intent(SettingsActivity.this, InformationActivity.class);
            startActivity(information);
        });

        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.Buyer_radio:
                    stringLooking = "Buyer";
                    break;
                case R.id.Seller_radio:
                    stringLooking = "Seller";
                    break;
                case R.id.Renter_radio:
                    stringLooking = "Renter";
                    break;
                case R.id.Shop_radio:
                    stringLooking = "Shop";
                    break;
                case R.id.all_radio:
                    stringLooking = "All";
                    break;
                default:
                    break;
            }
        });

        mRadioGroupDonate.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_none:
                    stringDonate = "None";
                    spinner.setVisibility(View.GONE);
                    stringDonateHow = "None";
                    stringFetch = "None";
                    mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                    break;
                case R.id.radio_donate:
                    stringDonate = "Ready To Donate";
                    spinner.setVisibility(View.VISIBLE);
                    spinner.setSelection(0);
                    stringFetch = "Ready to accept A+, AB+";
                    mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                    break;
                case R.id.radio_accept:
                    stringDonate = "Ready To Accept";
                    spinner.setVisibility(View.VISIBLE);
                    spinner.setSelection(0);
                    stringFetch = "Ready to donate A+, A-, O+, O-";
                    mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                    break;

                default:
                    stringDonate = "None";
                    break;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                stringDonateHow = spinner.getSelectedItem().toString();

                if (stringDonate.equals("Ready To Donate")) {
                    switch (stringDonateHow) {
                        case "A+":
                            stringFetch = "Ready to accept A+, AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "A-":
                            stringFetch = "Ready to accept A-, A+, AB-, AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "B+":
                            stringFetch = "Ready to accept B+, AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "B-":
                            stringFetch = "Ready to accept B-, B+, AB-, AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "O+":
                            stringFetch = "Ready to accept O+, A+, B+, AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "O-":
                            stringFetch = "Ready to accept A+, A-, B+, B-, O+, O-, AB+, AB-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "AB+":
                            stringFetch = "Ready to accept AB+";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "AB-":
                            stringFetch = "Ready to accept AB-, A-, B-, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Intestine":
                            stringFetch = "Ready to accept Intestine";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Kidney":
                            stringFetch = "Ready to accept Kidney";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Lung":
                            stringFetch = "Ready to accept Lung";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Liver":
                            stringFetch = "Ready to accept Liver";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Pancreas":
                            stringFetch = "Ready to accept Pancreas";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Plasma":
                            stringFetch = "Ready to accept Plasma";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Platelets":
                            stringFetch = "Ready to accept Platelets";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                    }
                } else if (stringDonate.equals("Ready To Accept")) {
                    switch (stringDonateHow) {
                        case "A+":
                            stringFetch = "Ready to donate A+, A-, O+, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "A-":
                            stringFetch = "Ready to donate A-, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "B+":
                            stringFetch = "Ready to donate B+, B-, O+, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "B-":
                            stringFetch = "Ready to donate B-, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "O+":
                            stringFetch = "Ready to donate O+, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "O-":
                            stringFetch = "Ready to donate O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "AB+":
                            stringFetch = "Ready to donate A+, A-, B+, B-, O+, O-, AB+, AB-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "AB-":
                            stringFetch = "Ready to donate AB-, A-, B-, O-";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Intestine":
                            stringFetch = "Ready to donate Intestine";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Kidney":
                            stringFetch = "Ready to donate Kidney";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Lung":
                            stringFetch = "Ready to donate Lung";
                            break;
                        case "Liver":
                            stringFetch = "Ready to donate Liver";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Pancreas":
                            stringFetch = "Ready to donate Pancreas";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Plasma":
                            stringFetch = "Ready to donate Plasma";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                        case "Platelets":
                            stringFetch = "Ready to donate Platelets";
                            mFetching.setText(String.format("%s %s", getString(R.string.fetching), stringFetch));
                            break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        spinnerLookingFor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                stringLookingFor = spinnerLookingFor.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        toggleGhostMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                /* The toggle is enabled
                the background resource which mean status is enable
                */
                stringGhostMode = "yes";
            } else {
                /* The toggle is disabled
                the background resource which mean status is disabled
                */
                stringGhostMode = "no";
            }
        });

        toggleLocationMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                /* The toggle is enabled
                the background resource which mean status is enable
                */
                stringLocationMode = "yes";

            } else {
                /* The toggle is disabled
                the background resource which mean status is disabled
                */
                stringLocationMode = "no";
            }
        });

        sliderSetAge.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                /* get the minimum and maximum value set on the slider */
                List<Float> values = slider.getValues();
//                Log.d("Settings", "Age From: " + values.get(0));
//                Log.d("Settings", "Age T0: " + values.get(1));
                ageFrom = String.valueOf(values.get(0)).split("\\.")[0];
                ageTo = String.valueOf(values.get(1)).split("\\.")[0];

                mAgeRange.setText(String.format("%s - %s", ageFrom, ageTo));
            }
        });

        sliderSearchDistance.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                /* get the minimum and maximum value set on the slider */
                Float values = slider.getValue();
                Log.d("Settings", "Distance From: " + values);
//                Log.d("Settings", "Distance T0: " + values.get(1));
                distanceTo = String.valueOf(values).split("\\.")[0];
                mDistanceRange.setText(String.format("%s - %skm", distanceFrom, distanceTo));
            }
        });

    }

    /**
     * Manually delete user data in fire store
     * This method is no longer used because we already installed an extension
     * to do the work for us
     */
    private void deleteAllData() {
        /* delete all data with this user in fireStore */
        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(userId)
                .delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    /**
     * Fetch user from preference and populates elements
     */
    private void getUserInfoFromPrefs() {
        String user_email = dataProccessor.getStr(Constants.EMAIL);
        String user_name = dataProccessor.getStr(Constants.NAME);
        String user_username = dataProccessor.getStr(Constants.USERNAME);
        String interest = dataProccessor.getStr(Constants.LOOKING);
        String user_donate = dataProccessor.getStr(Constants.DONATE);
        String user_donate_how = dataProccessor.getStr(Constants.DONATE_HOW);
        String alert_ghost_mode = dataProccessor.getStr(Constants.GHOST_MODE);
        String alert_location_mode = dataProccessor.getStr(Constants.LOCATION_MODE);
        String age_min = dataProccessor.getStr(Constants.AGE_MINIMUM);
        String age_max = dataProccessor.getStr(Constants.AGE_MAXIMUM);
        String distance_min = dataProccessor.getStr(Constants.DISTANCE_MINIMUM);
        String distance_max = dataProccessor.getStr(Constants.DISTANCE_MAXIMUM);
        String joined_date = dataProccessor.getStr(Constants.JOINED_DATE);
        String looking_for = dataProccessor.getStr(Constants.LOOKING_FOR);
        String user_fetching = dataProccessor.getStr(Constants.FETCH);
        String mobile_number = dataProccessor.getStr(Constants.MOBILE_NUMBER);

        if (!mobile_number.equals("")){
            String[] splitStr = mobile_number.split("\\s+");

            etMobileNumber.setText(splitStr[1]);
        }


        mShowEmail.setText(user_email);
        mJoinedDated.setText(String.format("Joined %s", joined_date));
        assert user_fetching != null;
        mFetching.setText(String.format("%s %s", getString(R.string.fetching), user_fetching));


        try {
            /* set the text to retrieved values */
            mAgeRange.setText(String.format("%s - %s", age_min, age_max));
            mDistanceRange.setText(String.format("%s - %s km", distance_min, distance_max));

            /* set the range slider and normal slider to retrieved values */
            sliderSetAge.setValues(convertStringToFloat(age_min), convertStringToFloat(age_max));
            sliderSearchDistance.setValue(convertStringToFloat(distance_max));
        } catch (Exception e) {
            /* set the text to default values */
            mAgeRange.setText(String.format("%s - %s", "18", "30"));
            mDistanceRange.setText(String.format("%s - %s km", "0", "100"));
            /* set the range slider and normal slider to default values */
            sliderSetAge.setValues(convertStringToFloat("18"), convertStringToFloat("30"));
            sliderSearchDistance.setValue(convertStringToFloat("100"));
            Log.d(TAG, "slider error: " + e.getMessage());

        }

        /* For image */
        String user_image = dataProccessor.getStr(Constants.IMAGE);
        assert user_image != null;
        if (user_image.equals("")) {
            mUserImage.setImageResource(R.drawable.no_image);
        } else {
            /* get image url and display */
            Glide.with(getApplicationContext())
                    .load(user_image)
                    .placeholder(R.drawable.no_image)
                    .into(mUserImage);
        }

        assert user_username != null;
        if (user_username.equals("")) {
            mShowName.setText(user_name);
        } else {
            mShowName.setText(String.format("%s @%s", user_name, user_username));
        }

        if (interest == null) {
            ((RadioButton) findViewById(R.id.Shop_radio)).setChecked(true);
        }
        assert interest != null;
        switch (interest) {
            case "Buyer":
                ((RadioButton) findViewById(R.id.Buyer_radio)).setChecked(true);
                stringLooking = "Buyer";
                break;
            case "Seller":
                ((RadioButton) findViewById(R.id.Seller_radio)).setChecked(true);
                stringLooking = "Seller";
                break;
            case "Renter":
                ((RadioButton) findViewById(R.id.Renter_radio)).setChecked(true);
                stringLooking = "Renter";
                break;
            case "Shop":
                ((RadioButton) findViewById(R.id.Shop_radio)).setChecked(true);
                stringLooking = "Shop";
                break;
            case "All":
                ((RadioButton) findViewById(R.id.all_radio)).setChecked(true);
                stringLooking = "All";
                break;
        }

        if (user_donate == null) {
            ((RadioButton) findViewById(R.id.radio_none)).setChecked(true);
        }
        assert user_donate != null;
        switch (user_donate) {
            case "None":
                ((RadioButton) findViewById(R.id.radio_none)).setChecked(true);
                break;
            case "Ready To Donate":
                ((RadioButton) findViewById(R.id.radio_donate)).setChecked(true);
                break;
            case "Ready To Accept":
                ((RadioButton) findViewById(R.id.radio_accept)).setChecked(true);
                break;
        }

        assert user_donate_how != null;
        switch (user_donate_how) {
            case "A+":
                spinner.setSelection(0);
                break;
            case "A-":
                spinner.setSelection(1);
                break;
            case "B+":
                spinner.setSelection(2);
                break;
            case "B-":
                spinner.setSelection(3);
                break;
            case "O+":
                spinner.setSelection(4);
                break;
            case "O-":
                spinner.setSelection(5);
                break;
            case "AB+":
                spinner.setSelection(6);
                break;
            case "AB-":
                spinner.setSelection(7);
                break;
            case "Intestine":
                spinner.setSelection(8);
                break;
            case "Kidney":
                spinner.setSelection(9);
                break;
            case "Lung":
                spinner.setSelection(10);
                break;
            case "Liver":
                spinner.setSelection(11);
                break;
            case "Pancreas":
                spinner.setSelection(12);
                break;
            case "Plasma":
                spinner.setSelection(13);
                break;
            case "Platelets":
                spinner.setSelection(14);
                break;
        }

        assert looking_for != null;
        switch (looking_for) {
            case "Avid Foodie":
                spinnerLookingFor.setSelection(1);
                break;
            case "Avid Traveler":
                spinnerLookingFor.setSelection(2);
                break;
            case "Buyer":
                spinnerLookingFor.setSelection(3);
                break;
            case "Dating":
                spinnerLookingFor.setSelection(4);
                break;
            case "Employees":
                spinnerLookingFor.setSelection(5);
                break;
            case "Fitness Partner":
                spinnerLookingFor.setSelection(6);
                break;
            case "Globetrotter":
                spinnerLookingFor.setSelection(7);
                break;
            case "Guide":
                spinnerLookingFor.setSelection(8);
                break;
            case "Hangouts":
                spinnerLookingFor.setSelection(9);
                break;
            case "Hook-up":
                spinnerLookingFor.setSelection(10);
                break;
            case "Investment":
                spinnerLookingFor.setSelection(11);
                break;
            case "Investors":
                spinnerLookingFor.setSelection(12);
                break;
            case "Jobs":
                spinnerLookingFor.setSelection(13);
                break;
            case "Marriage":
                spinnerLookingFor.setSelection(14);
                break;
            case "Relationship":
                spinnerLookingFor.setSelection(15);
                break;
            case "None":
            default:
                spinnerLookingFor.setSelection(0);
        }


        if (alert_ghost_mode == null) {
            toggleGhostMode.setChecked(false);
        }
        assert alert_ghost_mode != null;
        switch (alert_ghost_mode) {
            case "yes":
                toggleGhostMode.setChecked(true);
                stringGhostMode = "yes";
                break;
            case "no":
                toggleGhostMode.setChecked(false);
                stringGhostMode = "no";
                break;
            default:
                break;
        }

        if (alert_location_mode == null) {
            toggleLocationMode.setChecked(true);
        }
        assert alert_location_mode != null;
        switch (alert_location_mode) {
            case "yes":
                toggleLocationMode.setChecked(true);
                stringLocationMode = "yes";
                break;
            case "no":
                toggleLocationMode.setChecked(false);
                stringLocationMode = "no";
                break;
        }

    }

    /**
     * Saves user search settings to the database
     */
    private void saveUserInformation() {
        progressBar.setVisibility(View.VISIBLE);

        if (!etMobileNumber.getText().toString().equals("")){
            stringPhone = "+" + countryCodePicker.getSelectedCountryCode() + " " + etMobileNumber.getText().toString();
        } else {
            stringPhone = "";
        }


        /* check to see if sliders are not empty else save details */
        if (ageFrom.equals("") && ageTo.equals("")) {
            emptySlidersAge();
        } else if (distanceTo.equals("")) {
            emptySlidersDistance();
        } else {
//            Log.d(TAG, "save with details called");
            getSlidersDetails();
        }

    }

    /* Don't save slider details since nothing changed for age only */
    private void emptySlidersAge() {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put(Constants.USER_LOOKING, stringLooking);
        userProfile.put(Constants.USER_DONATE, stringDonate);
        userProfile.put(Constants.USER_FETCH, stringFetch);
        userProfile.put(Constants.USER_DONATE_HOW, stringDonateHow);
        userProfile.put(Constants.ALERT_GHOST_MODE, stringGhostMode);
        userProfile.put(Constants.ALERT_LOCATION_MODE, stringLocationMode);
        userProfile.put(Constants.AGE_MIN, dataProccessor.getStr(Constants.AGE_MINIMUM));
        userProfile.put(Constants.AGE_MAX, dataProccessor.getStr(Constants.AGE_MAXIMUM));
        userProfile.put(Constants.DISTANCE_MIN, distanceFrom);
        userProfile.put(Constants.DISTANCE_MAX, distanceTo);
        userProfile.put(Constants.USER_LOOKING_FOR, stringLookingFor);
        userProfile.put(Constants.USER_PHONE, stringPhone);

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(userId)
                .update(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);

                        dataProccessor.setStr(Constants.LOOKING, stringLooking);
                        dataProccessor.setStr(Constants.DONATE, stringDonate);
                        dataProccessor.setStr(Constants.DONATE_HOW, stringDonateHow);
                        dataProccessor.setStr(Constants.FETCH, stringFetch);
                        dataProccessor.setStr(Constants.GHOST_MODE, stringGhostMode);
                        dataProccessor.setStr(Constants.LOCATION_MODE, stringLocationMode);
                        dataProccessor.setStr(Constants.DISTANCE_MINIMUM, distanceFrom);
                        dataProccessor.setStr(Constants.DISTANCE_MAXIMUM, distanceTo);
                        dataProccessor.setStr(Constants.LOOKING_FOR, stringLookingFor);
                        dataProccessor.setStr(Constants.LOOKING_FOR, stringLookingFor);
                        dataProccessor.setStr(Constants.MOBILE_NUMBER, stringPhone);


                        /* finish the activity */
                        finish();
                    }


                });
    }

    /* Don't save slider details since nothing changed for distance only */
    private void emptySlidersDistance() {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put(Constants.USER_LOOKING, stringLooking);
        userProfile.put(Constants.USER_DONATE, stringDonate);
        userProfile.put(Constants.USER_FETCH, stringFetch);
        userProfile.put(Constants.USER_DONATE_HOW, stringDonateHow);
        userProfile.put(Constants.ALERT_GHOST_MODE, stringGhostMode);
        userProfile.put(Constants.ALERT_LOCATION_MODE, stringLocationMode);
        userProfile.put(Constants.AGE_MIN, ageFrom);
        userProfile.put(Constants.AGE_MAX, ageTo);
        userProfile.put(Constants.DISTANCE_MIN, dataProccessor.getStr(Constants.DISTANCE_MINIMUM));
        userProfile.put(Constants.DISTANCE_MAX, dataProccessor.getStr(Constants.DISTANCE_MAXIMUM));
        userProfile.put(Constants.USER_LOOKING_FOR, stringLookingFor);
        userProfile.put(Constants.USER_PHONE, stringPhone);


        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(userId)
                .update(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);

                        dataProccessor.setStr(Constants.LOOKING, stringLooking);
                        dataProccessor.setStr(Constants.DONATE, stringDonate);
                        dataProccessor.setStr(Constants.FETCH, stringFetch);
                        dataProccessor.setStr(Constants.DONATE_HOW, stringDonateHow);
                        dataProccessor.setStr(Constants.GHOST_MODE, stringGhostMode);
                        dataProccessor.setStr(Constants.LOCATION_MODE, stringLocationMode);
                        dataProccessor.setStr(Constants.AGE_MINIMUM, ageFrom);
                        dataProccessor.setStr(Constants.AGE_MAXIMUM, ageTo);
                        dataProccessor.setStr(Constants.LOOKING_FOR, stringLookingFor);
                        dataProccessor.setStr(Constants.MOBILE_NUMBER, stringPhone);

                        /* finish the activity */
                        finish();
                    }


                });
    }

    /* Save slider details since it has be altered */
    private void getSlidersDetails() {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put(Constants.USER_LOOKING, stringLooking);
        userProfile.put(Constants.USER_DONATE, stringDonate);
        userProfile.put(Constants.USER_FETCH, stringFetch);
        userProfile.put(Constants.USER_DONATE_HOW, stringDonateHow);
        userProfile.put(Constants.ALERT_GHOST_MODE, stringGhostMode);
        userProfile.put(Constants.ALERT_LOCATION_MODE, stringLocationMode);
        userProfile.put(Constants.AGE_MIN, ageFrom);
        userProfile.put(Constants.AGE_MAX, ageTo);
        userProfile.put(Constants.DISTANCE_MIN, distanceFrom);
        userProfile.put(Constants.DISTANCE_MAX, distanceTo);
        userProfile.put(Constants.USER_LOOKING_FOR, stringLookingFor);
        userProfile.put(Constants.USER_PHONE, stringPhone);

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(userId)
                .update(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);

                        dataProccessor.setStr(Constants.LOOKING, stringLooking);
                        dataProccessor.setStr(Constants.DONATE, stringDonate);
                        dataProccessor.setStr(Constants.FETCH, stringFetch);
                        dataProccessor.setStr(Constants.DONATE_HOW, stringDonateHow);
                        dataProccessor.setStr(Constants.GHOST_MODE, stringGhostMode);
                        dataProccessor.setStr(Constants.LOCATION_MODE, stringLocationMode);
                        dataProccessor.setStr(Constants.AGE_MINIMUM, ageFrom);
                        dataProccessor.setStr(Constants.AGE_MAXIMUM, ageTo);
                        dataProccessor.setStr(Constants.DISTANCE_MINIMUM, distanceFrom);
                        dataProccessor.setStr(Constants.DISTANCE_MAXIMUM, distanceTo);
                        dataProccessor.setStr(Constants.LOOKING_FOR, stringLookingFor);
                        dataProccessor.setStr(Constants.MOBILE_NUMBER, stringPhone);


                        /* finish the activity */
                        finish();
                    }


                });
    }

    /**
     * Logs out user and takes it to the WelcomeActivity
     */
    private void logOut(Context context) {
        /* user status changed to offline */
        userStatus("offline");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /* remove all saved prefs */
        dataProccessor.removeAllPrefs();
        deleteCache(context);
        startActivity(intent);
        finish();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveUserInformation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            saveUserInformation();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get user details before the activity shows up
     */
    @Override
    public void onStart() {
        super.onStart();
//        getUserInfo();
        getUserInfoFromPrefs();
    }

    private void onlineUser() {
        String currentUserId = currentUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserId)
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
    protected void onResume() {
        super.onResume();
        userStatus("online");
        onlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }
        getUserInfoFromPrefs();

    }

    @Override
    protected void onPause() {
        super.onPause();
        userStatus("paused");
        Locart.appRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStatus("offline");
    }

    /* Function to convert String to Float */
    public static float convertStringToFloat(String str) {
        /* Convert string to float
         using parseFloat() method */
        return Float.parseFloat(str);
    }
}

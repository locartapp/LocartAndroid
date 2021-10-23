package com.locart.Login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.locart.Utils.DatePickerFragment;
import com.locart.Utils.GpsTracker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.locart.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    EditText mEmail, mPassword, mConfirmPassword;
    EmojiconEditText mName;
    TextView mTextViewBirthday, mShopInfo;
    Button mRegister;
    ImageView mProfileImage;
    RadioGroup mRadioGroup;
    RadioButton radioButton;
    /* Image request code */
    private int PICK_IMAGE_REQUEST = 1;

    /* storage and location permission code */
    private static final int RC_LOCATION_CONTACTS_PERM = 101;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    Bitmap bitmap, convertedImage;

    /* Uri to store the image uri */
    Uri url, imageUri;
    /* Constant for logging */
    private static final String TAG = RegisterActivity.class.getSimpleName();
    String userId, saveCurrentDate, saveCurrentTime, selectedGender = "", latitude, longitude,
            sPassword, sConfirmPassword;

    String string_city, string_state, string_country, string_location, stringLooking,
            stringDateOfBirth, stringEmail, stringPassword, stringAge, downloadUri, stringSex = "Shop";

    private GpsTracker gpsTracker;
    DataProccessor dataProccessor;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private EmojIconActions emojIcon;
    private View contentRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initializeObjects();
    }

    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    @SuppressLint("NonConstantResourceId")
    private void initializeObjects() {
        mEmail = findViewById(R.id.email);
        mName = findViewById(R.id.name);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirmPassword);
        mRegister = findViewById(R.id.register);
        mRadioGroup = findViewById(R.id.radio);
        mProfileImage = findViewById(R.id.profile_image);
        mTextViewBirthday = findViewById(R.id.tv_birthday);
        progressBar = findViewById(R.id.progressBar);
        mShopInfo = findViewById(R.id.Shop_info);

        mRegister.setOnClickListener(this);
        mProfileImage.setOnClickListener(this);
        mTextViewBirthday.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

//        FirebaseFirestore.setLoggingEnabled(true);
        dataProccessor = new DataProccessor(this);

        emojIcon = new EmojIconActions(this, contentRoot, mName, mProfileImage);
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
        emojIcon.addEmojiconEditTextList(mName);
        emojIcon.setUseSystemEmoji(true);

        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.Buyer_radio:
                    mShopInfo.setVisibility(View.GONE);
                    stringSex = "Buyer";
                    break;
                case R.id.Seller_radio:
                    mShopInfo.setVisibility(View.GONE);
                    stringSex = "Seller";
                    break;
                case R.id.Renter_radio:
                    mShopInfo.setVisibility(View.GONE);
                    stringSex = "Renter";
                    break;
                case R.id.Shop_radio:
                    mShopInfo.setVisibility(View.VISIBLE);
                    stringSex = "Shop";
                    break;
                default:
                    break;
            }
        });

//        String sex = radioButton.getText().toString();
        Log.d(TAG, "Sex selected: "+stringSex);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                /* start process for registration */
                register();
                break;
            case R.id.profile_image:
                /* select image from storage */
                selectImage();
                break;
            case R.id.tv_birthday:
                /* show dialog to select date of birth */
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
                break;

        }
    }

    /**
     * Register the user, but before that check if every field is correct.
     * After that registers the user and creates an entry for it in the database
     */
    private void register() {

        if (mEmail.getText().length() == 0) {
            mEmail.setError("Please fill this field");
            return;
        }
        if (mName.getText().length() == 0) {
            mName.setError("Please fill this field");
            return;
        }
        if (mPassword.getText().length() == 0) {
            mPassword.setError("Please fill this field");
            return;
        }
        if (mPassword.getText().length() < 6) {
            mPassword.setError("Password must have at least 6 characters");
            return;
        }

        if (mTextViewBirthday.getText().length() == 0) {
            mTextViewBirthday.setError("Date of birth must be specified");
            return;
        }
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "Please select profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        /* hide keyboard layout */
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        assert inputManager != null;
        inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        /* disable button and all fields */
        disableAllFields();
        if (checkPasswordNotMatch()) {
            /* are equal, proceed with registration */
            int selectedId = mRadioGroup.getCheckedRadioButtonId();
            radioButton = findViewById(selectedId);
            selectedGender = radioButton.getText().toString();

            stringLooking = "All";

            stringEmail = mEmail.getText().toString();
            stringPassword = mPassword.getText().toString();
            stringDateOfBirth = mTextViewBirthday.getText().toString();

            registerUser(stringEmail, stringPassword);

        } else {
            /* passwords are different, show toast with error message */
            enableAllFields();
            Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();
        }

    }

    private void registerUser(final String email, final String password) {
        /* check if email contain necessary details */
        if (isValidEmail(email)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            /* Get and save the Uid to string for later upload */
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            assert currentUser != null;
                            userId = currentUser.getUid();

                            /* send verification email */
                            currentUser.sendEmailVerification();

                            /* local storage path */
                            final String PATH = "/storage/emulated/0/Locart/Image/picture.jpg";
                            /* cloud storage task for image upload */
                            UploadTask uploadTask;
                            StorageReference imageRef = storageReference.child("user_image").child(userId + ".jpg");
                            uploadTask = imageRef.putFile(Uri.fromFile(new File(PATH)));

                            Task<Uri> urlTask = uploadTask.continueWithTask(task2 -> {
                                if (!task2.isSuccessful()) {
                                    throw task2.getException();
                                }

                                /* Continue with the task to get the download URL */
                                return imageRef.getDownloadUrl();
                            }).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    downloadUri = String.valueOf(task2.getResult());

                                    /* get location in lat and long */
                                    getLocation();
                                    /* upload details to fire store */
                                    upload();

                                    Log.d(TAG, "onSuccess 2..." + downloadUri);

                                    /* delete temporary image on device */
                                    String fileName = "picture.jpg";
                                    File myDirectory = new File(Environment.getExternalStorageDirectory() +
                                            File.separator + "Locart" + File.separator +
                                            File.separator + "Image");
                                    File file = new File(myDirectory, fileName);
                                    if (file.exists()) {
                                        file.delete();
                                    }

                                } else {
                                    // Handle failures
                                    Log.d(TAG, "onFailure()...");
                                }
                            });

                        } else {
                            enableAllFields();
                            Toast.makeText(getApplicationContext(), "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
        } else {
            Toast.makeText(RegisterActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
        }

    }

    public void upload() {
        /* Get current date */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat CurrentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
        saveCurrentDate = CurrentDate.format(calendar.getTime());

        /* Get current time */
        Calendar time = Calendar.getInstance();
        SimpleDateFormat CurrentTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        saveCurrentTime = CurrentTime.format(time.getTime());

        if (imageUri != null) {
            /* store profile images in a folder name profile_images */

            DocumentReference docRef = db.collection(Constants.FIRE_STORE_COLLECTION).document(userId);
            Map<String, Object> users = new HashMap<>();
            users.put(Constants.USER_NAME, mName.getText().toString());
            users.put(Constants.USER_EMAIL, mEmail.getText().toString());
            users.put(Constants.USER_SEX, stringSex);
            users.put(Constants.USER_IMAGE, downloadUri);
            users.put(Constants.USER_ID, userId);
            users.put(Constants.USER_DATE_OF_BIRTH, stringDateOfBirth);
            users.put(Constants.USER_AGE, stringAge);
            users.put(Constants.USER_DATE_REGISTRATION, saveCurrentDate);
            users.put(Constants.USER_TIME_REGISTRATION, saveCurrentTime);
            users.put(Constants.USER_CITY, string_city);
            users.put(Constants.USER_WEBSITE, "");
            users.put(Constants.USER_STATE, string_state);
            users.put(Constants.USER_COUNTRY, string_country);
            users.put(Constants.USER_LOCATION, string_location);
            users.put(Constants.USER_PHONE, "");
            users.put(Constants.USER_EDUCATION, "");
            users.put(Constants.USER_JOB, "");
            users.put(Constants.USER_COMPANY, "");
            users.put(Constants.USER_STATUS, "offline");
            users.put(Constants.USER_USERNAME, "");
            users.put(Constants.USER_ABOUT, "");
            users.put(Constants.USER_LATITUDE, latitude);
            users.put(Constants.USER_LONGITUDE, longitude);
            users.put(Constants.USER_LOOKING, stringLooking);
            users.put(Constants.USER_ONLINE, Timestamp.now());
            users.put(Constants.USER_JOINED, Timestamp.now());
            users.put(Constants.USER_VERIFIED, "no");
            users.put(Constants.USER_PREMIUM, "no");
            users.put(Constants.ALERT_CHATS, "yes");
            users.put(Constants.ALERT_MATCH, "yes");
            users.put(Constants.ALERT_GHOST_MODE, "no");
            users.put(Constants.ALERT_LOCATION_MODE, "yes");
            users.put(Constants.SHOW_FEEDS, "yes");
            users.put(Constants.SHOW_STATUS, "yes");
            users.put(Constants.USER_DONATE, "None");
            users.put(Constants.USER_FETCH, "None");
            users.put(Constants.USER_FETCH_HOW, "None");
            users.put(Constants.USER_DONATE_HOW, "None");
            users.put(Constants.USER_NEXUS, "0");
            users.put(Constants.AGE_MIN, "18");
            users.put(Constants.AGE_MAX, "30");
            users.put(Constants.DISTANCE_MIN, "0");
            users.put(Constants.DISTANCE_MAX, "100");
            users.put(Constants.USER_LOOKING_FOR, "None");
            users.put(Constants.USER_DEVICE, "Android");

            /* add user to database */
            docRef.set(users)
                    .addOnSuccessListener(aVoid -> {
//                        Log.d(TAG, "onSuccess: Created User ");
                        /* show alert dialog */
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setCancelable(false);
                        builder.setTitle("Verify Email");
                        builder.setMessage("Check your email inbox & click on the verify email link to get verified for a successful first login");

                        /* add a button */
                        builder.setPositiveButton("DISMISS", (dialog, which) -> {
                            mAuth.signOut();
                            Intent intent = new Intent(RegisterActivity.this, InstructionsActivity.class);
                            startActivity(intent);
                            finish();

                        });

                        progressBar.setVisibility(View.GONE);
                        /* create and show the alert dialog */
                        AlertDialog dialog = builder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();

                    })
                    .addOnFailureListener(e -> {
                        enableAllFields();
                        Toast.makeText(getApplicationContext(), "Error! Failed to Create User", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "onFailure: Failed to Create User " + e.toString());
                    });

        }
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void selectImage() {
        /* Check for location and storage permission */
        locationAndStorageTask();
    }

    /* method to show file chooser */
    private void showFileChooser() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE_REQUEST);
    }

    /* handling the image chooser activity result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            String imagePath = getFullPathFromContentUri(getApplicationContext(), imageUri);
            final String PATH = "/storage/emulated/0/Locart/Image/picture.jpg";

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                /* check if bitmap is not empty */
                if (bitmap != null) {
                    /* resize image to smaller size in phone storage
                    set bitmap to image view for user*/
                    compressImage(imagePath);
                    Log.d(TAG, "debugging...");

                    //create folder if absent
                    File file = new File(Environment.getExternalStorageDirectory() + "/Locart/Image");
                    if (!file.exists()) {
                        boolean res = file.mkdirs();
                    }

                    convertedImage = BitmapFactory.decodeFile(PATH);

                    mProfileImage.setImageBitmap(convertedImage);

                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to load image from memory", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void locationAndStorageTask() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            /* Have permissions, do the thing! check if location is enabled */
            if (isLocationEnabled()) {
                showFileChooser();
                getLocation();
            }else {
                /* show alert dialog to turn on location */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Enable Location");
                builder.setMessage("You need to turn on your location to continue");
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
                    RC_LOCATION_CONTACTS_PERM, perms);
        }
    }

    /* method to check if location is enabled */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLocation();
    }

    /* set date of birth with dialog */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Calendar today = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY", Locale.US);
        String strDate = format.format(calendar.getTime());

        int age = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
//        Log.d(TAG, "Age : "+ String.valueOf(age));

        if (age < 18) {
            Toast.makeText(getApplicationContext(),
                    "Only 18 & above age individuals are allowed to register",
                    Toast.LENGTH_LONG).show();
            mTextViewBirthday.setText("");

        } else {
            mTextViewBirthday.setText(strDate);
            stringDateOfBirth = mTextViewBirthday.getText().toString();
//            Toast.makeText(getActivity(), AgeUser(stringBirthday), Toast.LENGTH_SHORT).show();
            stringAge = String.valueOf(age);
//            Log.d(TAG, "Age 2: "+ String.valueOf(age));

        }

    }

    /* check password/pin for match */
    private boolean checkPasswordNotMatch() {
        /* get values of edit text to save in string temporarily */
        sPassword = mPassword.getText().toString();
        sConfirmPassword = mConfirmPassword.getText().toString();

        /* check for password if matched when entered twice */
        if (!sPassword.equals(sConfirmPassword)) {
            mConfirmPassword.setError(getString(R.string.err_msg_password_confirm));
            mPassword.setError(getString(R.string.err_msg_password_confirm));
            return false;
        }

        return true;
    }

    public void disableAllFields() {
        progressBar.setVisibility(View.VISIBLE);
        mEmail.setEnabled(false);
        mName.setEnabled(false);
        mPassword.setEnabled(false);
        mConfirmPassword.setEnabled(false);
        mRegister.setEnabled(false);
        mRadioGroup.setEnabled(false);
        mProfileImage.setEnabled(false);
        mTextViewBirthday.setEnabled(false);

    }

    public void enableAllFields() {
        mEmail.setEnabled(true);
        mName.setEnabled(true);
        mPassword.setEnabled(true);
        mConfirmPassword.setEnabled(true);
        mRegister.setEnabled(true);
        mRadioGroup.setEnabled(true);
        mProfileImage.setEnabled(true);
        mTextViewBirthday.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
}
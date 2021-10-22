package com.locart.Activity;

import android.Manifest;
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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.locart.Locart;
import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.locart.Utils.DatePickerChange;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.locart.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Activity responsible for handling the edit of user's data
 */
public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private ImageView mProfileImage;
    /* Uri to store the image uri */
    Uri url, imageUri;
    Bitmap bitmap;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    /* Constant for logging */
    private static final String TAG = EditProfileActivity.class.getSimpleName();


    EditText editTextProfileEditJob, editTextProfileEditCompany, editTextProfileEditEducation,
            editTextProfileEditCity, editTextProfileEditWebsite, editTextProfileEditUsername;

    EmojiconEditText editTextProfileEditAbout, editTextProfileEditName;

    TextView tvBirthday, mShopInfo;
    String stringDateOfBirth, stringAge, stringGender, stringUsername = "", stringName = "",
            downloadUri, currentUser = firebaseUser.getUid(), stringAbout = "";

    /* Image request code */
    private int PICK_IMAGE_REQUEST = 1;
    /* storage permission code */
    private static final int RC_STORAGE_PERM = 101;
    RadioGroup mRadioGroup;
    DataProccessor dataProccessor;
    private ProgressBar progressBar;
    private EmojIconActions emojIcon;
    private View contentRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initUi();
    }

    private void initUi() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextProfileEditName = findViewById(R.id.editTextProfileEditName);
        editTextProfileEditAbout = findViewById(R.id.editTextProfileEditAbout);
        editTextProfileEditJob = findViewById(R.id.editTextProfileEditJob);
        editTextProfileEditCompany = findViewById(R.id.editTextProfileEditCompany);
        editTextProfileEditEducation = findViewById(R.id.editTextProfileEditEducation);
        editTextProfileEditCity = findViewById(R.id.editTextProfileEditCity);
        editTextProfileEditWebsite = findViewById(R.id.editTextProfileEditWebsite);
        editTextProfileEditUsername = findViewById(R.id.editTextProfileEditUsername);
        tvBirthday = findViewById(R.id.tvBirthday);
        mRadioGroup = findViewById(R.id.radio);
        mProfileImage = findViewById(R.id.profileImage);
        mShopInfo = findViewById(R.id.Shop_info);

        stringGender = "";
        progressBar = findViewById(R.id.progressBar);

        dataProccessor = new DataProccessor(this);

        tvBirthday.setOnClickListener(this);
        mProfileImage.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.Buyer_radio:
                    stringGender = "Buyer";
                    mShopInfo.setVisibility(View.GONE);
                    break;
                case R.id.Seller_radio:
                    stringGender = "Seller";
                    mShopInfo.setVisibility(View.GONE);
                    break;
                case R.id.Renter_radio:
                    stringGender = "Renter";
                    mShopInfo.setVisibility(View.GONE);
                    break;
                case R.id.Shop_radio:
                    stringGender = "Shop";
                    mShopInfo.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        });


        editTextProfileEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                if (s.length() == 0) {
                    emojIcon.setUseSystemEmoji(true);
                }
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (editTextProfileEditName.getText().toString().trim().length() == 0) {
                    /* Your code here */
                    editTextProfileEditName.setError("Name cannot be blank");
                    stringName = dataProccessor.getStr(Constants.NAME);
                } else {
                    editTextProfileEditName.setError(null);
                    stringName = editTextProfileEditName.getText().toString().trim();
                }
            }
        });

        editTextProfileEditAbout.addTextChangedListener(new TextWatcher() {
            boolean ignore = false; // This is used to prevent infinite recursion in the afterTextChanged method

            @Override
            public void afterTextChanged(Editable arg0) {
                if (ignore) return;

                ignore = true;

                String s = arg0.toString();

                if (s.length() > 0) {

                    String originalStr = editTextProfileEditAbout.getText().toString();
                    originalStr = originalStr.replaceAll("[\\n ]", " ");

                    stringAbout = originalStr;
                    Log.d(TAG, stringAbout);

                }

                ignore = false;
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

//        editTextProfileEditUsername.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(final Editable s) {
//                if (editTextProfileEditUsername.getText().toString().trim().length() < 5) {
//                    /* Your code here */
//                    editTextProfileEditUsername.setError("Username is too short");
//                    stringUsername = "";
//                } else {
//                    editTextProfileEditUsername.setError(null);
//                    stringUsername = editTextProfileEditUsername.getText().toString().trim();
//                }
//            }
//        });

        emojIcon = new EmojIconActions(this, contentRoot, editTextProfileEditAbout, mProfileImage);
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
        emojIcon.addEmojiconEditTextList(editTextProfileEditAbout);
        emojIcon.addEmojiconEditTextList(editTextProfileEditName);
        emojIcon.setUseSystemEmoji(true);

    }

    private void OnlineUser() {
        String currentUserId = firebaseUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
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
    protected void onResume() {
        super.onResume();
        UserStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }
        getUserInfoFromPrefs();

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
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = firebaseUser.getUid();
        getUserInfoFromPrefs();

    }

    private void getUserInfoFromPrefs() {
        String user_about = dataProccessor.getStr(Constants.ABOUT);
        String name = dataProccessor.getStr(Constants.NAME);
        String user_company = dataProccessor.getStr(Constants.COMPANY);
        String user_job = dataProccessor.getStr(Constants.JOB);
        String user_education = dataProccessor.getStr(Constants.EDUCATION);
        String user_city = dataProccessor.getStr(Constants.CITY);
        String user_website = dataProccessor.getStr(Constants.WEBSITE);
        String user_username = dataProccessor.getStr(Constants.USERNAME);
        String user_birthday = dataProccessor.getStr(Constants.DATE_OF_BIRTH);
        String user_sex = dataProccessor.getStr(Constants.SEX);

        /* For image */
        String user_cover_zero = dataProccessor.getStr(Constants.IMAGE);

        assert user_cover_zero != null;
        if (user_cover_zero.equals("")) {
            mProfileImage.setImageResource(R.drawable.no_image);
        } else {
            /* get image and load */
            Glide.with(getApplicationContext())
                    .load(user_cover_zero)
                    .placeholder(R.drawable.no_image)
                    .into(mProfileImage);
        }

        if (name != null) {
            editTextProfileEditName.setText(name);
        }
        if (user_about != null) {
            editTextProfileEditAbout.setText(user_about);
        }
        if (user_company != null) {
            editTextProfileEditCompany.setText(user_company);
        }
        if (user_job != null) {
            editTextProfileEditJob.setText(user_job);
        }
        if (user_education != null) {
            editTextProfileEditEducation.setText(user_education);
        }
        if (user_city != null) {
            editTextProfileEditCity.setText(user_city);
        }
        if (user_website != null) {
            editTextProfileEditWebsite.setText(user_website);
        }
//        if (user_username != null) {
//            editTextProfileEditUsername.setText(user_username);
//        }
        if (user_birthday != null) {
            tvBirthday.setText(user_birthday);
        }

        if (user_sex == null) {
            ((RadioButton) findViewById(R.id.Shop_radio)).setChecked(true);
        }
        assert user_sex != null;
        switch (user_sex) {
            case "Buyer":
                ((RadioButton) findViewById(R.id.Buyer_radio)).setChecked(true);
                stringGender = "Buyer";
                break;
            case "Seller":
                ((RadioButton) findViewById(R.id.Seller_radio)).setChecked(true);
                stringGender = "Seller";
                break;
            case "Renter":
                ((RadioButton) findViewById(R.id.Renter_radio)).setChecked(true);
                stringGender = "Renter";
                break;
            case "Shop":
                ((RadioButton) findViewById(R.id.Shop_radio)).setChecked(true);
                stringGender = "Shop";
                break;

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileImage:
                /* select image from storage */
                selectImage();
                break;
            case R.id.tvBirthday:
                /* show dialog to select date of birthday */
                DialogFragment datePicker = new DatePickerChange();
                datePicker.show(getSupportFragmentManager(), "date picker");

                break;
        }
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            String imagePath = getFullPathFromContentUri(getApplicationContext(), imageUri);
//            final String PATH = "/storage/emulated/0/Locart/Image/picture.jpg";
            /* resize image to smaller size in phone storage
                    set bitmap to image view for user*/
            compressImage(imagePath);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                /* check if bitmap is not empty */
                if (bitmap != null) {

                    File file2 = new File(getFilename());
                    Picasso picasso = Picasso.get();
                    picasso.invalidate(file2);
                    picasso.load(file2)
                            .placeholder(R.drawable.no_image)
                            .error(R.drawable.no_image)
                            .into(mProfileImage);


                    UploadTask uploadTask;
                    StorageReference imageRef = storageReference.child("user_image").child(currentUser + ".jpg");
                    uploadTask = imageRef.putFile(Uri.fromFile(new File(getFilename())));

                    Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        /* Continue with the task to get the download URL */
                        return imageRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            downloadUri = String.valueOf(task.getResult());

                            Log.d(TAG, "onSuccess 2..." + downloadUri);

                            /* save the profile picture to fire store */
                            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                                    .document(currentUser)
                                    .update(
                                            "z_image", downloadUri

                                    );
                            dataProccessor.setStr(Constants.IMAGE, downloadUri);

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
                    Toast.makeText(EditProfileActivity.this, "Failed to load image from memory", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
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

    private void selectImage() {
        /* Check for storage permission */
        storageTask();
    }

    private void storageTask() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(EditProfileActivity.this, perms)) {
            // Have permission, do the thing!
            showFileChooser();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(EditProfileActivity.this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERM, perms);
        }
    }

    /* method to show file chooser */
    private void showFileChooser() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE_REQUEST);
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
//            checkIfUsernameExist();
            saveAllDetailsToFirebase();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAllDetailsToFirebase() {
        disableAllFields();
        progressBar.setVisibility(View.VISIBLE);

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .update(
                        Constants.USER_NAME, stringName,
                        Constants.USER_ABOUT, stringAbout,
                        Constants.USER_JOB, editTextProfileEditJob.getText().toString(),
                        Constants.USER_COMPANY, editTextProfileEditCompany.getText().toString(),
                        Constants.USER_EDUCATION, editTextProfileEditEducation.getText().toString(),
                        Constants.USER_CITY, editTextProfileEditCity.getText().toString(),
                        Constants.USER_WEBSITE, editTextProfileEditWebsite.getText().toString(),
//                        "user_username", stringUsername,
                        Constants.USER_SEX, stringGender)
                .addOnSuccessListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    dataProccessor.setStr(Constants.NAME, stringName);
//                dataProccessor.setStr(Constants.USERNAME, stringUsername);
                    dataProccessor.setStr(Constants.SEX, stringGender);

                    dataProccessor.setStr(Constants.CITY, editTextProfileEditCity.getText().toString().trim());
                    dataProccessor.setStr(Constants.WEBSITE, editTextProfileEditWebsite.getText().toString().trim());
                    dataProccessor.setStr(Constants.ABOUT, stringAbout);
                    dataProccessor.setStr(Constants.JOB, editTextProfileEditJob.getText().toString().trim());
                    dataProccessor.setStr(Constants.COMPANY, editTextProfileEditCompany.getText().toString().trim());
                    dataProccessor.setStr(Constants.EDUCATION, editTextProfileEditEducation.getText().toString().trim());

                    /* finish the activity */
                    finish();
                });


    }

    private void checkIfUsernameExist() {
        progressBar.setVisibility(View.VISIBLE);
        disableAllFields();
//        String username = editTextProfileEditUsername.getText().toString().trim();
        CollectionReference usersRef = firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION);
        Query query = usersRef.whereEqualTo(Constants.USER_USERNAME, stringUsername);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    String user = documentSnapshot.getString(Constants.USER_USERNAME);

                    assert user != null;
                    if (user.equals(stringUsername)) {
//                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "User Exists");
                        stringUsername = dataProccessor.getStr(Constants.USERNAME);
                        saveAllDetailsToFirebase();
                        Toast.makeText(EditProfileActivity.this, "Previous username maintained", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            if (Objects.requireNonNull(task.getResult()).size() == 0) {
//                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "User not Exists");
                //You can store new user information here
                saveAllDetailsToFirebase();
//                stringUsername = editTextProfileEditUsername.getText().toString().trim();
            }
        });
    }

    private void disableAllFields() {
        editTextProfileEditName.setEnabled(false);
        editTextProfileEditAbout.setEnabled(false);
        editTextProfileEditJob.setEnabled(false);
        editTextProfileEditCompany.setEnabled(false);
        editTextProfileEditEducation.setEnabled(false);
        editTextProfileEditCity.setEnabled(false);
        editTextProfileEditWebsite.setEnabled(false);
        editTextProfileEditUsername.setEnabled(false);
        mProfileImage.setEnabled(false);
        tvBirthday.setEnabled(false);
        mRadioGroup.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        checkIfUsernameExist();
        saveAllDetailsToFirebase();
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
            Toast.makeText(EditProfileActivity.this,
                    "Only 18 & above age individuals are allowed to use the app",
                    Toast.LENGTH_LONG).show();
            tvBirthday.setText("");
            stringDateOfBirth = dataProccessor.getStr(Constants.DATE_OF_BIRTH);

        } else {
            tvBirthday.setText(strDate);
            stringDateOfBirth = tvBirthday.getText().toString();
//            Log.d(TAG, "DOB : "+ stringBirthday);

            stringAge = String.valueOf(age);
//            Log.d(TAG, "Age 2: "+ String.valueOf(age));

            /* save the profile picture to fire store */
            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .update(
                            Constants.USER_DATE_OF_BIRTH, stringDateOfBirth,
                            Constants.USER_AGE, stringAge

                    );

        }

    }
}

package com.locart.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;

import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;

import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.locart.Message.MessageActivity;
import com.locart.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static androidx.camera.core.ImageCapture.FLASH_MODE_AUTO;
import static androidx.camera.core.ImageCapture.FLASH_MODE_OFF;
import static androidx.camera.core.ImageCapture.FLASH_MODE_ON;

public class CameraActivity extends AppCompatActivity {

    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int REQUEST_CODE_PERMISSIONS = 112;
    RecyclerView recyclerView;
    private PreviewView viewFinder;
    private ImageView mFlash, mSwitch, mCapture, mCapturePreview, mSendButton;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private int cameraLens;
    private int flashMode;
    private FrameLayout showPreview;

    private static final String TAG = "CameraActivity";
    private ImageCapture imageCapture;
    File imageDirectory = new File(Environment.getExternalStorageDirectory() +
            File.separator + "Locart" + File.separator +
            File.separator + "Image");

    private ImageCapture mImageCapture = null;
    private int flashType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            startCamera();
        }

        viewFinder = findViewById(R.id.textureView);
        mFlash = findViewById(R.id.img_flash);
        mCapture = findViewById(R.id.img_capture);
        mSwitch = findViewById(R.id.img_switch);
        showPreview = findViewById(R.id.show_preview);
        mCapturePreview = findViewById(R.id.preview_image);
        mSendButton = findViewById(R.id.send_button);

        recyclerView = findViewById(R.id.rv_image_gallery);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1, RecyclerView.HORIZONTAL, false));

        mFlash.setOnClickListener(v -> toggleFlash());
        mCapture.setOnClickListener(v -> captureImage());
        mSwitch.setOnClickListener(v -> switchCamera());

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraLens = CameraSelector.LENS_FACING_BACK;
        flashMode = ImageCapture.FLASH_MODE_OFF;


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions not granted by the user. " + allPermissionsGranted(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permissions : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(
                () -> {
                    try {
                        cameraProvider = cameraProviderFuture.get();
                        setCameraLens();
                        bindCameraUseCases();
                    } catch (Exception e) {
                        Log.e(TAG, "Error binding preview", e);
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setFlashMode(flashMode).build();

        cameraProvider.unbindAll();
        Camera camera =
                cameraProvider.bindToLifecycle(
                        this,
                        new CameraSelector.Builder().requireLensFacing(cameraLens).build(),
                        preview,
                        imageCapture);

        setupTapFocusAndZoom(camera);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTapFocusAndZoom(Camera camera) {
        CameraControl cameraControl = camera.getCameraControl();

        // Setup pinch zoom
        ScaleGestureDetector scaleGestureDetector =
                new ScaleGestureDetector(
                        this,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScale(ScaleGestureDetector detector) {
                                float scale =
                                        camera.getCameraInfo().getZoomState().getValue().getZoomRatio()
                                                * detector.getScaleFactor();
                                cameraControl.setZoomRatio(scale);
                                return true;
                            }
                        });

        viewFinder.setOnTouchListener(
                (v, event) -> {
                    scaleGestureDetector.onTouchEvent(event);

                    // Setup tap to focus
                    // TODO(hukevin) need some ui indicator for tap focus point
                    if (event.getAction() != MotionEvent.ACTION_UP) {
                        return true;
                    }
                    Log.i(TAG, "Touch focus at (" + event.getX() + "," + event.getY() + ")");
                    MeteringPointFactory meteringPointFactory =
                            new SurfaceOrientedMeteringPointFactory(
                                    (float) viewFinder.getWidth(), (float) viewFinder.getHeight());

                    MeteringPoint focusPoint = meteringPointFactory.createPoint(event.getX(), event.getY());
                    FocusMeteringAction action =
                            new FocusMeteringAction.Builder(focusPoint, FocusMeteringAction.FLAG_AF)
                                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                                    .build();
                    ListenableFuture<FocusMeteringResult> future =
                            cameraControl.startFocusAndMetering(action);
                    future.addListener(
                            () -> {
                                try {
                                    FocusMeteringResult result = future.get();
                                    Log.i(TAG, "FocusMeteringResult: " + result.isFocusSuccessful());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error focus and metering", e);
                                }
                            },
                            cameraExecutor);
                    return true;
                });
    }

    private void switchCamera() {
        if (cameraLens == CameraSelector.LENS_FACING_BACK
                && hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
            cameraLens = CameraSelector.LENS_FACING_FRONT;
        } else if (cameraLens == CameraSelector.LENS_FACING_FRONT
                && hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
            cameraLens = CameraSelector.LENS_FACING_BACK;
        }
        bindCameraUseCases();
    }

    private void setCameraLens() {
        if (hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
            cameraLens = CameraSelector.LENS_FACING_BACK;
        } else if (hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
            cameraLens = CameraSelector.LENS_FACING_FRONT;
        } else {
            Log.e(TAG, "No cameras found");
            throw new IllegalStateException("No camera found!");
        }
    }

    private boolean hasCamera(CameraSelector cameraSelector) {
        try {
            return cameraProvider != null && cameraProvider.hasCamera(cameraSelector);
        } catch (CameraInfoUnavailableException e) {
            Log.e(TAG, "Error checking for camera: " + cameraSelector, e);
        }
        return false;
    }

    private void toggleFlash() {
        // TODO(hukevin) Use icons for no-flash and auto-flash. using toasts for now
        String flashModeStr;
        if (flashMode == FLASH_MODE_OFF) {
            flashMode = FLASH_MODE_ON;
            flashModeStr = "Flash ON";
        } else if (flashMode == FLASH_MODE_ON) {
            flashMode = FLASH_MODE_AUTO;
            flashModeStr = "Flash AUTO";
        } else {
            flashMode = FLASH_MODE_OFF;
            flashModeStr = "Flash OFF";
        }
        // TODO(hukevin) shouldn't start preview on flash toggle
        bindCameraUseCases();

        Toast.makeText(this, flashModeStr, Toast.LENGTH_SHORT).show();
    }

    private void captureImage() {
        if (imageCapture == null) {
            return;
        }
        final File mediaFile = new File(imageDirectory, "capture" + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(mediaFile).build();
        //play camera sound
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);

        imageCapture.takePicture(
                outputFileOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(mediaFile);
                        String msg = "Photo capture succeeded: " + savedUri.toString();
//                        showToast(msg);
                        //compress image
                        compressImage(savedUri.toString());

                        final String PATH = "/storage/emulated/0/Locart/Image/capture.jpg";
                        Intent intent = new Intent();
                        intent.putExtra("messageType", "IMAGE");
                        intent.putExtra("file", PATH);
                        setResult(Activity.RESULT_OK, intent);
                        finish();

                        //show image with option to send or cancel
//                        showPreview.setVisibility(View.VISIBLE);
//                        mCapture.setVisibility(View.GONE);
//                        viewFinder.setVisibility(View.GONE);
//                        mSwitch.setVisibility(View.GONE);
//                        mFlash.setVisibility(View.GONE);

//                        final String PATH = "/storage/emulated/0/Locart/Image/capture.jpg";
//                        Bitmap convertedImage = BitmapFactory.decodeFile(PATH);
//                        mCapturePreview.setImageBitmap(convertedImage);
//                        findViewById(R.id.send_button).setOnClickListener(v -> {
//                            //send image
//                            Intent intent = new Intent();
//                            intent.putExtra("messageType", "IMAGE");
//                            intent.putExtra("file", PATH);
//                            setResult(Activity.RESULT_OK, intent);
//                            finish();
//                        });


                        Log.i(TAG, msg);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        showToast("Photo capture failed");
                        Log.e(TAG, "Photo capture failed: " + e.getMessage(), e);
                    }
                });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show());
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
        String uriSting = (file.getAbsolutePath() + "/" + "capture" + ".jpg");
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

}
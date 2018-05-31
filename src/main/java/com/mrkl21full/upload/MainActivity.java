package com.mrkl21full.upload;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String imageStoragePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_device_no_camera), Toast.LENGTH_LONG).show();
            finish();
        }

        Button btnCapturePicture = findViewById(R.id.btnCapturePicture);
        Button btnRecordVideo = findViewById(R.id.btnRecordVideo);

        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureImage();
                } else {
                    requestCameraPermission(AppConfig.MEDIA_TYPE_IMAGE);
                }
            }
        });

        btnRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureVideo();
                } else {
                    requestCameraPermission(AppConfig.MEDIA_TYPE_VIDEO);
                }
            }
        });

        restoreFromBundle(savedInstanceState);
    }

    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AppConfig.KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(AppConfig.KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + AppConfig.IMAGE_EXTENSION)) {
                        loadUploadActivity(true);
                    } else if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + AppConfig.VIDEO_EXTENSION)) {
                        loadUploadActivity(false);
                    }
                }
            }
        }
    }

    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (type == AppConfig.MEDIA_TYPE_IMAGE) {
                                captureImage();
                            } else {
                                captureVideo();
                            }
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(AppConfig.MEDIA_TYPE_IMAGE);
        if (file != null)
            imageStoragePath = file.getAbsolutePath();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, CameraUtils.getOutputMediaFileUri(getApplicationContext(), file));

        startActivityForResult(intent, AppConfig.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(AppConfig.MEDIA_TYPE_VIDEO);
        if (file != null)
            imageStoragePath = file.getAbsolutePath();

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, CameraUtils.getOutputMediaFileUri(getApplicationContext(), file));

        startActivityForResult(intent, AppConfig.CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppConfig.KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageStoragePath = savedInstanceState.getString(AppConfig.KEY_IMAGE_STORAGE_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConfig.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);
                loadUploadActivity(true);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_image_canceled), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_image_err), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == AppConfig.CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);
                loadUploadActivity(false);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_video_canceled), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_video_err), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUploadActivity(boolean isImage) {
        Intent i = new Intent(MainActivity.this, UploadActivity.class);
        i.putExtra("filePath", imageStoragePath);
        i.putExtra("isImage", isImage);
        startActivity(i);
    }

    private void showPermissionsAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.info_permissions))
                .setMessage(getString(R.string.info_permissions_content))
                .setPositiveButton(getString(R.string.button_settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(MainActivity.this);
                    }
                })
                .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public static boolean checkInternet(Context c) {
        ConnectivityManager conManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conManager != null;
        return (conManager.getActiveNetworkInfo() != null && conManager.getActiveNetworkInfo().isConnected());
    }
}
package com.mrkl21full.upload;

import com.mrkl21full.upload.AndroidMultiPartEntity.ProgressListener;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

@SuppressLint("Registered")
public class UploadActivity extends Activity {
    private String filePath = null;
    private ImageView imgPreview;
    private VideoView vidPreview;
    long totalSize = 0;

    private ProgressDialog progressBarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        imgPreview = findViewById(R.id.imgPreview);
        vidPreview = findViewById(R.id.videoPreview);

        progressBarDialog = new ProgressDialog(this);
        progressBarDialog.setTitle(getString(R.string.dialog_uploading));
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setCancelable(false);
        progressBarDialog.setCanceledOnTouchOutside(false);

        Intent i = getIntent();

        filePath = i.getStringExtra("filePath");

        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_file_path), Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.btnUploadContent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.checkInternet(getApplicationContext())) {
                    new UploadFileToServer().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void previewMedia(boolean isImage) {
        if (isImage) {
            try {
                vidPreview.setVisibility(View.GONE);

                imgPreview.setVisibility(View.VISIBLE);
                imgPreview.setImageBitmap(CameraUtils.optimizeBitmap(AppConfig.BITMAP_SAMPLE_SIZE, filePath));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            try {
                imgPreview.setVisibility(View.GONE);

                vidPreview.setVisibility(View.VISIBLE);
                vidPreview.setVideoPath(filePath);

                vidPreview.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            progressBarDialog.show();
            progressBarDialog.setProgress(0);
            super.onPreExecute();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBarDialog.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(AppConfig.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                entity.addPart("UImage", new FileBody(sourceFile));
                entity.addPart("UserID", new StringBody("1"));
                entity.addPart("EMail", new StringBody("kl21@evergames.pl"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                responseString = (statusCode == 200) ? EntityUtils.toString(r_entity) : getString(R.string.error_http_status_code) + statusCode;
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(final String result) {
            if(progressBarDialog.isShowing()) progressBarDialog.dismiss();
            showAlert(result);
            super.onPostExecute(result);
        }
    }

    private void showAlert(String message) {
        try {
            JSONObject response = new JSONObject(message);

            message = getString(R.string.response_userid) + response.getString("UserID") + "\n" +
                    getString(R.string.response_email) + " " + response.getString("EMail") + "\n" +
                    ((response.getBoolean("error")) ? getString(R.string.response_error) + " "  + response.getString("message") + "\n\n": getString(R.string.response_message) + " " + response.getString("message") + "\n\n") +
                    getString(R.string.response_fileurl) + " " + response.getString("file_path");

            new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setTitle(getString(R.string.response_from_server))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.button_upload_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            finish();
                            startActivity(intent);
                        }
                    }).show();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if(progressBarDialog.isShowing()) progressBarDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(progressBarDialog.isShowing()) progressBarDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(progressBarDialog.isShowing()) progressBarDialog.dismiss();
        super.onStop();
    }
}
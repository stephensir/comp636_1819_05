package com.example.intentcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener {

    // Properties
    private String TAG = "IntentCamera===>";
    Toolbar toolbar;
    private ImageButton iBtnPicture, iBtnVideo;
    private ImageView imageView;
    private VideoView videoView;
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the toolbar from the layout
        toolbar = findViewById(R.id.toolbar);

        //Set the properties of toolbar
        toolbar.setBackgroundColor(Color.DKGRAY);
        toolbar.setTitleTextColor(Color.YELLOW);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitleTextColor(Color.RED);
        toolbar.setSubtitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        // Reference
        iBtnPicture = findViewById(R.id.iBtnPicture);
        iBtnVideo = findViewById(R.id.iBtnVideo);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);

        // Set Listener
        iBtnPicture.setOnClickListener(this);
        iBtnVideo.setOnClickListener(this);


        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        getPermission();

    } //onCreate()

    // ========== Obtain permission from user ==========
    private void getPermission(){
        Log.d(TAG,"getPermission()");

        // Save permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }

        // Android N (API24)
        if(Build.VERSION.SDK_INT>=24){
            Log.d(TAG,"getPermission()->Android API"+Build.VERSION.SDK_INT);
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    } //getPermission()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult()");
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry !, You cannot use Camera。",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                // close the app
                Toast.makeText(MainActivity.this, "OK, you can use Camera。",
                        Toast.LENGTH_LONG).show();
            }
        }
    } //onRequestPermissionsResult()

    @Override
    public void onClick(View v) {

        ImageButton _ib = (ImageButton) v;
        switch (_ib.getId()) {
            case R.id.iBtnPicture:
                doPicture();
                break;
            case R.id.iBtnVideo:
                doVideo();
                break;
        }

    } //onCreate()

    private void doVideo(){
        Log.d(TAG,"doVideo");

        // set Visibility
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);

        // set URI and play
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.countdown);
        videoView.setVideoURI(uri);
        videoView.start();
    }

    private void doPicture(){
        Log.d(TAG,"doPicture()");

        // set Visibility
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG,"doPicture()->startActivityForResult()");
            startActivityForResult(intent, REQUEST_PICTURE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult()");
        Bitmap bitmap ;

        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG,"onActivityResult()->REQUEST_PICTURE_CAPTURE");
            if (data != null){
                // get photo from intent
                bitmap = (Bitmap)data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                savefile(bitmap);
            }
        } //REQUEST_SAVE
    } //onActivityResult()

    private void savefile(Bitmap bitmap){
        // Android 26+ file storage permission required
        File dirPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        // Save in "Internal Storage > DCIM > Camera

        Log.d(TAG,"savefile()->dirPath="+dirPath);

        String filename = new java.text.SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new java.util.Date());
        Log.d(TAG,"savefile()->FileName="+filename);

        String filenameExtension = ".png";

        File destinationFile = new File(dirPath, filename + filenameExtension);
        Log.d(TAG,"savefile()->destinationFile="+destinationFile);

        try {
            FileOutputStream out = new FileOutputStream(destinationFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // refresh MediaStore record
            Log.d(TAG,"savefile()->update MediaStore indexing");
            MediaStore.Images.Media.insertImage(getContentResolver()
                    ,destinationFile.getAbsolutePath(),destinationFile.getName(),destinationFile.getName());

        } catch (Exception e) {
            Log.e(TAG, "ERROR:" + e.toString());
        }
    } //savefile()
}

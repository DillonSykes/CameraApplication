package com.example.solution_color;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/*
    Links used: https://developer.android.com/training/camera/photobasics.html, https://developer.android.com/training/camera/photobasics.html
 */
public class MainActivity extends AppCompatActivity  {
    private static final float SATURATION_SETTING = (float) 98.0;
    File outputFileUri = Environment.getExternalStoragePublicDirectory("/myPic.jpg");
    File thePicPath = new File(outputFileUri.getAbsolutePath() + "/Camera/myPic.jpg" );
    ImageView myImage;
    final int TAKE_PICTURE = 1;
    ImageButton cameraButton;
    String mCurrentPhotoPath = "drawable/gutters.png";
    DisplayMetrics metrics;
    final int PERCENTAGE_FOR_SKETCHY = 50;
    boolean sketchy = false;
    boolean colorized = false;
    Bitmap skethcyBm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar myToolbar = findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);
        myImage = findViewById(R.id.backgroundPic);
        myImage.setImageResource(R.drawable.gutters);
        cameraButton = findViewById(R.id.camera);
        metrics = this.getResources().getDisplayMetrics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            default:
                break;
        }
        return true;
    }

    public int getScreenWidth(DisplayMetrics m){
        return m.widthPixels;
    }
    public int getScreenHeight(DisplayMetrics m){
        return m.heightPixels;
    }
    public void setPictureWithBitMap(Bitmap bm){
        myImage.setImageBitmap(bm);
    }
    public void resetFunction(MenuItem item) {
        Camera_Helpers.delSavedImage(mCurrentPhotoPath);
        myImage.setImageResource(R.drawable.gutters);
        myImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        myImage.setScaleType(ImageView.ScaleType.FIT_XY);
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PICTURE);
            }
        }
    }
    private Bitmap createBitmap(String pathofPic){
        return Camera_Helpers.loadAndScaleImage(pathofPic,getScreenHeight(metrics),getScreenWidth(metrics));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Bitmap bm = createBitmap(mCurrentPhotoPath);
                myImage.setImageBitmap(bm);
            }
            if(resultCode == RESULT_CANCELED){
                //back to main activity
            }
        }
    }

    public void takePicture(View view) {
        dispatchTakePictureIntent();
    }
    public void toggleSketchy(){
        sketchy = !sketchy;
    }
    public void toggleColorized(){
        colorized = !colorized;
    }
    public Bitmap createSketchyBitMap(){
        Bitmap bm = createBitmap(mCurrentPhotoPath);
        return BitMap_Helpers.thresholdBmp(bm, PERCENTAGE_FOR_SKETCHY);
    }
    public void sketchPic(MenuItem item) {
        if (sketchy == false) {
            setPictureWithBitMap(createSketchyBitMap());
            toggleSketchy();
        } else {
            setPictureWithBitMap(createBitmap(mCurrentPhotoPath));
            toggleSketchy();
        }
    }
    public Bitmap createColorizedBitMap(){
        Bitmap bm = createBitmap(mCurrentPhotoPath);
        return BitMap_Helpers.colorBmp(bm,SATURATION_SETTING);
    }
    public void colorize(MenuItem item) {
        if (colorized == false) {
            Bitmap sketchy1 = createSketchyBitMap();
            Bitmap colorized2 = createColorizedBitMap();
            BitMap_Helpers.merge(colorized2, sketchy1);
            setPictureWithBitMap(colorized2);
        } else {
            setPictureWithBitMap(createBitmap(mCurrentPhotoPath));
            toggleColorized();
        }

    }
}


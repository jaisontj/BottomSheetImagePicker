package com.jaison.librarytestapplication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.jaison.bsimagepicker.BottomSheetImagePicker;
import com.jaison.librarytestapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Opt for both camera and gallery
        binding.both.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetImagePicker.getInstance().showImagePicker(BottomSheetImagePicker.PickerType.BOTH, MainActivity.this, binding.bottomSheet, imagePickerListener);
            }
        });

        //Opt for camera only
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetImagePicker.getInstance().showImagePicker(BottomSheetImagePicker.PickerType.CAMERA, MainActivity.this, binding.bottomSheet, imagePickerListener);
            }
        });

        //Opt for gallery only
        binding.gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetImagePicker.getInstance().showImagePicker(BottomSheetImagePicker.PickerType.GALLERY, MainActivity.this, binding.bottomSheet, imagePickerListener);
            }
        });

        setSupportActionBar(binding.toolbar);
    }

    BottomSheetImagePicker.Listener imagePickerListener = new BottomSheetImagePicker.Listener() {
        @Override
        public void onImageArrived(Uri selectedImageUri) {
            Glide.with(MainActivity.this).load(selectedImageUri).into(binding.image);
        }
    };

    /**
     * BOTTOM SHEET IMAGE PICKER METHODS
     **/
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        BottomSheetImagePicker.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BottomSheetImagePicker.getInstance().onActivityResult(requestCode, resultCode, data);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

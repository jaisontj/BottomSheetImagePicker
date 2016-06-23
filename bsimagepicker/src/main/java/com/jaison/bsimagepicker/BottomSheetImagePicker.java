package com.jaison.bsimagepicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jaison on 20/04/16.
 * <p/>
 * USAGE :
 * Step 1: Include BottomSheet as your parent view in your layout
 * Step 2: Call BottomSheetImagePicker.getInstance().showImagePicker(Activity,BottomSheetLayout,BottomSheetImagePicker.Listener)
 * Step 3: Override OnActivityResult and call BottonSheetImagePicker.OnActivityResult(int requestCode, int resultCode, @Nullable Intent data)
 * Step 4: Override onRequestPermissionsResult and call BottomSheetImagePicker.onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
 * Step 5: You will get the selected Image URI in the listener callBack
 * Step 6: Make sure to call the destroy method inside of the onDestroy method of the activity -> This is important to avoid leaking memory
 * <p/>
 * FOR STEPS 3 and 4 , copy and paste the following code into your activity or fragment
 *
 * @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 * BottomSheetImagePicker.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
 * }
 * @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
 * super.onActivityResult(requestCode, resultCode, data);
 * BottomSheetImagePicker.getInstance().onActivityResult(requestCode,resultCode,data);
 * }
 * <p/>
 * REQUIREMENT :
 * Include the following in your app level gradle file
 * BottomSheet
 * compile 'com.flipboard:bottomsheet-core:1.5.0'
 * compile 'com.flipboard:bottomsheet-commons:1.5.0'
 * compile 'com.github.bumptech.glide:glide:3.7.0'
 */
public class BottomSheetImagePicker {

    public enum PickerType {
        CAMERA, GALLERY, BOTH
    }

    private static final int REQUEST_STORAGE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = REQUEST_STORAGE + 1;
    private static final int REQUEST_LOAD_IMAGE = REQUEST_IMAGE_CAPTURE + 1;

    PickerType pickerType;
    Activity activity;
    BottomSheetLayout bottomSheet;
    Uri selectedImageUri;
    Listener listener;
    private static BottomSheetImagePicker instance;

    public static BottomSheetImagePicker getInstance() {
        if (instance == null)
            instance = new BottomSheetImagePicker();
        return instance;
    }

    public void showImagePicker(Activity activity, BottomSheetLayout bottomSheet, Listener listener) {
        showImagePicker(PickerType.BOTH,activity,bottomSheet,listener);
    }

    public void showImagePicker(PickerType pickerType, Activity activity, BottomSheetLayout bottomSheet, Listener listener) {
        this.pickerType = pickerType;
        this.activity = activity;
        this.bottomSheet = bottomSheet;
        this.listener = listener;
        this.selectedImageUri = null;
        if (checkNeedsPermission()) {
            requestStoragePermission();
        } else {
            showSheetView();
        }
    }

    private Activity getActivity() {
        return activity;
    }

    private Context getContext() {
        return activity;
    }

    /**
     * IMAGE PICKER METHODS
     **/

    private boolean checkNeedsPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSheetView();
            } else {
                // Permission denied
                Toast.makeText(getActivity(), "Sheet is useless without access to external storage :/", Toast.LENGTH_SHORT).show();
            }
        } else {
            activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Show an {@link ImagePickerSheetView}
     */
    private void showSheetView() {
        ImagePickerSheetView sheetView = new ImagePickerSheetView.Builder(getContext())
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider(new ImagePickerSheetView.ImageProvider() {
                    @Override
                    public void onProvideImage(ImageView imageView, Uri imageUri, int size) {
                        Glide.with(activity)
                                .load(imageUri)
                                .centerCrop()
                                .crossFade()
                                .into(imageView);
                    }
                })
                .setOnTileSelectedListener(new ImagePickerSheetView.OnTileSelectedListener() {
                    @Override
                    public void onTileSelected(ImagePickerSheetView.ImagePickerTile selectedTile) {
                        bottomSheet.dismissSheet();
                        if (selectedTile.isCameraTile()) {
                            dispatchTakePictureIntent();
                        } else if (selectedTile.isPickerTile()) {
                            activity.startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
                        } else if (selectedTile.isImageTile()) {
                            listener.onImageArrived(selectedTile.getImageUri());
                        } else {
                            genericError();
                        }
                    }
                })
                .setTitle("Choose an image :")
                .create();

        bottomSheet.showWithSheetView(sheetView);
    }

    /**
     * For images captured from the camera, we need to create a File first to tell the camera
     * where to store the image.
     *
     * @return the File created for the image to be store under.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        selectedImageUri = Uri.fromFile(imageFile);
        return imageFile;
    }

    /**
     * This checks to see if there is a suitable activity to handle the `ACTION_PICK` intent
     * and returns it if found. {@link Intent#ACTION_PICK} is for picking an image from an external app.
     *
     * @return A prepared intent if found.
     */
    @Nullable
    private Intent createPickIntent() {
        if (pickerType != PickerType.CAMERA) {
            Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                return picImageIntent;
            }
        }
        return null;
    }

    /**
     * This checks to see if there is a suitable activity to handle the {@link MediaStore#ACTION_IMAGE_CAPTURE}
     * intent and returns it if found. {@link MediaStore#ACTION_IMAGE_CAPTURE} is for letting another app take
     * a picture from the camera and store it in a file that we specify.
     *
     * @return A prepared intent if found.
     */
    @Nullable
    private Intent createCameraIntent() {
        if (pickerType != PickerType.GALLERY) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                return takePictureIntent;
            }
        }
        return null;
    }

    /**
     * This utility function combines the camera intent creation and image file creation, and
     * ultimately fires the intent.
     *
     * @see {@link #createCameraIntent()}
     * @see {@link #createImageFile()}
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = createCameraIntent();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent != null) {
            // Create the File where the photo should go
            try {
                File imageFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                // Error occurred while creating the File
                genericError("Could not create imageFile for camera");
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LOAD_IMAGE && data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri == null) {
                    genericError();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Do something with imagePath
                //selectedImageUri has the path
            }

            if (selectedImageUri != null) {
                listener.onImageArrived(selectedImageUri);
            } else {
                genericError();
            }
        }
    }


    private void genericError() {
        genericError(null);
    }

    private void genericError(String message) {
        Toast.makeText(getContext(), message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
    }

    public interface Listener {
        void onImageArrived(Uri selectedImageUri);
    }

    public void destroy() {
        this.instance = null;
        this.activity = null;
        this.bottomSheet = null;
        this.listener = null;
    }
}

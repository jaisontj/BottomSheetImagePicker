# BottomSheetImagePicker
An Imagepicker for Android that uses Flipboards BottomSheet. The purpose of this library is to write a wrapper over the library to provide even lesser code for integration.

# Download 

Using Gradle 

```
repositories {
    jcenter()
}

compile 'com.jaison:bsimagepicker:0.1'
```

# Usage 

Step 1: Include BottomSheet as your parent view in your layout.Check out [flipboards repo](https://github.com/Flipboard/bottomsheet) for more details on this

Step 2: Call 
```
BottomSheetImagePicker.getInstance().showImagePicker(Activity,BottomSheetLayout,BottomSheetImagePicker.Listener)
```
when you want to show the imagepicker.

Step 3: 
```
Override OnActivityResult and call BottonSheetImagePicker.OnActivityResult(int requestCode, int resultCode, @Nullable Intent data)
```

Step 4:
```
Override onRequestPermissionsResult and call BottomSheetImagePicker.onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
```

Step 5:
You will get the selected Image URI in the listener callBack

Step 6: 
Make sure to call the destroy method inside of the onDestroy method of the activity -> This is important to avoid leaking memory

EXTRA :

There are three types/modes which can be set for the BottomSheetImagePicker :

OPTION 1 - Camera & Gallery (BOTH)
OPTION 2 - ONLY CAMERA (CAMERA)
OPTION 3 - ONLY GALLERY (GALLERY)

These can be accessed by using the following:

```
BottomSheetImagePicker.getInstance().showImagePicker(BottomSheetImagePicker.PickerType,Activity,BottomSheetLayout,BottomSheetImagePicker.Listener)
```


BOILER PLATE CODE :

```
FOR STEPS 3 and 4 , copy and paste the following code into your activity or fragment

@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
  BottomSheetImagePicker.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
}

@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  BottomSheetImagePicker.getInstance().onActivityResult(requestCode,resultCode,data);
}

```


# Requirements 

As this library is just to avoid boiler plate code on Flipboards Implementation of the BottomSheet ImagePicker, the following dependencies are added to your project.

```
compile 'com.flipboard:bottomsheet-core:1.5.0'
compile 'com.flipboard:bottomsheet-commons:1.5.0'
compile 'com.github.bumptech.glide:glide:3.7.0'
```

#Contribution 

Pull requests and issues are always welcome. Also, feel free to add an issue if this readme is not clear enough. 

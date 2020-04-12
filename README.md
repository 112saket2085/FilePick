# FilePick
# FilePick Library for Android


A FilePick library for Android for selecting file from gallery or camera.

It shows Intent of all apps available to handle file request in Bottom Sheet.

[Sample Apk](https://tinyurl.com/ur25czx)

[New Apk](https://tinyurl.com/yx2ohmbh)

[Debug Library](https://tinyurl.com/ujst2rn)

[Release Library](https://tinyurl.com/vu6k6an)



## Usage

Step 1: Add it in your root build.gradle at the end of repositories

```bash
 allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

Step 2: Add the dependency

```bash
 dependencies {
        ...
       	implementation 'com.github.112saket2085:FilePick:1.2'

    }
```

Step 4: Launch FilePicker module using below method.
```bash
     Configuration configuration=new Configuration.Builder().setCropRequired(false).setAspectRatioX(1).setAspectRatioY(1).build();
     FilePickIntentCreator.loadFilePickerRequest(activity,configuration);
     FilePickIntentCreator.loadFilePickerRequest(fragment,context,configuration);

```

Step 5: Receive results in onActivityResult(...).

```bash
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickConstants.REQ_CODE_FILE_PICK) {
            switch (resultCode) {
                case RESULT_OK:
                    mediaFiles = FilePickIntentCreator.getFilePickSuccessResult(data);
                    if (mediaFiles != null) {
                        // Get various Image file output eg. Bitmap,File Size,File Path,File bytes,File Uri.
                        Uri uri=mediaFiles.getUri();
                        loadImage(mediaFiles.getFile());
                        textViewFileName.setText(getString(R.string.str_file_name_detail, mediaFiles.getFileName()));
                        textViewFilePath.setText(getString(R.string.str_file_path_detail, mediaFiles.getFilePath()));
                        textViewFileSize.setText(getString(R.string.str_file_size_detail, mediaFiles.getFileSize()));
                     }
                    break;
                case RESULT_CANCELED:
                    break;
                case RESULT_FIRST_USER:
                    int errorCode = FilePickIntentCreator.getFilePickErrorResult(data);
                    break;
            }
        }
    }


```

## Configuration

The FilePick Module can be configured by using Configuration methods

```bash
 Configuration builder=new Configuration.Builder().setCropRequired(false).setAspectRatioX(1).setAspectRatioY(1).build();

```

## Builder Methods

1. setIntentType
```
Set File Pick Intent Type. Default Value is "image/*".
```
2. setBottomSheetTitle
```
Set Title to show in bottom sheet. Default Value is "Select File".
```
3. setAspectRatioX & setAspectRatioY
```
Set File Cropper Aspect Ratio. Default Crop Shape is Rectangle.
```
4. setCropRequired
```
Set is cropping required. Default cropping is set to false.
```

## MediaFile methods

1. MediaFiles getMediaFiles
```
Media Files containing various properties of file eg. File,File Size,File Path,File Uri.
```
2. Bitmap getBitmap
```
Get Image Bitmap using Uri or file
```
3.Uri storeImage
```
Method to add Image in External Storage (For Android version above Q WRITE_EXTERNAL_PERMISSION is not needed)
```
4.Uri insertImageWithStoragePermission
```
Method to save Image into External Storage upto Android P(Android SDK 28) using WRITE_EXTERNAL_PERMISSION needed
```
5. Uri createTempImageFile
```
Create Temporary Image file.
```
6.Uri getFileProviderUri
```
Get File Provider Uri to make file available to share to other apps.
```
7.void openImageSharingClient
```
Method to show sharing client installed.
```
7.void loadImageUsingGlide
```
Method to load Image using Glide.
```
8.void getCompressFile
```
Method to Get Compressed File.
```
9.void getCompressedImageBitmap
```
Method to Get Compressed Bitmap Image.
```
10.void getBitmapFromView
```
Method to Get Bitmap from View.
```
```
And various methods are avialble under MediaFiles class.
```

## Change log

## 1.3

```
Added File name from file object Added changes in isImageFile method
Removed Action_GET_CONTENT and Added Action_Pick and handled read storage permission denied result

```
## 1.2
```
Added support for adding file bytes into file.
Added support for more glide loading options.
Added support for inserting image in cache directory.
Added support for storing compressed file into destination path.
Added support for getting storage directory for different path.
Removed File App from Bottom Sheet for not getting proper file.
Added File Info in toolbar.
Removed AppBuilder application class
Added support to launch file pick library from fragment
Removed AppBuilder Context and added context as parameter in every method
Removed AppBuilder from android:name

```
## 1.1
```
Added is crop required test case in check box.
Added Set Enable crop option.
Removed byte array,file provider uri from sending data with mediafiles.
Added Image Laoding with Glide.
Added File Compression.
Added File Size.
Added Method for Bitmap Compression,Removed Get Bitmap from mediaFile.Get Bitmap from MediaFiles static method getBitmap.
Added technique for File compression.
Added technique for getting Bitmap image and byte Array.
Added Method to get Btimap Image From View.Added Get Bitmap using File parameter and Get Byte Array using File parameter.
Added READ_EXTERNAL_STORAGE Permission for File Not Found Exception while loading file using Glide or compressing File.
Added various test cases in app.
All method are written with description.And Technique to use these methods are written in application module MainActivity File Class
```
## 1.0
```
Bottom Sheet with intent of all apps that can handle file request.
Method to get various File Properties eg. Bitmap,File Size,File Path,File bytes,File Uri.
Method to add and store Image into specific directory.(Along with Android Q file creation methods).
Method to create temp file.
Method to get File Provider Uri to make file available to share to others apps.
Method to show sharing client installed.
Method to get Bitmap from uri.
Method to show WRITE_EXTERNAL_SORAGE permission dialog.
```

## Credits

Inspired by [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper)

Used Android Image Cropper created by 
ArthurHub in this project.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

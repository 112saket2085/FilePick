# FilePick
# FilePick Library for Android


A FilePick library for Android for selecting file from gallery or camera.

It shows Intent of all apps available to handle file request in Bottom Sheet.

[Sample Apk](https://tinyurl.com/ur25czx)



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
       	implementation 'com.github.112saket2085:FilePick:1.0'

    }
```

Step 4: Launch FilePicker module using below method.
```bash
     Configuration builder=new Configuration.Builder().setCropRequired(false).setAspectRatioX(1).setAspectRatioY(1).build();
     FilePickIntentCreator.loadFilePickerRequest(MainActivity.this,builder);

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
                        Bitmap bitmap = mediaFiles.getBitmap();
                        imageView.setImageBitmap(bitmap);
                        //Use below technique to create image File and insert bitmap in external storage
                        Uri uri = MediaFiles.insertImage(this, "Pictures/Example/", "", mediaFiles.getBitmap());
                        if(uri!=null) {
                            MediaFiles.openImageSharingClient(MainActivity.this, uri, FilePickConstants.IMAGE_INTENT_TYPE);
                        }
                    }
                    break;
                case RESULT_CANCELED:
                    break;
                case RESULT_FIRST_USER:
                    int errorCode=FilePickIntentCreator.getFilePickErrorResult(data);
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
Media Files containing various properties of file eg. Bitmap,File Size,File Path,File bytes,File Uri.
```
2. Bitmap getBitmap
```
Get Image Bitmap 
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
```
And various methods are avialble under MediaFiles class.
```
## Credits

Inspired by [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper)

Used Android Image Cropper created by 
ArthurHub in this project.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

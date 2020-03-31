package com.example.filepicklibrary.model;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.example.filepicklibrary.R;
import com.example.filepicklibrary.app.AppBuilder;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.ui.activity.FilePickActivity;
import com.example.filepicklibrary.utility.DialogBuilder;
import com.example.filepicklibrary.utility.PermissionCompatBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.filepicklibrary.app.FilePickConstants.FILE_PROVIDER_NAME;
import static com.example.filepicklibrary.app.FilePickConstants.PNG_FILE_FORMAT;

/**
 * MediaFiles - Class that store various image results.
 * Created by SAKET on 29/03/2020
 */
public class MediaFiles {

    private Bitmap bitmap;
    private File file;
    private String filePath = "";
    private String fileName = "";
    private long fileSize;
    private byte[] byteData;
    private Uri uri;
    private static String cameraPhotoPath;


    /**
     * Media Files containing various properties of file eg. Bitmap,File Size,File Path,File bytes,File Uri.
     * @param context Activity context
     * @param selectedImageUri Image Uri
     * @return MediaFiles
     */
    public static MediaFiles getMediaFiles(Context context, Uri selectedImageUri) {
        MediaFiles mediaFiles = new MediaFiles();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedImageUri);
            byte[] fileByte = null;
            if (inputStream != null) {
                fileByte = getBytes(inputStream);
            }
            if (fileByte != null) {
                mediaFiles.setByteData(fileByte);
                mediaFiles.setFileSize(fileByte.length);
            }
            mediaFiles.setFilePath(getFilePath(context, selectedImageUri));
            mediaFiles.setFileName(getFileName(context, selectedImageUri));
            String path = selectedImageUri.getPath();
            if (path != null) {
                mediaFiles.setFile(new File(Objects.requireNonNull(path)));
            }
            mediaFiles.setBitmap(getBitmap(context, selectedImageUri));
            selectedImageUri = getFileProviderUri(AppBuilder.getAppContext(), new File(Objects.requireNonNull(path)));
            mediaFiles.setUri(selectedImageUri);
            return mediaFiles;
        } catch (Exception e) {
            mediaFiles.setUri(selectedImageUri);
            return mediaFiles;
        }
    }

    /**
     * Get Image Bitmap
     * @param context Application context
     * @param selectedImageUri Uri
     * @return Image Bitmap
     */

    public static Bitmap getBitmap(Context context, Uri selectedImageUri) {
        Bitmap bitmap = null;
        ImageDecoder.Source source = null;
        try {
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(context.getContentResolver(), selectedImageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImageUri));
            }
        } catch (Exception ignore) {
        }
        return bitmap;
    }

    /**
     * Method to add Image in External Storage (For Android version above Q WRITE_EXTERNAL_PERMISSION is not needed)
     *
     * @param context   Context context
     * @param directory Storage Directory
     * @return Image Uri
     */
    public static Uri insertImage(Activity context, String directory, String fileName, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, TextUtils.isEmpty(fileName) ? getDefaultImageFileName(false) : fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory);
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            insertImageIntoFileOutput(uri, bitmap);
            contentValues.clear();
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            return uri;
        } else {
            return insertImageWithStoragePermission(context, directory, fileName, bitmap);
        }
    }

    /**
     * Method to save Image into External Storage upto Android P(Android SDK 28) using WRITE_EXTERNAL_PERMISSION needed
     *
     * @param directory Storage Directory
     * @param fileName  File Name
     * @param bitmap    Bitmap image to add to file.
     * @return File Uri
     */
    public static Uri insertImageWithStoragePermission(Activity activity, String directory, String fileName, Bitmap bitmap) {
        if (PermissionCompatBuilder.checkSelfPermission(AppBuilder.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionCompatBuilder.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCompatBuilder.Code.REQ_CODE_WRITE_STORAGE);
            return null;
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + (TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        File file = new File(mediaStorageDir.getPath() + File.separator + (TextUtils.isEmpty(fileName) ? getDefaultImageFileName(true) : fileName));
        insertImageIntoFileOutput(file, bitmap);
        return getFileProviderUri(AppBuilder.getAppContext(), file);
    }

    public static void onRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, onPermissionEnabledListener listener) {
        switch (requestCode) {
            case PermissionCompatBuilder.Code.REQ_CODE_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (listener != null) {
                        listener.onPermissionGranted();
                    }
                } else if (PermissionCompatBuilder.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionCompatBuilder.showRequestPermissionRationaleDialog(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity.getString(R.string.permission_external_storage_denied_msg), new PermissionCompatBuilder.RationalDialogCallback() {
                        @Override
                        public void allowedRequest(String permission) {
                            PermissionCompatBuilder.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCompatBuilder.Code.REQ_CODE_WRITE_STORAGE);
                        }

                        @Override
                        public void deniedRequest(String permission) {
                            DialogBuilder.dismissDialog();
                        }
                    }, false);
                } else {
                    PermissionCompatBuilder.showPermissionDeniedDialog(activity, PermissionCompatBuilder.Code.REQ_CODE_WRITE_STORAGE, activity.getString(R.string.permission_external_storage_denied_msg));
                }
                break;
        }
    }

    public interface onPermissionEnabledListener {
        void onPermissionGranted();
    }


    /**
     * Get File From uri
     *
     * @param uri File uri
     * @return File
     */
    public static File getFileFromUri(Uri uri) {
        return new File(Objects.requireNonNull(uri.getPath()));
    }

    /**
     * Get File Provider to make file available to share to other apps.
     * @param context Application context
     * @return File Provider Uri.
     */
    public static Uri getFileProviderUri(Context context, File file) {
        Uri photoURI = null;
        if (file != null) {
            photoURI = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, file);
        }
        return photoURI;
    }


    /**
     * Insert Image data into file
     *
     * @param uri Uri Image
     * @param bitmap Bitmap image to be inserted into file
     */
    public static void insertImageIntoFileOutput(Uri uri, Bitmap bitmap) {
        OutputStream imageOut = null;
        try {
            imageOut = AppBuilder.getAppContext().getContentResolver().openOutputStream(uri);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);
            }
            if (imageOut != null) {
                imageOut.flush();
                imageOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * Insert Image data into file
     *
     * @param file   Image file
     * @param bitmap Bitmap image to be inserted into file
     */
    public static void insertImageIntoFileOutput(File file, Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Get Default Image File Name
     *
     * @param requireImageFormatSuffix Image Format to be appended eg. png,jpg
     * @return Image File Name
     */
    public static String getDefaultImageFileName(boolean requireImageFormatSuffix) {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        return "JPEG_" + timeStamp + (requireImageFormatSuffix ? PNG_FILE_FORMAT : "");
    }

    /**
     * Get Default Image File Name
     *
     * @param imageFormatSuffix Image Format to be appended eg. png,jpg
     * @return Image File Name
     */
    public static String getDefaultImageFileName(String imageFormatSuffix) {
        return getDefaultImageFileName(false) + imageFormatSuffix;
    }

    private static String getFilePath(Context context, Uri selectedImageUri) {
        String filePathName = "";
        if (selectedImageUri != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filepath = cursor.getString(columnIndex);
                    if (filepath == null) {
                        filePathName = selectedImageUri.getPath();
                    }
                }
                cursor.close();
            } else {
                filePathName = selectedImageUri.getPath();
            }
        } else {
            filePathName = context.getString(R.string.str_file_path);
        }
        return filePathName;
    }

    private static String getFileName(Context context, Uri selectedImageUri) {
        String fileName = "";
        if (selectedImageUri != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int fileNameIndex = cursor.getColumnIndex(filePathColumn[1]);
                    fileName = cursor.getString(fileNameIndex);
                }
                cursor.close();
            } else {
                fileName = selectedImageUri.getLastPathSegment();
            }
        } else {
            fileName = context.getString(R.string.str_file_name);
        }
        return fileName;
    }

    public static boolean isImageFile(Context context, Uri selectedImageUri) {
        final String[] okFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};
        String filename = getFileName(context, selectedImageUri);
        for (String extension : okFileExtensions) {
            if (filename.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to show sharing client installed - Only uri should be file provider uri
     *
     * @param context    Application context
     * @param uri        File Provider Uri
     * @param intentType Intent Type
     */
    public static void openImageSharingClient(Context context, Uri uri, String intentType) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if (uri != null) {
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sharingIntent.setType(TextUtils.isEmpty(intentType) ? FilePickConstants.IMAGE_INTENT_TYPE : intentType);
        }
        try {
            context.startActivity(Intent.createChooser(sharingIntent, "Share image using"));
        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException) {
                showToastMessage(context, context.getString(R.string.str_no_sharing), Toast.LENGTH_LONG);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && e instanceof FileUriExposedException) {
                showToastMessage(context, context.getString(R.string.uri_exposed_exception), Toast.LENGTH_LONG);
            } else {
                showToastMessage(context, context.getString(R.string.str_error), Toast.LENGTH_LONG);
            }
        }
    }

    /**
     * Create Temporary Image file.
     * @param context Application context
     * @return Empty Temporary storage file
     */
    public static Uri createTempImageFile(Context context, Bitmap bitmap) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File file = File.createTempFile(getDefaultImageFileName(false), ".png", storageDir);
            if (bitmap != null) {
                insertImageIntoFileOutput(file, bitmap);
            }
            MediaFiles.cameraPhotoPath = file.getAbsolutePath();
            return getFileProviderUri(context,file);
        } catch (IOException e) {
            return null;
        }
    }


    private static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    public static void showToastMessage(Context context, String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    private void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public File getFile() {
        return file;
    }

    private void setFile(File file) {
        this.file = file;
    }

    public String getFilePath() {
        return filePath;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    private void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getByteData() {
        return byteData;
    }

    private void setByteData(byte[] byteData) {
        this.byteData = byteData;
    }

    public Uri getUri() {
        return uri;
    }

    private void setUri(Uri uri) {
        this.uri = uri;
    }

    public static String getCameraPhotoPath() {
        return cameraPhotoPath;
    }
}

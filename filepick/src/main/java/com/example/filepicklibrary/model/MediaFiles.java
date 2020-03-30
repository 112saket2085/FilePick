package com.example.filepicklibrary.model;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.filepicklibrary.R;
import com.example.filepicklibrary.app.FilePickConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.filepicklibrary.app.FilePickConstants.FILE_PROVIDER_NAME;

/**
 * MediaFiles - Class that store various image results.
 * Created by SAKET on 29/03/2020
 */
public class MediaFiles {

    private Bitmap bitmap;
    private File file;
    private String filePath="";
    private String fileName="";
    private long fileSize;
    private byte[] byteData;
    private Uri uri;
    private static String cameraPhotoPath;

    public static MediaFiles getMediaFiles(Context context, Uri selectedImageUri) {
        MediaFiles mediaFiles = new MediaFiles();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImageUri);
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
            mediaFiles.setBitmap(bitmap);
            selectedImageUri = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, new File(Objects.requireNonNull(path)));
            mediaFiles.setUri(selectedImageUri);
            return mediaFiles;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), mediaFiles.getBitmap(), "Title", null);
                mediaFiles.setUri(Uri.parse(path));
            }
            return mediaFiles;
        }
    }

    private static String getFilePath(Context context,Uri selectedImageUri) {
        String filePathName = "";
        if(selectedImageUri!=null) {
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
        }
        else {
            filePathName=context.getString(R.string.str_file_path);
        }
        return filePathName;
    }

    private static String getFileName(Context context,Uri selectedImageUri) {
        String fileName = "";
        if(selectedImageUri!=null) {
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
        }
        else {
            fileName=context.getString(R.string.str_file_name);
        }
        return fileName;
    }

    public static boolean isImageFile(Context context,Uri selectedImageUri) {
        final String[] okFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};
        String filename = getFileName(context,selectedImageUri);
        for (String extension : okFileExtensions) {
            if (filename.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to show sharing client installed - Only uri should be file provider uri
     * @param context Application context
     * @param uri File Provider Uri
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
            } else if (e instanceof FileUriExposedException) {
                showToastMessage(context, context.getString(R.string.uri_exposed_exception), Toast.LENGTH_LONG);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create Temporary Image file.
     * @param context Application context
     * @return Empty Temporary storage file
     */
    public static File createEmptyTempImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File file= File.createTempFile(imageFileName, ".jpg", storageDir);
            MediaFiles.cameraPhotoPath = file.getAbsolutePath();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     *
     * @param context Application context
     * @return Empty Temporary storage file Uri to be used in Intent Extra Output to store image.
     */
    public static Uri getCameraFileProviderUri(Context context) {
        File file = MediaFiles.createEmptyTempImageFile(context);
        Uri photoURI = null;
        if (file != null) {
            photoURI = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, file);
        }
        return photoURI;
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

    public static void showToastMessage(Context context,String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
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

    public  byte[] getByteData() {
        return byteData;
    }

    private   void setByteData(byte[] byteData) {
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

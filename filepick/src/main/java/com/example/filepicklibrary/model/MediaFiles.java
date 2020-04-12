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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.filepicklibrary.R;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.utility.DialogBuilder;
import com.example.filepicklibrary.utility.PermissionCompatBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import id.zelory.compressor.Compressor;
import static com.example.filepicklibrary.app.FilePickConstants.FILE_PROVIDER_NAME;
import static com.example.filepicklibrary.app.FilePickConstants.PNG_FILE_FORMAT;

/**
 * MediaFiles - Class that store various image results.
 * Created by SAKET on 29/03/2020
 */
public class MediaFiles {

    private File file;
    private String filePath = "";
    private String fileName = "";
    private String fileSize;
    private Uri uri;
    private static String cameraPhotoPath;


    /**
     * Media Files containing various properties of file eg. Bitmap,File,File Size,File Path,File Uri.
     *
     * @param selectedImageUri Image Uri
     * @return MediaFiles
     */
    public static MediaFiles getMediaFiles(Context context,Uri selectedImageUri) {
        MediaFiles mediaFiles = new MediaFiles();
        try {
            String path=getFilePath(context,selectedImageUri);
            mediaFiles.file = getFileFromPath(path);
            mediaFiles.filePath = path;
            mediaFiles.fileName = mediaFiles.file.getName();
            mediaFiles.fileSize = getFileSize(mediaFiles.file);
            mediaFiles.uri = selectedImageUri;

            return mediaFiles;
        } catch (Exception e) {
            return mediaFiles;
        }
    }

    /**
     * Get Image Bitmap - Use this method to get Bitmap on onActivity Result otherwise you will get Security Exception.
     * You can also use @link(getBitmap(File file) to get Bitmap.
     * Life of Uri is not too long to perform operations.
     * Dont use this method if
     * @param selectedImageUri Uri
     * @return Image Bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static Bitmap getBitmap(Context context,Uri selectedImageUri) {
        Bitmap bitmap = null;
        ImageDecoder.Source source = null;
        source = ImageDecoder.createSource(context.getContentResolver(), selectedImageUri);
        try {
            bitmap = ImageDecoder.decodeBitmap(source);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Get Image Bitmap - Use this method to get Bitmap
     * @param file File
     * @return Image Bitmap
     */

    public static Bitmap getBitmap(File file) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * Method to add Image into External Storage (For Android version above Q WRITE_EXTERNAL_PERMISSION is not needed)
     * For Android Version P and below @link(storeImageWithStoragePermission() will be called.
     * @param context   Context context
     * @param directory Storage Directory
     * @return Image Uri
     */
    public static Uri storeImageByte(Activity context, String directory, String fileName, byte[] data) {
        return storeImageIntoFile(context,directory,fileName,null,data);
    }

    /**
     * Method to add Image into External Storage (For Android version above Q WRITE_EXTERNAL_PERMISSION is not needed)
     * For Android Version P and below @link(storeImageWithStoragePermission() will be called.
     * @param context   Context context
     * @param directory Storage Directory
     * @return Image Uri
     */
    public static Uri storeImageBitmap(Activity context, String directory, String fileName,Bitmap bitmap) {
        return storeImageIntoFile(context,directory,fileName,bitmap,null);
    }


    private static Uri storeImageIntoFile(Activity context, String directory, String fileName, Bitmap bitmap,byte[] data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, TextUtils.isEmpty(fileName) ? getDefaultImageFileName(false) : fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory);
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            insertImageIntoFileOutput(context,uri, bitmap,data);
            contentValues.clear();
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            return uri;
        } else {
            return storeImageWithStoragePermission(context, directory, fileName, bitmap,data);
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
    private static Uri storeImageWithStoragePermission(Activity activity, String directory, String fileName, Bitmap bitmap,byte[] data) {
        if (PermissionCompatBuilder.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionCompatBuilder.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCompatBuilder.Code.REQ_CODE_WRITE_STORAGE);
            return null;
        }
        File mediaStorageDir = new File(getExternalStorageDirectoryPath(activity,directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        File file = new File(mediaStorageDir.getPath() + File.separator + (TextUtils.isEmpty(fileName) ? getDefaultImageFileName(true) : fileName));
        insertImageIntoFileOutput(file, bitmap,data);
        return getFileProviderUri(activity,file);
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
    public static File getFileFromUri(Context context,Uri uri) {
        return new File(getFilePath(context,uri));
    }

    /**
     * Get File From path
     *
     * @param path File Path
     * @return File
     */
    public static File getFileFromPath(String path) {
        return new File(path);
    }

    /**
     * Get File Provider to make file available to share to other apps.
     *
     * @return File Provider Uri.
     */
    public static Uri getFileProviderUri(Context context,File file) {
        Uri photoURI = null;
        if (file != null) {
            photoURI = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, file);
        }
        return photoURI;
    }


    /**
     * Insert Image data into file
     *
     * @param uri    Uri Image
     * @param bitmap Bitmap image to be inserted into file
     */
    public static void insertImageIntoFileOutput(Context context,Uri uri, Bitmap bitmap, byte[] data) {
        OutputStream imageOut = null;
        try {
            imageOut = context.getContentResolver().openOutputStream(uri);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, imageOut);
            } else if (imageOut != null && data != null) {
                imageOut.write(data);
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
    public static void insertImageIntoFileOutput(File file, Bitmap bitmap,byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos);
            }
            else if(data!=null) {
                fos.write(data);
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

    /**
     * Get File Path
     * @param selectedImageUri Uri
     * @return File Path
     */
    public static String getFilePath(Context context,Uri selectedImageUri) {
        String filePathName = "";
        try {
            if (selectedImageUri != null) {
                String[] filePathColumn = {"_data", MediaStore.Images.Media.DISPLAY_NAME};
                Cursor cursor = context.getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        if (columnIndex != -1) {
                            filePathName = cursor.getString(columnIndex);
                            if (TextUtils.isEmpty(filePathName)) {
                                filePathName = selectedImageUri.getPath();
                            }
                        } else {
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
        } catch (Exception e) {
            filePathName = Objects.requireNonNull(selectedImageUri.getPath());
        }
        return filePathName;
    }

    /**
     * Get File Name
     * @param selectedImageUri Uri
     * @return File Name
     */
    public static String getFileName(Uri selectedImageUri) {
        return Objects.requireNonNull(selectedImageUri.getLastPathSegment());
    }

    /**
     * Use this method to check if uri is of type image
     * @param selectedImageUri Uri
     * @return true if image file
     */
    public static boolean isImageFile(Context context,Uri selectedImageUri) {
        final String[] okFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};
        String filename = getFilePath(context,selectedImageUri);
        for (String extension : okFileExtensions) {
            if (filename.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to show sharing client installed - Uri should be file provider uri
     *
     * @param uri        File Provider Uri
     * @param intentType Intent Type
     */
    public static void openImageSharingClient(Context context,Uri uri, String intentType) {
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
                showToastMessage(context,context.getString(R.string.str_no_sharing), Toast.LENGTH_LONG);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && e instanceof FileUriExposedException) {
                showToastMessage( context,context.getString(R.string.uri_exposed_exception), Toast.LENGTH_LONG);
            } else {
                showToastMessage(context,context.getString(R.string.str_error), Toast.LENGTH_LONG);
            }
        }
    }

    /**
     * Create Temporary Image file,file will be deleted on app uninstall.
     * @param data byte dta
     * @return Temporary storage file Uri
     */
    public static Uri createCacheByteFile(Context context,byte[] data,String directory) {
        File file= createTempFile(context,null,data,false,directory);
        return getFileProviderUri(context,file);
    }

    /**
     * Create Temporary Image file,file will be deleted on app uninstall.
     * @param bitmap Bitmap Image
     * @param directory File Additional directory
     * @return Temporary storage file Uri
     */
    public static Uri createCacheBitmapFile(Context context,Bitmap bitmap,String directory) {
        File file= createTempFile(context,bitmap,null,false,directory);
        return getFileProviderUri(context,file);
    }

    /**
     * Create Temporary Image file,file will be deleted on app uninstall.
     * @param bitmap Bitmap Image
     * @param directory File Additional directory
     * @return Temporary storage file Uri
     */
    public static Uri createTempBitmapFile(Context context,Bitmap bitmap,String directory) {
        File file= createTempFile(context,bitmap,null,true,directory);
        return getFileProviderUri(context,file);
    }

    /**
     * Create Temporary Image file,file will be deleted on app uninstall.
     * @param data byte dta
     * @param directory File Additional directory
     * @return Temporary storage file Uri
     */
    public static Uri createTempByteFile(Context context,byte[] data,String directory) {
        File file= createTempFile(context,null,data,true,directory);
        return getFileProviderUri(context,file);
    }

    /**
     * @param bitmap Bitmap Image
     * @param data byte data
     * @param directory File Additional directory
     * @return Temporary File
     */
    public static File createTempFile(Context context,Bitmap bitmap, byte[] data,boolean isFiesDir,String directory) {
        File storageDir = new File(isFiesDir ? getExternalFilesDirectoryPath(context,directory) : getExternalCacheDirectoryPath(context,directory));
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }
        try {
            File file=new File(storageDir.getPath() + File.separator+getDefaultImageFileName(true));
            insertImageIntoFileOutput(file, bitmap, data);
            MediaFiles.cameraPhotoPath = file.getAbsolutePath();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get External Storage Directory
     * @param context App Context
     * @return Directory Path
     */
    public static String getExternalStorageDirectoryPath(Context context,String directory) {
        return Environment.getExternalStorageDirectory() + File.separator + (TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory);
    }

    /**
     * Get External Cache Directory
     * @param context App Context
     * @return Directory Path
     */
    public static String getExternalCacheDirectoryPath(Context context,String directory) {
        return context.getExternalCacheDir() + File.separator + (TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory);
    }

    /**
     * Get External File Directory
     * @param context App Context
     * @return Directory Path
     */
    public static String getExternalFilesDirectoryPath(Context context,String directory) {
        return context.getExternalFilesDir("") + File.separator + (TextUtils.isEmpty(directory) ? FilePickConstants.PICTURE_FOLDER : directory);
    }

    /**
     * Method to get File Bytes
     *
     * @param file File file
     * @return File bytes
     * @throws IOException Exception
     */
    public static byte[] getFileBytes(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len;
        while ((len = Objects.requireNonNull(inputStream).read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    /**
     * Get File Sie
     *
     * @param file File
     * @return File size
     */
    public static String getFileSize(File file) {
        long length = file.length();
        if (length <= 1000) {
            return length + " " + "bytes";
        }
        length = file.length() / 1024;
        if (length <= 1000) {
            return length + " " + "KB";
        }
        length = (file.length() / 1024) / 1024;
        if (length <= 1000) {
            return length + " " + "MB";
        } else {
            return "";
        }
    }

    /**
     * Load Image using Glide
     * @param imageView ImageView to load image into
     * @param placeholderResId Placeholder Resource
     * @param url Url to load
     */
    public static void loadImageUsingGlide(Context context,ImageView imageView,String url, int placeholderResId, final GlideListener listener) {
        loaGlideIntoView(context,imageView,null,null,null,url,placeholderResId,listener);
    }

    /**
     * Load Image using Glide
     * @param imageView ImageView to load image into
     * @param placeholderResId Placeholder Resource
     * @param data byte data
     */
    public static void loadImageUsingGlide(Context context,ImageView imageView, byte[] data, int placeholderResId, final GlideListener listener) {
        loaGlideIntoView(context,imageView,null,null,data,null,placeholderResId,listener);
    }

    /**
     * Load Image using Glide
     * @param imageView ImageView to load image into
     * @param placeholderResId Placeholder Resource
     * @param uri Image Uri
     */
    public static void loadImageUsingGlide(Context context,ImageView imageView, Uri uri, int placeholderResId, final GlideListener listener) {
        loaGlideIntoView(context,imageView,uri,null,null,null,placeholderResId,listener);
    }

    /**
     * Load Image using Glide
     * @param imageView ImageView to load image into
     * @param placeholderResId Placeholder Resource
     * @param file Image File
     */
    public static void loadImageUsingGlide(Context context,ImageView imageView, File file, int placeholderResId, final GlideListener listener) {
        loaGlideIntoView(context,imageView,null,file,null,null,placeholderResId,listener);
    }

    private static void loaGlideIntoView(Context context,ImageView imageView, Uri uri, File file, byte[] data,String url, int placeholderResId, final GlideListener listener) {
        RequestBuilder<Bitmap> bitmapRequestBuilder=Glide.with(context).asBitmap();
        Glide.with(context).asBitmap().load(uri != null ? uri : (file != null ? file : (data != null ? data : url))).placeholder(placeholderResId).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                if (listener != null) {
                    listener.onLoadFailed(e, model,isFirstResource);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                if (listener != null) {
                    listener.onResourceReady(resource, model,isFirstResource);
                }
                return false;
            }
        }).into(imageView);

    }

    public interface GlideListener {
        void onLoadFailed(@Nullable Exception e, Object model, boolean isFirstResource);
        void onResourceReady(Bitmap resource, Object model, boolean isFirstResource);
    }


    /**
     * Get Compressed Bitmap image  Set default value to -1 for int variable and null for Object type
     * @param file File
     * @param destinationDirectoryPath Destination path to insert compressed file
     * @param quality Image Quality Default set to 100
     * @param compressFormat Image CompressedFormat Default set to WEBP
     * @param maxWidth Image Width Default set to 612
     * @param maxHeight Image Height Default set to 816
     * @return Compressed Bitmap image
     */
    public static File getCompressedImageFile(Context context,File file,String destinationDirectoryPath,int quality,Bitmap.CompressFormat compressFormat,int maxWidth,int maxHeight) {
        File compressedFile = null;
        try {
            Compressor compressor=new Compressor(context);
            compressor.setDestinationDirectoryPath(TextUtils.isEmpty(destinationDirectoryPath) ? "" : destinationDirectoryPath);
            compressor.setQuality(quality == -1 ? 80 : quality);
            compressor.setCompressFormat(compressFormat == null ? Bitmap.CompressFormat.WEBP : compressFormat);
            compressor.setMaxWidth(maxWidth == -1 ? 612 : maxWidth );
            compressor.setMaxHeight(maxHeight == -1 ? 816 : maxHeight);
            compressedFile = compressor.compressToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedFile;
    }

    /**
     * Get Compressed Bitmap image   Set default value to -1 for int variable and null for Object type
     * @param file File
     * @param destinationDirectoryPath Destination path to insert compressed file
     * @param quality Image Quality Default set to 100
     * @param compressFormat Image CompressedFormat Default set to WEBP
     * @param maxWidth Image Width Default set to 612
     * @param maxHeight Image Height Default set to 816
     * @return Compressed Bitmap image
     */
    public static Bitmap getCompressedImageBitmap(Context context,File file,String destinationDirectoryPath,int quality,Bitmap.CompressFormat compressFormat,int maxWidth,int maxHeight) {
        Bitmap compressedImageBitmap = null;
        try {
            Compressor compressor=new Compressor(context);
            compressor.setDestinationDirectoryPath(TextUtils.isEmpty(destinationDirectoryPath) ? "" : destinationDirectoryPath);
            compressor.setQuality(quality == -1 ? 100 : quality);
            compressor.setCompressFormat(compressFormat == null ? Bitmap.CompressFormat.WEBP : compressFormat);
            compressor.setMaxWidth(maxWidth == -1 ? 612 : maxWidth );
            compressor.setMaxHeight(maxHeight == -1 ? 816 : maxHeight);
            compressedImageBitmap = compressor.compressToBitmap(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedImageBitmap;
    }

    /**
     * Get View Bitmap
     * @param view View
     * @return Image Bitmap
     */
    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    /**
     * Method to show Toast Message
     * @param msg Message
     * @param duration Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void showToastMessage(Context context,String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }

    public File getFile() {
        return file;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public Uri getUri() {
        return uri;
    }

    public static String getCameraPhotoPath() {
        return cameraPhotoPath;
    }
}

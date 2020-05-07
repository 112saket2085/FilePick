package com.example.filepick;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.app.FilePickIntentCreator;
import com.example.filepicklibrary.model.Configuration;
import com.example.filepicklibrary.model.MediaFiles;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button buttonAdd;
    private MediaFiles mediaFiles;
    private int selectedViewId;
    private Menu menu;
    private boolean isCropRequired;
    private  String DOWNLOAD_FOLDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setOnClickListener();
    }

    private void initViews() {
        DOWNLOAD_FOLDER="Pictures/"+getString(R.string.app_name);
        imageView=findViewById(R.id.image_view);
        buttonAdd=findViewById(R.id.button_add);
    }

    private void setOnClickListener() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configuration configuration=new Configuration.Builder().setCropRequired(isCropRequired).setAspectRatioX(1).setAspectRatioY(1).build();
                FilePickIntentCreator.loadFilePickerRequest(MainActivity.this,configuration);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu=menu;
        getMenuInflater().inflate(R.menu.menu_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        selectedViewId = item.getItemId();
        switch (item.getItemId()) {
            case R.id.item_info:
                if (mediaFiles == null) {
                    MediaFiles.showToastMessage(this,getString(R.string.str_select_file), Toast.LENGTH_LONG);
                    break;
                }
                showFileInfoDialog(mediaFiles.getFilePath(),mediaFiles.getFileName(),mediaFiles.getFileSize());
                break;
            case R.id.item_share:
                if (mediaFiles == null) {
                    MediaFiles.showToastMessage(this,getString(R.string.str_select_file), Toast.LENGTH_LONG);
                    break;
                }
                //Use below method to get compressed Bitmap image
                //Bitmap bitmap = MediaFiles.getCompressedImageBitmap(mediaFiles.getFile(),-1);

                //Use below method to get Bitmap Image From View
                Bitmap bitmap=MediaFiles.getBitmapFromView(imageView);

                Uri uri = null;
//                //Use below technique store Image into Pdf File
                try {
                     uri = MediaFiles.getImageAsPdf(this,MediaFiles.getExternalCacheDirectoryFile(this,"",MediaFiles.getDefaultPdfFileName()),MediaFiles.getByteFromBitmap(bitmap,Bitmap.CompressFormat.PNG));
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                //Use below technique to create temp image File and insert bitmap in external storage
//                try {
//                    uri = MediaFiles.storeImageIntoAppStorage(this,MediaFiles.getExternalFilesDirectoryFile(this,"","TEMP_FILE.png"),bitmap, Bitmap.CompressFormat.PNG);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                MediaFiles.openImageSharingClient(this,uri, FilePickConstants.IMAGE_INTENT_TYPE);
                break;
            case R.id.item_download:
                if (mediaFiles == null) {
                    MediaFiles.showToastMessage(this,getString(R.string.str_select_file), Toast.LENGTH_LONG);
                    break;
                }
                //Use below method to get compressed Bitmap image
                //Bitmap bitmap = MediaFiles.getCompressedImageBitmap(mediaFiles.getFile(),-1);

                //Use below method to get Bitmap Image From View
                Bitmap bitmapImage=MediaFiles.getBitmapFromView(imageView);
                //Use below technique to create image File and insert bitmap in external storage
                Uri imageUri = MediaFiles.storeImageIntoExternalStorage(this, DOWNLOAD_FOLDER, "", bitmapImage,Bitmap.CompressFormat.PNG);
                if (imageUri != null) {
                    MediaFiles.showToastMessage(this,getString(R.string.str_file_Stored, DOWNLOAD_FOLDER), Toast.LENGTH_LONG);
                }
                break;
            case R.id.item_compress:
                if (mediaFiles == null) {
                    MediaFiles.showToastMessage(this,getString(R.string.str_select_file), Toast.LENGTH_LONG);
                    break;
                }
                //Use below method for Image Compression
                File compressedFile = null;
                try {
                    compressedFile = MediaFiles.getCompressedImageFile(this,mediaFiles.getFile(), MediaFiles.getExternalCacheDirectoryPath(this,""), -1, null, -1, -1);
                    loadImage(mediaFiles.getFile());
                    String size = MediaFiles.getFileSize(compressedFile);
                    showFileInfoDialog(mediaFiles.getFilePath(),mediaFiles.getFileName(),size);
                    MediaFiles.showToastMessage(this,getString(R.string.str_file_compressed), Toast.LENGTH_LONG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.item_enable_crop:
                item.setChecked(!item.isChecked());
                isCropRequired = item.isChecked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickConstants.REQ_CODE_FILE_PICK) {
            switch (resultCode) {
                case RESULT_OK:
                    mediaFiles = FilePickIntentCreator.getFilePickSuccessResult(this,data);
                    if (mediaFiles != null) {
                        // Get various Image file output eg. Bitmap,File Size,File Path,File bytes,File Uri.
                        Uri uri=mediaFiles.getUri();
                        loadImage(mediaFiles.getFile());
                        // getValue();
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

    /**
     * Load Image With Glide
     * @param file File
     */
    private void loadImage(File file) {
        MediaFiles.loadImageUsingGlide(this,imageView, file, -1, new MediaFiles.GlideListener() {
            @Override
            public void onLoadFailed(@Nullable Exception e, Object model, boolean isFirstResource) {
            }
            @Override
            public void onResourceReady(Bitmap resource, Object model, boolean isFirstResource) {
            }
        });
    }

    /**
     * Use below Technique to get Image Bitmap and Byte Array once image is selected from camera or gallery.
     */
    private void getValue() {
        if (mediaFiles != null) {
            Bitmap bitmap=getBitmap();
            try {
                byte[] bytes = MediaFiles.getFileBytes(mediaFiles.getFile());
                Log.d("value is", "" + Arrays.toString(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to get Bitmap
     * @return Bitmap Iamge
     */
    private Bitmap getBitmap() {
        Bitmap bitmap;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            bitmap = MediaFiles.getBitmap(this,mediaFiles.getUri()); //If Security Exception occurs, you can also use @link(MediaFiles.getBitmap(File file) to get Bitmap.
        } else {
            bitmap = MediaFiles.getBitmap(mediaFiles.getFile());
        }
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Use below technique to request WRITE_EXTERNAL_STORAGE permission and create file
        MediaFiles.onRequestPermissionsResult(this, requestCode, permissions, grantResults, new MediaFiles.onPermissionEnabledListener() {
            @Override
            public void onPermissionGranted() {
              onOptionsItemSelected(menu.findItem(selectedViewId));
            }
        });
    }

    private void showFileInfoDialog(String filePath,String fileName,String fileSize) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_file_details, (ViewGroup) null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(true);
        TextView textViewFileName=view.findViewById(R.id.textViewFileName);
        TextView textViewFilePath=view.findViewById(R.id.textViewFilePath);
        TextView textViewFileSize =view.findViewById(R.id.textViewFileSie);
        textViewFileName.setText(getString(R.string.str_file_name_detail, fileName));
        textViewFilePath.setText(getString(R.string.str_file_path_detail,filePath));
        textViewFileSize.setText(getString(R.string.str_file_size_detail, fileSize));
        Dialog dialog=builder.create();
        dialog.show();
    }

}

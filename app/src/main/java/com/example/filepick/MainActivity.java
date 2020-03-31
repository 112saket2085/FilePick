package com.example.filepick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.app.FilePickIntentCreator;
import com.example.filepicklibrary.model.Configuration;
import com.example.filepicklibrary.model.MediaFiles;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button buttonAdd;
    private MediaFiles mediaFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image_view);
        buttonAdd=findViewById(R.id.button_add);
        setOnClickListener();
    }

    private void setOnClickListener() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configuration builder=new Configuration.Builder().setCropRequired(false).setAspectRatioX(1).setAspectRatioY(1).build();
                FilePickIntentCreator.loadFilePickerRequest(MainActivity.this,builder);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                if (mediaFiles != null) {
                    MediaFiles.openImageSharingClient(MainActivity.this, mediaFiles.getUri(), FilePickConstants.IMAGE_INTENT_TYPE);
                }
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
                    mediaFiles = FilePickIntentCreator.getFilePickSuccessResult(data);
                    if (mediaFiles != null) {
                        // Get various Image file output eg. Bitmap,File Size,File Path,File bytes,File Uri.
                        Bitmap bitmap = mediaFiles.getBitmap();
                        imageView.setImageBitmap(bitmap);
                        //Use below technique to create image File and insert bitmap in external storage
                        Uri uri = MediaFiles.storeImage(this, "Pictures/Example/", "", mediaFiles.getBitmap());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Use below technique to request WRITE_EXTERNAL_STORAGE permission and create file
        MediaFiles.onRequestPermissionsResult(this, requestCode, permissions, grantResults, new MediaFiles.onPermissionEnabledListener() {
            @Override
            public void onPermissionGranted() {
                Uri uri = MediaFiles.storeImage(MainActivity.this, "Pictures/Example/", MediaFiles.getDefaultImageFileName(true), mediaFiles.getBitmap());
                MediaFiles.openImageSharingClient(MainActivity.this, uri, FilePickConstants.IMAGE_INTENT_TYPE);
            }
        });
    }
}

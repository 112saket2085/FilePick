package com.example.filepick;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.app.FilePickIntentCreator;
import com.example.filepicklibrary.model.Configuration;
import com.example.filepicklibrary.model.MediaFiles;

import java.io.File;
import java.io.FileOutputStream;

import static com.example.filepicklibrary.model.MediaFiles.getOutputMediaFile;
import static com.example.filepicklibrary.model.MediaFiles.insertImage;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button buttonAdd;
    private Button buttonShareFile;
    private MediaFiles mediaFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image_view);
        buttonAdd=findViewById(R.id.button_add);
        buttonShareFile=findViewById(R.id.button_share);
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
        buttonShareFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaFiles.openImageSharingClient(MainActivity.this,mediaFiles.getUri(),FilePickConstants.IMAGE_INTENT_TYPE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickConstants.REQ_CODE_FILE_PICK) {
            switch (resultCode) {
                case RESULT_OK:
                    mediaFiles = FilePickIntentCreator.getFilePickSuccessResult(data);
                    if (mediaFiles != null) {
                        buttonShareFile.setVisibility(View.VISIBLE);
                        Bitmap bitmap = mediaFiles.getBitmap();
                        imageView.setImageBitmap(bitmap);
                        //Method to create File in external storage
                        Uri uri = insertImage(this,"/SaketBhai", "",mediaFiles.getBitmap());
                        MediaFiles.openImageSharingClient(MainActivity.this,uri,FilePickConstants.IMAGE_INTENT_TYPE);
                    }
                    break;
                case RESULT_CANCELED:
                    break;
                case RESULT_FIRST_USER:
                    break;
            }
        }
    }
}

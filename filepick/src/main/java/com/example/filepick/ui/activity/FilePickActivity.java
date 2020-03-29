package com.example.filepick.ui.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filepick.R;
import com.example.filepick.app.FilePickConstants;
import com.example.filepick.model.Configuration;
import com.example.filepick.model.FileItemModel;
import com.example.filepick.ui.adapter.FileItemAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static com.example.filepick.app.FilePickConstants.INTENT_FILE_TEXT;

/**
 * File Pick Activity that shows all file options available in Bottom Sheet that can handle file request.
 * Created by SAKET on 29/03/2020
 */

public class FilePickActivity extends AppCompatActivity implements FileItemAdapter.OnBottomSheetItemClickListener {

    private BottomSheetDialog bottomSheetDialog;
    private TextView textView;
    private List<FileItemModel> fileItemModelList = new ArrayList<>();
    private List<Intent> filePickerIntents = new ArrayList<>();
    private List<Drawable> fileIconList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FileItemAdapter fileItemAdapter;
    private Intent fileIntent;
    private String cameraPhotoPath;
    private Configuration configuration = new Configuration();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentValues();
        setFilePickerIntents();
        setFileIteModel();
        showBottomSheetDialog();
        initRecyclerView();
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        if (intent != null) {
            configuration = (Configuration) intent.getSerializableExtra(FilePickConstants.FILE_PICK_REQUEST);
        }
    }

    /**
     * Create a chooser intent to select the  source to get image from.<br>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br>
     * All possible sources are added to the intent chooser.
     */
    public void setFilePickerIntents() {
        //Add All Camera Intents
        List<Intent> cameraIntents = getCameraIntents();
        if (!cameraIntents.isEmpty()) {
            filePickerIntents.addAll(cameraIntents);
        }

        //Add All Gallery Intents
        List<Intent> galleryIntents = getGalleryIntents();
        if (!galleryIntents.isEmpty()) {
            filePickerIntents.addAll(galleryIntents);
        }
    }

    /**
     * Get all Camera intents for capturing image using device camera apps.
     */
    public List<Intent> getCameraIntents() {
        List<Intent> allIntents = new ArrayList<>();
        Intent captureIntent = new Intent(ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        fileIconList.clear();
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            fileIconList.add(res.activityInfo.loadIcon(packageManager));
            String title = res.activityInfo.loadLabel(packageManager).toString();
            intent.putExtra(INTENT_FILE_TEXT, title);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        return allIntents;
    }

    /**
     * Get all Gallery intents for getting image from one of the apps of the device that handle images.
     */
    public List<Intent> getGalleryIntents() {
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent = new Intent(ACTION_GET_CONTENT);
        galleryIntent.setType(configuration.getIntentType());
        if(configuration.getIntentType().equalsIgnoreCase(FilePickConstants.FILE_INTENT_TYPE)) {
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            fileIconList.add(res.activityInfo.loadIcon(packageManager));
            String title = res.activityInfo.loadLabel(packageManager).toString();
            intent.putExtra(INTENT_FILE_TEXT, title);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }
        return intents;
    }

    public void showBottomSheetDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_file_picker, (ViewGroup) null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);
        recyclerView = view.findViewById(R.id.recycler_view);
        textView = view.findViewById(R.id.text_view_title);
        textView.setText(configuration.getBottomSheetTitle());
        bottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        bottomSheetDialog.show();
    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        fileItemAdapter = new FileItemAdapter(fileItemModelList, this);
        recyclerView.setAdapter(fileItemAdapter);

    }

    private void setFileIteModel() {
        for (int i = 0; i < filePickerIntents.size(); i++) {
            Intent intent = filePickerIntents.get(i);
            String title = intent.getStringExtra(INTENT_FILE_TEXT);
            setFileItemList(new FileItemModel(intent, fileIconList.get(i)));
        }
    }

    private void setFileItemList(FileItemModel fileItemModel) {
        fileItemModelList.add(fileItemModel);
    }


    @Override
    public void onBottomSheetClick(int position) {

    }
}

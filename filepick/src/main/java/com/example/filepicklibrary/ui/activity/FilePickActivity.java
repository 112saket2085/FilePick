package com.example.filepicklibrary.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filepicklibrary.R;
import com.example.filepicklibrary.app.FilePickConstants;
import com.example.filepicklibrary.model.Configuration;
import com.example.filepicklibrary.model.FileItemModel;
import com.example.filepicklibrary.model.MediaFiles;
import com.example.filepicklibrary.ui.adapter.FileItemAdapter;
import com.example.filepicklibrary.utility.DialogBuilder;
import com.example.filepicklibrary.utility.PermissionCompatBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.ACTION_PICK;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static com.example.filepicklibrary.app.FilePickConstants.ERROR_CODE_FILE_PICK_;
import static com.example.filepicklibrary.app.FilePickConstants.INTENT_FILE_PICK;
import static com.example.filepicklibrary.app.FilePickConstants.INTENT_FILE_TEXT;

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
    private Configuration configuration = new Configuration();
    private Intent resultingIntent;

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
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            allIntents.add(intent);
        }

        return allIntents;
    }

    /**
     * Get all Gallery intents for getting image from one of the apps of the device that handle images.
     */
    public List<Intent> getGalleryIntents() {
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent = new Intent(ACTION_PICK);
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
            String name = res.activityInfo.packageName;
            intent.putExtra(INTENT_FILE_TEXT, title);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            setFileItemList(new FileItemModel(intent, fileIconList.get(i)));
        }
    }

    private void setFileItemList(FileItemModel fileItemModel) {
        fileItemModelList.add(fileItemModel);
    }


    @Override
    public void onBottomSheetClick(Intent intent) {
        this.resultingIntent=intent;
        if(bottomSheetDialog!=null) {
            bottomSheetDialog.dismiss();
        }
        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(MediaStore.ACTION_IMAGE_CAPTURE)) {
            launchCamera(intent);
            return;
        }
        launchGallery(intent);
    }

    private void launchGallery(Intent intent) {
        if(PermissionCompatBuilder.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionCompatBuilder.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PermissionCompatBuilder.Code.REQ_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        startActivityForResult(intent, INTENT_FILE_PICK);
    }


    private void launchCamera(Intent intent) {
        if(configuration.isCameraPermissionRequired() && PermissionCompatBuilder.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            PermissionCompatBuilder.requestPermissions(this,new String[]{Manifest.permission.CAMERA},PermissionCompatBuilder.Code.REQ_CODE_CAMERA);
            return;
        }
        Uri photoURI = MediaFiles.createTempBitmapFile(this,null,"");
        if (photoURI != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }
        startActivityForResult(intent, INTENT_FILE_PICK);
    }


    /**
     * Added Read External Storage Permission to grant access to file for File Compression and for using File to load image using Glide
     *
     * @param requestCode  RequestCode
     * @param permissions  Permissions
     * @param grantResults Grant Results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionCompatBuilder.Code.REQ_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onBottomSheetClick(resultingIntent);
                } else if (PermissionCompatBuilder.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    PermissionCompatBuilder.showRequestPermissionRationaleDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.permission_read_external_storage_denied_msg), new PermissionCompatBuilder.RationalDialogCallback() {
                        @Override
                        public void allowedRequest(String permission) {
                            PermissionCompatBuilder.requestPermissions(FilePickActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionCompatBuilder.Code.REQ_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
                        }

                        @Override
                        public void deniedRequest(String permission) {
                            DialogBuilder.dismissDialog();
                            setFilePickErrorResult();
                            MediaFiles.showToastMessage(FilePickActivity.this, getString(R.string.str_permission_denied), Toast.LENGTH_SHORT);
                        }
                    }, false);
                } else {
                    PermissionCompatBuilder.showPermissionDeniedDialog(this, PermissionCompatBuilder.Code.REQ_CODE_READ_EXTERNAL_STORAGE_PERMISSION, getString(R.string.permission_read_external_storage_denied_msg));
                }
                break;
            case PermissionCompatBuilder.Code.REQ_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onBottomSheetClick(resultingIntent);
                } else if (PermissionCompatBuilder.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    PermissionCompatBuilder.showRequestPermissionRationaleDialog(this, Manifest.permission.CAMERA, getString(R.string.permission_camera_denied_msg), new PermissionCompatBuilder.RationalDialogCallback() {
                        @Override
                        public void allowedRequest(String permission) {
                            PermissionCompatBuilder.requestPermissions(FilePickActivity.this, new String[]{Manifest.permission.CAMERA}, PermissionCompatBuilder.Code.REQ_CODE_CAMERA);
                        }

                        @Override
                        public void deniedRequest(String permission) {
                            DialogBuilder.dismissDialog();
                            setFilePickErrorResult();
                            MediaFiles.showToastMessage(FilePickActivity.this, getString(R.string.str_permission_denied), Toast.LENGTH_SHORT);
                        }
                    }, false);
                } else {
                    PermissionCompatBuilder.showPermissionDeniedDialog(this, PermissionCompatBuilder.Code.REQ_CODE_CAMERA, getString(R.string.permission_camera_denied_msg));
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_FILE_PICK:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri;
                    if (data != null && data.getData()!=null) {
                        selectedImageUri = data.getData();
                    } else {
                        selectedImageUri = Uri.fromFile(new File(MediaFiles.getCameraPhotoPath()));
                    }
                    if (data!=null && !TextUtils.isEmpty(data.getAction()) && data.getAction().equalsIgnoreCase(MediaStore.ACTION_IMAGE_CAPTURE) && configuration.isCropRequired()) {
                        openCropperActivity(selectedImageUri);
                    } else if (configuration.isCropRequired() && MediaFiles.isImageFile(this,selectedImageUri)) {
                        openCropperActivity(selectedImageUri);
                    } else {
                        setFilePickSuccessResult(selectedImageUri);
                    }
                } else {
                    handleErrorResultCode(resultCode);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (result != null) {
                        Uri uri = result.getUri();
                        setFilePickSuccessResult(uri);
                    }
                } else {
                    handleErrorResultCode(resultCode);
                }
                break;

            default:
                setFilePickErrorResult();
                break;
        }
    }

    private void handleErrorResultCode(int resultCode) {
        switch (resultCode) {
            case RESULT_FIRST_USER:
                setFilePickErrorResult();
                break;
            case RESULT_CANCELED:
                setFilePickCancelled();
                break;
            default:
                setFilePickErrorResult();
                break;
        }
    }

    private void setFilePickSuccessResult(Uri uri) {
        Intent intent = new Intent();
        intent.putExtra(FilePickConstants.FILE_PICK_SUCCESS, uri);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setFilePickCancelled() {
        Intent intent = new Intent();
        intent.putExtra(FilePickConstants.FILE_PICK_ERROR, getString(R.string.str_intent_cancel));
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void setFilePickErrorResult() {
        Intent intent = new Intent();
        intent.putExtra(FilePickConstants.FILE_PICK_ERROR, getString(R.string.str_file_error));
        setResult(RESULT_FIRST_USER, intent);
        finish();
    }

    /**
     * Method to start cropper activity
     * @param uri File Uri
     */
    private void openCropperActivity(Uri uri) {
        CropImage.ActivityBuilder activityBuilder = CropImage.activity(uri);
        if (configuration.getAspectRatioX() != -1 && configuration.getAspectRatioY() != -1) {
            activityBuilder.setAspectRatio(configuration.getAspectRatioX(), configuration.getAspectRatioY());
        }
        activityBuilder.start(this);
    }

}

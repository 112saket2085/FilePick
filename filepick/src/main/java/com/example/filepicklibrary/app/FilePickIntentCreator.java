package com.example.filepicklibrary.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.example.filepicklibrary.model.Configuration;
import com.example.filepicklibrary.model.MediaFiles;
import com.example.filepicklibrary.ui.activity.FilePickActivity;

import static com.example.filepicklibrary.app.FilePickConstants.REQ_CODE_FILE_PICK;

/**
 * Class For Launching File Pick Module and Getting Result on onActivity Result.
 * Created by SAKET on 29/03/2020
 */
public class FilePickIntentCreator {


    /**
     * Load File Picker from Activity and get result in onActivity Result
     * @param activity Activity
     * @param configuration Configuration Object with configurable builder pattern
     */
    public static void loadFilePickerRequest(Activity activity, Configuration configuration) {
        if (activity != null) {
            Intent intent=new Intent(activity, FilePickActivity.class);
            intent.putExtra(FilePickConstants.FILE_PICK_REQUEST,configuration);
            activity.startActivityForResult(intent,REQ_CODE_FILE_PICK);
        }
    }

    /**
     * Load File Picker from Fragment and get result in onActivity Result
     * @param fragment Fragment
     * @param configuration Configuration Object with configurable builder pattern
     */
    public static void loadFilePickerRequest(Fragment fragment, Context context,Configuration configuration) {
        if (fragment != null) {
            Intent intent=new Intent(context, FilePickActivity.class);
            intent.putExtra(FilePickConstants.FILE_PICK_REQUEST,configuration);
            fragment.startActivityForResult(intent,REQ_CODE_FILE_PICK);
        }
    }

    public static MediaFiles getFilePickSuccessResult(Context context,Intent data) {
        if (data != null) {
            Uri uri = data.getParcelableExtra(FilePickConstants.FILE_PICK_SUCCESS);
            return MediaFiles.getMediaFiles(context,uri);

        }
        return null;
    }

    public static int getFilePickErrorResult(Intent data) {
        if (data != null) {
            return data.getIntExtra(FilePickConstants.FILE_PICK_ERROR,-1);
        }
        return -1;
    }
}

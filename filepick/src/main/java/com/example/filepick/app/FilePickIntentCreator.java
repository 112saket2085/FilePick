package com.example.filepick.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.example.filepick.model.Configuration;
import com.example.filepick.model.MediaFiles;
import com.example.filepick.ui.activity.FilePickActivity;

import static com.example.filepick.app.FilePickConstants.REQ_CODE_FILE_PICK;

/**
 * Intent For Launching Activity and Getting Result on onActivity Result.
 * Created by SAKET on 29/03/2020
 */
public class FilePickIntentCreator {

    public static void loadFilePickerRequest(Activity activity, Configuration configuration) {
        if (activity != null) {
            Intent intent=new Intent(activity, FilePickActivity.class);
            intent.putExtra(FilePickConstants.FILE_PICK_REQUEST,configuration);
            activity.startActivityForResult(intent,REQ_CODE_FILE_PICK);
        }
    }

    public static MediaFiles getFilePickSuccessResult(Intent data) {
        if (data != null) {
            Uri uri = data.getParcelableExtra(FilePickConstants.FILE_PICK_SUCCESS);
            return MediaFiles.getMediaFiles(AppBuilder.getInstance(), uri);

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

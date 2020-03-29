package com.example.filepick.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * FileItemModel - Model class that set file items.
 * Created by SAKET on 29/03/2020
 */
public class FileItemModel {

    private Intent intent;
    private Drawable imageResource;

    public FileItemModel(Intent intent,Drawable imageResource) {
        this.intent = intent;
        this.imageResource = imageResource;
    }

    public Drawable getImageResource() {
        return imageResource;
    }

    public void setImageResource(Drawable imageResource) {
        this.imageResource = imageResource;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}

package com.example.filepick.model;

import android.graphics.drawable.Drawable;

/**
 * FileItemModel - Model class that set file items.
 * Created by SAKET on 29/03/2020
 */
public class FileItemModel {

    private Drawable imageResource;
    private String imageTitle;

    public FileItemModel(Drawable imageResource, String imageTitle) {
        this.imageResource = imageResource;
        this.imageTitle = imageTitle;
    }

    public Drawable getImageResource() {
        return imageResource;
    }

    public void setImageResource(Drawable imageResource) {
        this.imageResource = imageResource;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

}

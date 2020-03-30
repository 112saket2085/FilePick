package com.example.filepicklibrary.model;

import java.io.Serializable;
import static com.example.filepicklibrary.app.FilePickConstants.BOTTOM_SHEET_TITLE;
import static com.example.filepicklibrary.app.FilePickConstants.IMAGE_INTENT_TYPE;

/**
 * Configuration - Builder Class to set various request.
 * Created by SAKET on 29/03/2020
 */
public class Configuration implements Serializable {

    private boolean isCropRequired = true;
    private int aspectRatioX = -1;
    private int aspectRatioY = -1;
    private String intentType = IMAGE_INTENT_TYPE;
    private String bottomSheetTitle = BOTTOM_SHEET_TITLE;


    public static class Builder {
        private Configuration configuration;

        public Builder() {
            configuration = new Configuration();
        }

        public Builder setCropRequired(boolean cropRequired) {
            configuration.isCropRequired = cropRequired;
            return this;
        }

        public Builder setAspectRatioX(int aspectRatioX) {
            configuration.aspectRatioX = aspectRatioX;
            return this;
        }

        public Builder setAspectRatioY(int aspectRatioY) {
            configuration.aspectRatioY = aspectRatioY;
            return this;
        }

        public Builder setIntentType(String fileIntentType) {
            configuration.intentType = fileIntentType;
            return this;
        }

        public Builder setBottomSheetTitle(String bottomSheetTitle) {
            configuration.bottomSheetTitle = bottomSheetTitle;
            return this;
        }

        public Configuration build() {
            return configuration;
        }
    }

    public boolean isCropRequired() {
        return isCropRequired;
    }

    public int getAspectRatioX() {
        return aspectRatioX;
    }

    public int getAspectRatioY() {
        return aspectRatioY;
    }

    public String getIntentType() {
        return intentType;
    }

    public String getBottomSheetTitle() {
        return bottomSheetTitle;
    }
}

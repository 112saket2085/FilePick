package com.example.filepick.app;

import android.app.Application;
import android.content.Context;

/**
 * Application Class module
 * Created by SAKET on 29/03/2020
 */
public class AppBuilder extends Application {

    private static AppBuilder instance;

    public AppBuilder() {
        instance = this;
    }

    public static AppBuilder getInstance() {
        return instance;
    }

    /**
     * @return Context : Application context
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}

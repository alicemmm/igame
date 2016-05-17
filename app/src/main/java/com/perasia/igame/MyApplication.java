package com.perasia.igame;


import android.app.Application;
import android.content.Context;

import com.endless.game.android.Constants;

public class MyApplication extends Application {

    private static MyApplication mInstance = null;

    private static Context mContext;

    public static MyApplication getInstance() {
        return mInstance;
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        mContext = getApplicationContext();

        Constants.initApp(mContext);

    }

}

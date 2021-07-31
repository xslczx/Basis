package com.xslczx.basis.sample;

import android.app.Application;

public class App extends Application {

    private static App sContext;

    public static App getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }
}

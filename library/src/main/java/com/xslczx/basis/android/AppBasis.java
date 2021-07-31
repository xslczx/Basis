package com.xslczx.basis.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

public final class AppBasis {

    private static final Stack<Activity> STACK = new Stack<>();
    private static Application sApp;

    private AppBasis() {
    }

    public static void init(final Application app) {
        if (app == null) {
            return;
        }
        if (sApp == null) {
            sApp = app;
            sApp.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksImpl());
            return;
        }
        if (sApp.equals(app)) return;
        sApp = app;
    }

    public static Application getApp() {
        if (sApp != null) return sApp;
        init(getApplicationByReflect());
        if (sApp == null) throw new NullPointerException("reflect failed.");
        return sApp;
    }

    public static Context getContext() {
        Activity peek = STACK.peek();
        if (peek != null && !peek.isDestroyed()) return peek;
        return getApp();
    }

    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object thread = getActivityThread();
            Object app = activityThreadClass.getMethod("getApplication").invoke(thread);
            if (app == null) {
                return null;
            }
            return (Application) app;
        } catch (InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException
                | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getActivityThread() {
        Object activityThread = getActivityThreadInActivityThreadStaticField();
        if (activityThread != null) return activityThread;
        return getActivityThreadInActivityThreadStaticMethod();
    }

    private static Object getActivityThreadInActivityThreadStaticField() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField =
                    activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            return sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle",
                    "getActivityThreadInActivityThreadStaticField: " + e.getMessage());
            return null;
        }
    }

    private static Object getActivityThreadInActivityThreadStaticMethod() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            return activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle",
                    "getActivityThreadInActivityThreadStaticMethod: " + e.getMessage());
            return null;
        }
    }

    private static class ActivityLifecycleCallbacksImpl implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity,
                                      Bundle savedInstanceState) {
            STACK.push(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity,
                                                Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            STACK.remove(activity);
        }
    }
}

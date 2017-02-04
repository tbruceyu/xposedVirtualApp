package com.tby.main.mirror.android.app;

import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import mirror.RefClass;
import mirror.RefObject;
import mirror.RefStaticMethod;

/**
 * Created by yutao on 17/1/16.
 */

public class ActivityThread {
    public static Class<?> TYPE = RefClass.load(ActivityThread.class, "android.app.ActivityThread");
    public static RefObject<Object> mBoundApplication;
    public static RefStaticMethod currentActivityThread;
    public static RefObject<Instrumentation> mInstrumentation;
    public static RefObject<Bundle> mCoreSettings;


    public static class AppBindData {
        public static Class<?> TYPE = RefClass.load(AppBindData.class, "android.app.ActivityThread$AppBindData");
        public static RefObject<Configuration> config;
        public static RefObject<Object> compatInfo;
        public static RefObject<ApplicationInfo> appInfo;

    }
}

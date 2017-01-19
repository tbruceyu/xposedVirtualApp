package com.tby.main.mirror.android.app;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;
import mirror.RefMethod;
import mirror.MethodParams;

public class LoadedApk {
    public static Class Class = RefClass.load(LoadedApk.class, "android.app.LoadedApk");
    public static RefObject<ApplicationInfo> mApplicationInfo;
    @MethodParams({boolean.class, Instrumentation.class})
    public static RefMethod<Application> makeApplication;
}
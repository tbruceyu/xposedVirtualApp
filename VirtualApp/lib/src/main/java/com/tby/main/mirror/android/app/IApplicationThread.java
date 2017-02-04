package com.tby.main.mirror.android.app;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;

public class IApplicationThread {
    public static Class<?> TYPE = RefClass.load(IApplicationThread.class, "android.app.IApplicationThread");

    @MethodReflectParams({"java.lang.String", "android.content.pm.ApplicationInfo", "java.util.List", "android.content.ComponentName",
            "android.app.ProfilerInfo", "android.os.Bundle", "android.app.IInstrumentationWatcher",
            "android.app.IUiAutomationConnection", "int", "boolean", "boolean", "boolean", "android.content.res.Configuration",
            "android.content.res.CompatibilityInfo", "java.util.Map", "android.os.Bundle"})
    public static RefMethod<Void> bindApplication;
}
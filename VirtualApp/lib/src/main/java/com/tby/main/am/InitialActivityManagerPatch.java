package com.tby.main.am;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.helper.compat.BundleCompat;
import com.tby.main.RuntimeInit;
import com.tby.main.client.IStartupClient;
import com.tby.main.mirror.android.app.ActivityManagerNative;
import com.tby.main.mirror.android.app.ActivityThread;
import com.tby.main.mirror.android.app.ContextImpl;
import com.tby.main.mirror.android.app.IActivityManager;
import com.tby.main.mirror.android.app.LoadedApk;

import java.lang.reflect.Method;

import mirror.android.util.Singleton;
import tby.main.service.IStartupActivityManager;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */


public class InitialActivityManagerPatch extends PatchDelegate<HookDelegate<IInterface>> {
    private final int userId;
    private ProcessRecord processRecord;
    private final String packageName;
    public Object originalAms;
    public InitialActivityManagerPatch(String packageName, int userId) {
        super(new HookDelegate<IInterface>(ActivityManagerNative.getDefault.call()));
        this.packageName = packageName;
        this.userId = userId;
    }

    @Override
    public void inject() throws Throwable {
        if (ActivityManagerNative.gDefault.type() == IActivityManager.TYPE) {
            ActivityManagerNative.gDefault.set(getHookDelegate().getProxyInterface());
            originalAms = ActivityManagerNative.gDefault;
        } else if (ActivityManagerNative.gDefault.type() == Singleton.TYPE) {
            Object gDefault = ActivityManagerNative.gDefault.get();
            originalAms = ActivityManagerNative.getDefault.call();
            Singleton.mInstance.set(gDefault, getHookDelegate().getProxyInterface());
        }
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new StaticHook("attachApplication") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
//                Instrumentation instrumentation = new Instrumentation();
//                Object activityThread = ActivityThread.currentActivityThread.call();
//                ActivityThread.mInstrumentation.set(activityThread, instrumentation);
//                Object context = ContextImpl.createSystemContext.call(activityThread);
//                Object packageInfo = ContextImpl.mPackageInfo.get(context);
//                Application application = LoadedApk.makeApplication.call(packageInfo, false, null);
                Object appThread = args[0];
                RuntimeInit.getInstance().getStartupActivityManager().attachApplication((IBinder) appThread, packageName, Process.myUid());
                return null;
            }
        });
    }
    @Override
    public boolean isEnvBad() {
        return ActivityManagerNative.getDefault.call() != getHookDelegate().getProxyInterface();
    }
}

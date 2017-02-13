package com.tby.main.am;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.tby.main.RuntimeInit;
import com.tby.main.mirror.android.app.ActivityManagerNative;
import com.tby.main.mirror.android.app.IActivityManager;

import java.lang.reflect.Method;

import mirror.android.util.Singleton;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */


public class InitialActivityManagerPatch extends PatchDelegate<HookDelegate<IInterface>> {
    private int vpid;
    public Object originalAms;
    private int vuid;

    public InitialActivityManagerPatch() {
        super(new HookDelegate<IInterface>(ActivityManagerNative.getDefault.call()));
        try {
            inject();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void setVpid(int vpid) {
        this.vpid = vpid;
    }

    public void setVuid(int vuid) {
        this.vuid = vuid;
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
                RuntimeInit.getInstance().getActivityManager().attachApplication((IBinder) appThread, vpid, vuid);
                return null;
            }
        });
    }

    @Override
    public boolean isEnvBad() {
        return ActivityManagerNative.getDefault.call() != getHookDelegate().getProxyInterface();
    }
}

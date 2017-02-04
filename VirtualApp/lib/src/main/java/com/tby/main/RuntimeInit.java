package com.tby.main;

import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.service.IActivityManager;
import com.tby.main.am.InitialActivityManagerPatch;

/**
 * Created by yutao on 17/1/11.
 */

public class RuntimeInit {
    private static final RuntimeInit sInstance = new RuntimeInit();
    private IActivityManager activityManager;
    private InitialActivityManagerPatch activityManagerPatch = new InitialActivityManagerPatch();


    public InitialActivityManagerPatch getActivityManagerPatch() {
        return activityManagerPatch;
    }

    public static RuntimeInit getInstance() {
        return sInstance;
    }

    public IActivityManager getActivityManager() {
        return activityManager;
    }

    private void start(String[] args) {
        String packageName = args[1];
        activityManagerPatch.setPackageName(packageName);
        try {
            activityManager = VActivityManager.get().getService();
            startActivityThread();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void startActivityThread() {
        Reflect reflect = Reflect.on("android.app.ActivityThread");
        try {
            String[] callArgs = {"main"};
            reflect.call("main", new Object[]{callArgs});
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void main(String[] args) {
        sInstance.start(args);
    }
}

package com.tby.main.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.server.pm.VPackageManagerService;
import com.tby.main.client.IStartupClient;
import com.tby.main.mirror.android.app.ActivityThread;
import com.tby.main.mirror.android.app.IApplicationThread;

import java.util.concurrent.atomic.AtomicReference;

import mirror.android.app.ApplicationThreadNative;
import tby.main.service.IStartupActivityManager;

/**
 * Created by yutao on 17/1/13.
 */

public class StartupActivityManagerService extends IStartupActivityManager.Stub {
    public static final String DEFAULT_ACTION = "com.tby.main.am";
    public static final String EXTRA_CLIENT_BINDER = "_VA_|_binder_";
    private static final AtomicReference<StartupActivityManagerService> sService = new AtomicReference<>();
    Object activityThread;
    private Context context;
    private IStartupClient startupClient;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            IBinder binder = BundleCompat.getBinder(bundle, EXTRA_CLIENT_BINDER);
            try {
                startupClient = IStartupClient.Stub.asInterface(binder);
                startupClient.bindService(StartupActivityManagerService.this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    };

    public static void systemReady(Context context) {
        new StartupActivityManagerService().onCreate(context);
    }

    public StartupActivityManagerService() {
        super();
    }

    public void onCreate(Context context) {
        this.context = context;
        activityThread = ActivityThread.currentActivityThread.call();
        sService.set(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DEFAULT_ACTION);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static StartupActivityManagerService get() {
        return sService.get();
    }

    @Override
    public boolean attachApplication(IBinder appThread, String packageName, int userId) throws RemoteException {
        int callingPid = Binder.getCallingPid();
        return attachApplicationLocked(appThread, packageName, userId);
    }

    private final boolean attachApplicationLocked(IBinder appThread, String packageName, int userId) {
        ApplicationInfo appInfo = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
//        String processName = packageName +"_v";
//        ProcessRecord processRecord = new ProcessRecord(appInfo, processName, userId, 0);
        IInterface thread = ApplicationThreadNative.asInterface.call(appThread);
        Object activityThread_mBoundApplication = ActivityThread.mBoundApplication.get(activityThread);
        return IApplicationThread.bindApplication.call(thread, packageName, appInfo, null, null, null, null, null, null,
                    0, false, false, false, ActivityThread.AppBindData.config.get(activityThread_mBoundApplication),
                    ActivityThread.AppBindData.compatInfo.get(activityThread_mBoundApplication),
                    null, ActivityThread.mCoreSettings.get(activityThread));
    }
}

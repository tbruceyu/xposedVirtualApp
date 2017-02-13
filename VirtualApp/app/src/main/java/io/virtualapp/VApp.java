package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.am.VActivityManagerService;

import jonathanfinerty.once.Once;
import mirror.android.app.ActivityThread;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static final String[] GMS_PKG = {
            "com.android.vending",

            "com.google.android.gsf",
            "com.google.android.gsf.login",
            "com.google.android.gms",

            "com.google.android.backuptransport",
            "com.google.android.backup",
            "com.google.android.configupdater",
            "com.google.android.syncadapters.contacts",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.partnersetup",
            "com.google.android.setupwizard",
            "com.google.android.syncadapters.calendar",};

    private static VApp gDefault;

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    protected void attachBaseContext(Context base) {
        StubManifest.STUB_CP_AUTHORITY = BuildConfig.APPLICATION_ID + "." + StubManifest.STUB_DEF_AUTHORITY;
        ServiceManagerNative.SERVICE_CP_AUTH = BuildConfig.APPLICATION_ID + "." + ServiceManagerNative.SERVICE_DEF_AUTH;
        super.attachBaseContext(base);
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        if (VirtualCore.get().isMainProcess()) {
            Once.initialise(this);
            installGms();
        } else if (VirtualCore.get().isVAppProcess()) {
            VirtualCore.get().setComponentDelegate(new MyComponentDelegate());
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
            // attach to virtual server
            initVirtualProcess();
        }
    }

    /**
     * Install the Google mobile service.
     */
    private void installGms() {
        VirtualCore virtualCore = VirtualCore.get();
        PackageManager pm = virtualCore.getUnHookPackageManager();
        for (String pkg : GMS_PKG) {
            if (virtualCore.isAppInstalled(pkg)) {
                continue;
            }
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                String apkPath = appInfo.sourceDir;
                InstallResult res = VirtualCore.get().installApp(apkPath,
                        InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.TERMINATE_IF_EXIST);
                if (!res.isSuccess) {
                    VLog.e(getClass().getSimpleName(), "Warning: Unable to install app %s: %s.", appInfo.packageName, res.error);
                }
            } catch (Throwable e) {
                // Ignore
            }
        }
    }

    private void initVirtualProcess() {
        ConditionVariable lock = VirtualCore.get().getInitLock();
        if (lock != null) {
            lock.block();
        }
        Object mainThread = ActivityThread.currentActivityThread.call();

        String processName = ActivityThread.getProcessName.call(mainThread);
        int vpid = VActivityManagerService.parseVPid(processName);
        int vuid = VActivityManagerService.parseVUid(processName);
        IBinder token = VActivityManager.get().getPendingProcessToken(vpid);

        VClientImpl client = VClientImpl.getClient();
        client.initProcess(token, vuid);
        VActivityManager.get().attachClientProcess(Process.myPid(), client);
    }
}

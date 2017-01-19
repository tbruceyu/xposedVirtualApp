package com.tby.main.am;

import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.ipc.LocalProxyUtils;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.service.IActivityManager;

import tby.main.service.IStartupActivityManager;

/**
 * Created by yutao on 17/1/16.
 */

public class StartupActivityManager {
    private IStartupActivityManager mRemote;
    private static final StartupActivityManager sAM = new StartupActivityManager();

    public static StartupActivityManager get() {
        return sAM;
    }

    public IStartupActivityManager getService() {
        if (mRemote == null) {
            synchronized (VActivityManager.class) {
                if (mRemote == null) {
                    final IStartupActivityManager remote = IStartupActivityManager.Stub
                            .asInterface(ServiceManagerNative.getService(ServiceManagerNative.STARTUP_ACTIVITY));
                    mRemote = LocalProxyUtils.genProxy(IStartupActivityManager.class, remote);
                }
            }
        }
        return mRemote;
    }

    public boolean attachApplication(IBinder appThread, String packageName, int userId) {
        try {
            return getService().attachApplication(appThread, packageName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }
}

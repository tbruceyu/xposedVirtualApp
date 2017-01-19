package com.tby.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.utils.Reflect;
import com.tby.main.am.InitialActivityManagerPatch;
import com.tby.main.am.StartupActivityManagerService;
import com.tby.main.client.IStartupClient;
import com.tby.main.mirror.android.app.IActivityManager;

import tby.main.service.IStartupActivityManager;

/**
 * Created by yutao on 17/1/11.
 */

public class RuntimeInit {
    public ClientService clientService = new ClientService();
    private IStartupActivityManager startupActivityManagerService;
    public static final RuntimeInit sInstance = new RuntimeInit();

    public static RuntimeInit getInstance() {
        return sInstance;
    }

    public IStartupActivityManager getStartupActivityManager() {
        return startupActivityManagerService;
    }

    private void start(String[] args) {
        String packageName = args[1];
        int userId = Integer.valueOf(args[2]);
        InitialActivityManagerPatch activityManagerPatch = new InitialActivityManagerPatch(packageName, userId);
        try {
            activityManagerPatch.inject();
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            BundleCompat.putBinder(bundle, StartupActivityManagerService.EXTRA_CLIENT_BINDER, clientService);
            intent.putExtras(bundle);
            intent.setAction(StartupActivityManagerService.DEFAULT_ACTION);
            IActivityManager.broadcastIntent.call(activityManagerPatch.originalAms, null, intent, null, null, 0, null, null, null, -1, null, true, false, Process.myUid());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public class ClientService extends IStartupClient.Stub {

        @Override
        public void bindService(IBinder serverBinder) throws RemoteException {
            startupActivityManagerService = IStartupActivityManager.Stub.asInterface(serverBinder);
            Reflect reflect = Reflect.on("android.app.ActivityThread");
            try {
                String[] callArgs = {"main"};
                reflect.call("main", new Object[]{callArgs});
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        sInstance.start(args);
    }
}

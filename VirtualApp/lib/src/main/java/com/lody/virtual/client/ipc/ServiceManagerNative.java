package com.lody.virtual.client.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.ServiceCache;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.service.interfaces.IServiceFetcher;
import com.tby.main.RuntimeInit;
import com.tby.main.client.IStartupClient;

import mirror.android.app.ActivityThread;

/**
 * @author Lody
 */
public class ServiceManagerNative {
	public static final String DEFAULT_ACTION = "com.tby.main.service_fetcher";
    public static final String EXTRA_CLIENT_BINDER = "_VA_|_binder_";


    public static final String PACKAGE = "package";
	public static final String ACTIVITY = "activity";
	public static final String USER = "user";
	public static final String APP = "app";
	public static final String ACCOUNT = "account";
	public static final String JOB = "job";
	public static final String INTENT_FILTER = "intent_filter";
	private static final String TAG = ServiceManagerNative.class.getSimpleName();
	public static final String SERVICE_DEF_AUTH = "virtual.service.BinderProvider";
	public static String SERVICE_CP_AUTH = "virtual.service.BinderProvider";

	private static IServiceFetcher sFetcher;

	public static ClientService clientService = new ClientService();

	private static class ClientService extends IStartupClient.Stub {

		@Override
		public void bindService(IBinder serverBinder) throws RemoteException {
			synchronized (ServiceManagerNative.class) {
				sFetcher = IServiceFetcher.Stub.asInterface(serverBinder);
				Log.d("yutao", "bindService");
				ServiceManagerNative.class.notify();
			}
		}
	}

	public synchronized static IServiceFetcher getServiceFetcher() {
		try {
		    if (sFetcher == null) {
				fetchServiceFetcher();
		    }
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return sFetcher;
	}

	private static void fetchServiceFetcher() throws Throwable {
		Context context = VirtualCore.get().getContext();
		if (context != null) {
			Object mainThread = ActivityThread.currentActivityThread.call();
			String processName = ActivityThread.getProcessName.call(mainThread);
			String mainProcessName = context.getApplicationInfo().processName;
			if (processName.equals(mainProcessName) || processName.endsWith(Constants.SERVER_PROCESS_NAME)) {
				Bundle response = new ProviderCall.Builder(context, SERVICE_CP_AUTH).methodName("@").call();
				if (response != null) {
					IBinder binder = BundleCompat.getBinder(response, "_VA_|_binder_");
					linkBinderDied(binder);
					sFetcher = IServiceFetcher.Stub.asInterface(binder);
				}
			} else {
				fetchServiceFetchWithBroadcast();
			}
		} else {
			fetchServiceFetchWithBroadcast();
		}
	}

	private static void fetchServiceFetchWithBroadcast() throws Throwable {
		synchronized (ServiceManagerNative.class) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, ServiceManagerNative.EXTRA_CLIENT_BINDER, clientService);
			intent.putExtras(bundle);
			intent.setAction(ServiceManagerNative.DEFAULT_ACTION);
			com.tby.main.mirror.android.app.IActivityManager.broadcastIntent.call(RuntimeInit.getInstance().getActivityManagerPatch().originalAms, null, intent, null, null, 0, null, null, null, -1, null, true, false, VUserHandle.getUserId(Process.myUid()));
			ServiceManagerNative.class.wait();
		}
	}

	private static void linkBinderDied(final IBinder binder) {
		IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
			@Override
			public void binderDied() {
				binder.unlinkToDeath(this, 0);
				VLog.e(TAG, "Ops, the server has crashed.");
				VirtualRuntime.exit();
			}
		};
		try {
			binder.linkToDeath(deathRecipient, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static IBinder getService(String name) {
		if (VirtualCore.get().isServerProcess()) {
			return ServiceCache.getService(name);
		}
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				return fetcher.getService(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		VLog.e(TAG, "GetService(%s) return null.", name);
		return null;
	}

	public static void addService(String name, IBinder service) {
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				fetcher.addService(name, service);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	public static void removeService(String name) {
		IServiceFetcher fetcher = getServiceFetcher();
		if (fetcher != null) {
			try {
				fetcher.removeService(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}

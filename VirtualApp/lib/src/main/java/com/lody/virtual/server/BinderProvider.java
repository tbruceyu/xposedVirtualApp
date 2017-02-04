package com.lody.virtual.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.lody.virtual.client.stub.DaemonService;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.server.accounts.VAccountManagerService;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.filter.IntentFilterService;
import com.lody.virtual.server.job.JobSchedulerService;
import com.lody.virtual.server.pm.VAppManagerService;
import com.lody.virtual.server.pm.VPackageManagerService;
import com.lody.virtual.server.pm.VUserManagerService;
import com.lody.virtual.service.interfaces.IServiceFetcher;
import com.tby.main.client.IStartupClient;

/**
 * @author Lody
 */
public final class BinderProvider extends BaseContentProvider {
	private final ServiceFetcher mServiceFetcher = new ServiceFetcher();
	private IStartupClient startupClient;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			IBinder binder = BundleCompat.getBinder(bundle, ServiceManagerNative.EXTRA_CLIENT_BINDER);
			try {
				Log.d("yutao", "onReceive");
				startupClient = IStartupClient.Stub.asInterface(binder);
				startupClient.bindService(mServiceFetcher);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DaemonService.startup(context);
		if (!VirtualCore.get().isStartup()) {
			return true;
		}
		VPackageManagerService.systemReady();
		addService(ServiceManagerNative.PACKAGE, VPackageManagerService.get());
		VActivityManagerService.systemReady(context);
		addService(ServiceManagerNative.ACTIVITY, VActivityManagerService.get());
		addService(ServiceManagerNative.USER, VUserManagerService.get());
		VAppManagerService.systemReady();
		addService(ServiceManagerNative.APP, VAppManagerService.get());
		VAccountManagerService.systemReady();
		addService(ServiceManagerNative.ACCOUNT, VAccountManagerService.get());
		addService(ServiceManagerNative.INTENT_FILTER, IntentFilterService.get());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			JobSchedulerService.systemReady(context);
			addService(ServiceManagerNative.JOB, JobSchedulerService.getStub());
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ServiceManagerNative.DEFAULT_ACTION);
		context.registerReceiver(broadcastReceiver, intentFilter);
		Log.d("yutao", "registerReceiver");
		return true;
	}

	private void addService(String name, IBinder service) {
		ServiceCache.addService(name, service);
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Bundle bundle = new Bundle();
		BundleCompat.putBinder(bundle, "_VA_|_binder_", mServiceFetcher);
		return bundle;
	}

	private class ServiceFetcher extends IServiceFetcher.Stub {
		@Override
		public IBinder getService(String name) throws RemoteException {
			if (name != null) {
				return ServiceCache.getService(name);
			}
			return null;
		}

		@Override
		public void addService(String name, IBinder service) throws RemoteException {
			if (name != null && service != null) {
				ServiceCache.addService(name, service);
			}
		}

		@Override
		public void removeService(String name) throws RemoteException {
			if (name != null) {
				ServiceCache.removeService(name);
			}
		}
	}

}

// IStartupActivityManager.aidl
package tby.main.service;

interface IStartupActivityManager {
    boolean attachApplication(IBinder appThread, String packageName, int userId);
}

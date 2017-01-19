// IStartupClient.aidl
package com.tby.main.client;

// Declare any non-default types here with import statements

interface IStartupClient {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void bindService(IBinder serverBinder);
}

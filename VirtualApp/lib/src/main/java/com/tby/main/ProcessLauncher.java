package com.tby.main;

/**
 * Created by yutao on 17/2/6.
 */

public class ProcessLauncher {
    static {
        System.loadLibrary("process_launcher");
    }

    public static native void launchProcess(int vpid, int vuid, String baseApkPath, String dataDirPath);
}

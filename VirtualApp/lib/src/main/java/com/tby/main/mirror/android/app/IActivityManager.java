package com.tby.main.mirror.android.app;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;

public class IActivityManager {
    public static Class<?> TYPE = RefClass.load(IActivityManager.class, "android.app.IActivityManager");
    /**
     int broadcastIntent(IApplicationThread caller, Intent intent,
     String resolvedType, IIntentReceiver resultTo, int resultCode,
     String resultData, Bundle map, String[] requiredPermissions,
     int appOp, Bundle options, boolean serialized, boolean sticky, int userId)
     **/
    @MethodReflectParams({"android.app.IApplicationThread", "android.content.Intent", "java.lang.String",
            "android.content.IIntentReceiver", "int", "java.lang.String", "android.os.Bundle",
            "[Ljava.lang.String;", "int", "android.os.Bundle", "boolean", "boolean", "int"})
    public static RefMethod<Integer> broadcastIntent;

}

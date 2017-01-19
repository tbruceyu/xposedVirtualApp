package com.tby.main.mirror.android.app;

import android.os.IInterface;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefStaticMethod;
import mirror.RefStaticObject;

/**
 * Created by yutao on 17/1/19.
 */

public class ActivityManagerNative {
    public static Class<?> TYPE = RefClass.load(ActivityManagerNative.class, "android.app.ActivityManagerNative");
    public static RefStaticObject<Object> gDefault;
    public static RefStaticMethod<IInterface> getDefault;

}

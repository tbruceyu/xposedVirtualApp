package com.tby.main.mirror.android.app;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefObject;
import mirror.RefStaticMethod;

/**
 * Created by yutao on 17/1/16.
 */

public class ContextImpl {
    public static Class<?> TYPE = RefClass.load(ContextImpl.class, "android.app.ContextImpl");

    public static RefObject<?> mPackageInfo;
    @MethodReflectParams({"android.app.ActivityThread"})
    public static RefStaticMethod<?> createSystemContext;

}

#include <jni.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/wait.h>
#include <android/log.h>
#include "helper/helper.h"

#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define JAVA_CLASS "com/tby/main/ProcessLauncher"

#define LOG_TAG "yutao"
#define APP_PROCESS  "/data/data/io.virtualapp/app_process64_bruce"

// const char* env_list[] = { "CLASSPATH=/sdcard/yutao/yutao.jar", "LD_LIBRARY_PATH=/data/data/cn.bruce.yu/", NULL };
int once = 0;
JavaVM *gVm;
jclass gClass;


static void sig_chld(int signo) {
    pid_t pid;
    int stat;
    pid = wait(&stat);
    return;
}

void launchProcess(JNIEnv *env, jobject object,
                   jint vpid, jint vuid, jstring jbaseApk, jstring jdataDir) {
    pid_t child_pid;
    const char *baseApk = env->GetStringUTFChars(jbaseApk, NULL);
    const char *dataDir = env->GetStringUTFChars(jdataDir, NULL);
    child_pid = fork();

    if (!once) {
        signal(SIGCHLD, &sig_chld);
        once = 1;
    }
    if (child_pid == 0) {
        // Child process
        char vpidStr[10], vuidStr[10];
        sprintf(vpidStr, "%d", vpid);
        sprintf(vuidStr, "%d", vuid);
        char *argv[] = {"app_process64_bruce", "com.tby.main.RuntimeInit", vpidStr, vuidStr, NULL};
        setenv("CLASSPATH", baseApk, 1);
        setenv("LD_LIBRARY_PATH", dataDir, 1);
        // execle(APP_PROCESS, "/sdcard/yutao/ yu.bruce.cn.jvcmd.Main >/sdcard/yutao/error.log", env_list);

        int result = execvp(APP_PROCESS, argv);
        LOGD("result: %d, close error with msg is: %s\n", result, strerror(errno));
    } else {
        // Parent process
        env->ReleaseStringUTFChars(jbaseApk, baseApk);
        env->ReleaseStringUTFChars(jdataDir, dataDir);
    }
}

static JNINativeMethod gMethods[] = {
        NATIVE_METHOD((void *) launchProcess, "launchProcess", "(IILjava/lang/String;Ljava/lang/String;)V"),
};

__attribute__((visibility("default")))
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    gVm = vm;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass javaClass = env->FindClass(JAVA_CLASS);
    if (javaClass == NULL) {
        LOGE("Ops: Unable to find hook class.");
        return JNI_ERR;
    }
    if (env->RegisterNatives(javaClass, gMethods, NELEM(gMethods)) < 0) {
        LOGE("Ops: Unable to register the native methods.");
        return JNI_ERR;
    }
    gClass = (jclass) env->NewGlobalRef(javaClass);
    env->DeleteLocalRef(javaClass);

    return JNI_VERSION_1_6;
}


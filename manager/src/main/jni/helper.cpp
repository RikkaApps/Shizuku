#include <jni.h>
#include <dirent.h>
#include <unistd.h>
#include <mntent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <cassert>
#include <cstring>
#include "selinux.h"

#define LOG_TAG "ShizukuServer"

#include "logging.h"

static jint setcontext(JNIEnv *env, jobject thiz, jstring jName) {
    const char *name = env->GetStringUTFChars(jName, nullptr);

    if (!se::setcon)
        return -1;

    int res = se::setcon(name);
    if (res == -1) PLOGE("setcon %s", name);

    env->ReleaseStringUTFChars(jName, name);

    return res;
}

static JNINativeMethod gMethods[] = {
        {"setSELinuxContext", "(Ljava/lang/String;)I", (void *) setcontext},
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr)
        return JNI_FALSE;

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
        return JNI_FALSE;

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, "moe/shizuku/server/utils/NativeHelper", gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    jint result;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;

    assert(env != nullptr);

    se::init();

    if (!registerNatives(env)) {
        LOGE("registerNatives NativeHelper");
        return -1;
    }

    result = JNI_VERSION_1_6;

    return result;
}

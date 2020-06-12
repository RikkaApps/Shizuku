
#include <jni.h>
#include <utility>
#include <elf.h>
#include <fcntl.h>
#include <vector>
#include <cinttypes>
#include "misc.h"

extern "C" {
#include "pmparser.h"
}

#define LOG_TAG "Bypass"

#include "logging.h"

using setHiddenApiExemptions_t = void(JNIEnv *env, jclass, jobjectArray exemptions);

static uintptr_t search_string(uintptr_t start, uintptr_t end, const char *str) {
    uintptr_t addr;
    auto len = strlen(str);
    if ((addr = memsearch(start, end, (const void *) str, len)) != 0) {
        LOGD("found \"%s\" at 0x%" PRIxPTR, (char *) addr, (uintptr_t) addr);
        return addr;
    }
    return 0;
}

static JNINativeMethod *search_jni_method(const std::vector<std::pair<uintptr_t, uintptr_t>> &addresses, const char *name, const char *signature) {
    uintptr_t name_addr, signature_addr, method_addr;

    // Step 1: search strings
    for (auto address : addresses) {
        name_addr = search_string(address.first, address.second, name);
        signature_addr = search_string(address.first, address.second, signature);
        if (name_addr != 0 && signature_addr != 0) {
            break;
        }
    }
    if (name_addr == 0 | signature_addr == 0) {
        LOGE("findMethod: can't find address for name or signature.");
    }

    // Step 2: search JNINativeMethod
    auto char_ptr_size = sizeof(const char *);
    JNINativeMethod *res = nullptr;
    JNINativeMethod method = JNINativeMethod{(const char *) name_addr, (const char *) signature_addr, nullptr};

    for (auto address : addresses) {
        method_addr = memsearch(address.first, address.second, &method, char_ptr_size * 2);
        if (method_addr != 0) {
            res = (JNINativeMethod *) method_addr;
            LOGI("found {\"%s\", \"%s\", %p} at 0x%" PRIxPTR".", res->name, res->signature, res->fnPtr, method_addr);
            break;
        } else {
            LOGD("JNINativeMethod struct not found between %" PRIxPTR"-%" PRIxPTR".", address.first, address.second);
        }
    }
    return res;
}

bool strend(char const *str, char const *suffix) {
    if (!str && !suffix) return true;
    if (!str || !suffix) return false;
    auto str_len = strlen(str);
    auto suffix_len = strlen(suffix);
    if (suffix_len > str_len) return false;
    return strcmp(str + str_len - suffix_len, suffix) == 0;
}

static bool doBypass(JNIEnv *env) {
    procmaps_iterator *maps = pmparser_parse(-1);
    if (maps == nullptr) {
        LOGE("cannot parse the memory map");
        return false;
    }

    std::vector<std::pair<uintptr_t, uintptr_t>> addresses;
    procmaps_struct *maps_tmp;
    while ((maps_tmp = pmparser_next(maps)) != nullptr) {
        if (strend(maps_tmp->pathname, "/libart.so")) {
            auto start = (uintptr_t) maps_tmp->addr_start;
            auto end = (uintptr_t) maps_tmp->addr_end;
            addresses.emplace_back(start, end);
            LOGD("%" PRIxPTR"-%" PRIxPTR" %s %ld %s", start, end, maps_tmp->perm, maps_tmp->offset, maps_tmp->pathname);
        }
    }
    pmparser_free(maps);

    auto method = search_jni_method(addresses, "setHiddenApiExemptions", "([Ljava/lang/String;)V");
    if (!method) {
        LOGE("unable to find setHiddenApiExemptions.");
    } else {
        auto setHiddenApiExemptions = (setHiddenApiExemptions_t *) method->fnPtr;
        auto exemption = env->NewStringUTF("L");
        auto exemptions = (jobjectArray) env->NewObjectArray(1, env->FindClass("java/lang/String"), exemption);
        setHiddenApiExemptions(env, nullptr, exemptions);
        env->DeleteLocalRef(exemption);
        env->DeleteLocalRef(exemptions);
        if (env->ExceptionCheck()) {
            env->ExceptionDescribe();
            env->ExceptionClear();
            LOGW("failed.");
        }
    }
    LOGD("succeeded");

    return true;
}

namespace bypass {

    bool Bypass(JNIEnv *env) {
        return doBypass(env);
    }
}

// ---------------------------------------------------------

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;

    bypass::Bypass(env);
    return JNI_VERSION_1_6;
}
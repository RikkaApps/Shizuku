#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <dirent.h>
#include <ctime>
#include <cstring>
#include <libgen.h>
#include <sys/stat.h>
#include <sys/system_properties.h>
#include <cerrno>
#include <string_view>
#include <termios.h>
#include "android.h"
#include "misc.h"
#include "selinux.h"
#include "cgroup.h"
#include "logging.h"

#ifdef DEBUG
#define JAVA_DEBUGGABLE
#endif

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_FATAL_UID 6
#define EXIT_FATAL_PM_PATH 7
#define EXIT_FATAL_KILL 9
#define EXIT_FATAL_BINDER_BLOCKED_BY_SELINUX 10

#define PACKAGE_NAME "moe.shizuku.privileged.api"
#define SERVER_NAME "shizuku_server"
#define SERVER_CLASS_PATH "rikka.shizuku.server.ShizukuService"

#if defined(__arm__)
#define ABI "armeabi-v7a"
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__aarch64__)
#define ABI "arm64-v8a"
#endif

static void run_server(const char *dex_path, const char *main_class, const char *process_name) {
    if (setenv("CLASSPATH", dex_path, true)) {
        LOGE("can't set CLASSPATH\n");
        exit(EXIT_FATAL_SET_CLASSPATH);
    }

#define ARG(v) char **v = nullptr; \
    char buf_##v[PATH_MAX]; \
    size_t v_size = 0; \
    uintptr_t v_current = 0;
#define ARG_PUSH(v, arg) v_size += sizeof(char *); \
if (v == nullptr) { \
    v = (char **) malloc(v_size); \
} else { \
    v = (char **) realloc(v, v_size);\
} \
v_current = (uintptr_t) v + v_size - sizeof(char *); \
*((char **) v_current) = arg ? strdup(arg) : nullptr;

#define ARG_END(v) ARG_PUSH(v, nullptr)

#define ARG_PUSH_FMT(v, fmt, ...) snprintf(buf_##v, PATH_MAX, fmt, __VA_ARGS__); \
    ARG_PUSH(v, buf_##v)

#ifdef JAVA_DEBUGGABLE
#define ARG_PUSH_DEBUG_ONLY(v, arg) ARG_PUSH(v, arg)
#define ARG_PUSH_DEBUG_VM_PARAMS(v) \
    if (android::GetApiLevel() >= 30) { \
        ARG_PUSH(v, "-Xcompiler-option"); \
        ARG_PUSH(v, "--debuggable"); \
        ARG_PUSH(v, "-XjdwpProvider:adbconnection"); \
        ARG_PUSH(v, "-XjdwpOptions:suspend=n,server=y"); \
    } else if (android::GetApiLevel() >= 28) { \
        ARG_PUSH(v, "-Xcompiler-option"); \
        ARG_PUSH(v, "--debuggable"); \
        ARG_PUSH(v, "-XjdwpProvider:internal"); \
        ARG_PUSH(v, "-XjdwpOptions:transport=dt_android_adb,suspend=n,server=y"); \
    } else { \
        ARG_PUSH(v, "-Xcompiler-option"); \
        ARG_PUSH(v, "--debuggable"); \
        ARG_PUSH(v, "-agentlib:jdwp=transport=dt_android_adb,suspend=n,server=y"); \
    }
#else
#define ARG_PUSH_DEBUG_VM_PARAMS(v)
#define ARG_PUSH_DEBUG_ONLY(v, arg)
#endif

    char lib_path[PATH_MAX]{0};
    snprintf(lib_path, PATH_MAX, "%s!/lib/%s", dex_path, ABI);

    ARG(argv)
    ARG_PUSH(argv, "/system/bin/app_process")
    ARG_PUSH_FMT(argv, "-Djava.class.path=%s", dex_path)
    ARG_PUSH_FMT(argv, "-Dshizuku.library.path=%s", lib_path)
    ARG_PUSH_DEBUG_VM_PARAMS(argv)
    ARG_PUSH(argv, "/system/bin")
    ARG_PUSH_FMT(argv, "--nice-name=%s", process_name)
    ARG_PUSH(argv, main_class)
    ARG_PUSH_DEBUG_ONLY(argv, "--debug")
    ARG_END(argv)

    LOGD("exec app_process");

    if (execvp((const char *) argv[0], argv)) {
        exit(EXIT_FATAL_APP_PROCESS);
    }
}

static void start_server(const char *path, const char *main_class, const char *process_name) {
    if (daemon(false, false) == 0) {
        LOGD("child");
        run_server(path, main_class, process_name);
    } else {
        perrorf("fatal: can't fork\n");
        exit(EXIT_FATAL_FORK);
    }
}

static int check_selinux(const char *s, const char *t, const char *c, const char *p) {
    int res = se::selinux_check_access(s, t, c, p, nullptr);
#ifndef DEBUG
    if (res != 0) {
#endif
    printf("info: selinux_check_access %s %s %s %s: %d\n", s, t, c, p, res);
    fflush(stdout);
#ifndef DEBUG
    }
#endif
    return res;
}

static int switch_cgroup() {
    int s_cuid, s_cpid;
    int spid = getpid();

    if (cgroup::get_cgroup(spid, &s_cuid, &s_cpid) != 0) {
        printf("warn: can't read cgroup\n");
        fflush(stdout);
        return -1;
    }

    printf("info: cgroup is /uid_%d/pid_%d\n", s_cuid, s_cpid);
    fflush(stdout);

    if (cgroup::switch_cgroup(spid, -1, -1) != 0) {
        printf("warn: can't switch cgroup\n");
        fflush(stdout);
        return -1;
    }

    if (cgroup::get_cgroup(spid, &s_cuid, &s_cpid) != 0) {
        printf("info: switch cgroup succeeded\n");
        fflush(stdout);
        return 0;
    }

    printf("warn: can't switch self, current cgroup is /uid_%d/pid_%d\n", s_cuid, s_cpid);
    fflush(stdout);
    return -1;
}

char *context = nullptr;

int starter_main(int argc, char *argv[]) {
    char *apk_path = nullptr;
    for (int i = 0; i < argc; ++i) {
        if (strncmp(argv[i], "--apk=", 6) == 0) {
            apk_path = argv[i] + 6;
        }
    }

    int uid = getuid();
    if (uid != 0 && uid != 2000) {
        perrorf("fatal: run Shizuku from non root nor adb user (uid=%d).\n", uid);
        exit(EXIT_FATAL_UID);
    }

    se::init();

    if (uid == 0) {
        chown("/data/local/tmp/shizuku_starter", 2000, 2000);
        se::setfilecon("/data/local/tmp/shizuku_starter", "u:object_r:shell_data_file:s0");
        switch_cgroup();

        int sdkLevel = 0;
        char buf[PROP_VALUE_MAX + 1];
        if (__system_property_get("ro.build.version.sdk", buf) > 0)
            sdkLevel = atoi(buf);

        if (sdkLevel >= 29) {
            printf("info: switching mount namespace to init...\n");
            switch_mnt_ns(1);
        }
    }

    if (uid == 0) {
        if (se::getcon(&context) == 0) {
            int res = 0;

            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "call");
            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "transfer");

            if (res != 0) {
                perrorf("fatal: the su you are using does not allow app (u:r:untrusted_app:s0) to connect to su (%s) with binder.\n",
                        context);
                exit(EXIT_FATAL_BINDER_BLOCKED_BY_SELINUX);
            }
            se::freecon(context);
        }
    }

    mkdir("/data/local/tmp/shizuku", 0707);
    chmod("/data/local/tmp/shizuku", 0707);
    if (uid == 0) {
        chown("/data/local/tmp/shizuku", 2000, 2000);
        se::setfilecon("/data/local/tmp/shizuku", "u:object_r:shell_data_file:s0");
    }

    printf("info: starter begin\n");
    fflush(stdout);

    // kill old server
    printf("info: killing old process...\n");
    fflush(stdout);

    foreach_proc([](pid_t pid) {
        if (pid == getpid()) return;

        char name[1024];
        if (get_proc_name(pid, name, 1024) != 0) return;

        if (strcmp(SERVER_NAME, name) != 0
            && strcmp("shizuku_server_legacy", name) != 0)
            return;

        if (kill(pid, SIGKILL) == 0)
            printf("info: killed %d (%s)\n", pid, name);
        else if (errno == EPERM) {
            perrorf("fatal: can't kill %d, please try to stop existing Shizuku from app first.\n", pid);
            exit(EXIT_FATAL_KILL);
        } else {
            printf("warn: failed to kill %d (%s)\n", pid, name);
        }
    });

    if (access(apk_path, R_OK) == 0) {
        printf("info: use apk path from argv\n");
        fflush(stdout);
    }

    if (!apk_path) {
        auto f = popen("pm path " PACKAGE_NAME, "r");
        if (f) {
            char line[PATH_MAX]{0};
            fgets(line, PATH_MAX, f);
            trim(line);
            if (strstr(line, "package:") == line) {
                apk_path = line + strlen("package:");
            }
            pclose(f);
        }
    }

    if (!apk_path) {
        perrorf("fatal: can't get path of manager\n");
        exit(EXIT_FATAL_PM_PATH);
    }

    printf("info: apk path is %s\n", apk_path);
    if (access(apk_path, R_OK) != 0) {
        perrorf("fatal: can't access manager %s\n", apk_path);
        exit(EXIT_FATAL_PM_PATH);
    }

    printf("info: starting server...\n");
    fflush(stdout);
    LOGD("start_server");
    start_server(apk_path, SERVER_CLASS_PATH, SERVER_NAME);
    exit(EXIT_SUCCESS);
}

using main_func = int (*)(int, char *[]);

static main_func applet_main[] = {starter_main, nullptr};

int main(int argc, char **argv) {
    std::string_view base = basename(argv[0]);

    LOGD("applet %s", base.data());

    constexpr const char *applet_names[] = {"shizuku_starter", nullptr};

    for (int i = 0; applet_names[i]; ++i) {
        if (base == applet_names[i]) {
            return (*applet_main[i])(argc, argv);
        }
    }

    return 1;
}

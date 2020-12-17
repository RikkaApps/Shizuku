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
#include "android.h"
#include "misc.h"
#include "selinux.h"
#include "cgroup.h"

#ifdef DEBUG
#define JAVA_DEBUGGABLE
#endif

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define EXIT_FATAL_CANNOT_ACCESS_PATH 1
#define EXIT_FATAL_PATH_NOT_SET 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_FATAL_UID 6
#define EXIT_FATAL_KILL 9
#define EXIT_FATAL_BINDER_BLOCKED_BY_SELINUX 10

#define SERVER_NAME "shizuku_server"
#define SERVER_CLASS_PATH "moe.shizuku.server.Starter"

static void run_server(const char *dex_path, const char *main_class, const char *process_name) {
    if (setenv("CLASSPATH", dex_path, true)) {
        perrorf("fatal: can't set CLASSPATH\n");
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

    ARG(argv)
    ARG_PUSH(argv, "/system/bin/app_process")
    ARG_PUSH_FMT(argv, "-Djava.class.path=%s", dex_path)
    ARG_PUSH_DEBUG_VM_PARAMS(argv)
    ARG_PUSH(argv, "/system/bin")
    ARG_PUSH_FMT(argv, "--nice-name=%s", process_name)
    ARG_PUSH(argv, main_class)
    ARG_PUSH_DEBUG_ONLY(argv, "--debug")
    ARG_END(argv)

    for (int i = 0; i < 4; ++i) {
        printf("%s\n", argv[i]);
    }
    fflush(stdout);

    if (execvp((const char *) argv[0], argv)) {
        exit(EXIT_FATAL_APP_PROCESS);
    }
}

static int start_server(const char *path, const char *main_class, const char *process_name) {
    pid_t pid = fork();
    if (pid == 0) {
        daemon(false, false);
        run_server(path, main_class, process_name);
        return 0;
    } else if (pid == -1) {
        perrorf("fatal: can't fork\n");
        exit(EXIT_FATAL_FORK);
    }
    return EXIT_SUCCESS;
}

static void check_access(const char *path, const char *name) {
    if (!path) {
        perrorf("fatal: %s not set.\n", name);
        exit(EXIT_FATAL_PATH_NOT_SET);
    }

    printf("info: %s is %s\n", name, path);

    if (access(path, F_OK) != 0) {
        perrorf("fatal: can't access %s, please open Shizuku app and try again.\n", path);
        exit(EXIT_FATAL_CANNOT_ACCESS_PATH);
    }
}

static void copy_if_not_exist(const char *src, const char *dst) {
#ifdef DEBUG
    remove(dst);
    copyfile(src, dst);
#else
    if (access(dst, F_OK)) {
        copyfile(src, dst);
    }
#endif
    chmod(dst, 0707);
    if (getuid() == 0) {
        chown(dst, 2000, 2000);
        se::setfilecon(dst, "u:object_r:shell_data_file:s0");
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

int main(int argc, char **argv) {
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

    char *_server_dex_path = nullptr, *_starter_dex_path = nullptr;
    for (int i = 0; i < argc; ++i) {
        if (strncmp(argv[i], "--server-dex=", 13) == 0) {
            _server_dex_path = argv[i] + 13;
        } else if (strncmp(argv[i], "--starter-dex=", 14) == 0) {
            _starter_dex_path = argv[i] + 14;
        }
    }

    if (uid == 0) {
        if (se::getcon(&context) == 0) {
            int res = 0;

            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "call");
            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "transfer");

            if (res != 0) {
                perrorf("fatal: the su you are using does not allow app (u:r:untrusted_app:s0) to connect to su (%s) with binder.\n", context);
                exit(EXIT_FATAL_BINDER_BLOCKED_BY_SELINUX);
            }
            se::freecon(context);
        }
    }

    check_access(_server_dex_path, "server dex path");
    check_access(_starter_dex_path, "starter dex path");

    mkdir("/data/local/tmp/shizuku", 0707);
    chmod("/data/local/tmp/shizuku", 0707);
    if (uid == 0) {
        chown("/data/local/tmp/shizuku", 2000, 2000);
        se::setfilecon("/data/local/tmp/shizuku", "u:object_r:shell_data_file:s0");
    }

    char server_dex_path[PATH_MAX], starter_dex_path[PATH_MAX];
    sprintf(server_dex_path, "/data/local/tmp/shizuku/%s", basename(_server_dex_path));
    sprintf(starter_dex_path, "/data/local/tmp/shizuku/%s", basename(_starter_dex_path));

    copy_if_not_exist(_server_dex_path, server_dex_path);
    copy_if_not_exist(_starter_dex_path, starter_dex_path);

    check_access(server_dex_path, "server dex path");
    check_access(starter_dex_path, "starter dex path");

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

    printf("info: starting server...\n");
    fflush(stdout);
    start_server(server_dex_path, SERVER_CLASS_PATH, SERVER_NAME);

    exit(EXIT_SUCCESS);
}

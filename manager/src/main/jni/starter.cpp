//
// Created by haruue on 17-6-28.
//


#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <dirent.h>
#include <ctime>
#include <cstring>
#include <libgen.h>
#include <sys/stat.h>
#include <sys/system_properties.h>
#include "misc.h"
#include "selinux.h"
#include "cgroup.h"

#define TRUE 1
#define FALSE 0

#define EXIT_FATAL_CANNOT_ACCESS_PATH 1
#define EXIT_FATAL_PATH_NOT_SET 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_FATAL_UID 6
#define EXIT_WARN_START_TIMEOUT 7
#define EXIT_WARN_SERVER_STOP 8

#define SERVER_NAME_LEGACY "shizuku_server_legacy"
#define SERVER_NAME "shizuku_server"

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define SERVER_CLASS_PATH_LEGACY "moe.shizuku.server.ShizukuServer"
#define SERVER_CLASS_PATH "moe.shizuku.server.Starter"

static struct timespec ts{};

static void logcat(time_t now) {
    char command[BUFSIZ];
    char time[BUFSIZ];
    struct tm *tm = localtime(&now);
    strftime(time, sizeof(time), "%m-%d %H:%M:%S.000", tm);
    printf("--- crash start ---\n");
    sprintf(command, "logcat -b crash -t '%s' -d", time);
    printf("[command] %s\n", command);
    fflush(stdout);
    system(command);
    fflush(stdout);
    printf("--- crash end ---\n");
    fflush(stdout);
    printf("--- shizuku start ---\n");
    sprintf(command, "logcat -b main -t '%s' -d -s ShizukuServer ShizukuServerV3 ShizukuManager",
            time);
    printf("[command] %s\n", command);
    fflush(stdout);
    system(command);
    fflush(stdout);
    printf("--- shizuku end ---\n");
    fflush(stdout);
}

static void exit_with_logcat(int code) {
    logcat(ts.tv_sec);
    exit(code);
}

static void setClasspathEnv(const char *path) {
    if (setenv("CLASSPATH", path, TRUE)) {
        perrorf("fatal: can't set CLASSPATH\n");
        exit(EXIT_FATAL_SET_CLASSPATH);
    }
    printf("info: CLASSPATH=%s\n", path);
    fflush(stdout);
}

static int start_server(const char *path, const char *main_class,const char *process_name, int change_context) {
    pid_t pid = fork();
    if (pid == 0) {
        pid = daemon(FALSE, FALSE);
        if (pid == -1) {
            printf("fatal: can't fork");
            exit_with_logcat(EXIT_FATAL_FORK);
        } else {
            // for now, set context to adb shell's context to avoid SELinux problem until we find a reliable way to patch policy
            if (change_context && getuid() == 0) {
                se::setcon("u:r:shell:s0");
            }

            char nice_name[128], class_path[PATH_MAX];
            sprintf(nice_name, "--nice-name=%s", process_name);
            setClasspathEnv(path);
            snprintf(class_path, PATH_MAX, "-Djava.class.path=%s", path);

#ifdef DEBUG
            int sdkLevel = -1, previewSdkLevel = -1;
            char buf[PROP_VALUE_MAX + 1];
            if (__system_property_get("ro.build.version.sdk", buf) > 0)
                sdkLevel = atoi(buf);

            if (__system_property_get("ro.build.version.preview_sdk", buf) > 0)
                previewSdkLevel = atoi(buf);

            if (sdkLevel == -1) {
                printf("fatal: can't read ro.build.version.sdk");
                exit_with_logcat(127);
            }

            if (sdkLevel >= 30 || (sdkLevel == 29 && previewSdkLevel > 0)) {
                const char *appProcessArgs[] = {
                        "/system/bin/app_process",

                        // vm params
                        class_path,
                        "-Xcompiler-option", "--debuggable",
                        "-XjdwpProvider:adbconnection",
                        "-XjdwpOptions:suspend=n,server=y",
                        "/system/bin",

                        // extra params
                        nice_name,

                        // class
                        main_class,

                        // Java params
                        "--debug",
                        nullptr
                };
                if (execvp((const char *) appProcessArgs[0], (char *const *) appProcessArgs)) {
                    exit_with_logcat(EXIT_FATAL_APP_PROCESS);
                }
            } else if (sdkLevel >= 28) {
                const char *appProcessArgs[] = {
                        "/system/bin/app_process",

                        // vm params
                        class_path,
                        "-Xcompiler-option", "--debuggable",
                        "-XjdwpProvider:internal",
                        "-XjdwpOptions:transport=dt_android_adb,suspend=n,server=y",
                        "/system/bin",

                        // extra params
                        nice_name,

                        // class
                        main_class,

                        // Java params
                        "--debug",
                        nullptr
                };
                if (execvp((const char *) appProcessArgs[0], (char *const *) appProcessArgs)) {
                    exit_with_logcat(EXIT_FATAL_APP_PROCESS);
                }
            } else {
                const char *appProcessArgs[] = {
                        "/system/bin/app_process",

                        // vm params
                        class_path,
                        "-Xcompiler-option", "--debuggable",
                        "-agentlib:jdwp=transport=dt_android_adb,suspend=n,server=y",

                        "/system/bin",

                        // extra params
                        nice_name,

                        // class
                        main_class,

                        // Java params
                        "--debug",
                        nullptr
                };
                if (execvp((const char *) appProcessArgs[0], (char *const *) appProcessArgs)) {
                    exit_with_logcat(EXIT_FATAL_APP_PROCESS);
                }
            }
#else
            char *appProcessArgs[] = {
                    const_cast<char *>("/system/bin/app_process"),
                    class_path,
                    const_cast<char *>("/system/bin"),
                    const_cast<char *>(nice_name),
                    const_cast<char *>(main_class),
                    nullptr
            };

            if (execvp(appProcessArgs[0], appProcessArgs)) {
                exit_with_logcat(EXIT_FATAL_APP_PROCESS);
            }
#endif
        }
        return 0;
    } else if (pid == -1) {
        perrorf("fatal: can't fork\n");
        exit_with_logcat(EXIT_FATAL_FORK);
    } else {
        signal(SIGCHLD, SIG_IGN);
        signal(SIGHUP, SIG_IGN);
        printf("info: process forked, pid=%d\n", pid);
        fflush(stdout);
        printf("info: checking %s start...\n", process_name);
        fflush(stdout);
        int count = 0;
        while (get_pids_by_name(process_name).empty()) {
            fflush(stdout);
            usleep(200 * 1000);
            count++;
            if (count >= 50) {
                perrorf("warn: timeout but can't get pid of %s.\n", process_name);
                exit_with_logcat(EXIT_WARN_START_TIMEOUT);
            }
        }
        count = 0;
        while (!get_pids_by_name(process_name).empty()) {
            printf("info: checking %s stability...\n", process_name);
            fflush(stdout);
            usleep(1000 * 500);
            count++;
            if (count >= 3) {
                printf("info: %s started.\n", process_name);
                fflush(stdout);
                return EXIT_SUCCESS;
            }
        }

        perrorf("warn: %s stopped after started.\n", process_name);
        return EXIT_WARN_SERVER_STOP;
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

static int kill_proc_by_name(const char *name) {
    for (auto pid : get_pids_by_name(name)) {
        if (pid == getpid())
            continue;

        if (kill(pid, SIGKILL) == 0)
            printf("info: killed %d (%s)\n", pid, name);
        else
            printf("warn: failed to kill %d (%s)\n", pid, name);
    }
    return 0;
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
    }

    clock_gettime(CLOCK_REALTIME, &ts);

    char *_path = nullptr;
    char *_path_legacy = nullptr;
    int i;
    int use_shell_context = 0;

    for (i = 0; i < argc; ++i) {
        if (strncmp(argv[i], "--path=", 7) == 0) {
            _path = strdup(argv[i] + 7);
        } else if (strncmp(argv[i], "--path-legacy=", 14) == 0) {
            _path_legacy = strdup(argv[i] + 14);
        } else if (strncmp(argv[i], "--use-shell-context", 19) == 0) {
            use_shell_context = 1;
        }
    }
#ifdef DEBUG
    printf("debug: use_shell_context=%d\n", use_shell_context);
    fflush(stdout);
#endif

    if (uid == 0) {
        if (se::getcon(&context) == 0) {
            int res = 0;

            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "call");
            res |= check_selinux("u:r:untrusted_app:s0", context, "binder", "transfer");

            if (res == 0) {
                if (use_shell_context) {
                    printf("warn: context %s seems safe, but force use shell context from cmd.\n", context);
                    fflush(stdout);
                }
            } else {
                use_shell_context = 1;
                printf("warn: app context can't connect to context %s, use shell context instead.\n", context);
                fflush(stdout);
            }
            se::freecon(context);
        }
    }

    check_access(_path, "source dex path");

    mkdir("/data/local/tmp/shizuku", 0707);
    chmod("/data/local/tmp/shizuku", 0707);
    if (uid == 0) {
        chown("/data/local/tmp/shizuku", 2000, 2000);
        se::setfilecon("/data/local/tmp/shizuku", "u:object_r:shell_data_file:s0");
    }

    char path[PATH_MAX], path_legacy[PATH_MAX];
    sprintf(path, "/data/local/tmp/shizuku/%s", basename(_path));
    sprintf(path_legacy, "/data/local/tmp/shizuku/%s", basename(_path_legacy));

    copy_if_not_exist(_path, path);

    check_access(path, "dex path");

    printf("info: starter begin\n");
    fflush(stdout);

    // kill old server
    printf("info: killing old process...\n");
    fflush(stdout);

    kill_proc_by_name(SERVER_NAME);
    kill_proc_by_name(SERVER_NAME_LEGACY);

    if (use_shell_context) {
        printf("info: use %s for Shizuku.\n", "u:r:shell:s0");
        fflush(stdout);
    }

    printf("info: starting server...\n");
    fflush(stdout);
    start_server(path, SERVER_CLASS_PATH, SERVER_NAME, use_shell_context);

    exit_with_logcat(EXIT_SUCCESS);
}

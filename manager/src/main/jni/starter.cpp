//
// Created by haruue on 17-6-28.
//


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <dirent.h>
#include <time.h>
#include <string.h>
#include "misc.h"

#define TRUE 1
#define FALSE 0

#define EXIT_FATAL_CANNOT_ACCESS_PATH 1
#define EXIT_FATAL_PATH_NOT_SET 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_WARN_START_TIMEOUT 7
#define EXIT_WARN_SERVER_STOP 8

#define SERVER_NAME_LEGACY "shizuku_server_legacy"
#define SERVER_NAME "shizuku_server"

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define SERVER_CLASS_PATH "moe.shizuku.server.ShizukuServer"

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
    sprintf(command, "logcat -b main -t '%s' -d -s ShizukuServer ShizukuServerV3 ShizukuManager", time);
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

static int start_server(const char *path, const char *main_class, const char *token,
                        const char *nice_name) {
    pid_t pid = fork();
    if (pid == 0) {
        pid = daemon(FALSE, FALSE);
        if (pid == -1) {
            printf("fatal: can't fork");
            exit_with_logcat(EXIT_FATAL_FORK);
        } else {
            char buf[128];
            sprintf(buf, "--nice-name=%s", nice_name);
            setClasspathEnv(path);
            char *appProcessArgs[] = {
                    const_cast<char *>("/system/bin/app_process"),
                    const_cast<char *>("/system/bin"),
                    const_cast<char *>(buf),
                    const_cast<char *>(main_class),
                    const_cast<char *>(token),
                    nullptr
            };
            if (execvp(appProcessArgs[0], appProcessArgs)) {
                exit_with_logcat(EXIT_FATAL_APP_PROCESS);
            }
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
        printf("info: checking %s start...\n", nice_name);
        fflush(stdout);
        int count = 0;
        while (get_pids_by_name(nice_name).empty()) {
            fflush(stdout);
            usleep(200 * 1000);
            count++;
            if (count >= 50) {
                perrorf("warn: timeout but can't get pid of %s.\n", nice_name);
                exit_with_logcat(EXIT_WARN_START_TIMEOUT);
            }
        }
        count = 0;
        while (!get_pids_by_name(nice_name).empty()) {
            printf("info: checking %s stability...\n", nice_name);
            fflush(stdout);
            usleep(1000 * 500);
            count++;
            if (count >= 3) {
                printf("info: %s started.\n", nice_name);
                fflush(stdout);
                return EXIT_SUCCESS;
            }
        }

        perrorf("warn: %s stopped after started.\n", nice_name);
        return EXIT_WARN_SERVER_STOP;
    }
    return EXIT_SUCCESS;
}

static void check_access(const char* path, const char *name) {
    if (!path) {
        perrorf("fatal: %s not set.\n", name);
        exit(EXIT_FATAL_PATH_NOT_SET);
    }

    if (access(path, F_OK) != 0) {
        perrorf("fatal: can't access %s, please open Shizuku Manager and try again.\n", path);
        exit(EXIT_FATAL_CANNOT_ACCESS_PATH);
    } else {
        printf("info: %s is %s\n", name, path);
    }
}

int kill_proc_by_name(const char *name) {
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

int main(int argc, char **argv) {
    clock_gettime(CLOCK_REALTIME, &ts);

    char *token = nullptr;
    char *path = nullptr;
    char *path_legacy = nullptr;
    int i;
    for (i = 0; i < argc; ++i) {
        if (strncmp(argv[i], "--token=", 8) == 0) {
            token = strdup(argv[i] + 8);
        } else if (strncmp(argv[i], "--path=", 7) == 0) {
            path = strdup(argv[i] + 7);
        } else if (strncmp(argv[i], "--path-legacy=", 14) == 0) {
            path_legacy = strdup(argv[i] + 14);
        }
    }

    if (!token) {
        perrorf("fatal: %s not set.\n", token);
        exit(EXIT_FATAL_PATH_NOT_SET);
    }

    check_access(path, "path");
    check_access(path_legacy, "path-legacy");

    printf("info: starter begin\n");
    fflush(stdout);

    // kill old server
    printf("info: killing old process...\n");
    fflush(stdout);

    kill_proc_by_name(SERVER_NAME);
    kill_proc_by_name(SERVER_NAME_LEGACY);

    printf("info: starting server v3...\n");
    fflush(stdout);
    start_server(path, SERVER_CLASS_PATH, token, SERVER_NAME);

    printf("info: starting server v2 (legacy)...\n");
    fflush(stdout);
    start_server(path_legacy, SERVER_CLASS_PATH, token, SERVER_NAME_LEGACY);

    exit_with_logcat(EXIT_SUCCESS);
}

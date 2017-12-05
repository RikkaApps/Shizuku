//
// Created by haruue on 17-6-28.
//


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <dirent.h>
#include <time.h>

#define TRUE 1
#define FALSE 0

#define EXIT_FATAL_CANNOT_ACCESS_PATH 1
#define EXIT_FATAL_PATH_NOT_SET 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_WARN_START_TIMEOUT 7
#define EXIT_WARN_SERVER_STOP 8

#define LOG_FILE_PATH "/sdcard/Android/data/moe.shizuku.privileged.api/files/shizuku_starter.log"

#define SERVER_NAME "shizuku_server"

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define SERVER_CLASS_PATH "moe.shizuku.server.ShizukuServer"

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
    sprintf(command, "logcat -b main -t '%s' -d -s ShizukuServer ShizukuManager", time);
    printf("[command] %s\n", command);
    fflush(stdout);
    system(command);
    fflush(stdout);
    printf("--- shizuku end ---\n");
    fflush(stdout);
}

void setClasspathEnv(char *path) {
    if (setenv("CLASSPATH", path, TRUE)) {
        perrorf("fatal: can't set CLASSPATH\n");
        exit(EXIT_FATAL_SET_CLASSPATH);
    }
    printf("info: CLASSPATH=%s\n", path);
    fflush(stdout);
}

pid_t getRikkaServerPid() {
    DIR *procDir = opendir("/proc");
    pid_t res = 0;
    if (procDir != NULL) {
        struct dirent *pidDirent;
        while ((pidDirent = readdir(procDir)) != NULL) {
            uint32_t pid = (uint32_t) atoi(pidDirent->d_name);
            if (pid != 0) {  // skip non number directory or files
                char cmdlinePath[PATH_MAX];
                memset(cmdlinePath, 0, PATH_MAX);
                sprintf(cmdlinePath, "/proc/%d/cmdline", pid);
                FILE *cmdlineFile = fopen(cmdlinePath, "r");
                if (cmdlineFile != NULL) {
                    char cmdline[50];
                    memset(cmdline, 0, 50);
                    fread(cmdline, 1, 50, cmdlineFile);
                    cmdline[49] = '\0';
                    fclose(cmdlineFile);
                    if (strstr(cmdline, SERVER_NAME) != NULL) {
                        res = (pid_t) pid;
                        break;
                    }
                }
            }
        }
        closedir(procDir);
    } else {
        perrorf("\nwarn: can't open /proc, please check by yourself.\n");
    }
    return res;
}

void killOldServer() {
    pid_t pid = getRikkaServerPid();
    if (pid != 0) {
        printf("info: old " SERVER_NAME " found, killing\n");
        fflush(stdout);
        if (kill(pid, SIGINT) != 0) {
            printf("info: can't kill old server.\n");
            fflush(stdout);
        }
    } else {
        printf("info: no old " SERVER_NAME " found.\n");
        fflush(stdout);
    }
}

int main(int argc, char **argv) {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    char *token = NULL;
    char *path = NULL;
    int i;
    for (i = 0; i < argc; ++i) {
        if (strncmp(argv[i], "--token=", 8) == 0) {
            token = strdup(argv[i] + 8);
        } else if (strncmp(argv[i], "--path=", 7) == 0) {
            path = strdup(argv[i] + 7);
        }
    }

    printf("info: starter begin\n");
    fflush(stdout);

    killOldServer();

    char buffer[PATH_MAX + 11];
    memset(buffer, 0, PATH_MAX + 11);
    if (path != NULL) {
        if (access(path, F_OK) != 0) {
            perrorf("fatal: can't access %s, please open Shizuku Manager and try again.\n", path);
            logcat(ts.tv_sec);
            return EXIT_FATAL_CANNOT_ACCESS_PATH;
        } else {
            printf("info: dex path is %s\n", path);
        }
    } else {
        perrorf("fatal: path is not set.\n");
        logcat(ts.tv_sec);
        return EXIT_FATAL_PATH_NOT_SET;
    }

    pid_t pid = fork();

    if (pid == 0) {
        pid = daemon(FALSE, FALSE);
        if (pid == -1) {
            printf("fatal: can't fork");
            return EXIT_FATAL_FORK;
        } else {
            freopen(LOG_FILE_PATH, "w", stdout);
            dup2(fileno(stdout), fileno(stderr));
            setClasspathEnv(path);
            char *appProcessArgs[] = {
                    "/system/bin/app_process",
                    "/system/bin",
                    "--nice-name=" SERVER_NAME,
                    SERVER_CLASS_PATH,
                    token,
                    NULL
            };
            if (execvp(appProcessArgs[0], appProcessArgs)) {
                logcat(ts.tv_sec);
                return EXIT_FATAL_APP_PROCESS;
            }
        }
    } else if (pid == -1) {
        perrorf("fatal: can't fork\n");
        logcat(ts.tv_sec);
        return EXIT_FATAL_FORK;
    } else {
        signal(SIGCHLD, SIG_IGN);
        signal(SIGHUP, SIG_IGN);
        printf("info: process forked, pid=%d\n", pid);
        fflush(stdout);
        printf("info: checking " SERVER_NAME " start...\n");
        fflush(stdout);
        int count = 0;
        while (getRikkaServerPid() == 0) {
            fflush(stdout);
            usleep(200 * 1000);
            count++;
            if (count >= 50) {
                perrorf("warn: timeout but can't get pid of " SERVER_NAME ".\n");
                logcat(ts.tv_sec);
                return EXIT_WARN_START_TIMEOUT;
            }
        }
        count = 0;
        while (getRikkaServerPid() != 0) {
            printf("info: checking " SERVER_NAME " stability...\n");
            fflush(stdout);
            usleep(1000 * 500);
            count++;
            if (count >= 3) {
                printf("info: " SERVER_NAME " started.\n");
                fflush(stdout);
                logcat(ts.tv_sec);
                return EXIT_SUCCESS;
            }
        }
        perrorf("warn: " SERVER_NAME " stopped after started.\n");
        logcat(ts.tv_sec);
        return EXIT_WARN_SERVER_STOP;
    }
}

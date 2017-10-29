//
// Created by haruue on 17-6-28.
//


#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <unistd.h>
#include <dirent.h>

#define TRUE 1
#define FALSE 0

#define EXIT_FATAL_CANNOT_ACCESS_PATH 1
#define EXIT_FATAL_PATH_NOT_SET 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_WARN_OPEN_PROC 6
#define EXIT_WARN_START_TIMEOUT 7
#define EXIT_WARN_SERVER_STOP 8
#define EXIT_FATAL_KILL_OLD_SERVER 9
#define EXIT_FATAL_FILE_NOT_FOUND 10

#define LOG_FILE_PATH "/sdcard/Android/data/moe.shizuku.privileged.api/files/shizuku_starter.log"
#define SERVER_LOG_FILE_PATH "/sdcard/Android/data/moe.shizuku.privileged.api/files/shizuku_server.log"

#define SERVER_NAME "shizuku_server"

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

#define SERVER_CLASS_PATH "moe.shizuku.server.ShizukuServer"

char *trimCRLF(char *str) {
    char *p;
    p = strchr(str, '\r');
    if (p != NULL) {
        *p = '\0';
    }
    p = strchr(str, '\n');
    if (p != NULL) {
        *p = '\0';
    }
    return str;
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

void showServerLogs() {
    printf("\ninfo: " SERVER_NAME " log: \n");
    FILE *logFile;
    if ((logFile = fopen(SERVER_LOG_FILE_PATH, "r")) != NULL) {
        int ch;
        while ((ch = fgetc(logFile)) != EOF) {
            fputc(ch, stderr);
        }
        fclose(logFile);
    } else {
        printf("info: log file is not exist.");
    }
    fflush(stdout);
}

void showLogs() {
    perrorf("\ninfo: please report bug with these log: \n");
    FILE *logFile;
    if ((logFile = fopen(LOG_FILE_PATH, "r")) != NULL) {
        int ch;
        while ((ch = fgetc(logFile)) != EOF) {
            fputc(ch, stderr);
        }
        fclose(logFile);
    } else {
        perrorf("info: log file is not exist.");
    }
}

int main(int argc, char **argv) {
    bool skip_check;
    char *token = NULL;
    char *path = NULL;
    int i;
    for (i = 0; i < argc; ++i) {
        if (strcmp("--skip-check", argv[i]) == 0) {
            skip_check = true;
        } else if (strncmp(argv[i], "--token=", 8) == 0) {
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
            return EXIT_FATAL_CANNOT_ACCESS_PATH;
        } else {
            printf("info: dex path is %s\n", path);
        }
    } else {
        perrorf("fatal: path is not set.\n");
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
                return EXIT_FATAL_APP_PROCESS;
            }
        }
    } else if (pid == -1) {
        perrorf("fatal: can't fork\n");
        return EXIT_FATAL_FORK;
    } else {
        signal(SIGCHLD, SIG_IGN);
        signal(SIGHUP, SIG_IGN);
        printf("info: process forked, pid=%d\n", pid);
        fflush(stdout);
        printf("info: check " SERVER_NAME " start");
        fflush(stdout);
        int rikkaServerPid;
        int count = 0;
        while ((rikkaServerPid = getRikkaServerPid()) == 0) {
            printf(".");
            fflush(stdout);
            usleep(200 * 1000);
            count++;
            if (count >= 50) {
                perrorf("\nwarn: timeout but can't get pid of " SERVER_NAME ".\n");
                showLogs();
                return EXIT_WARN_START_TIMEOUT;
            }
        }
        if (!skip_check) {
            printf("\ninfo: check " SERVER_NAME " stable");
            fflush(stdout);
            count = 0;
            while ((rikkaServerPid = getRikkaServerPid()) != 0) {
                printf(".");
                fflush(stdout);
                usleep(1000 * 1000);
                count++;
                if (count >= 5) {
                    printf("\ninfo: " SERVER_NAME " started.\n");
                    fflush(stdout);
                    showServerLogs();
                    return EXIT_SUCCESS;
                }
            }
            perrorf("\nwarn: " SERVER_NAME " stopped after started.\n");
            showLogs();
            showServerLogs();
            return EXIT_WARN_SERVER_STOP;
        } else {
            printf("\ninfo: " SERVER_NAME " started.\n");
            fflush(stdout);
            showServerLogs();
            return EXIT_SUCCESS;
        }
    }
    return EXIT_SUCCESS;
}

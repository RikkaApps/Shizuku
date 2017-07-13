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

#define EXIT_FATAL_PM_PATH 1
#define EXIT_FATAL_PM_PATH_MALFORMED 2
#define EXIT_FATAL_SET_CLASSPATH 3
#define EXIT_FATAL_FORK 4
#define EXIT_FATAL_APP_PROCESS 5
#define EXIT_WARN_OPEN_PROC 6
#define EXIT_WARN_START_TIMEOUT 7
#define EXIT_WARN_SERVER_STOP 8
#define EXIT_FATAL_KILL_OLD_SERVER 9
#define EXIT_FATAL_FILE_NOT_FOUND 10

#define LOG_FILE_PATH "/data/local/tmp/rikka_server_starter.log"

#define perrorf(...) fprintf(stderr, __VA_ARGS__)

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

char *getApkPath(char *buffer) {
    FILE *file = popen("pm path moe.shizuku.privileged.api", "r");
    if (file != NULL) {
        fgets(buffer, (PATH_MAX + 11) * sizeof(char), file);
    } else {
        perrorf("warn: can't invoke `pm path'\n");
        return NULL;
    }
    trimCRLF(buffer);
    buffer = strchr(buffer, ':');
    if (buffer == NULL) {
        perrorf("warn: can't get apk path\n");
        return NULL;
    }
    buffer++;
    printf("info: apk installed path: %s\n", buffer);
    fflush(stdout);
    return buffer;
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
    if (procDir != NULL) {
        struct dirent *pidDirent;
        while ((pidDirent = readdir(procDir)) != NULL) {
            uint32_t pid = (uint32_t) atoi(pidDirent->d_name);
            if (pid != 0) {  // skip non number directory or files
                char cmdlinePath[PATH_MAX];
                memset(cmdlinePath, 0, PATH_MAX);
                sprintf(cmdlinePath, "/proc/%d/cmdline", pid);
                FILE *cmdlineFile;
                if ((cmdlineFile = fopen(cmdlinePath, "r")) != NULL) {
                    char cmdline[50];
                    memset(cmdline, 0, 50);
                    fread(cmdline, 1, 50, cmdlineFile);
                    cmdline[49] = '\0';
                    fclose(cmdlineFile);
                    if (strstr(cmdline, "rikka_server") != NULL) {
                        return (pid_t) pid;
                    }
                }
            }
        }
        return 0;
    } else {
        perrorf("\nwarn: can't open /proc, please check by yourself.\n");
        exit(EXIT_WARN_OPEN_PROC);
    }
}

void killOldServer() {
    pid_t pid = getRikkaServerPid();
    if (pid != 0) {
        printf("info: old rikka_server found, killing\n");
        fflush(stdout);
        if (kill(pid, SIGINT) != 0) {
            perrorf("fatal: can't kill old server, if you started it by root, please stop it by:\n\n\t");
            perrorf("adb shell su -c \"kill %d\"", pid);
            perrorf("\n\nand retry.\n");
            exit(EXIT_FATAL_KILL_OLD_SERVER);
        }
    } else {
        printf("info: no old rikka_server found.\n");
        fflush(stdout);
    }
}

void showLogs() {
    perrorf("info: please report bug with these log: \n");
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
    char *fallback_path = NULL;
    for (int i = 0; i < argc; ++i) {
        if (strcmp("--skip-check", argv[i]) == 0) {
            skip_check = true;
        } else if (strncmp(argv[i], "--token=", 8) == 0) {
            token = strdup(argv[i] + 8);
        } else if (strncmp(argv[i], "--fallback-path=", 16) == 0) {
            fallback_path = strdup(argv[i] + 16);
        }
    }
    printf("info: starter begin\n");
    fflush(stdout);
    killOldServer();
    char buffer[PATH_MAX + 11];
    memset(buffer, 0, PATH_MAX + 11);
    char *path = getApkPath(buffer);
    if (path == NULL) {
        if (fallback_path != NULL) {
            if (access(fallback_path, F_OK)) {
                perrorf("\nwarn: can't get apk path using pm, use fallback path.\n");
                path = fallback_path;
            } else {
                perrorf("\nfatal: fallback path set but file not found, please open Shizuku Manager and try again.\n");
                exit(EXIT_FATAL_FILE_NOT_FOUND);
            }
        } else {
            perrorf("\nfatal: can't get apk path using pm and no fallback path set.\n");
            exit(EXIT_FATAL_PM_PATH);
        }
    }

    pid_t pid = fork();
    if (pid == 0) {
        pid = daemon(FALSE, FALSE);
        if (pid == -1) {
            printf("fatal: can't fork");
            exit(EXIT_FATAL_FORK);
        } else {
            freopen(LOG_FILE_PATH, "w", stdout);
            dup2(fileno(stdout), fileno(stderr));
            setClasspathEnv(path);
            char *appProcessArgs[] = {
                    "/system/bin/app_process",
                    "/system/bin",
                    "--nice-name=rikka_server",
                    "moe.shizuku.server.Server",
                    token,
                    NULL
            };
            if (execvp(appProcessArgs[0], appProcessArgs)) {
                char err[100];
                exit(EXIT_FATAL_APP_PROCESS);
            }
        }
    } else if (pid == -1) {
        perrorf("fatal: can't fork\n");
        exit(EXIT_FATAL_FORK);
    } else {
        signal(SIGCHLD, SIG_IGN);
        signal(SIGHUP, SIG_IGN);
        printf("info: process forked, pid=%d\n", pid);
        fflush(stdout);
        printf("info: check rikka_server start");
        fflush(stdout);
        int rikkaServerPid;
        int count = 0;
        while ((rikkaServerPid = getRikkaServerPid()) == 0) {
            printf(".");
            fflush(stdout);
            usleep(200 * 1000);
            count++;
            if (count >= 50) {
                perrorf("\nwarn: timeout but can't get pid of rikka_server.\n");
                showLogs();
                exit(EXIT_WARN_START_TIMEOUT);
            }
        }
        if (!skip_check) {
            printf("\ninfo: check rikka_server stable");
            fflush(stdout);
            count = 0;
            while ((rikkaServerPid = getRikkaServerPid()) != 0) {
                printf(".");
                fflush(stdout);
                usleep(1000 * 1000);
                count++;
                if (count >= 5) {
                    printf("\ninfo: rikka_server started.\n");
                    fflush(stdout);
                    exit(EXIT_SUCCESS);
                }
            }
            perrorf("\nwarn: rikka_server stopped after started.\n");
            showLogs();
            exit(EXIT_WARN_SERVER_STOP);
        } else {
            printf("\ninfo: rikka_server started.\n");
            fflush(stdout);
            exit(EXIT_SUCCESS);
        }
    }
}

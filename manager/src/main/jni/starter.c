//
// Created by haruue on 17-6-28.
//


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define TRUE 1
#define FALSE 0

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
        perror("fatal: can't invoke `pm path'\n");
        exit(1);
    }
    trimCRLF(buffer);
    buffer = strchr(buffer, ':');
    if (buffer == NULL) {
        perror("fatal: can't get apk path, have you installed Shizuku Manager? Get it from https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api\n");
        exit(2);
    }
    buffer++;
    printf("info: apk installed path: %s\n", buffer);
    return buffer;
}

void setClasspathEnv(char *path) {
    if (setenv("CLASSPATH", path, TRUE)) {
        perror("fatal: can't set CLASSPATH\n");
        exit(3);
    }
    printf("info: CLASSPATH=%s\n", path);
}

int main(int argc, char **argv) {
    char buffer[PATH_MAX + 11];
    char *path = getApkPath(buffer);
    pid_t pid = fork();
    if (pid == 0) {
        setClasspathEnv(path);
        char *appProcessArgs[] = {
                "/system/bin/app_process",
                "/system/bin",
                "--nice-name=rikka_server",
                "moe.shizuku.server.Server",
                NULL
        };
        if (execvp(appProcessArgs[0], appProcessArgs)) {
            perror("fatal: can't invoke app_process\n");
            exit(5);
        }
    } else if (pid == -1) {
        perror("fatal: can't fork\n");
        exit(4);
    } else {
        exit(0);
    }
}

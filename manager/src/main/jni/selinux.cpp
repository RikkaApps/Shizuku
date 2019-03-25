#include <fcntl.h>
#include <cstring>
#include <unistd.h>
#include <dlfcn.h>
#include <cerrno>
#include <syscall.h>
#include "selinux.h"

static int _setcon(const char *ctx) {
    int fd = open("/proc/self/attr/current", O_WRONLY | O_CLOEXEC);
    if (fd < 0)
        return fd;
    size_t len = strlen(ctx) + 1;
    ssize_t rc = write(fd, ctx, len);
    close(fd);
    return rc != len;
}

static int _setfilecon(const char *path, const char *ctx) {
    int rc = syscall(__NR_setxattr, path, "security.selinux"/*XATTR_NAME_SELINUX*/, ctx, strlen(ctx) + 1, 0);
    if (rc) {
        errno = -rc;
        return -1;
    }
    return 0;
}

int (*setcon)(const char *) = _setcon;
int (*setfilecon)(const char *, const char *) = _setfilecon;

void selinux_init() {
    if (access("/system/lib/libselinux.so", F_OK) != 0)
        return;

    void *handle = dlopen("libselinux.so", RTLD_LAZY);
    setcon = (int (*)(const char *)) dlsym(handle, "setcon");
    setfilecon = (int (*)(const char *, const char *)) dlsym(handle, "setfilecon");
}

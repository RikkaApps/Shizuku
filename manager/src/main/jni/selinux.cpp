#include <fcntl.h>
#include <cstring>
#include <unistd.h>
#include <dlfcn.h>
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

int (*setcon)(const char *) = _setcon;

void selinux_init() {
    if (access("/system/lib/libselinux.so", F_OK) != 0)
        return;

    void *handle = dlopen("libselinux.so", RTLD_LAZY);
    setcon = (int (*)(const char *)) dlsym(handle, "setcon");
}

#include <cstring>
#include <fcntl.h>
#include <unistd.h>
#include <cstdio>

namespace cgroup {
    bool switch_cgroup(const char *cgroup, int pid) {
        char buf[1024];
        snprintf(buf, sizeof(buf), "%s/cgroup.procs", cgroup);
        if (access(buf, F_OK) != 0)
            return false;
        int fd = open(buf, O_WRONLY | O_APPEND | O_CLOEXEC);
        if (fd == -1)
            return false;
        snprintf(buf, sizeof(buf), "%d\n", pid);
        ssize_t c = write(fd, buf, strlen(buf));
        close(fd);
        return c == strlen(buf);
    }
}

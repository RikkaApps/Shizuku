#include <cstring>
#include <fcntl.h>
#include <unistd.h>
#include <cstdio>

namespace cgroup {

    static ssize_t fdgets(char *buf, const size_t size, int fd) {
        ssize_t len = 0;
        buf[0] = '\0';
        while (len < size - 1) {
            ssize_t ret = read(fd, buf + len, 1);
            if (ret < 0)
                return -1;
            if (ret == 0)
                break;
            if (buf[len] == '\0' || buf[len++] == '\n') {
                break;
            }
        }
        buf[len] = '\0';
        buf[size - 1] = '\0';
        return len;
    }

    int get_cgroup(int pid, int* cuid, int *cpid) {
        char buf[PATH_MAX];
        snprintf(buf, PATH_MAX, "/proc/%d/cgroup", pid);

        int fd = open(buf, O_RDONLY);
        if (fd == -1)
            return -1;

        while (fdgets(buf, PATH_MAX, fd) > 0) {
            if (sscanf(buf, "%*d:cpuacct:/uid_%d/pid_%d", cuid, cpid) == 2) {
                close(fd);
                return 0;
            }
        }
        close(fd);
        return -1;
    }

    static int switch_cgroup(int pid, int cuid, int cpid, const char *name) {
        char buf[PATH_MAX];
        if (cuid != -1 && cpid != -1) {
            snprintf(buf, PATH_MAX, "/acct/uid_%d/pid_%d/%s", cuid, cpid, name);
        } else {
            snprintf(buf, PATH_MAX, "/acct/%s", name);
        }

        int fd = open(buf, O_WRONLY | O_APPEND);
        if (fd == -1)
            return -1;

        snprintf(buf, PATH_MAX, "%d\n", pid);
        if (write(fd, buf, strlen(buf)) == -1) {
            close(fd);
            return -1;
        }

        close(fd);
        return 0;
    }

    int switch_cgroup(int pid, int cuid, int cpid) {
        int res = 0;
        res += switch_cgroup(pid, cuid, cpid, "cgroup.procs");
        res += switch_cgroup(pid, cuid, cpid, "tasks");
        return res;
    }

}
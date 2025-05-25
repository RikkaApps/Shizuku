#include <sys/types.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <zconf.h>
#include <dirent.h>
#include <fcntl.h>
#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <sched.h>
#include <cctype>
#include <cerrno>
#include "misc.h"

ssize_t fdgets(char *buf, const size_t size, int fd) {
    buf[0] = '\0';
    ssize_t ret;
    do {
        ret = read(fd, buf, size - 1);
    } while (ret < 0 && errno == EINTR);
    if (ret < 0)
        return -1;
    buf[ret] = '\0';
    return ret;
}

int get_proc_name(int pid, char *name, size_t size) {
    int fd;
    char buf[PATH_MAX];
    snprintf(buf, sizeof(buf), "/proc/%d/cmdline", pid);
    if ((fd = open(buf, O_RDONLY)) == -1)
        return 1;
    fdgets(name, size, fd);
    close(fd);
    return 0;
}

int is_num(const char *s) {
    size_t len = strlen(s);
    for (size_t i = 0; i < len; ++i)
        if (s[i] < '0' || s[i] > '9')
            return 0;
    return 1;
}

int copyfileat(int src_path_fd, const char *src_path, int dst_path_fd, const char *dst_path) {
    int src_fd;
    int dst_fd;
    struct stat stat_buf{};
    int64_t size_remaining;
    size_t count;
    ssize_t result;

    if ((src_fd = openat(src_path_fd, src_path, O_RDONLY)) == -1)
        return -1;

    if (fstat(src_fd, &stat_buf) == -1)
        return -1;

    dst_fd = openat(dst_path_fd, dst_path, O_WRONLY | O_CREAT | O_TRUNC, stat_buf.st_mode);
    if (dst_fd == -1) {
        close(src_fd);
        return -1;
    }

    size_remaining = stat_buf.st_size;
    for (;;) {
        if (size_remaining > 0x7ffff000)
            count = 0x7ffff000;
        else
            count = static_cast<size_t>(size_remaining);

        result = sendfile(dst_fd, src_fd, nullptr, count);
        if (result == -1) {
            close(src_fd);
            close(dst_fd);
            unlink(dst_path);
            return -1;
        }

        size_remaining -= result;
        if (size_remaining == 0) {
            close(src_fd);
            close(dst_fd);
            return 0;
        }
    }
}

int copyfile(const char *src_path, const char *dst_path) {
    return copyfileat(0, src_path, 0, dst_path);
}

uintptr_t memsearch(const uintptr_t start, const uintptr_t end, const void *value, size_t size) {
    uintptr_t _start = start;
    while (true) {
        if (_start + size >= end)
            return 0;

        if (memcmp((const void *) _start, value, size) == 0)
            return _start;

        _start += 1;
    }
}

int switch_mnt_ns(int pid) {
    char mnt[32];
    snprintf(mnt, sizeof(mnt), "/proc/%d/ns/mnt", pid);
    if (access(mnt, R_OK) == -1) return -1;

    int fd = open(mnt, O_RDONLY);
    if (fd < 0) return -1;

    int res = setns(fd, 0);
    close(fd);
    return res;
}

void foreach_proc(foreach_proc_function *func) {
    DIR *dir;
    struct dirent *entry;

    if (!(dir = opendir("/proc")))
        return;

    while ((entry = readdir(dir))) {
        if (entry->d_type != DT_DIR) continue;
        if (!is_num(entry->d_name)) continue;
        pid_t pid = atoi(entry->d_name);
        func(pid);
    }

    closedir(dir);
}

char *trim(char *str) {
    size_t len = 0;
    char *frontp = str;
    char *endp = nullptr;

    if (str == nullptr) { return nullptr; }
    if (str[0] == '\0') { return str; }

    len = strlen(str);
    endp = str + len;

    /* Move the front and back pointers to address the first non-whitespace
     * characters from each end.
     */
    while (isspace((unsigned char) *frontp)) { ++frontp; }
    if (endp != frontp) {
        while (isspace((unsigned char) *(--endp)) && endp != frontp) {}
    }

    if (str + len - 1 != endp)
        *(endp + 1) = '\0';
    else if (frontp != str && endp == frontp)
        *str = '\0';

    /* Shift the string so that it starts at str so that if it's dynamically
     * allocated, we can still free it on the returned pointer.  Note the reuse
     * of endp to mean the front of the string buffer now.
     */
    endp = str;
    if (frontp != str) {
        while (*frontp) { *endp++ = *frontp++; }
        *endp = '\0';
    }

    return str;
}

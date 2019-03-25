#include <sys/types.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <zconf.h>
#include <vector>
#include <dirent.h>
#include <fcntl.h>

ssize_t fdgets(char *buf, const size_t size, int fd) {
    ssize_t len = 0;
    buf[0] = '\0';
    while (len < size - 1) {
        ssize_t ret = read(fd, buf + len, 1);
        if (ret < 0)
            return -1;
        if (ret == 0)
            break;
        if (buf[len] == '\0' || buf[len++] == '\n') {
            buf[len] = '\0';
            break;
        }
    }
    buf[len] = '\0';
    buf[size - 1] = '\0';
    return len;
}

int is_proc_name_equals(int pid, const char *name) {
    int fd;

    char buf[1024];
    snprintf(buf, sizeof(buf), "/proc/%d/cmdline", pid);
    if (access(buf, R_OK) == -1 || (fd = open(buf, O_RDONLY)) == -1)
        return 0;
    if (fdgets(buf, sizeof(buf), fd) == 0) {
        snprintf(buf, sizeof(buf), "/proc/%d/comm", pid);
        close(fd);
        if (access(buf, R_OK) == -1 || (fd = open(buf, O_RDONLY)) == -1)
            return 0;
        fdgets(buf, sizeof(buf), fd);
    }
    close(fd);

    return strcmp(buf, name) == 0;
}

int get_proc_name(int pid, char *name, size_t _size) {
    int fd;
    ssize_t __size;

    char buf[1024];
    snprintf(buf, sizeof(buf), "/proc/%d/cmdline", pid);
    if (access(buf, R_OK) == -1 || (fd = open(buf, O_RDONLY)) == -1)
        return 1;
    if ((__size = fdgets(buf, sizeof(buf), fd)) == 0) {
        snprintf(buf, sizeof(buf), "/proc/%d/comm", pid);
        close(fd);
        if (access(buf, R_OK) == -1 || (fd = open(buf, O_RDONLY)) == -1)
            return 1;
        __size = fdgets(buf, sizeof(buf), fd);
    }
    close(fd);

    if (__size < _size) {
        strncpy(name, buf, static_cast<size_t>(__size));
        name[__size] = '\0';
    } else {
        strncpy(name, buf, _size);
        name[_size] = '\0';
    }

    return 0;
}

int is_num(const char *s) {
    size_t len = strlen(s);
    for (size_t i = 0; i < len; ++i)
        if (s[i] < '0' || s[i] > '9')
            return 0;
    return 1;
}

std::vector<pid_t> get_pids_by_name(const char *name) {
    std::vector<pid_t> res;

    DIR *dir;
    struct dirent *entry;

    if (!(dir = opendir("/proc")))
        return res;

    while ((entry = readdir(dir))) {
        if (entry->d_type == DT_DIR) {
            if (is_num(entry->d_name)) {
                pid_t pid = atoi(entry->d_name);
                if (is_proc_name_equals(pid, name))
                    res.push_back(pid);
            }

        }
    }

    closedir(dir);
    return res;
}

int copyfileat(int src_path_fd, const char *src_path, int dst_path_fd, const char *dst_path) {
    int src_fd;
    int dst_fd;
    struct stat stat_buf{};
    off_t offset = 0;
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
        return false;
    }

    size_remaining = stat_buf.st_size;
    for (;;) {
        if (size_remaining > 0x7ffff000)
            count = 0x7ffff000;
        else
            count = static_cast<size_t>(size_remaining);

        result = sendfile(dst_fd, src_fd, &offset, count);
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
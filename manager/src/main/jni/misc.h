#ifndef MISC_H
#define MISC_H

#include <vector>

std::vector<pid_t> get_pids_by_name(const char *name);
int copyfile(const char *src_path, const char *dst_path);
uintptr_t memsearch(const uintptr_t start, const uintptr_t end, const void *value, size_t size);

#endif // MISC_H

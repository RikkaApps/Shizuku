#ifndef MISC_H
#define MISC_H

#include <vector>

std::vector<pid_t> get_pids_by_name(const char *name);
int copyfile(const char *src_path, const char *dst_path);

#endif // MISC_H

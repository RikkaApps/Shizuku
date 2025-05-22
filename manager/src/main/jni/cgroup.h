#ifndef CGROUP_H
#define CGROUP_H

namespace cgroup {
    bool switch_cgroup(const char *cgroup, int pid);
}

#endif // CGROUP_H

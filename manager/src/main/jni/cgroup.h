#ifndef CGROUP_H
#define CGROUP_H

namespace cgroup {
    int get_cgroup(int pid, int* cuid, int *cpid);
    int switch_cgroup(int pid, int cuid, int cpid);
}

#endif // CGROUP_H

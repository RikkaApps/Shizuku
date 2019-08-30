#ifndef SELINUX_H
#define SELINUX_H

namespace se {
    void init();

    using getcon_t = int(char **);
    using setcon_t = int(const char *);
    using setfilecon_t = int(const char *, const char *);
    using selinux_check_access_t = int(const char *, const char *, const char *, const char *,
                                       void *);
    using freecon_t = void(char *);

    extern getcon_t *getcon;
    extern setcon_t *setcon;
    extern setfilecon_t *setfilecon;
    extern selinux_check_access_t *selinux_check_access;
    extern freecon_t *freecon;
}

#endif // SELINUX_H

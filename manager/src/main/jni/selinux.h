#ifndef SELINUX_H
#define SELINUX_H

void selinux_init();
extern int (*setcon)(const char *);

#endif // SELINUX_H

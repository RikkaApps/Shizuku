#ifndef SELINUX_H
#define SELINUX_H

void selinux_init();
extern int (*setcon)(const char *);
extern int (*setfilecon)(const char *, const char *);

#endif // SELINUX_H

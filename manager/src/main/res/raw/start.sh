#!/system/bin/sh

STARTER_PATH="%%%STARTER_PATH%%%"
STARTER_PARAM="%%%STARTER_PARAM%%%"
LIBRARY_PATH="%%%LIBRARY_PATH%%%"

echo "info: start.sh begin"

if [[ -f "$STARTER_PATH" ]]; then
    rm -f /data/local/tmp/shizuku_starter
    cp "$STARTER_PATH" /data/local/tmp/shizuku_starter
    chmod 700 /data/local/tmp/shizuku_starter
    chown 2000 /data/local/tmp/shizuku_starter
    chgrp 2000 /data/local/tmp/shizuku_starter
    chcon u:object_r:shell_data_file:s0 /data/local/tmp/shizuku_starter

    #mkdir -p /data/local/tmp/shizuku
    #chmod 700 /data/local/tmp/shizuku
    #chown 2000 /data/local/tmp/shizuku
    #chgrp 2000 /data/local/tmp/shizuku
    #chcon u:object_r:shell_data_file:s0 /data/local/tmp/shizuku_starter

    #rm -f /data/local/tmp/shizuku/libhelper.so
    #cp "$LIBRARY_PATH" /data/local/tmp/shizuku/libhelper.so
    #chmod 700 /data/local/tmp/shizuku/libhelper.so
    #chown 2000 /data/local/tmp/shizuku/libhelper.so
    #chgrp 2000 /data/local/tmp/shizuku/libhelper.so

    export PATH=/data/local/tmp:/system/bin:$PATH
    shizuku_starter ${STARTER_PARAM} $1
    result=$?
    if [[ ${result} -ne 0 ]]; then
        echo "info: shizuku_starter exit with non-zero value $result"
    else
        echo "info: shizuku_starter exit with 0"
    fi
else
    echo "Starter file not exist, please open Shizuku Manager and try again."
fi
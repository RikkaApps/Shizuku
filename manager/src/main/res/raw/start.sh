#!/system/bin/sh

STARTER_PATH="%%%STARTER_PATH%%%"
STARTER_PARAM="%%%STARTER_PARAM%%%"

echo "info: start.sh begin"

if [[ -f "$STARTER_PATH" ]]; then
    rm /data/local/tmp/shizuku_starter 2> /dev/null
    cp "$STARTER_PATH" /data/local/tmp/shizuku_starter
    chmod 755 /data/local/tmp/shizuku_starter

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
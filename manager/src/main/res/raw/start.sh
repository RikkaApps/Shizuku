#!/system/bin/sh

SOURCE_PATH="%%%STARTER_PATH%%%"
STARTER_PATH="/data/local/tmp/shizuku_starter"

echo "info: start.sh begin"

if [ -f "$SOURCE_PATH" ]; then
    rm -f $STARTER_PATH
    cp "$SOURCE_PATH" $STARTER_PATH
    chmod 700 $STARTER_PATH
    chown 2000 $STARTER_PATH
    chgrp 2000 $STARTER_PATH
    echo "info: copy starter from $SOURCE_PATH"
fi

if [ -f $STARTER_PATH ]; then
  echo "info: exec $STARTER_PATH"
    $STARTER_PATH "$1"
    result=$?
    if [ ${result} -ne 0 ]; then
        echo "info: shizuku_starter exit with non-zero value $result"
    else
        echo "info: shizuku_starter exit with 0"
    fi
else
    echo "Starter file not exist, please open Shizuku and try again."
fi
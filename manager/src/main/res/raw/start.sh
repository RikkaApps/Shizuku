#!/system/bin/sh

STARTER_PATH=%%%STARTER_PATH%%%

echo "info: start.sh begin"

if [ -f "$STARTER_PATH" ]; then
    rm /data/local/tmp/shizuku_starter 2> /dev/null
    cp "$STARTER_PATH" /data/local/tmp/shizuku_starter
    chmod +x /data/local/tmp/shizuku_starter
    export PATH=/data/local/tmp:/system/bin:$PATH
    shizuku_starter
    if [ $? -ne 0 ]; then
        echo "shizuku_starter exit with non-zero value $?"
    fi
else
    echo "Starter file not exist, please open Shizuku Manager and try again."
fi
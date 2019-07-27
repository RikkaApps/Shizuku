#!/bin/bash

# check local.properties

function prompt() {
    echo -n "$1"
    read $2
}

echo "checking local.properties..."

if [ -f local.properties ]; then
    grep -q 'sdk\.dir=' local.properties
    isSdkDirSet=$?
    grep -q 'ndk\.dir=' local.properties
    isNdkDirSet=$?
else
    isSdkDirSet=1
    isNdkDirSet=1
fi

if [ $isSdkDirSet -ne 0 ]; then
    prompt 'Android SDK Path (C:\\Android\\sdk): ' sdkDir
    echo "sdk.dir=${sdkDir//\\/\\\\}" >> local.properties
fi

if [ $isNdkDirSet -ne 0 ]; then
    prompt 'Android NDK Path (C:\\Android\\sdk\\ndk-bundle): ' ndkDir
    echo "ndk.dir=${ndkDir//\\/\\\\}" >> local.properties
fi

function download() {
    if ! [ -f $2 ];
    then
       echo "downloading $1"
       curl "$1" --create-dirs -s -S -o "$2"
    else
        echo "$2 exists, skip"
    fi
}

# download android.jar with hidden api

download "https://raw.githubusercontent.com/anggrayudi/android-hidden-api/master/android-27/android.jar" "android-$api/android.jar"

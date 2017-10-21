#!/bin/bash

# download system service aidl files

branches=(lollipop-release lollipop-mr1-release marshmallow-release nougat-release nougat-mr1-release oreo-release)
apis=(21 22 23 24 25 26)

files=(
    android/content/pm/IPackageManager.aidl
    android/os/IUserManager.aidl
    com/android/internal/app/IAppOpsService.aidl
)

function download_aidl() {
    file="source-$3/$1"
    url="https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/$2/core/java/$1"
    download $url $file
}

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

echo download aidl from aosp-mirror/platform_frameworks_base

for ((i = 0; i < ${#files[@]}; i++));
do
    for ((j = 0; j < ${#apis[@]}; j++));
    do
        download_aidl ${files[$i]} ${branches[$j]} ${apis[$j]}
    done
done

echo
echo download android.jar from anggrayudi/android-hidden-api

for api in {21..25}
do
    download "https://raw.githubusercontent.com/anggrayudi/android-hidden-api/master/android-$api/android.jar" "android-$api/android.jar"
done

echo
echo download android.jar from Trumeet/android-hidden-api

download "https://raw.githubusercontent.com/Trumeet/android-hidden-api/master/android-26/android.jar" "android-26/android.jar"

# generate codes and shizuku server dex

echo
echo generate codes and shizuku server dex

./gradlew :manager:generate

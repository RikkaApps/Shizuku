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


# download system service aidl files

branches=(lollipop-release lollipop-mr1-release marshmallow-release nougat-release nougat-mr1-release oreo-release)
apis=(21 22 23 24 25 26)

files=(
    android/content/pm/IPackageManager.aidl                     platform_frameworks_base    core/java
    android/os/IUserManager.aidl                                platform_frameworks_base    core/java
    com/android/internal/app/IAppOpsService.aidl                platform_frameworks_base    core/java
    android/nfc/INfcAdapter.aidl                                platform_frameworks_base    core/java
    com/android/ims/internal/IImsService.aidl                   platform_frameworks_base    telephony/java
    com/android/internal/telephony/ITelephony.aidl              platform_frameworks_base    telephony/java
    com/android/internal/telephony/ISms.aidl                    platform_frameworks_base    telephony/java
    com/android/internal/telephony/ICarrierConfigLoader.aidl    platform_frameworks_base    telephony/java
    com/android/internal/telephony/IPhoneSubInfo.aidl           platform_frameworks_base    telephony/java

    android/content/pm/ILauncherApps.aidl                       platform_frameworks_base    core/java
    android/app/job/IJobScheduler.aidl                          platform_frameworks_base    core/java
    android/app/backup/IBackupManager.aidl                      platform_frameworks_base    core/java
    android/app/ISearchManager.aidl                             platform_frameworks_base    core/java
    android/app/INotificationManager.aidl                       platform_frameworks_base    core/java
    com/android/internal/statusbar/IStatusBarService.aidl       platform_frameworks_base    core/java
)

function download_aidl() {
    file="source-$5/$1"
    url="https://raw.githubusercontent.com/aosp-mirror/$2/$4/$3/$1"
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

for ((i = 0; i < ${#files[@]}; i+=3));
do
    for ((j = 0; j < ${#apis[@]}; j++));
    do
        download_aidl ${files[$i]} ${files[$i + 1]} ${files[$i + 2]} ${branches[$j]} ${apis[$j]}
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

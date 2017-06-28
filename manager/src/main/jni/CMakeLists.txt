cmake_minimum_required(VERSION 3.4.1)

# build executable files to manager/build/starter/<arch>/libshizuku.so, which is
# set as jniLibs.srcDirs in manager/build.gradle.
set(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/../../../build/starter/${ANDROID_ABI})

add_executable( libshizuku.so
                starter.c )

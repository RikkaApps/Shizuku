cmake_minimum_required(VERSION 3.4.1)

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/../../../build/starter/${ANDROID_ABI})

add_executable( libshizuku.so
                starter.c )

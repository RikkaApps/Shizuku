cmake_minimum_required(VERSION 3.4.1)

set(LINKER_FLAGS "-Wl,--hash-style=both")
set(C_FLAGS "-Werror=format")

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    message("Builing Release...")

    set(LINKER_FLAGS "${LINKER_FLAGS} -Wl,-exclude-libs,ALL -Wl,--gc-sections")
    set(C_FLAGS "${C_FLAGS} -O2 -fvisibility=hidden -fvisibility-inlines-hidden")
elseif (CMAKE_BUILD_TYPE STREQUAL "Debug")
    message("Builing Debug...")

    add_definitions(-DDEBUG)
endif ()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_FLAGS}")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${LINKER_FLAGS}")
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} ${LINKER_FLAGS}")

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/../../../build/starter/${ANDROID_ABI})

find_library(log-lib log)
find_package(boringssl REQUIRED CONFIG)

add_executable(libshizuku.so
        starter.cpp misc.cpp selinux.cpp cgroup.cpp android.cpp)

target_link_libraries(libshizuku.so ${log-lib})

#add_library(helper SHARED
#        helper.cpp selinux.cpp)
#
#target_link_libraries(helper ${log-lib})

add_library(adb SHARED
        adb_pairing.cpp bypass.cpp misc.cpp pmparser.c)

target_link_libraries(adb ${log-lib} boringssl::crypto)
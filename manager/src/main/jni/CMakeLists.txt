cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--hash-style=both,-exclude-libs,ALL")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden")

set(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/../../../build/starter/${ANDROID_ABI})

if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    add_definitions(-DDEBUG)
endif ()

find_library(log-lib log)

add_executable(libshizuku.so
        starter.cpp misc.cpp selinux.cpp cgroup.cpp)

target_link_libraries(libshizuku.so ${log-lib})

add_library(helper SHARED
        helper.cpp selinux.cpp)

target_link_libraries(helper ${log-lib})
cmake_minimum_required(VERSION 3.31)

project("shizuku")

set(CMAKE_CXX_STANDARD 17)

add_compile_options(-Werror=format -fdata-sections -ffunction-sections -fno-exceptions -fno-rtti -fno-threadsafe-statics)

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    message("Builing Release...")

    add_compile_options(-Os -flto -fvisibility=hidden -fvisibility-inlines-hidden)
    add_link_options(-flto -Wl,--exclude-libs,ALL -Wl,--gc-sections)
    add_link_options(-Wl,--strip-all -Wl,--script,${CMAKE_CURRENT_SOURCE_DIR}/script.ld)
else ()
    message("Builing Debug...")

    add_definitions(-DDEBUG)
endif ()

find_package(boringssl REQUIRED CONFIG)
find_package(cxx REQUIRED CONFIG)

add_executable(libshizuku.so
        starter.cpp misc.cpp selinux.cpp cgroup.cpp)

target_link_libraries(libshizuku.so log cxx::cxx)

add_library(adb SHARED
        adb_pairing.cpp misc.cpp)

target_link_libraries(adb log boringssl::crypto_static cxx::cxx)

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_STANDARD 17)

set(C_FLAGS "-Werror=format -fdata-sections -ffunction-sections -fno-exceptions -fno-rtti -fno-threadsafe-statics")
set(LINKER_FLAGS "-Wl,--hash-style=both")

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    message("Builing Release...")

    set(C_FLAGS "${C_FLAGS} -O2 -fvisibility=hidden -fvisibility-inlines-hidden")
    set(LINKER_FLAGS "${LINKER_FLAGS} -Wl,-exclude-libs,ALL -Wl,--gc-sections")
elseif (CMAKE_BUILD_TYPE STREQUAL "Debug")
    message("Builing Debug...")

    add_definitions(-DDEBUG)
endif ()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_FLAGS}")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${LINKER_FLAGS}")
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} ${LINKER_FLAGS}")

find_library(log-lib log)
find_package(boringssl REQUIRED CONFIG)
find_package(cxx REQUIRED CONFIG)

add_executable(libshizuku.so
        starter.cpp misc.cpp selinux.cpp cgroup.cpp android.cpp)

target_link_libraries(libshizuku.so ${log-lib} cxx::cxx)

add_custom_command(TARGET libshizuku.so POST_BUILD
        COMMAND ${CMAKE_STRIP} --remove-section=.comment "libshizuku.so")

add_library(adb SHARED
        adb_pairing.cpp misc.cpp)

target_link_libraries(adb ${log-lib} boringssl::crypto_static cxx::cxx)

add_custom_command(TARGET adb POST_BUILD
        COMMAND ${CMAKE_STRIP} --remove-section=.comment "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libadb.so")

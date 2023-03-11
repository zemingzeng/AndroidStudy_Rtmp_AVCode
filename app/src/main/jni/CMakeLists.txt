cmake_minimum_required(VERSION 3.4.1)

########jepg压缩
#include_directories( ${CMAKE_SOURCE_DIR}/head/jpeg)
#
##引入turbojpeg库
#set(LIB_DIR ../../../../libs)
#add_library( jpeg
#        SHARED
#        IMPORTED )
#set_target_properties( jpeg
#        PROPERTIES IMPORTED_LOCATION
#        ${LIB_DIR}/${ANDROID_ABI}/libjpeg.so )
#
##bitmap操作库
#find_library( lib-jnigraphics
#        jnigraphics)
#
#find_library(lib-log log)
##message("zemingzeng")
#
#add_library(play SHARED  ${CMAKE_SOURCE_DIR}/cpp/jpeg/Play.cpp)
#
#target_link_libraries(
#        play
#        jpeg
#        ${lib-jnigraphics}
#        ${lib-log}
#        )


##################硬编码rtmp
#根据其他目录的cmakelists编译其他目录下的源文件
#include_directories(${CMAKE_SOURCE_DIR}/head/rtmp)
#add_subdirectory(./cpp/rtmp)
#find_library(lib-log log)
#add_library(rtmp_live SHARED  ${CMAKE_SOURCE_DIR}/cpp/rtmp/RtmpLive.cpp)
#target_link_libraries(
#        rtmp_live
#        ${lib-log}
#        rtmp
#)


###############################软编码x264
include_directories(${CMAKE_SOURCE_DIR}/head/rtmp)
include_directories(${CMAKE_SOURCE_DIR}/head/x264)
include_directories(${CMAKE_SOURCE_DIR}/head/x264/faac)

add_subdirectory(./cpp/rtmp)

find_library(lib-log log)

#静态库
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/x264/${ANDROID_ABI}/lib")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/faac/${ANDROID_ABI}/lib")

add_library(x264
        SHARED
        IMPORTED)
set_target_properties(x264
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libx264.so)

message(WARNING "zemingzeng")

add_library(faac
        SHARED
        IMPORTED)
set_target_properties(faac
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libfaac.so)

file(GLOB source_files ${CMAKE_SOURCE_DIR}/cpp/x264/*.cpp)

message(WARNING ${source_files})

add_library(x264_camera2 SHARED ${source_files})

target_link_libraries(
        x264_camera2
        x264
        faac
        rtmp
        ${lib-log}
)
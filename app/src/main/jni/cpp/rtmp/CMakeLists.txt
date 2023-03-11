#关闭ssl
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
#定义一个包含rtmp源码c文件的变量
file(GLOB rtmp_source *.c)
add_library(
        rtmp
        SHARED
        ${rtmp_source}
)
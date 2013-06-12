set(Boost_INCLUDE_DIRS /home/martin/src/crystal_explorer/jni/boost/include)
set(Boost_LIBRARIES libboost_date_time-gcc-mt-1_53.a libboost_program_options-gcc-mt-1_53.a libboost_system-gcc-mt-1_53.a libboost_filesystem-gcc-mt-1_53.a libboost_regex-gcc-mt-1_53.a libboost_thread-gcc-mt-1_53.a libboost_iostreams-gcc-mt-1_53.a libboost_signals-gcc-mt-1_53.a)
link_directories(/home/martin/src/crystal_explorer/jni/boost/lib)

#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} @Boost_C_FLAGS@")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} @Boost_CXX_FLAGS@")

# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The program to use to edit the cache.
CMAKE_EDIT_COMMAND = /usr/bin/ccmake

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/martin/src/crystal_explorer/jni/armadillo-3.900.2

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build

# Include any dependencies generated for this target.
include CMakeFiles/armadillo.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/armadillo.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/armadillo.dir/flags.make

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o: CMakeFiles/armadillo.dir/flags.make
CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o: ../src/wrap_libs.cpp
	$(CMAKE_COMMAND) -E cmake_progress_report /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building CXX object CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o"
	/home/martin/dev/android-toolchain/bin/arm-linux-androideabi-g++   $(CXX_DEFINES) $(CXX_FLAGS) -o CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o -c /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/src/wrap_libs.cpp

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/armadillo.dir/src/wrap_libs.cpp.i"
	/home/martin/dev/android-toolchain/bin/arm-linux-androideabi-g++  $(CXX_DEFINES) $(CXX_FLAGS) -E /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/src/wrap_libs.cpp > CMakeFiles/armadillo.dir/src/wrap_libs.cpp.i

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/armadillo.dir/src/wrap_libs.cpp.s"
	/home/martin/dev/android-toolchain/bin/arm-linux-androideabi-g++  $(CXX_DEFINES) $(CXX_FLAGS) -S /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/src/wrap_libs.cpp -o CMakeFiles/armadillo.dir/src/wrap_libs.cpp.s

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.requires:
.PHONY : CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.requires

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.provides: CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.requires
	$(MAKE) -f CMakeFiles/armadillo.dir/build.make CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.provides.build
.PHONY : CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.provides

CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.provides.build: CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o

# Object files for target armadillo
armadillo_OBJECTS = \
"CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o"

# External object files for target armadillo
armadillo_EXTERNAL_OBJECTS =

../libs/armeabi-v7a/libarmadillo.so.3.900.2: CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o
../libs/armeabi-v7a/libarmadillo.so.3.900.2: CMakeFiles/armadillo.dir/build.make
../libs/armeabi-v7a/libarmadillo.so.3.900.2: CMakeFiles/armadillo.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking CXX shared library ../libs/armeabi-v7a/libarmadillo.so"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/armadillo.dir/link.txt --verbose=$(VERBOSE)
	$(CMAKE_COMMAND) -E cmake_symlink_library ../libs/armeabi-v7a/libarmadillo.so.3.900.2 ../libs/armeabi-v7a/libarmadillo.so.3 ../libs/armeabi-v7a/libarmadillo.so

../libs/armeabi-v7a/libarmadillo.so.3: ../libs/armeabi-v7a/libarmadillo.so.3.900.2

../libs/armeabi-v7a/libarmadillo.so: ../libs/armeabi-v7a/libarmadillo.so.3.900.2

# Rule to build all files generated by this target.
CMakeFiles/armadillo.dir/build: ../libs/armeabi-v7a/libarmadillo.so
.PHONY : CMakeFiles/armadillo.dir/build

CMakeFiles/armadillo.dir/requires: CMakeFiles/armadillo.dir/src/wrap_libs.cpp.o.requires
.PHONY : CMakeFiles/armadillo.dir/requires

CMakeFiles/armadillo.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/armadillo.dir/cmake_clean.cmake
.PHONY : CMakeFiles/armadillo.dir/clean

CMakeFiles/armadillo.dir/depend:
	cd /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/martin/src/crystal_explorer/jni/armadillo-3.900.2 /home/martin/src/crystal_explorer/jni/armadillo-3.900.2 /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build /home/martin/src/crystal_explorer/jni/armadillo-3.900.2/build/CMakeFiles/armadillo.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/armadillo.dir/depend


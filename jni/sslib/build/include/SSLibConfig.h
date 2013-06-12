



// Version number from CMake
#define SSLIB_VERSION_MAJOR 0
#define SSLIB_VERSION_MINOR 3
#define SSLIB_VERSION_PATCH 0
#define SSLIB_VERSION 0.3.0

#define SSLIB_USE_YAML
/* #undef SSLIB_USE_CGAL */
#define SSLIB_USE_LAPACK

/* #undef SSLIB_DISABLE_CPP11 */

#if __cplusplus >= 201103L && !SSLIB_DISABLE_CPP11
#  define SSLIB_USE_CPP11
#endif

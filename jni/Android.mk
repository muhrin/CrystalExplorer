
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := boost_system
LOCAL_SRC_FILES := boost/lib/libboost_system-gcc-mt-1_53.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/boost/include
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := boost_filesystem
LOCAL_SRC_FILES := boost/lib/libboost_filesystem-gcc-mt-1_53.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/boost/include
LOCAL_STATIC_LIBRARIES := boost_system
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := boost_regex
LOCAL_SRC_FILES := boost/lib/libboost_regex-gcc-mt-1_53.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/boost/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := spglib
LOCAL_SRC_FILES := cslibs/libs/$(TARGET_ARCH_ABI)/libspglib.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := yaml-cpp
LOCAL_SRC_FILES := cslibs/libs/$(TARGET_ARCH_ABI)/libyaml-cpp.a
LOCAL_STATIC_LIBRARIES := boost_filesystem
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := sslib
LOCAL_SRC_FILES := sslib/libs/$(TARGET_ARCH_ABI)/libsslib.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/sslib/include $(LOCAL_PATH)/boost/include $(LOCAL_PATH)/armadillo-3.900.2/include
LOCAL_STATIC_LIBRARIES := boost_filesystem spglib yaml-cpp
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := sslib-jni
LOCAL_SRC_FILES := sslib-jni.cpp
LOCAL_STATIC_LIBRARIES := sslib
include $(BUILD_SHARED_LIBRARY)

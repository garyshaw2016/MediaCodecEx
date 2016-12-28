LOCAL_PATH:=$(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=rtmp_publisher.cpp

LOCAL_MODULE := test

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

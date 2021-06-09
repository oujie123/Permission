#
# Copyright (C) 2021 Galaxy Auto Technology
#
# All Rights Reserved by Galaxy Auto Technology Co., Ltd and its affiliates.
# You may not use, copy, distribute, modify, transmit in any form this file
# except in compliance with Galaxy Auto Technology in writing by applicable law.
#
# Edit History
#
# DATE            NAME     DESCRIPTION
# 2021-05-31     jieou     init
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, aidl)
LOCAL_AIDL_INCLUDES += $(LOCAL_PATH)/aidl

LOCAL_RESOURCE_DIRS := $(LOCAL_PATH)/res
LOCAL_MANIFEST_FILE := AndroidManifest.xml

LOCAL_STATIC_JAVA_LIBRARIES := \
    gson_2_8_5
#	android-support-v7-appcompat

LOCAL_STATIC_ANDROID_LIBRARIES := android-support-annotations

LOCAL_PACKAGE_NAME := GxaPermission
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

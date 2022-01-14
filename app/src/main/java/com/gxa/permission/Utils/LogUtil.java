/**
 * Copyright (C) 2021 Galaxy Auto Technology
 * <p>
 * All Rights Reserved by Galaxy Auto Technology Co., Ltd and its affiliates.
 * You may not use, copy, distribute, modify, transmit in any form this file
 * except in compliance with Galaxy Auto Technology in writing by applicable law.
 * <p>
 * Edit History
 * <p>
 * DATE            NAME     DESCRIPTION
 * 2021-05-31     jieou     init
 */
package com.gxa.permission.Utils;

import android.os.SystemProperties;
import android.util.Log;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class LogUtil {
    private static final String PERSIST_PERMISSION_DEBUGGABLE = "ro.debuggable";
    private static boolean sDebuggable = SystemProperties.getInt(PERSIST_PERMISSION_DEBUGGABLE, 0) == 1;

    LogUtil() {
        Log.d(Constants.TAG + "debug", "debug switcher status: " + sDebuggable);
    }

    public static void i(String info) {
        if (sDebuggable) {
            Log.i(Constants.TAG + "info", info);
        }
    }

    public static void w(String info) {
        if (sDebuggable) {
            Log.w(Constants.TAG + "warn", info);
        }
    }

    public static void d(String info) {
        if (sDebuggable) {
            Log.d(Constants.TAG + "debug", info);
        }
    }

    public static void e(String info, Exception e) {
        if (sDebuggable) {
            Log.e(Constants.TAG + "debug", info, e);
        }
    }
}

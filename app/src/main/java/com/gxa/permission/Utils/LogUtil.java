/**
 * Copyright (C) 2021 Galaxy Auto Technology
 *
 * All Rights Reserved by Galaxy Auto Technology Co., Ltd and its affiliates.
 * You may not use, copy, distribute, modify, transmit in any form this file
 * except in compliance with Galaxy Auto Technology in writing by applicable law.
 *
 * Edit History
 *
 * DATE            NAME     DESCRIPTION
 * 2021-05-31     jieou     init
 */
package com.gxa.permission.Utils;

import android.util.Log;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class LogUtil {
    private static final String PERSIST_PERMISSION_DEBUGGABLE = "persist.sys.permission.debuggable";
    //private static boolean sDebuggable = SystemProperties.getBoolean("persist.sys.permission.debuggable", false);
    private static boolean sDebuggable = true;

    public static void info(String info) {
        if (sDebuggable) {
            Log.i(Constants.TAG + "info", info);
        }
    }
}

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
package com.gxatek.permission;

import android.app.Application;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.gxatek.permission.Utils.Constants;

/**
 * @author Jack_Ou  created on 2021/3/2.
 */
public class CoreApplication extends Application {
    private static final String TAG = Constants.TAG + CoreApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CoreApplication start");
        startService(new Intent(this, CoreService.class));
    }

}

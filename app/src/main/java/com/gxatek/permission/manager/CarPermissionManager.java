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
package com.gxatek.permission.manager;

import android.content.Context;

import com.gxatek.permission.Utils.Constants;

/**
 * @author Jack_Ou  created on 2021/3/2.
 */
public abstract class CarPermissionManager {
    public static final int PERMISSION_DEFAULT = 4;
    public static final int PERMISSION_DENIED = 2;
    public static final int PERMISSION_GRANTED = 1;
    public static final int PERMISSION_INQUIRY = 3;
    protected static final String TAG = Constants.TAG + CarPermissionManager.class.getSimpleName();
    private static CarPermissionManagerImpl mPermissionManager;

    public static CarPermissionManager getPermissionManager(Context context) {
        if (mPermissionManager == null) {
            synchronized (CarPermissionManager.class) {
                if (mPermissionManager == null) {
                    mPermissionManager = new CarPermissionManagerImpl(context);
                }
            }
        }
        return mPermissionManager;
    }

    public abstract int checkPermission(String packageName, String permissionName);

    public abstract void handlePackagePostInstall(String packageName, boolean success);

    public abstract void installPackageByPi(String packageName, String installerPackageName);

    public abstract void installStageByPms(String packageName, String installerPackageName);
}


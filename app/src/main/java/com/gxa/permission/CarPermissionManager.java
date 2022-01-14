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
package com.gxa.permission;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * @author Jack_Ou  created on 2021/3/2.
 */
public abstract class CarPermissionManager {

    public static final int PERMISSION_GRANTED = 1;
    public static final int PERMISSION_DENIED = 2;
    public static final int PERMISSION_INQUIRY = 3;
    public static final int PERMISSION_DEFAULT = 4;

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

    /**
     * Check permission from the permission config file by packageName and permissionName.
     * @param packageName The name of the package you are checking against.
     * @param permissionName The name of the permission you are checking for.
     * @return the permission status in config.
     *
     * @see #PERMISSION_GRANTED
     * @see #PERMISSION_DENIED
     * @see #PERMISSION_INQUIRY
     * @see #PERMISSION_DEFAULT
     */
    public abstract int checkPermission(String packageName,String permissionName);

    /**
     * Install package from PackageInstaller.
     * @param packageName The name of the package you are installing.
     * @param installerPackageName The name of the installer package.
     */
    public abstract void installPackageByPi(String packageName, String installerPackageName);

    /**
     * Install package from PackageManagerService.
     * @param packageName The name of the package you are installing.
     * @param installerPackageName The name of the installer package.
     */
    public abstract void installStageByPms(String packageName, String installerPackageName);

    /**
     * Handle install result.
     * @param packageName install packageName.
     * @param success install success or failed.
     */
    public abstract void handlePackagePostInstall(String packageName,boolean success);

    /**
     * check signature permission from permission config.
     *
     * @param applicationInfo application info
     * @param permissionName request permission
     * @return CarPermissionManager.PERMISSION_GRANTED / PERMISSION_DENIED / PERMISSION_DEFAULT
     */
    public abstract int checkSignaturePermission(ApplicationInfo applicationInfo, String permissionName);
}


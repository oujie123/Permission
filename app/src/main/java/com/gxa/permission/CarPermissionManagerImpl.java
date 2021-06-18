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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.gxa.permission.ICarPermissionManager;


/**
 * Realize CarPermissionManager.
 *
 * @author Jack_Ou  created on 2021/3/2.
 */
public class CarPermissionManagerImpl extends CarPermissionManager {

    private static final String SERVICE_PACKAGENAME = "com.gxa.permission";
    private static final String SERVICE_NAME = "com.gxa.permission.CoreService";
    private ICarPermissionManager mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (ICarPermissionManager) ICarPermissionManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    protected CarPermissionManagerImpl(Context paramContext) {
        initService(paramContext);
    }

    private void initService(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(SERVICE_PACKAGENAME, SERVICE_NAME));
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int checkPermission(String packageName, String permissionName) {
        if (mService != null) {
            try {
                return mService.checkPermission(packageName, permissionName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return PERMISSION_DEFAULT;
    }

    @Override
    public void handlePackagePostInstall(String packageName, boolean success) {
        if (mService != null) {
            try {
                mService.handlePackagePostInstall(packageName, success);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int checkSignaturePermission(ApplicationInfo applicationInfo, String permissionName) {
        if (mService != null) {
            try {
                return mService.checkSignaturePermission(applicationInfo, permissionName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return CarPermissionManager.PERMISSION_DEFAULT;
    }

    @Override
    public void installPackageByPi(String packageName, String installerPackageName) {
        if (mService != null) {
            try {
                mService.installPackageByPi(packageName, installerPackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void installStageByPms(String packageName, String installerPackageName) {
        if (mService != null) {
            try {
                mService.installStageByPms(packageName, installerPackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

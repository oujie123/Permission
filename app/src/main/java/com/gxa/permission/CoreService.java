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
package com.gxa.permission;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;

import com.gxa.permission.Utils.Constants;
import com.gxa.permission.server.CarPermissionManagerService;

import java.util.Iterator;
import java.util.List;

public class CoreService extends Service {
    private static final int INIT_PERMISSIONS = 1;
    private static final String TAG = Constants.TAG + CoreService.class.getSimpleName();
    private static final String THREAD_NAME = "carpermission_thread";
    private static final int FIRST_APPLICATION_UID = 10000;
    private CarPermissionManagerService mCpms;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private void initGrantPermissions() {
        PackageManager pm = getPackageManager();
        ApplicationInfo appInfo = getApplicationInfo();
        List packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        if ((packages != null) && (packages.size() > 0)) {
            Iterator iterator = packages.iterator();
            while (iterator.hasNext()) {
                PackageInfo packageInfo = (PackageInfo) iterator.next();
                int appId = UserHandle.getAppId(packageInfo.applicationInfo.uid);
                // uid小于10000为系统应用uid
                if ((pm.checkSignatures(packageInfo.applicationInfo.uid, appInfo.uid) == PackageManager.SIGNATURE_MATCH) && (appId < FIRST_APPLICATION_UID)) {
                    iterator.remove();
                }
            }
            this.mCpms.grantPermissions(packages);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mCpms;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CoreService onCreate");
        this.mCpms = new CarPermissionManagerService(this);
        this.mHandlerThread = new HandlerThread(THREAD_NAME);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if ((msg != null) && (msg.what == INIT_PERMISSIONS)) {
                    initGrantPermissions();
                }
            }
        };
        this.mHandler.sendEmptyMessage(INIT_PERMISSIONS);
    }
}

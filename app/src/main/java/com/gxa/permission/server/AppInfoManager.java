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
package com.gxa.permission.server;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;


import com.gxa.permission.Utils.CarGson;
import com.gxa.permission.Utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class AppInfoManager {
    private static final String TAG = Constants.TAG + "_AppInfoManager";

    private static final String TS_INSTALL_PACKAGES = "/data/system/gxa_install_packages.json";

    private static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";

    private static final String PRESET_DIR_VENDOR_APP = "/vendor/app/";
    private static final String PRESET_DIR_SYSTEM_APP = "/system/app/";
    private static final String PRESET_DIR_SYSTEM_PRIVAPP = "/system/priv-app/";

    private static AppInfoManager mInstance;
    private PackageManager mPm;
    private List<String> mPresetShops;
    private Map<String, String> mInstallPackageMap;

    private Map<String,String> mTempInstallMap;
    private Map<String,String> mOldInstallMap;

    private List<String> mPresetDirs;

    /**
     * singleton.
     */
    public static AppInfoManager getInstance(PackageManager pm) {
        if (mInstance == null) {
            synchronized (AppInfoManager.class) {
                if (mInstance == null) {
                    mInstance = new AppInfoManager(pm);
                }
            }
        }
        return mInstance;
    }

    private AppInfoManager(PackageManager pm) {
        mPm = pm;
        mInstallPackageMap = new HashMap<>();
        mTempInstallMap = new HashMap<>();
        mOldInstallMap = new HashMap<>();
        initPresetDirs();
        readInstallPackages();
    }

    private void initPresetDirs() {
        mPresetDirs = new ArrayList<>();
        mPresetDirs.add(PRESET_DIR_VENDOR_APP);
        mPresetDirs.add(PRESET_DIR_SYSTEM_APP);
        mPresetDirs.add(PRESET_DIR_SYSTEM_PRIVAPP);
    }

    /**
     * set dirs of preset apps.
     * @param presetDirs the dirs of preset apps
     */
    public void setPresetDirs(List<String> presetDirs) {
        if (presetDirs != null) {
            this.mPresetDirs = presetDirs;
        }
    }

    /**
     * set dirs of preset shop apps.
     * @param presetShops the dirs of preset shop apps
     */
    public void setPresetShops(List<String> presetShops) {
        this.mPresetShops = presetShops;
    }

    /**
     * check if it is signature verification passed app.
     */
    public boolean isSignatureApp(String packageName) {
        // TODO should signature check function.
        return false;
    }

    /**
     * check if it is preset app with packageName.
     */
    public boolean isPresetApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.w(TAG,"packageName cannot be null when check preset app");
            return false;
        }
        if (mPm == null) {
            Log.w(TAG,"PackageManager is null");
            return false;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = mPm.getApplicationInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException exception) {
            Log.e(TAG,"cannot file the app with the packageName : " + packageName,exception);
        }
        if (appInfo != null) {
            if (TextUtils.isEmpty(appInfo.sourceDir)) {
                Log.w(TAG,"cannot found the apk path with the packageName : " + packageName);
            } else {
                return checkPresetApp(appInfo.sourceDir);
            }
        }
        return false;
    }

    /**
     * check if it is preset app with ApplicationInfo.
     */
    public boolean isPresetApp(ApplicationInfo appInfo) {
        if (appInfo != null) {
            if (TextUtils.isEmpty(appInfo.sourceDir)) {
                Log.w(TAG,"cannot found the apk path with the packageName : " + appInfo);
            } else {
                return checkPresetApp(appInfo.sourceDir);
            }
        }
        return false;
    }

    /**
     * check if it is preset app with packageName.
     */
    public boolean isShopApp(String packageName) {
        if (mPresetShops == null || mPresetShops.size() == 0 || TextUtils.isEmpty(packageName)) {
            return false;
        }
        boolean checkInstaller = checkInstallPackage(packageName);
        if (checkInstaller) {
            return true;
        }
        readInstallPackages();
        String installPackageName = mInstallPackageMap.get(packageName);
        if (installPackageName != null) {
            return isPackageInPresetShops(installPackageName);
        }
        return false;
    }

    private boolean checkInstallPackage(String packageName) {
        if (mPm == null) {
            Log.w(TAG,"PackageManager is null");
            return false;
        }
        String installerPackageName = mPm.getInstallerPackageName(packageName);
        return isPackageInPresetShops(installerPackageName);
    }

    /**
     * App install event from PackageInstaller.
     */
    public void installPackageByPi(String packageName, String installerPackageName) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(installerPackageName)) {
            return;
        }
        mTempInstallMap.put(packageName,installerPackageName);
    }

    /**
     * App install event from PackageManagerService.
     */
    public void installStageByPms(String packageName, String installerPackageName) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(installerPackageName)) {
            return;
        }
        if (TextUtils.equals(installerPackageName, PACKAGE_INSTALLER_NAME)) {
            String tempInstallName = mTempInstallMap.get(packageName);
            if (tempInstallName != null) {
                refreshInstallMap(packageName, tempInstallName);
                writeInstallPackagesIntoJsonFile();
            }
            mTempInstallMap.remove(packageName);
        } else {
            refreshInstallMap(packageName, installerPackageName);
            writeInstallPackagesIntoJsonFile();
        }
    }

    /**
     * mInstallPackageMap 应用商城的应用
     * mOldInstallMap 安装过的所有应用
     * <p>
     * {"bubei.tingshu.hd":"cn.gaei.appstore"}
     *
     * @param packageName
     * @param tempInstallName
     */
    private void refreshInstallMap(String packageName, String tempInstallName) {
        if (isPackageInPresetShops(tempInstallName)) {
            if (mInstallPackageMap.containsKey(packageName)) {
                mOldInstallMap.put(packageName, mInstallPackageMap.get(packageName));
            }
            mInstallPackageMap.put(packageName, tempInstallName);
        } else {
            if (mInstallPackageMap.containsKey(packageName)) {
                mOldInstallMap.put(packageName, mInstallPackageMap.get(packageName));
            }
            mInstallPackageMap.remove(packageName);
        }
    }

    /**
     * handle install result.
     * @param packageName install packageName
     * @param success install success or failed
     */
    public void handlePackagePostInstall(String packageName, boolean success) {
        if (packageName == null) {
            return;
        }
        if (!success) {
            if (mOldInstallMap.containsKey(packageName)) {
                mInstallPackageMap.put(packageName,mOldInstallMap.remove(packageName));
                writeInstallPackagesIntoJsonFile();
            }
        } else {
            mOldInstallMap.remove(packageName);
        }
    }

    private void writeInstallPackagesIntoJsonFile() {
        CarGson carGson = new CarGson();
        boolean writeFlag = carGson.writeObjectToJsonFile(TS_INSTALL_PACKAGES, mInstallPackageMap);
        if (writeFlag) {
            Log.i(TAG,"write install packages success");
        } else {
            Log.i(TAG,"write install packages failed");
        }
    }

    private boolean isPackageInPresetShops(String packageName) {
        if (packageName != null) {
            if (mPresetShops != null && mPresetShops.size() > 0) {
                return mPresetShops.contains(packageName);
            }
        }
        return false;
    }

    private void readInstallPackages() {
        mInstallPackageMap.clear();
        CarGson carGson = new CarGson();
        Map<String, String> map = carGson.getObjectFromJsonFile(
                TS_INSTALL_PACKAGES,mInstallPackageMap.getClass());
        if (map != null) {
            mInstallPackageMap.putAll(map);
        }
    }

    private boolean checkPresetApp(String apkPath) {
        if (apkPath == null) {
            return false;
        }
        for (String presetDir : mPresetDirs) {
            if (apkPath.contains(presetDir)) {
                return true;
            }
        }
        return false;
    }
}

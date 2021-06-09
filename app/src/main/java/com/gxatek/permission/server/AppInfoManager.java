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
package com.gxatek.permission.server;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.gxatek.permission.Utils.CarGson;
import com.gxatek.permission.Utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class AppInfoManager {
    private static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";
    private static final String PRESET_DIR_SYSTEM_APP = "/system/app/";
    private static final String PRESET_DIR_SYSTEM_PRIVAPP = "/system/priv-app/";
    private static final String PRESET_DIR_VENDOR_APP = "/vendor/app/";
    private static final String TAG = Constants.TAG + AppInfoManager.class.getSimpleName();
    private static final String TS_INSTALL_PACKAGES = "/data/system/ts_install_packages.json";
    private static AppInfoManager mInstance;
    private Map<String, String> mInstallPackageMap;
    private Map<String, String> mOldInstallMap;
    private PackageManager mPm;
    private List<String> mPresetDirs;
    private List<String> mPresetShops;
    private Map<String, String> mTempInstallMap;

    private AppInfoManager(PackageManager paramPackageManager) {
        this.mPm = paramPackageManager;
        this.mInstallPackageMap = new HashMap();
        this.mTempInstallMap = new HashMap();
        this.mOldInstallMap = new HashMap();
        initPresetDirs();
        readInstallPackages();
    }

    private boolean checkInstallPackage(String packageName) {
        if (this.mPm == null) {
            Log.w(TAG, "PackageManager is null");
            return false;
        }
        return isPackageInPresetShops(this.mPm.getInstallerPackageName(packageName));
    }

    private boolean checkPresetApp(String appPath) {
        if (appPath == null) {
            return false;
        }
        Iterator iterator = this.mPresetDirs.iterator();
        while (iterator.hasNext()) {
            if (appPath.contains((String) iterator.next())) {
                return true;
            }
        }
        return false;
    }

    public static AppInfoManager getInstance(PackageManager paramPackageManager) {
        if (mInstance == null) {
            synchronized (AppInfoManager.class) {
                if (mInstance == null) {
                    mInstance = new AppInfoManager(paramPackageManager);
                }
            }
        }
        return mInstance;
    }

    private void initPresetDirs() {
        this.mPresetDirs = new ArrayList<>();
        this.mPresetDirs.add(PRESET_DIR_VENDOR_APP);
        this.mPresetDirs.add(PRESET_DIR_SYSTEM_APP);
        this.mPresetDirs.add(PRESET_DIR_SYSTEM_PRIVAPP);
    }

    private boolean isPackageInPresetShops(String installerPackageName) {
        if ((installerPackageName != null) && (this.mPresetShops != null) && (this.mPresetShops.size() > 0)) {
            return this.mPresetShops.contains(installerPackageName);
        }
        return false;
    }

    private void readInstallPackages() {
        this.mInstallPackageMap.clear();
        Map localMap = (Map) new CarGson().getObjectFromJsonFile(TS_INSTALL_PACKAGES, this.mInstallPackageMap.getClass());
        if (localMap != null) {
            this.mInstallPackageMap.putAll(localMap);
        }
    }

    /**
     * mInstallPackageMap 应用商城的应用
     * mOldInstallMap 安装过的所有应用
     * <p>
     * {"bubei.tingshu.hd":"cn.gaei.appstore"}
     *
     * @param packageName
     * @param installerPackageName
     */
    private void refreshInstallMap(String packageName, String installerPackageName) {
        if (isPackageInPresetShops(installerPackageName)) {
            if (this.mInstallPackageMap.containsKey(packageName)) {
                this.mOldInstallMap.put(packageName, this.mInstallPackageMap.get(packageName));
            }
            this.mInstallPackageMap.put(packageName, installerPackageName);
        } else {
            if (this.mInstallPackageMap.containsKey(packageName)) {
                this.mOldInstallMap.put(packageName, this.mInstallPackageMap.get(packageName));
            }
            this.mInstallPackageMap.remove(packageName);
        }
    }

    private void writeInstallPackagesIntoJsonFile() {
        if (new CarGson().writeObjectToJsonFile(TS_INSTALL_PACKAGES, this.mInstallPackageMap)) {
            Log.i(TAG, "write install packages success");
        } else {
            Log.i(TAG, "write install packages failed");
        }
    }

    public void handlePackagePostInstall(String packageName, boolean success) {
        if (packageName == null) {
            return;
        }
        if (!success) {
            if (this.mOldInstallMap.containsKey(packageName)) {
                this.mInstallPackageMap.put(packageName, this.mOldInstallMap.remove(packageName));
                writeInstallPackagesIntoJsonFile();
            }
        } else {
            this.mOldInstallMap.remove(packageName);
        }
    }

    public void installPackageByPi(String packageName, String installerPackageName) {
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(installerPackageName)) {
            this.mTempInstallMap.put(packageName, installerPackageName);
        }
    }

    public void installStageByPms(String packageName, String installerPackageName) {
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(installerPackageName)) {
            if (TextUtils.equals(installerPackageName, PACKAGE_INSTALLER_NAME)) {
                String installer = this.mTempInstallMap.get(packageName);
                if (installer != null) {
                    refreshInstallMap(packageName, installer);
                    writeInstallPackagesIntoJsonFile();
                }
                this.mTempInstallMap.remove(packageName);
            } else {
                refreshInstallMap(packageName, installerPackageName);
                writeInstallPackagesIntoJsonFile();
            }
        }
    }

    public boolean isPresetApp(ApplicationInfo appInfo) {
        if (appInfo != null) {
            if (TextUtils.isEmpty(appInfo.sourceDir)) {
                Log.w(TAG, "cannot found the apk path with the packageName : " + appInfo);
            } else {
                return checkPresetApp(appInfo.sourceDir);
            }
        }
        return false;
    }

    public boolean isPresetApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.w(TAG, "packageName cannot be null when check preset app");
            return false;
        }
        if (this.mPm == null) {
            Log.w(TAG, "PackageManager is null");
            return false;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mPm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "cannot find the app with the packageName : " + packageName, e);
        }
        if (appInfo != null) {
            if (TextUtils.isEmpty(appInfo.sourceDir)) {
                Log.w(TAG, "cannot found the apk path with the packageName : " + packageName);
                return false;
            }
            return checkPresetApp(appInfo.sourceDir);
        }
        return false;
    }

    public boolean isShopApp(String packageName) {
        if ((this.mPresetShops != null) && (this.mPresetShops.size() != 0)) {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (checkInstallPackage(packageName)) {
                return true;
            }
            readInstallPackages();
            String installerPackageName = this.mInstallPackageMap.get(packageName);
            if (installerPackageName != null) {
                return isPackageInPresetShops(installerPackageName);
            }
            return false;
        }
        return false;
    }

    public boolean isSignatureApp(String paramString) {
        return false;
    }

    public void setPresetDirs(List<String> presetDirs) {
        if (presetDirs != null) {
            this.mPresetDirs = presetDirs;
        }
    }

    public void setPresetShops(List<String> presetShops) {
        this.mPresetShops = presetShops;
    }
}

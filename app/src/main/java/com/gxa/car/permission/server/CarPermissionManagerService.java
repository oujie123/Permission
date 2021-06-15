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
package com.gxa.car.permission.server;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.gxa.car.permission.Utils.Constants;
import com.gxa.car.permission.Utils.LogUtil;
import com.gxa.car.permission.bean.App;
import com.gxa.car.permission.bean.Permission;
import com.gxa.car.permission.bean.PermissionConfig;
import com.gxa.car.permission.bean.Rule;

import java.util.List;

import gxa.car.permission.CarPermissionManager;
import gxa.car.permission.ICarPermissionManager;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class CarPermissionManagerService extends ICarPermissionManager.Stub {

    private static final String PLATFORM_PACKAGE_NAME = "android";
    protected static final String TAG = Constants.TAG + CarPermissionManagerService.class.getSimpleName();
    private AppInfoManager mAppInfoManager;
    private AppOpsManager mAppOps;
    private PackageManager mPackageManager;
    private PermissionConfig mPermissionConfig;

    public CarPermissionManagerService(Context context) {
        mAppOps = ((AppOpsManager) context.getSystemService(AppOpsManager.class));
        mPackageManager = context.getPackageManager();
        ConfigManager configManager = ConfigManager.getInstance();
        mAppInfoManager = AppInfoManager.getInstance(mPackageManager);
        mPermissionConfig = configManager.readPermissionConfig();
        if (mPermissionConfig != null) {
            mPermissionConfig.checkConfig();
            initAppInfoManagerDatas();
        }
    }

    private int checkPermissionFromApp(App app, String permissionName) {
        switch (app.getAuthorizationType()) {
            case Rule.PART_GRANTED:
                List<Permission> permissions = app.getPermissions();
                if (permissions != null && permissions.size() > 0) {
                    for (Permission permission : permissions) {
                        if (TextUtils.equals(permissionName, permission.getName())) {
                            return permission.getStatus();
                        }
                    }
                }
                if (app.getDefaultState() == Rule.DEFAULT_STATE_DENIED) {
                    return CarPermissionManager.PERMISSION_DENIED;
                } else if (app.getDefaultState() == Rule.DEFAULT_STATE_GRANTED) {
                    return CarPermissionManager.PERMISSION_GRANTED;
                }
                break;
            case Rule.ALL_GRANTED:
                return CarPermissionManager.PERMISSION_GRANTED;
            case Rule.ALL_DENIED:
                return CarPermissionManager.PERMISSION_DENIED;
            default:
                break;
        }
        return CarPermissionManager.PERMISSION_DEFAULT;
    }

    private int checkPermissionFromPms(String packageName, String permission) {
        return mPackageManager.checkPermission(permission, packageName);
    }

    private int checkPermissionFromRule(Rule rule, String permissionName) {
        switch (rule.getAuthorizationType()) {
            case Rule.PART_GRANTED:
                List<Permission> permissions = rule.getPermissions();
                if (permissions != null && permissions.size() > 0) {
                    for (Permission permission : permissions) {
                        if (TextUtils.equals(permissionName, permission.getName())) {
                            return permission.getStatus();
                        }
                    }
                }
                if (rule.getDefaultState() == Rule.DEFAULT_STATE_DENIED) {
                    return CarPermissionManager.PERMISSION_DENIED;
                } else if (rule.getDefaultState() == Rule.DEFAULT_STATE_GRANTED) {
                    return CarPermissionManager.PERMISSION_GRANTED;
                }
                break;
            case Rule.ALL_GRANTED:
                return CarPermissionManager.PERMISSION_GRANTED;
            case Rule.ALL_DENIED:
                return CarPermissionManager.PERMISSION_DENIED;
            default:
                break;
        }
        return CarPermissionManager.PERMISSION_DEFAULT;
    }

    private boolean checkRule(Rule rule, ApplicationInfo applicationInfo) {
        switch (rule.getMajorType()) {
            case Rule.MAJORTYPE_WHITE:
                App whiteApp = getWhiteApp(rule, applicationInfo.packageName);
                if (whiteApp != null) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SIGNATURE:
                if (mAppInfoManager.isSignatureApp(applicationInfo.packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_PRESET:
                if (mAppInfoManager.isPresetApp(applicationInfo)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SHOP:
                if (mAppInfoManager.isShopApp(applicationInfo.packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_OTHER:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 关联应用与规则，并且检查某包是否满足某规则，默认关联Rule.MAJORTYPE_OTHER
     * @param rule rule label in config file
     * @param packageName current
     * @return
     */
    private boolean checkRule(Rule rule, String packageName) {
        switch (rule.getMajorType()) {
            case Rule.MAJORTYPE_WHITE:
                App whiteApp = getWhiteApp(rule, packageName);
                if (whiteApp != null) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SIGNATURE:
                if (mAppInfoManager.isSignatureApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_PRESET:
                if (mAppInfoManager.isPresetApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SHOP:
                if (mAppInfoManager.isShopApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_OTHER:
                return true;
            default:
                break;
        }
        return false;
    }

    private Rule getRuleByPackageName(String packageName) {
        if (mPermissionConfig != null) {
            List<Rule> rules = mPermissionConfig.getRules();
            if (rules != null && rules.size() > 0) {
                for (Rule rule : rules) {
                    if (checkRule(rule, packageName)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    private Rule getRuleByPackageName(ApplicationInfo applicationInfo) {
        if (mPermissionConfig != null) {
            List<Rule> rules = mPermissionConfig.getRules();
            if (rules != null && rules.size() > 0) {
                for (Rule rule : rules) {
                    if (checkRule(rule, applicationInfo)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    private App getWhiteApp(Rule rule, String packageName) {
        List<App> apps = rule.getApps();
        if (apps != null && apps.size() > 0) {
            for (App app : apps) {
                List<String> packageNames = app.getPackageNames();
                if (packageNames != null) {
                    if (packageNames.contains(packageName)) {
                        return app;
                    }
                }
            }
        }
        return null;
    }

    private void grantOrRevokePermissionByType(PackageInfo packageInfo, int permissionType, List<Permission> permissionList, PermissionInfo permissionInfo) {
        switch (permissionType) {
            case Rule.ALL_DENIED:
                revokeRuntimePermissionByPms(packageInfo.packageName, permissionInfo.name);
                break;
            case Rule.PART_GRANTED:
                if (permissionList != null && permissionList.size() > 0) {
                    for (Permission permission : permissionList) {
                        if (TextUtils.equals(permissionInfo.name, permission.getName())) {
                            if (permission.getStatus() == CarPermissionManager.PERMISSION_GRANTED) {
                                grantRuntimePermission(packageInfo, permissionInfo);
                            } else if (permission.getStatus() == CarPermissionManager.PERMISSION_DENIED) {
                                revokeRuntimePermissionByPms(packageInfo.packageName, permissionInfo.name);
                            }
                            break;
                        }
                        // 对于其他应用没有禁止的权限，如果需要授权，则解开如下注释
                        //else {
                        //    grantRuntimePermission(packageInfo, permissionInfo);
                        //}
                    }
                }
                break;
            case Rule.ALL_GRANTED:
                grantRuntimePermission(packageInfo, permissionInfo);
                break;
            default:
        }
    }

    /**
     * 根据配置授予/撤销权限
     *
     * @param packageInfo  应用信息
     * @param authorizationType 全部授权/部分授权/撤销权限
     * @param permissions 应用申请的权限
     * @param permissions 规则中限制的权限
     */
    private void grantOrRevokePermissionsByConfig(PackageInfo packageInfo, int authorizationType,
                                                  String[] permissionInfos, List<Permission> permissions) {
        if (permissionInfos == null || permissionInfos.length == 0) {
            Log.w(TAG, "package " + packageInfo.packageName + "has no permissions");
            return;
        }
        for (String permissionName : permissionInfos) {
            PermissionInfo permissionInfo;
            try {
                permissionInfo = mPackageManager.getPermissionInfo(permissionName, 0);
            } catch (PackageManager.NameNotFoundException excepiton) {
                continue;
            }
            if (permissionInfo == null
                    || (permissionInfo.protectionLevel & PermissionInfo.PROTECTION_DANGEROUS) == 0) {
                continue;
            }
            int flags = mPackageManager.getPermissionFlags(permissionInfo.name,
                    packageInfo.packageName, Process.myUserHandle());
            if (((flags & PackageManager.FLAG_PERMISSION_SYSTEM_FIXED) != 0)
                    || ((flags & PackageManager.FLAG_PERMISSION_POLICY_FIXED) != 0)) {
                continue;
            }
            grantOrRevokePermissionByType(packageInfo, authorizationType, permissions, permissionInfo);
        }
    }

    private void grantPackagePermissions(PackageInfo packageInfo, String[] permissionInfos) {
        if (packageInfo == null || permissionInfos == null || permissionInfos.length == 0) {
            return;
        }

        if (packageInfo.applicationInfo.isInstantApp()) {
            return;
        }

        Rule rule = getRuleByPackageName(packageInfo.packageName);
        LogUtil.info("packageName : " + packageInfo.packageName);
        if (rule != null) {
            LogUtil.info("majorType : " + rule.getMajorType());
            if (rule.getMajorType() == Rule.MAJORTYPE_WHITE) {
                App whiteApp = getWhiteApp(rule, packageInfo.packageName);
                if (whiteApp != null) {
                    grantOrRevokePermissionsByConfig(packageInfo, whiteApp.getAuthorizationType(),
                            permissionInfos, whiteApp.getPermissions());
                }
            } else {
                grantOrRevokePermissionsByConfig(packageInfo, rule.getAuthorizationType(),
                        permissionInfos, rule.getPermissions());
            }
        }
    }

    private void grantRuntimePermission(PackageInfo packageInfo, PermissionInfo permissionInfo) {
        if (packageInfo == null || permissionInfo == null) {
            return;
        }
        final String appOp = PLATFORM_PACKAGE_NAME.equals(permissionInfo.packageName)
                ? AppOpsManager.permissionToOp(permissionInfo.name) : null;
        grantRuntimePermissionByAppOp(packageInfo, appOp);
        grantRuntimePermissionByPms(packageInfo.packageName, permissionInfo.name);
    }

    private void grantRuntimePermissionByAppOp(PackageInfo packageInfo, String appOp) {
        if (appOp == null) {
            return;
        }
        final boolean appOpAllowed = mAppOps.checkOpNoThrow(appOp,
                packageInfo.applicationInfo.uid, packageInfo.packageName)
                == AppOpsManager.MODE_ALLOWED;
        if (!appOpAllowed) {
            LogUtil.info("grantRuntimePermissionByPms -- packageName : "
                    + packageInfo.packageName
                    + " | appOp : "
                    + appOp);
            mAppOps.setUidMode(appOp, packageInfo.applicationInfo.uid, AppOpsManager.MODE_ALLOWED);
        }
    }

    private void grantRuntimePermissionByPms(String packageName, String permission) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(permission)) {
            return;
        }
        int permissionResult = checkPermissionFromPms(packageName, permission);
        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            LogUtil.info("package name ("
                    + packageName
                    + ") runtime permission ("
                    + permission + ") is GRANTED");
            mPackageManager.grantRuntimePermission(packageName, permission, Process.myUserHandle());
        }
    }

    private void initAppInfoManagerDatas() {
        if (mPermissionConfig.getPresetPaths() != null) {
            mAppInfoManager.setPresetDirs(mPermissionConfig.getPresetPaths());
        }
        mAppInfoManager.setPresetShops(mPermissionConfig.getPresetShops());
    }

    private void revokeRuntimePermissionByPms(String packageName, String permission) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(permission)) {
            return;
        }
        int permissionResult = checkPermissionFromPms(packageName, permission);
        if (permissionResult != PackageManager.PERMISSION_DENIED) {
            mPackageManager.revokeRuntimePermission(packageName, permission, Process.myUserHandle());
        }
    }

    // frameworks/base/services/core/java/com/android/server/pm/CarPackageManagerService.java
    /**
     * check signature permission from permission config.
     */
    public int checkSignaturePermission(ApplicationInfo applicationInfo, String permissionName) {
        if (applicationInfo == null || TextUtils.isEmpty(permissionName)) {
            return CarPermissionManager.PERMISSION_DEFAULT;
        }
        LogUtil.info("checkSignaturePermission -- packageName : "
                + applicationInfo.packageName
                + " | permissionName : "
                + permissionName);
        Rule rule = getRuleByPackageName(applicationInfo);
        if (rule != null) {
            LogUtil.info("checkSignaturePermission -- rule majorType : " + rule.getMajorType());
            if (rule.getMajorType() == Rule.MAJORTYPE_WHITE) {
                App whiteApp = getWhiteApp(rule, applicationInfo.packageName);
                if (whiteApp != null) {
                    return checkPermissionFromApp(whiteApp, permissionName);
                }
            } else {
                return checkPermissionFromRule(rule, permissionName);
            }
        }
        return CarPermissionManager.PERMISSION_DEFAULT;
    }

    /**
     * grant applications permissions with permission config.
     */
    public void grantPermissions(List<PackageInfo> packageInfos) {
        LogUtil.info("packageInfos : " + packageInfos);
        if (packageInfos == null || packageInfos.size() == 0) {
            return;
        }
        for (PackageInfo packageInfo : packageInfos) {
            grantPackagePermissions(packageInfo, packageInfo.requestedPermissions);
        }
    }

    @Override
    public int checkPermission(String packageName, String permissionName) throws RemoteException {
        LogUtil.info("checkPermission -- packageName : " + packageName + " | permissionName : " + permissionName);
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(permissionName)) {
            Rule rule = getRuleByPackageName(packageName);
            if (rule != null) {
                LogUtil.info("checkPermission -- rule majorType : " + rule.getMajorType());
                if (rule.getMajorType() == Rule.MAJORTYPE_WHITE) {
                    App whiteApp = getWhiteApp(rule, packageName);
                    if (whiteApp != null) {
                        return checkPermissionFromApp(whiteApp, permissionName);
                    }
                    return CarPermissionManager.PERMISSION_DEFAULT;
                } else {
                    return checkPermissionFromRule(rule, permissionName);
                }
            } else {
                return CarPermissionManager.PERMISSION_DEFAULT;
            }
        } else {
            Log.w(TAG, "checkPermission -- packageName or permissionName is null!");
            return CarPermissionManager.PERMISSION_DEFAULT;
        }
    }

    @Override
    public void handlePackagePostInstall(String packageName, boolean success) throws RemoteException {
        LogUtil.info("handlePackagePostInstall -- packageName : "
                + packageName
                + " | success : "
                + success);
        mAppInfoManager.handlePackagePostInstall(packageName, success);
    }

    @Override
    public void installPackageByPi(String packageName, String installerPackageName) throws RemoteException {
        LogUtil.info("installPackageByPi -- packageName : "
                + packageName
                + " | installerPackageName : "
                + installerPackageName);
        mAppInfoManager.installPackageByPi(packageName, installerPackageName);
    }

    @Override
    public void installStageByPms(String packageName, String installerPackageName) throws RemoteException {
        LogUtil.info("installStageByPms -- packageName : "
                + packageName
                + " | installerPackageName : "
                + installerPackageName);
        mAppInfoManager.installStageByPms(packageName, installerPackageName);
    }
}

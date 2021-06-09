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
package com.gxatek.permission.server;

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

import com.gxatek.permission.Utils.Constants;
import com.gxatek.permission.Utils.LogUtil;
import com.gxatek.permission.bean.App;
import com.gxatek.permission.bean.Permission;
import com.gxatek.permission.bean.PermissionConfig;
import com.gxatek.permission.bean.Rule;
import com.gxatek.permission.manager.ICarPermissionManager;

import java.util.Iterator;
import java.util.List;

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
        this.mAppOps = ((AppOpsManager) context.getSystemService(AppOpsManager.class));
        this.mPackageManager = context.getPackageManager();
        ConfigManager configManager = ConfigManager.getInstance();
        this.mAppInfoManager = AppInfoManager.getInstance(this.mPackageManager);
        this.mPermissionConfig = configManager.readPermissionConfig();
        if (this.mPermissionConfig != null) {
            this.mPermissionConfig.checkConfig();
            initAppInfoManagerDatas();
        }
    }

    private int checkPermissionFromApp(App app, String paramPermission) {
        switch (app.getAuthorizationType()) {
            case Rule.ALL_DENIED:
                return Rule.DEFAULT_STATE_DENIED;
            case Rule.PART_GRANTED:
                List<Permission> permissionList = app.getPermissions();
                if ((permissionList != null) && (permissionList.size() > 0)) {
                    Iterator iterator = permissionList.iterator();
                    while (iterator.hasNext()) {
                        Permission permission = (Permission) iterator.next();
                        if (TextUtils.equals(paramPermission, permission.getName())) {
                            return permission.getStatus();
                        }
                    }
                }
                if (app.getDefaultState() == Rule.DEFAULT_STATE_DENIED) {
                    return Rule.DEFAULT_STATE_DENIED;
                }
                if (app.getDefaultState() == Rule.DEFAULT_STATE_GRANTED) {
                    return Rule.DEFAULT_STATE_GRANTED;
                }
                break;
            case Rule.ALL_GRANTED:
                return Rule.ALL_GRANTED;
            default:
                return Rule.ERROR;
        }
        return Rule.ERROR;
    }

    private int checkPermissionFromPms(String packageName, String permName) {
        return this.mPackageManager.checkPermission(permName, packageName);
    }

    private int checkPermissionFromRule(Rule paramRule, String paramPermission) {
        switch (paramRule.getAuthorizationType()) {
            case Rule.ALL_DENIED:
                return Rule.DEFAULT_STATE_DENIED;
            case Rule.PART_GRANTED:
                List<Permission> permissions = paramRule.getPermissions();
                if ((permissions != null) && (permissions.size() > 0)) {
                    Iterator iterator = permissions.iterator();
                    while (iterator.hasNext()) {
                        Permission permission = (Permission) iterator.next();
                        if (TextUtils.equals(paramPermission, permission.getName())) {
                            return permission.getStatus();
                        }
                    }
                }
                if (paramRule.getDefaultState() == Rule.DEFAULT_STATE_DENIED) {
                    return Rule.DEFAULT_STATE_DENIED;
                }
                if (paramRule.getDefaultState() == Rule.DEFAULT_STATE_GRANTED) {
                    return Rule.DEFAULT_STATE_GRANTED;
                }
                break;
            case Rule.ALL_GRANTED:
                return Rule.ALL_GRANTED;
            default:
                return Rule.ERROR;
        }
        return Rule.ERROR;
    }

    private boolean checkRule(Rule rule, ApplicationInfo appInfo) {
        switch (rule.getMajorType()) {
            default:
                break;
            case Rule.MAJORTYPE_OTHER:
                return true;
            case Rule.MAJORTYPE_SHOP:
                if (this.mAppInfoManager.isShopApp(appInfo.packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_PRESET:
                if (this.mAppInfoManager.isPresetApp(appInfo)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SIGNATURE:
                if (this.mAppInfoManager.isSignatureApp(appInfo.packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_WHITE:
                if (getWhiteApp(rule, appInfo.packageName) != null) {
                    return true;
                }
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
            default:
                break;
            case Rule.MAJORTYPE_OTHER:
                return true;
            case Rule.MAJORTYPE_SHOP:
                if (this.mAppInfoManager.isShopApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_PRESET:
                if (this.mAppInfoManager.isPresetApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_SIGNATURE:
                // reserve：用于做应用签名白名单，及应用用了平台签名，对某些系统权限进行放行。
                if (this.mAppInfoManager.isSignatureApp(packageName)) {
                    return true;
                }
                break;
            case Rule.MAJORTYPE_WHITE:
                if (getWhiteApp(rule, packageName) != null) {
                    return true;
                }
                break;
        }
        return false;
    }

    private Rule getRuleByPackageName(ApplicationInfo appInfo) {
        if (this.mPermissionConfig != null) {
            List<Rule> rules = this.mPermissionConfig.getRules();
            if ((rules != null) && (rules.size() > 0)) {
                Iterator iterator = rules.iterator();
                while (iterator.hasNext()) {
                    Rule rule = (Rule) iterator.next();
                    if (checkRule(rule, appInfo)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    private Rule getRuleByPackageName(String packageName) {
        if (this.mPermissionConfig != null) {
            List<Rule> rules = this.mPermissionConfig.getRules();
            if ((rules != null) && (rules.size() > 0)) {
                Iterator iterator = rules.iterator();
                while (iterator.hasNext()) {
                    Rule rule = (Rule) iterator.next();
                    if (checkRule(rule, packageName)) {
                        return rule;
                    }
                }
            }
        }
        return null;
    }

    private App getWhiteApp(Rule rule, String packageName) {
        List<App> apps = rule.getApps();
        if ((apps != null) && (apps.size() > 0)) {
            Iterator iterator = apps.iterator();
            while (iterator.hasNext()) {
                App app = (App) iterator.next();
                List packageNames = app.getPackageNames();
                if ((packageNames != null) && (packageNames.contains(packageName))) {
                    return app;
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
                    Iterator iterator = permissionList.iterator();
                    while (iterator.hasNext()) {
                        Permission permission = (Permission) iterator.next();
                        // 对于其他应用默认拒绝权限，需要授权的根据配置文件配置
                        if (TextUtils.equals(permissionInfo.name, permission.getName())) {
                            if (permission.getStatus() == Rule.DEFAULT_STATE_GRANTED) {
                                grantRuntimePermission(packageInfo, permissionInfo);
                            } else if (permission.getStatus() == Rule.DEFAULT_STATE_DENIED) {
                                revokeRuntimePermissionByPms(packageInfo.packageName, permissionInfo.name);
                            }
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
     * @param permissionType 全部授权/部分授权/撤销权限
     * @param permissions 应用申请的权限
     * @param permissionList 规则中限制的权限
     */
    private void grantOrRevokePermissionsByConfig(PackageInfo packageInfo, int permissionType, String[] permissions, List<Permission> permissionList) {
        if (permissions != null && permissions.length != 0) {
            for (int i = 0; i < permissions.length; i++) {
                String permissionName = permissions[i];
                PermissionInfo permissionInfo;
                try {
                    permissionInfo = this.mPackageManager.getPermissionInfo(permissionName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }

                if (permissionInfo != null && (permissionInfo.protectionLevel & PermissionInfo.PROTECTION_DANGEROUS) != 0) {
                    int flags = this.mPackageManager.getPermissionFlags(permissionInfo.name, packageInfo.packageName, Process.myUserHandle());
                    if ((flags & PackageManager.FLAG_PERMISSION_SYSTEM_FIXED) == 0 && (flags & PackageManager.FLAG_PERMISSION_POLICY_FIXED) == 0) {
                        this.grantOrRevokePermissionByType(packageInfo, permissionType, permissionList, permissionInfo);
                    }
                }
            }

        } else {
            Log.w(TAG, "package " + packageInfo.packageName + "has no permissions");
        }
    }

    private void grantPackagePermissions(PackageInfo packageInfo, String[] permissions) {
        if ((packageInfo != null) && (permissions != null)) {
            if (permissions.length == 0) {
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
                    App app = getWhiteApp(rule, packageInfo.packageName);
                    if (app != null) {
                        grantOrRevokePermissionsByConfig(packageInfo, app.getAuthorizationType(), permissions, app.getPermissions());
                    }
                } else {
                    grantOrRevokePermissionsByConfig(packageInfo, rule.getAuthorizationType(), permissions, rule.getPermissions());
                }
            }
        }
    }

    private void grantRuntimePermission(PackageInfo packageInfo, PermissionInfo permissionInfo) {
        if (packageInfo != null) {
            if (permissionInfo == null) {
                return;
            }
            String str;
            if (PLATFORM_PACKAGE_NAME.equals(permissionInfo.packageName)) {
                str = AppOpsManager.permissionToOp(permissionInfo.name);
            } else {
                str = null;
            }
            grantRuntimePermissionByAppOp(packageInfo, str);
            grantRuntimePermissionByPms(packageInfo.packageName, permissionInfo.name);
        }
    }

    private void grantRuntimePermissionByAppOp(PackageInfo paramPackageInfo, String appOp) {
        if (appOp == null) {
            return;
        }
        int i;
        if (this.mAppOps.checkOpNoThrow(appOp, paramPackageInfo.applicationInfo.uid, paramPackageInfo.packageName) != 0) {
            LogUtil.info("grantRuntimePermissionByPms -- packageName : " + paramPackageInfo.packageName + " | appOp : " + appOp);
            this.mAppOps.setUidMode(appOp, paramPackageInfo.applicationInfo.uid, 0);
        }
    }

    private void grantRuntimePermissionByPms(String packageName, String permissionName) {
        if (!TextUtils.isEmpty(packageName)) {
            if (TextUtils.isEmpty(permissionName)) {
                return;
            }
            if (checkPermissionFromPms(packageName, permissionName) != 0) {
                LogUtil.info("package name (" + packageName + ") runtime permission (" + permissionName + ") is GRANTED");
                this.mPackageManager.grantRuntimePermission(packageName, permissionName, Process.myUserHandle());
            }
        }
    }

    private void initAppInfoManagerDatas() {
        if (this.mPermissionConfig.getPresetPaths() != null) {
            this.mAppInfoManager.setPresetDirs(this.mPermissionConfig.getPresetPaths());
        }
        this.mAppInfoManager.setPresetShops(this.mPermissionConfig.getPresetShops());
    }

    private void revokeRuntimePermissionByPms(String packageName, String permissionName) {
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(permissionName)) {
            if (checkPermissionFromPms(packageName, permissionName) != PackageManager.PERMISSION_DENIED) {
                this.mPackageManager.revokeRuntimePermission(packageName, permissionName, Process.myUserHandle());
            }
        }
    }

    public int checkSignaturePermission(ApplicationInfo appInfo, String permissionName) {
        if (appInfo != null) {
            if (TextUtils.isEmpty(permissionName)) {
                return Rule.ERROR;
            }
            LogUtil.info("checkSignaturePermission -- packageName : " + appInfo.packageName + " | permissionName : " + permissionName);
            Rule rule = getRuleByPackageName(appInfo);
            if (rule != null) {
                LogUtil.info("checkSignaturePermission -- rule majorType : " + rule.getMajorType());
                if (rule.getMajorType() == Rule.MAJORTYPE_WHITE) {
                    App app = getWhiteApp(rule, appInfo.packageName);
                    if (app != null) {
                        return checkPermissionFromApp(app, permissionName);
                    }
                    return Rule.ERROR;
                }
                return checkPermissionFromRule(rule, permissionName);
            }
            return Rule.ERROR;
        }
        return Rule.ERROR;
    }

    public void grantPermissions(List<PackageInfo> packageInfos) {
        LogUtil.info("packageInfos : " + packageInfos);
        if (packageInfos != null && packageInfos.size() > 0) {
            Iterator iterator = packageInfos.iterator();
            while (iterator.hasNext()) {
                PackageInfo info = (PackageInfo) iterator.next();
                grantPackagePermissions(info, info.requestedPermissions);
            }
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
                    App app = getWhiteApp(rule, packageName);
                    if (app != null) {
                        return checkPermissionFromApp(app, permissionName);
                    }
                    return Rule.ERROR;
                }
                return checkPermissionFromRule(rule, permissionName);
            }
            return Rule.ERROR;
        }
        Log.w(TAG, "checkPermission -- packageName or permissionName is null!");
        return Rule.ERROR;
    }

    @Override
    public void handlePackagePostInstall(String packageName, boolean success) throws RemoteException {
        LogUtil.info("handlePackagePostInstall -- packageName : " + packageName + " | success : " + success);
        this.mAppInfoManager.handlePackagePostInstall(packageName, success);
    }

    @Override
    public void installPackageByPi(String packageName, String installerPackageName) throws RemoteException {
        LogUtil.info("installPackageByPi -- packageName : " + packageName + " | installerPackageName : " + installerPackageName);
        this.mAppInfoManager.installPackageByPi(packageName, installerPackageName);
    }

    @Override
    public void installStageByPms(String packageName, String installerPackageName) throws RemoteException {
        LogUtil.info("installStageByPms -- packageName : " + packageName + " | installerPackageName : " + installerPackageName);
        this.mAppInfoManager.installStageByPms(packageName, installerPackageName);
    }
}

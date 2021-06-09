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
package com.gxatek.permission.bean;

import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class App extends BaseConfig {
    private int authorizationType;
    private int defaultState;
    private List<String> packageNames;
    private List<Permission> permissions;

    private void checkAuthorizationType() {
        switch (this.authorizationType) {
            case Rule.ALL_GRANTED:
            case Rule.ALL_DENIED:
                Log.i(TAG, "App authorizationType value is " + this.authorizationType);
                break;
            case Rule.PART_GRANTED:
                checkPermissions();
                break;
            default:
                Log.w(TAG, "App authorizationType error,the error authorizationType value is " + this.authorizationType);
                this.checkMark += 1;
        }
    }

    private void checkPermissions() {
        if (permissions != null && permissions.size() > 0){
            Iterator iterator = permissions.iterator();
            while (iterator.hasNext()) {
                Permission permission = (Permission) iterator.next();
                if (permission == null) {
                    iterator.remove();
                } else if (!permission.isValid()) {
                    iterator.remove();
                }
            }
        } else {
            Log.w(TAG, "permissions cannot be null when app authorizationType value is PART_GRANTED");
        }
    }

    @Override
    public boolean isValid() {
        this.checkMark = 0;
        if ((this.packageNames == null) || (this.packageNames.size() == 0)) {
            Log.w(TAG, "app packageNames cannot be null");
            this.checkMark += 1;
        }
        checkAuthorizationType();
        return this.checkMark <= 0;
    }

    public int getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(int authorizationType) {
        this.authorizationType = authorizationType;
    }

    public int getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(int defaultState) {
        this.defaultState = defaultState;
    }

    public List<String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(List<String> packageNames) {
        this.packageNames = packageNames;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}

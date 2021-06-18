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
package com.gxa.permission.bean;

import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * White app permission config javabean.
 *
 * @author Jack_Ou  created on 2021/3/1.
 */
public class App extends BaseConfig {

    private List<String> packageNames;
    private int authorizationType;
    private int defaultState;
    private List<Permission> permissions;

    public List<String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(List<String> packageNames) {
        this.packageNames = packageNames;
    }

    public int getAuthorizationType() {
        return authorizationType;
    }

    public int getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(int defaultState) {
        this.defaultState = defaultState;
    }

    public void setAuthorizationType(int authorizationType) {
        this.authorizationType = authorizationType;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean isValid() {
        checkMark = 0;
        if (packageNames == null || packageNames.size() == 0) {
            Log.w(TAG,"app packageNames cannot be null");
            checkMark++;
        }
        checkAuthorizationType();
        return checkMark > 0 ? false : true;
    }

    private void checkAuthorizationType() {
        switch (authorizationType) {
            case Rule.PART_GRANTED:
                checkPermissions();
                break;
            case Rule.ALL_GRANTED:
            case Rule.ALL_DENIED:
                break;
            default:
                Log.w(TAG,"App authorizationType error,the error authorizationType value is " + authorizationType);
                checkMark++;
                break;
        }
    }

    private void checkPermissions() {
        if (permissions == null || permissions.size() == 0) {
            Log.w(TAG,"permissions cannot be null when app authorizationType value is PART_GRANTED");
        } else {
            Iterator<Permission> iterator = permissions.iterator();
            while (iterator.hasNext()) {
                Permission permission = iterator.next();
                if (permission == null) {
                    iterator.remove();
                } else {
                    boolean isValid = permission.isValid();
                    if (!isValid) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}
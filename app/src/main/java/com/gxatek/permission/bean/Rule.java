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
public class Rule extends BaseConfig implements Comparable<Rule> {
    public static final int ALL_DENIED = 3;
    public static final int PART_GRANTED = 2;
    public static final int ALL_GRANTED = 1;
    public static final int ERROR = 4;
    public static final int DEFAULT_STATE_DENIED = 2;
    public static final int DEFAULT_STATE_GRANTED = 1;
    public static final int MAJORTYPE_OTHER = 5;
    public static final int MAJORTYPE_PRESET = 3;
    public static final int MAJORTYPE_SHOP = 4;
    public static final int MAJORTYPE_SIGNATURE = 2;
    public static final int MAJORTYPE_WHITE = 1;
    private List<App> apps;
    private int authorizationType;
    private int defaultState;
    private boolean enable;
    private int majorType;
    private List<Permission> permissions;
    private int priority;

    private void checkApps() {
        if (apps != null && apps.size() != 0) {
            Iterator iterator = apps.iterator();
            while (iterator.hasNext()) {
                App app = (App) iterator.next();
                if (app == null) {
                    iterator.remove();
                } else if (!app.isValid()) {
                    iterator.remove();
                }
            }
        } else {
            Log.w(TAG, "apps cannot be null when majorType value is 1");
        }
    }

    private void checkAuthorizationType() {
        switch (this.authorizationType) {
            case ALL_DENIED:
            case ALL_GRANTED:
                Log.i(TAG, "Rule authorizationType value is " + this.authorizationType +
                        ", majorType" + this.majorType);
                break;
            case PART_GRANTED:
                checkPermissions();
                break;
            default:
                Log.w(TAG, "Rule authorizationType error,the error authorizationType value is "
                        + this.authorizationType);
                this.checkMark += 1;
                break;
        }
    }

    private void checkPermissions() {
        if (permissions != null && permissions.size() > 0) {
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
    public int compareTo(Rule rule) {
        return rule.priority - this.priority;
    }

    @Override
    public boolean isValid() {
        this.checkMark = 0;
        switch (this.majorType) {
            case MAJORTYPE_WHITE:
                checkApps();
                break;
            case MAJORTYPE_SIGNATURE:
            case MAJORTYPE_SHOP:
            case MAJORTYPE_PRESET:
            case MAJORTYPE_OTHER:
                checkAuthorizationType();
                break;
            default:
                Log.w(TAG, "majorType error,the error majorType value is " + this.majorType);
                this.checkMark += 1;
                break;
        }
        return super.isValid();
    }

    public List<App> getApps() {
        return this.apps;
    }

    public int getAuthorizationType() {
        return this.authorizationType;
    }

    public int getDefaultState() {
        return this.defaultState;
    }

    public int getMajorType() {
        return this.majorType;
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setApps(List<App> appList) {
        this.apps = appList;
    }

    public void setAuthorizationType(int authorizationType) {
        this.authorizationType = authorizationType;
    }

    public void setDefaultState(int state) {
        this.defaultState = state;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setMajorType(int majorType) {
        this.majorType = majorType;
    }

    public void setPermissions(List<Permission> permissionList) {
        this.permissions = permissionList;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "apps=" + apps +
                ", authorizationType=" + authorizationType +
                ", defaultState=" + defaultState +
                ", enable=" + enable +
                ", majorType=" + majorType +
                ", permissions=" + permissions +
                ", priority=" + priority +
                '}';
    }
}

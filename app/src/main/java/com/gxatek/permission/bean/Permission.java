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

import android.text.TextUtils;
import android.util.Log;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class Permission extends BaseConfig {
    private String name;
    private int status;

    @Override
    public boolean isValid() {
        this.checkMark = 0;
        if (TextUtils.isEmpty(this.name)) {
            Log.w(TAG, "permission name cannot be null");
            this.checkMark += 1;
        }
        return this.checkMark <= 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
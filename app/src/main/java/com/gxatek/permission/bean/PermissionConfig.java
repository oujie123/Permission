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

import com.gxatek.permission.Utils.Constants;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class PermissionConfig {
    private static final String TAG = Constants.TAG + PermissionConfig.class.getSimpleName();
    private List<String> presetPaths;
    private List<String> presetShops;
    private List<Rule> rules;
    private int version;

    public void checkConfig() {
        if ((this.rules != null) && (this.rules.size() != 0)) {
            Iterator iterator = this.rules.iterator();
            while (iterator.hasNext()) {
                Rule rule = (Rule) iterator.next();
                if (rule == null) {
                    iterator.remove();
                } else if ((!rule.isValid()) || (!rule.isEnable())) {
                    iterator.remove();
                }
            }
            Collections.sort(this.rules);
        } else {
            Log.w(TAG, "no permission rules");
        }
    }

    public List<String> getPresetPaths() {
        return this.presetPaths;
    }

    public List<String> getPresetShops() {
        return this.presetShops;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public int getVersion() {
        return this.version;
    }

    public void setPresetPaths(List<String> presetPaths) {
        this.presetPaths = presetPaths;
    }

    public void setPresetShops(List<String> presetShops) {
        this.presetShops = presetShops;
    }

    public void setRules(List<Rule> ruleList) {
        this.rules = ruleList;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

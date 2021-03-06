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
package com.gxa.permission.server;

import com.gxa.permission.Utils.CarGson;
import com.gxa.permission.Utils.LogUtil;
import com.gxa.permission.bean.PermissionConfig;

import java.io.File;

/**
 * Manager for read config.
 * @author Jack_Ou  created on 2021/3/2.
 */
public class ConfigManager {
    private static final String PERMISSION_CONFIG_DIR = "/vendor/etc/data/gxa_permission_config.json";
    private static final String PERMISSION_CONFIG_DIR_PRIMARY = "/vendor/etc/data/permission_config.json";
    private static ConfigManager mInstance;

    /**
     * singleton.
     */
    public static ConfigManager getInstance() {
        if (mInstance == null) {
            synchronized (ConfigManager.class) {
                if (mInstance == null) {
                    mInstance = new ConfigManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * read config from file.
     */
    public PermissionConfig readPermissionConfig() {
        CarGson carGson = new CarGson();
        File file = new File(PERMISSION_CONFIG_DIR_PRIMARY);
        LogUtil.d("readPermissionConfig");
        if ((file.exists()) && (file.isFile())) {
            LogUtil.d("permission_config.json exists");
            return carGson.getObjectFromJsonFile(PERMISSION_CONFIG_DIR_PRIMARY, PermissionConfig.class);
        }
        return carGson.getObjectFromJsonFile(PERMISSION_CONFIG_DIR, PermissionConfig.class);
    }
}

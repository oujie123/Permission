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

import com.gxa.permission.Utils.Constants;

/**
 * Config base javabean.
 *
 * @author Jack_Ou  created on 2021/3/1.
 */
public abstract class BaseConfig
{
    protected static final String TAG = Constants.TAG + BaseConfig.class.getSimpleName();
    protected int checkMark;

    /**
     * check effectiveness.
     */
    protected boolean isValid() {
        return checkMark <= 0;
    }
}

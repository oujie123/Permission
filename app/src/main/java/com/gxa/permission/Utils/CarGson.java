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
package com.gxa.permission.Utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * @author Jack_Ou  created on 2021/3/1.
 */
public class CarGson {

    private static final String TAG = Constants.TAG + CarGson.class.getSimpleName();

    /**
     * get javabean from json file.
     */
    public <T> T getObjectFromJsonFile(String fileDir, Class<T> clazz) {
        String json = readJsonFile(fileDir);
        if (!TextUtils.isEmpty(json)) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(json, clazz);
            } catch (Exception exception) {
                LogUtil.e("readPermissionConfig fromJson error : " + json, exception);
            }
        }
        return null;
    }

    /**
     * get javabean from json file.
     */
    public <T> T getObjectFromJsonFile(String fileDir) {
        String json = readJsonFile(fileDir);
        if (!TextUtils.isEmpty(json)) {
            try {
                LogUtil.i("getObjectFromJsonFile json : " + json);
                Gson gson = new Gson();
                Type type = new TypeToken<T>() {
                }.getType();
                return gson.fromJson(json, type);
            } catch (Exception exception) {
                LogUtil.e("readPermissionConfig fromJson error : " + json, exception);
            }
        }
        return null;
    }

    /**
     * write javabean into json file.
     */
    public boolean writeObjectToJsonFile(String fileDir, Object object) {
        if (object == null || TextUtils.isEmpty(fileDir)) {
            return false;
        }
        try {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            return writeToJsonFile(fileDir, json);
        } catch (Exception exception) {
            LogUtil.e("writeObjectToJsonFile error : ", exception);
        }
        return false;
    }

    private String readJsonFile(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        File jsonFile = new File(dir);

        if (!jsonFile.exists()) {
            LogUtil.w("readJsonFile <" + dir + "> not exist");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new FileInputStream(jsonFile);
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(readLine);
            }
            return stringBuilder.toString();
        } catch (IOException exception) {
            LogUtil.e("readJsonFile IOException", exception);
        } finally {
            closeQuietly(inputStream);
            closeQuietly(inputStreamReader);
            closeQuietly(bufferedReader);
        }
        return null;
    }

    private boolean writeToJsonFile(String filePath, String content) {
        Log.d(TAG, "writeToJsonFile() filePath = " + filePath);
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(content)) {
            return false;
        }

        if (TextUtils.isEmpty(content)) {
            LogUtil.w("writeToJsonFile() saveContent is empty");
            return false;
        }

        FileOutputStream fos = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            byte[] bytesArray = content.getBytes();
            fos = new FileOutputStream(filePath);
            fos.write(bytesArray);
            fos.flush();
            LogUtil.i("writeToJsonFile() Success!!!");
            return true;
        } catch (IOException exception) {
            LogUtil.w("writeToJsonFile() IOException: " + exception);
            return false;
        } finally {
            closeQuietly(fos);
        }
    }

    private void closeQuietly(AutoCloseable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                Log.d(TAG, "Exception ignored");
            }
        }
    }
}

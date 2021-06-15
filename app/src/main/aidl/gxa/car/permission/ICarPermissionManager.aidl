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
package gxa.car.permission;

interface ICarPermissionManager {

    int checkPermission(String packageName, String permissionName);

    void handlePackagePostInstall(String packageName, boolean success);

    void installPackageByPi(String packageName, String installerPackageName);

    void installStageByPms(String packageName, String installerPackageName);
}

/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.survey;//

import android.graphics.drawable.Drawable;

//TODO create class description
//
public class AppItem {

    public boolean appWritePriv;
    public boolean appReadPriv;
    public String appName;
    public Drawable appIcon;
    public String appPackageName;

    public AppItem(){
        appWritePriv = false;
        appReadPriv = false;
        appName = "";
        appIcon = null;
        appPackageName = "";
    }
}

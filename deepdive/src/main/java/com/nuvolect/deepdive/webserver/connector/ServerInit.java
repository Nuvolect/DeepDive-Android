/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.util.Omni;

/**
 * Initialization related to the web server.
 */
public class ServerInit {

    public static void init(Context ctx){

        /**
         * Initialize application file system support
         */
        Omni.init( ctx );

        CmdUpload.init( ctx );
    }
}

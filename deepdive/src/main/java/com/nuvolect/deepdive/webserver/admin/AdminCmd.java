/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.admin;

import android.content.Context;

import com.nuvolect.deepdive.util.LogUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * Dispatch to serve RESTful services.
 * This class will be expanded to serve the full set of elFinder commands.
 */
public class AdminCmd {

    private static boolean DEBUG = LogUtil.DEBUG;

    private enum CMD {

        // App commands
        debug,     // debugging commands
        login,
        logout,
        ping,      // simple ping, returns the time
        test,      // run a test
    }

    public static InputStream process(Context ctx, Map<String, String> params) {

        String error = "";

        CMD cmd = null;
        try {
            cmd = CMD.valueOf(params.get("cmd"));
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("cmd");
        }
        InputStream inputStream = null;

        switch ( cmd){

            case debug:
                inputStream = CmdDebug.go(ctx, params);
                break;
            case login:
                inputStream = CmdLogin.go(ctx, params);
                break;
            case logout:
                inputStream = CmdLogout.go(ctx, params);
                break;
            case ping:
                inputStream = CmdPing.go(params);
                break;
            case test:
                inputStream = CmdTest.go(ctx, params);
                break;
            default:
                LogUtil.log(LogUtil.LogType.ADMIN_SERVE_CMD, "Invalid command: "+error);
        }

        return inputStream;
    }
}

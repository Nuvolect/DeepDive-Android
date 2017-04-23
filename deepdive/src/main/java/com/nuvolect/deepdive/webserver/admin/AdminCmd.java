/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
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

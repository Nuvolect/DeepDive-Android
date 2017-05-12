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

package com.nuvolect.deepdive.webserver.connector;

import android.content.Context;

import com.nuvolect.deepdive.license.LicenseManager;
import com.nuvolect.deepdive.util.Analytics;
import com.nuvolect.deepdive.util.LogUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * Dispatch to serve RESTful services.
 * This class will be expanded to serve the full set of elFinder commands.
 */
public class ServeCmd {

    private static boolean DEBUG = LogUtil.DEBUG;

    private enum CMD {
        // elFinder commands in documentation order
        open,      // open directory and initializes data when no directory is defined (first iteration)
        file,      // output file contents to the browser (download/preview)
        tree,      // return child directories
        parents,   // return parent directories and its subdirectory childs
        ls,        // list files in directory
        tmb,       // create thumbnails for selected files
        size,      // return size for selected files or total folder(s) size
        dim,       // return image dimensions
        mkdir,     // create directory
        mkfile,    // create text file
        rm,        // delete files/directories
        rename,    // rename file
        duplicate, // create copy of file
        paste,     // copy or move files
        upload,    // upload file
        get,       // output plain/text file contents (preview)
        put,       // save text file content
        archive,   // create archive
        extract,   // extract archive
        search,    // search for files
        info,      // return info for files. (used by client "places" ui)
        resize,    // modify image file (resize/crop/rotate)
        url,       // return file url
        netmount,  // mount network volume during user session. Only ftp now supported.
        ping,      // simple ping, returns the time
        zipdl,     // zip and download files
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

            case archive:
                inputStream = CmdArchive.go(ctx, params);
                break;
            case dim:
                break;
            case duplicate:
                inputStream = CmdDuplicate.go(params);
                break;
            case extract:
                inputStream = CmdExtract.go(params);
                break;
            case file:
                inputStream = CmdFile.go(params);
                break;
            case get:
                inputStream = CmdGet.go(params);
                break;
            case info:
                inputStream = CmdInfo.go(params);
                break;
            case ls:
                inputStream = CmdLs.go(params);
                break;
            case mkdir:
                inputStream = CmdMkdir.go(params);
                break;
            case mkfile:
                inputStream = CmdMkfile.go(params);
                break;
            case netmount:
                break;
            case open:
                inputStream = CmdOpen.go(params);
                break;
            case parents:
                inputStream = CmdParents.go(params);
                break;
            case paste:
                inputStream = CmdPaste.go(params);
                break;
            case put:
                inputStream = CmdPut.go(params);
                break;
            case size:
                inputStream = CmdSize.go(params);
                break;
            case tmb:
                break;
            case tree:
                inputStream = CmdTree.go(params);
                break;
            case rename:
                inputStream = CmdRename.go(params);
                break;
            case resize:
                inputStream = CmdResize.go(params);
                break;
            case rm:
                inputStream = CmdRm.go(ctx, params);
                break;
            case search:
                inputStream = CmdSearch.go(params);
                break;
            case upload:
                inputStream = CmdUpload.go(ctx, params);
                break;
            case url:
                break;
            case zipdl:
                inputStream = CmdZipdl.go(ctx, params);
                break;
            default:
                LogUtil.log(LogUtil.LogType.CONNECTOR_SERVE_CMD, "Invalid connector command: "+error);
        }

        if(LicenseManager.isFreeUser()){

            String category = Analytics.FINDER;
            String action = cmd.toString();
            String label = "";
            long value = 1;

            Analytics.send( ctx, category, action, label, value);

//                LogUtil.log(ServeCmd.class, "cat: "+category+", act: "+action+", lab: "+label+", hits: "+value);
        }

        return inputStream;
    }
}

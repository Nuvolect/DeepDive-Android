package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;

import com.nuvolect.deepdive.ddUtil.Analytics;
import com.nuvolect.deepdive.ddUtil.LogUtil;

import java.io.InputStream;
import java.util.Map;

//TODO create class description
//
public class ServeCmd {

    private static boolean DEBUG = LogUtil.DEBUG;

    enum CMD {
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

        // Image-swipe commands
        image_query,//TODO remove

        // App commands
        debug,     // debugging commands
        login,
        logout,
//        deepdive,  // penetration testing
        test,      // run a test
    }

    public static InputStream process(Context ctx, Map<String, String> params) {

        String error = "";
        boolean sendAnalytics = true;

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
            case debug:
                inputStream = CmdDebug.go(ctx, params);
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
            case image_query:
                inputStream = CmdImageQuery.go(params);
                break;
            case info:
                inputStream = CmdInfo.go(params);
                break;
            case ls:
                inputStream = CmdLs.go(params);
                break;
            case login:
                inputStream = CmdLogin.go(ctx, params);
                break;
            case logout:
                inputStream = CmdLogout.go(ctx, params);
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
            case ping:
                inputStream = CmdPing.go(params);
                break;
//            case deepdive:
//                /**
//                 * Skip analytics that generate excessive api calls
//                 */
//                String test_id = params.get("test_id");
//                boolean skip = test_id.contains("get_stream") || test_id.contains("get_status");
//                if( ! skip){
//                    String extra = "";
//                    if( params.containsKey("package_name"))
//                        extra = params.get("package_name");
//                    Analytics.send(ctx, Analytics.PEN_TEST, cmd.name(), extra, 1L);
//                }
//                inputStream = TestAndroid.go(ctx, params);
//                break;
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
            case test:
                inputStream = CmdTest.go(ctx, params);
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
                LogUtil.log(LogUtil.LogType.SERVE, "Invalid connector command: "+error);
        }

        if( sendAnalytics)
            Analytics.send(ctx, Analytics.FINDER, cmd.name(), "label", 1L);

        return inputStream;
    }
}

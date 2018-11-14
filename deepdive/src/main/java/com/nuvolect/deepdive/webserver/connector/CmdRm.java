/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import android.content.Context;
import android.media.MediaScannerConnection;

import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniImage;
import com.nuvolect.deepdive.util.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;


/**
 *<pre>
 *
 * rm
 *
 * Recursively removes files and directories.
 *
 * Arguments:
 *
 * cmd : rm
 * targets : (Array) array of file and directory hashes to delete
 * Response:
 *
 * removed : (Array) array of file and directory 'hashes' that were successfully deleted
 *
 * Example:( 5B == '[', 5D == ']'
 * http://hypweb.net/elFinder-nightly/demo/2.1/php/connector.minimal.php?
 * cmd=rm
 * &targets%5B%5D=l2_TmV3RmlsZS50eHQ
 * &targets%5B%5D=l2_TmV3RmlsZSBjb3B5IDEudHh0
 * &_=1459218951979
 *
 *</pre>
 */
public class CmdRm {

    private Context context;
    private ArrayList<OmniFile> removedFiles = new ArrayList<>();

    public CmdRm(Context context) {
        this.context = context;
    }

    public static InputStream go(Context ctx, Map<String, String> params) {
        CmdRm instance = new CmdRm(ctx);
        return instance.deleteFiles(params);
    }

    public static boolean delete(Context context, OmniFile omniFile) {
        CmdRm instance = new CmdRm(context);
        return instance.delete(omniFile, true);
    }

    private InputStream deleteFiles(Map<String, String> params) {
        ArrayList<String> targets = new ArrayList<>();

        /**
         * Params only has the first element of the targets[] array.
         * This is fine if there is only one target but an issue for multiple file operations.
         * Manually parse the query parameter strings to get all targets.
         */
        String[] qps = params.get("queryParameterStrings").split("&");

        for(String candidate : qps){

            if( candidate.contains("targets")){
                String[] parts = candidate.split("=");
                targets.add( parts[1]);
            }
        }

        boolean success = true;

        for (String target : targets) {

            OmniFile targetFile = OmniUtil.getFileFromHash(target);

            /**
             * Recursively delete files and folders adding each delete to an arraylist.
             */
            success = delete(targetFile, false);
            if(! success)
                break;
        }

        JSONObject wrapper = new JSONObject();

        try {
            if (success) {
                JSONArray removed = new JSONArray();
                ArrayList<String> pathsToScan = new ArrayList<>();
                for (OmniFile file: removedFiles) {
                    removed.put(file.getHash());
                    if (needScanFile(file)) {
                        pathsToScan.add(file.getAbsolutePath());
                    }
                }
                if (pathsToScan.size() > 0) {
                    MediaScannerConnection.scanFile(
                            context,
                            pathsToScan.toArray(new String[pathsToScan.size()]),
                            null,
                            null);
                }

                wrapper.put("removed", removed);
            }

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param f The directory (or file) to delete
     * @return true if the delete succeeded, false otherwise
     */
    private boolean delete(OmniFile f, boolean scanFile) {
        if (f.isDirectory()) {
            for (OmniFile child : f.listFiles()) {
                if (!delete(child, scanFile)) {
                    return false;
                }
            }
        }

        /**
         * Delete thumbnail, if there is one
         */
        OmniImage.deleteThumbnail( f );

        boolean success = f.delete();

        if (success) {
            removedFiles.add(f);

            /**
             * The crypto storage does not use the media scanner.
             */
            if (scanFile && needScanFile(f)) {
                MediaScannerConnection.scanFile(
                        context,
                        new String[]{f.getAbsolutePath()},
                        null,
                        null);
            }

            return true;
        }

        return false;
    }

    private boolean needScanFile(OmniFile file) {
        //here you can add additional conditions if the file needs to be scanned
        return file.isStd();
    }
}

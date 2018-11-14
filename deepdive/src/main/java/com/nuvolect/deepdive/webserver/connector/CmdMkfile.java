/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Create a new blank file.
 *
 * Arguments:
 *
 * cmd : mkfile
 * target : hash of target directory,
 * name : New file name
 * Response:
 *
 * added : (Array) Array with a single object - a new file. Information about File/Directory
 */
public class CmdMkfile {

    public static InputStream go(Map<String, String> params) {

        String targetDirHash = "";// Target is a hashed volume and directory path
        if( params.containsKey("target"))
            targetDirHash = params.get("target");

        String serverUrl = params.get("url");

        /**
         * TargetFile is the new file.
         */
        OmniFile targetDir = OmniUtil.getFileFromHash(targetDirHash);
        String volumeId = targetDir.getVolumeId();
        LogUtil.log(LogUtil.LogType.CMD_MKFILE, "Target dir" + targetDir.getPath());

        String name = "";
        if( params.containsKey("name"))
            name = params.get("name");

        String path = targetDir.getPath();

        OmniFile targetFile = new OmniFile(volumeId, path+"/"+name);
        boolean success = targetFile.createNewFile();

        JSONArray added = new JSONArray();
        JSONObject wrapper = new JSONObject();

        try {

            if( success ){
                JSONObject newDir = FileObj.makeObj( volumeId, targetFile, serverUrl);
                added.put( newDir);
            }
            wrapper.put("added", added);

            return new ByteArrayInputStream(wrapper.toString(2).getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

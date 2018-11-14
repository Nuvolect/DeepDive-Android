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
 * Create a new directory.
 *
 * Arguments:
 *
 * cmd : mkdir
 * target : hash of target directory,
 * name : New directory name
 * dirs[] : array of new directories path (requests at pre-flight of folder upload)
 * Response:
 *
 * added : (Array) Array with a single object - a new directory. Information about File/Directory
 * hashes : (Object) Object of the hash value as a key to the given path in the dirs[]
 */
public class CmdMkdir {

    public static InputStream go(Map<String, String> params) {

        String target = "";// Target is a hashed volume and path
        if( params.containsKey("target"))
            target = params.get("target");

        String httpIpPort = params.get("url");

        OmniFile targetFile = OmniUtil.getFileFromHash(target);
        LogUtil.log(LogUtil.LogType.CMD_MKDIR, "Target " + targetFile.getPath());

        String name = "";
        if( params.containsKey("name"))
            name = params.get("name");

        String volumeId = targetFile.getVolumeId();
        String path = targetFile.getPath();

        OmniFile file = new OmniFile(volumeId, path+"/"+name);
        boolean success = file.mkdir();

        JSONArray added = new JSONArray();
        JSONObject wrapper = new JSONObject();

        try {

            if( success ) {
                JSONObject newDir = FileObj.makeObj(volumeId, file, httpIpPort);
                added.put(newDir);
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

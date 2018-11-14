/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * tree
 *
 * Return folder's subfolders.
 *
 * Arguments:
 *
 * cmd : tree
 * target : folder's hash
 * Response:
 *
 * tree : (Array) Folders list. Information about File/Directory
 */
public class CmdTree {

    public static ByteArrayInputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");

        String target = params.get("target");
        OmniFile targetFile = new OmniFile( target );
        String volumeId = targetFile.getVolumeId();

        LogUtil.log(LogUtil.LogType.CMD_TREE, "volumeId: "+volumeId+", path: "+targetFile.getPath());

        try {
            JSONArray tree = new JSONArray();
            tree.put(FileObj.makeObj(volumeId, targetFile, httpIpPort));

            int i=0;
            OmniFile[] files = targetFile.listFiles();
            if( files == null)
                files = new OmniFile[0];


                for( OmniFile file: files){

                    if( file.isDirectory()){

                        tree.put(FileObj.makeObj(volumeId, file, httpIpPort));
                        LogUtil.log(LogUtil.LogType.CMD_TREE, "File "+ ++i +", "+file.getName());
                    }
                }

            JSONObject wrapper = new JSONObject();
            wrapper.put("tree", tree);
            String result = wrapper.toString(2);

            return new ByteArrayInputStream(result.getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}

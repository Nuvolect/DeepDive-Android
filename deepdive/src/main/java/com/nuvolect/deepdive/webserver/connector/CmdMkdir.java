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

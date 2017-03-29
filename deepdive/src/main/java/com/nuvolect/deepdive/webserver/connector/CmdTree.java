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

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
import com.nuvolect.deepdive.util.OmniHash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * info
 *
 * Returns information about places nodes
 *
 * Arguments:
 *
 * cmd : info
 * targets[] : array of hashed paths of the nodes
 * Response:
 *
 * files: (Array of data) places directories info data Information about File/Directory
 */
public class CmdInfo {

    private static boolean DEBUG = LogUtil.DEBUG;

    public static ByteArrayInputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");

        String[] targets = new String[ 100 ];
        if (params.containsKey("targets[]"))
            targets[0] = params.get("targets[]");

        /**
         * A non-empty targets is a hashed path starting with with the volume
         * followed by a encoded relative path.
         */
        String segments[] = targets[0].split("_");
        String volumeId = segments[0] + "_";

        String path = OmniHash.decode(segments[1]);
        OmniFile targetFile = new OmniFile(volumeId, path);

        LogUtil.log(LogUtil.LogType.CMD_INFO, "volumeId: " + volumeId + ", relativePath: " + path);

        try {
            JSONArray files = new JSONArray();

            files.put(FileObj.makeObj(volumeId, targetFile, httpIpPort));
            LogUtil.log(LogUtil.LogType.INFO, "File " + targetFile.getName());

            JSONObject wrapper = new JSONObject();
            wrapper.put("files", files);

            if( DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_PARENTS, wrapper.toString(2));

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}

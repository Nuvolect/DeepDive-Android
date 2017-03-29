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
import com.nuvolect.deepdive.util.OmniZip;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * extract
 *
 * Unpacks an archive.
 *
 * Arguments:
 *
 * cmd : extract
 * target : hash of the archive file
 * makedir : "1" to extract to new directory
 * Response:
 *
 * added : (Array) Information about File/Directory of extracted items
 *
 */
public class CmdExtract {

    private static boolean DEBUG = LogUtil.DEBUG;

    public static InputStream go(Map<String, String> params) {

        String httpIpPort = params.get("url");

        OmniFile zipFile = new OmniFile(params.get("target"));
        String volumeId = zipFile.getVolumeId();
        OmniFile destinationFolder = zipFile.getParentFile();
        JSONArray added = new JSONArray();

        /**
         * Get the parent directory and inside it create a new directory
         */
        if( params.containsKey("makedir") &&
                params.get("makedir").contentEquals("1")){

            String dirName = FilenameUtils.getBaseName( zipFile.getName());
            if( dirName == null || dirName.isEmpty())
                dirName = "Archive";

            /**
             * Create a new directory.
             * Avoid collision by adding ~ to make directory name unique.
             */
            String path = destinationFolder.getPath() + "/" + dirName;
            destinationFolder = new OmniFile(volumeId, path);
            destinationFolder = OmniUtil.makeUniqueName( destinationFolder);
            destinationFolder.mkdir();

            // Record addition of the new directory
            added.put( destinationFolder.getFileObject(httpIpPort));
        }

        try {

            /**
             * Unzip files and directories and record additions to 'added'
             */
            OmniZip.unzipFile( zipFile, destinationFolder, added, httpIpPort);

            JSONObject wrapper = new JSONObject();
            wrapper.put("added",added);

            if( DEBUG)
                LogUtil.log(LogUtil.LogType.CMD_EXTRACT, "json result: "+wrapper.toString(2));

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

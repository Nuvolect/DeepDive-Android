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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * file
 *
 * Output file into browser. This command applies to download and preview actions.
 *
 * Arguments:
 *
 * cmd : file
 * target : file's hash,
 * download : Send headers to force download file instead of opening it in the browser.
 *
 * May need to set Content-Disposition, Content-Location and Content-Transfer-Encoding.
 * Content-Disposition should have 'inline' for preview action or 'attachments' for download.
 */
public class CmdFile {
    public static InputStream go(Map<String, String> params) {

        String target = "";// Target is a hashed volume and path
        if (params.containsKey("target")) {
            target = params.get("target");

            OmniFile targetFile = OmniUtil.getFileFromHash(target);
            LogUtil.log(LogUtil.LogType.CMD_FILE, "Target " + targetFile.getPath());

            InputStream is = null;
            try {

                if (targetFile.isCryp())
                    is = new info.guardianproject.iocipher.FileInputStream(
                            targetFile.getCryFile());
                else
                    is = new java.io.FileInputStream(
                            targetFile.getStdFile());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return is;
        }
        if (params.containsKey("path")) {

            String path  = params.get("path");
            InputStream is = null;
            try {
                File file = new File( path );
                is = new java.io.FileInputStream( file );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return is;
        }
        return null;
    }
}

package com.nuvolect.deepdive.webserver.connector;//

//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.ddUtil.OmniFile;
import com.nuvolect.deepdive.ddUtil.OmniUtil;
import com.nuvolect.deepdive.ddUtil.OmniZip;
import com.nuvolect.deepdive.webserver.MimeUtil;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * ZipDl - zip multiple files to "Archive.zip", download and delete(FUTURE) "Archive.zip".
 * TODO delete zip archive
 *
 * zipdl

 1st request to make temporary archive file on server side

 Arguments:

 cmd : zipdl
 targets[] : array of hashed paths of the nodes
 Response:

 zipdl array data for 2nd request
 {
   file: "elfzdljKWIxU",        // temporary archive file name
   name: "Test here_Files.zip", // download file name
   mime: "application/zip"      // MIME type
 }

 2nd requset to download an archive

 Arguments:

 cmd : zipdl
 download : 1
 targets[0] : hash path for detect target volume drive (e.g. cwd hash)
 targets[1] : target temporary archive file name
 targets[2] : download file name
 targets[3] : MIME type

 Response: RAW data of archive file with HTTP headers for download

 {
   "zipdl": {
     "file": "elfzdlwjPURf",
     "name": "MIME-types_Files.zip",
     "mime": "application\/zip"
   },
   "debug": {
     "connector": "php",
     "phpver": "5.5.36",
     "time": 0.10684299468994,
     "memory": "2159Kb \/ 800Kb \/ 256M",
     "upload": "",
   "volumes": [{
     "id": "l1_",
     "name": "localfilesystem",
     "mimeDetect": "finfo",
     "imgLib": "imagick"
   }, {
     "id": "l2_",
     "name": "localfilesystem",
     "mimeDetect": "finfo",
     "imgLib": "imagick"
   }, {
     "id": "l3_",
     "name": "localfilesystem",
     "mimeDetect": "finfo",
     "imgLib": "imagick"
   }],
     "mountErrors": [],
     "phpErrors": []
   }
 }

 First call - request JSON object with "file", "name", "mime", make ZIP
 ~~~~
 GET '/connector'
 {
 queryParameterStrings=
 cmd=zipdl&
 targets%5B%5D=c0_L0tlcGxlci9zaWduYWwtMjAxNi0wNC0yNy0xOTI0MjEuanBn&
 targets%5B%5D=c0_L0tlcGxlci9zaWduYWwtMjAxNi0wNS0wMi0xMzIzNTkuanBn&
 _=1467751633659,
 _=1467751633659,
 cmd=zipdl,
 targets[]=c0_L0tlcGxlci9zaWduYWwtMjAxNi0wNS0wMi0xMzIzNTkuanBn
 }
 ~~~~

 Second call is POST, download ZIP
 ~~~~
 POST '/connector'
 {
 queryParameterStrings=
 cmd=zipdl&
 targets%5B%5D=c0_L0tlcGxlci9zaWduYWwtMjAxNi0wNC0yNy0xOTI0MjEuanBn&
 targets%5B%5D=c0_L0tlcGxlci9zaWduYWwtMjAxNi0wNS0wMi0xMzIzNTkuanBn&
 _=1467751633659
 }
 ~~~~
 */

public class CmdZipdl {

    private static boolean download_ready = false;
    private static OmniFile zipOmniFile = null;
    public static String zipdlFilename = "download_multiple.zip";// temp name, always overwritten

    public static InputStream go(Context ctx, Map<String, String> params) {

        if( download_ready ){

            InputStream is = null;
            try {

                if( zipOmniFile.isCryp())
                    is = new info.guardianproject.iocipher.FileInputStream(
                            zipOmniFile.getCryFile());
                else
                    is = new java.io.FileInputStream(
                            zipOmniFile.getStdFile());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            LogUtil.log(LogUtil.LogType.CMD_ZIPDL, "second request");
            download_ready = false; // setup for next use
            return is;
        }

//        String httpIpPort = params.get("url");
        ArrayList<String> targetsArrayList = new ArrayList<>();

        /**
         * Params only has the first element of the targets[] array.
         * This is fine if there is only one target but an issue for multiple file operations.
         * Manually parse the query parameter strings to get all targets.
         */
        String[] qps = params.get("queryParameterStrings").split("&");

        for (String candidate : qps) {

            if (candidate.contains("targets")) {
                String[] parts = candidate.split("=");
                targetsArrayList.add(parts[1]);
            }
        }

        OmniFile[] targets = new OmniFile[ targetsArrayList.size()];
        for( int i = 0; i < targetsArrayList.size(); i++){

            targets[i] = new OmniFile( targetsArrayList.get( i ));

            if(LogUtil.DEBUG){
                LogUtil.log(LogUtil.LogType.CMD_ZIPDL, targetsArrayList.get( i ));
            }
        }

        /**
         * Create a file for the target archive in the directory of the first target file.
         */
        String volumeId = targets[0].getVolumeId();
        zipdlFilename = "Archive.zip";
        String zipPath = targets[0].getParentFile().getPath()+"/"+ zipdlFilename;
        zipOmniFile = new OmniFile( volumeId, zipPath);
        zipOmniFile = OmniUtil.makeUniqueName(zipOmniFile);// Add '~' to make unique

        zipdlFilename = FilenameUtils.getName(zipOmniFile.getPath()); // Filename may have changed

        JSONObject zipdl = new JSONObject();
        JSONObject wrapper = new JSONObject();
        try {
            zipdl.put("file", zipOmniFile.getHash());
            zipdl.put("name", zipdlFilename);
            zipdl.put("mime", MimeUtil.MIME_ZIP);
            wrapper.put("zipdl", zipdl);

            boolean success = OmniZip.zipFiles(ctx, targets, zipOmniFile, 0);
            if( ! success)
                return null;

            download_ready = true;
            LogUtil.log(LogUtil.LogType.CMD_ZIPDL, "first request");
            LogUtil.log(LogUtil.LogType.CMD_ZIPDL, wrapper.toString(4));

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }
}

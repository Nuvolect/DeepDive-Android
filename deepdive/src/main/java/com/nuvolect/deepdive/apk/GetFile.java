package com.nuvolect.deepdive.apk;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.util.Util;
import com.nuvolect.deepdive.webserver.MimeUtil;
import com.nuvolect.deepdive.webserver.connector.VolUtil;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class GetFile {

    public static JSONObject getText(String path) {

        String file_name = FilenameUtils.getName( path);
        boolean isText = MimeUtil.isText( FilenameUtils.getExtension(file_name));

        File file = new File( path );
        String file_content = "";

        if( file == null || ! file.exists())
            file_content = "File does not exist: "+path;
        else
        if( file.isDirectory())
            file_content = "File is a directory: "+path;
        else
        if( ! isText)
            file_content = "File is binary: "+path;
        else{
            Context ctx = App.getContext();
            file_content = Util.readFile(ctx, file);
        }
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put("file_name", file_name);
            wrapper.put("file_content", file_content);
            wrapper.put("file_relative_path", "/"+path.replaceFirst(VolUtil.getRoot(VolUtil.sdcardVolumeId),""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wrapper;
    }
}

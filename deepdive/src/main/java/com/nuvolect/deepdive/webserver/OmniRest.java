/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.probe.ProbeUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniHash;
import com.nuvolect.deepdive.util.OmniUtil;
import com.nuvolect.deepdive.webserver.connector.FileObj;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class OmniRest {

    private enum CMD_ID {
        NIL,
        decode_hash,
        delete,
        encode_hash,
        get,
        get_info,
        get_text,
        list_files,
        mime,
        mkdir,
        upload,
    }
    public static InputStream process(Context ctx, Map<String, String> params) {

        long timeStart = System.currentTimeMillis();
        CMD_ID cmd_id = CMD_ID.NIL;
        String error = "";
        String volumeId = App.getUser().getDefaultVolumeId();
        if( params.containsKey("volume_id"))
            volumeId = params.get("volume_id");
        String path = "";
        if( params.containsKey("path"))
            path = params.get("path");

        try {
            String uri = params.get("uri");
            String segments[] = uri.split("/");
            cmd_id = CMD_ID.valueOf( segments[2]);
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("cmd");
        }

        JSONObject wrapper = new JSONObject();
        String extra = "";

        try {
            switch ( cmd_id){

                case NIL:
                    break;
                case decode_hash:{
                    String result = decode_hash(params.get("data"));
                    wrapper.put("result",result);
                    break;
                }
                case delete:{
                    JSONObject result = new JSONObject();
                    OmniFile file = new OmniFile( params.get("encoded_path"));
                    boolean delete_result = file.delete();
                    result.put("result", delete_result);
                    result.put("error", "");
                    wrapper.put("result",result);
                    break;
                }
                case encode_hash:{
                    String result = encode_hash(params.get("data"));
                    wrapper.put("result",result);
                    break;
                }
                case get:{
//                    InputStream is = null;
//                    OmniFile of = new OmniFile( encodedPath);
//
//                    if( of.exists())
//                        is = of.getFileInputStream();
//                    else
//                        is = IOUtils.getInputStreamFromString("File not found: " + encodedPath);
//
//                    try {
//                        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
//                        response.flushBuffer();
//
//                    } catch (IOException e) {
//                        LogUtil.logException(OmniRest.class, e);
//                    }
                    break;
                }
                case get_info:{
                    String result = ProbeUtil.getInfo(params.get("encoded_path")).toString();
                    wrapper.put("result",result);
                    break;
                }
                case get_text:{
                    JSONObject result = OmniUtil.getText( volumeId, path);
                    wrapper.put("result", result.toString());
                    break;
                }
                case list_files:{
                    JSONObject result = new JSONObject();
                    OmniFile om = new OmniFile( params.get("encoded_path"));
                    OmniFile[] files = om.listFiles();

                    JSONArray jsonArray = new JSONArray();
                    for( OmniFile file : files){

                        JSONObject fileObj = FileObj.makeObj( file, "");
                        jsonArray.put( fileObj);
                    }

                    result.put("result", jsonArray.toString());
                    result.put("error", "");
                    wrapper.put("result",result);
                    break;
                }
                case mime:{
                    String result = MimeUtil.getMime(params.get("data"));
                    wrapper.put("result",result);
                    break;
                }
                case mkdir:{
                    JSONObject result = new JSONObject();
                    OmniFile file = new OmniFile( params.get("encoded_path"));
                    boolean mkdir_result = file.mkdirs();
                    result.put("result", mkdir_result);
                    result.put("error", "");
                    wrapper.put("result",result);
                    break;
                }
                case upload:
                    break;
            }
            if( ! error.isEmpty())
                LogUtil.log( OmniRest.class, "Error: "+error);

            wrapper.put("error", error);
            wrapper.put("cmd_id", cmd_id.toString());
            wrapper.put("delta_time", String.valueOf(System.currentTimeMillis() - timeStart) + " ms");

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String decode_hash(String data) {

        String hash = data;
        if( Omni.matchVolume( data)){

            hash = Omni.getPathFromUri( data);
        }

        return OmniHash.decode( hash );
    }

    private static String encode_hash(String data) {

        return OmniHash.encode( data );
    }
}

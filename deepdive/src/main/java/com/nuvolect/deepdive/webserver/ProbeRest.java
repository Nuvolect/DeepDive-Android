package com.nuvolect.deepdive.webserver;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.probe.DecompileApk;
import com.nuvolect.deepdive.probe.ProbeMgr;
import com.nuvolect.deepdive.probe.ProbeUtil;
import com.nuvolect.deepdive.probe.TestAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ProbeRest {

    private enum CMD_ID {
        NIL,
        clear_log,
        copy_apk,
        create_package,
        decompile_action,
        delete_package,
        get_log,
        get_package_list,
        get_status,
        installed_apps,
        stop_thread,
        upload
    }
    public static InputStream process(Context ctx, Map<String, String> params) {

        long timeStart = System.currentTimeMillis();
        CMD_ID cmd_id = CMD_ID.NIL;
        String volumeId = App.getUser().getDefaultVolumeId();
        String error = "";
        String package_name = "";

        try {
            String uri = params.get("uri");
            String segments[] = uri.split("/");
            cmd_id = CMD_ID.valueOf( segments[2]);
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("cmd");
        }

        if( params.containsKey("volume_id"))
            volumeId = params.get("volume_id");
        if( params.containsKey("package_name"))
            package_name = params.get("package_name");

        JSONObject wrapper = new JSONObject();

        try {
            switch ( cmd_id){

                case NIL: {
                    break;
                }
                case clear_log:{
                    DecompileApk decompiler = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    decompiler.clearStream();
                    break;
                }
                case copy_apk:{
                    DecompileApk decompile = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    JSONObject status = decompile.copyApk();
                    wrapper.put("status", status.toString());
                    break;
                }
                case create_package:{
                    String folderPath = (CConst.USER_FOLDER_PATH+package_name+"/").replace("//","/");
                    JSONObject result = ProbeUtil.createPackage( ctx, volumeId, folderPath);
                    wrapper.put("result", result.toString());
                    break;
                }
                case decompile_action:{
                    String action = params.get("action");
                    DecompileApk decompile = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    JSONObject status = decompile.startThread( ctx, action);
                    wrapper.put("status", status.toString());
                    break;
                }
                case delete_package:{
                    //FIXME first kill any running processes associated with the package
                    String folderPath = (CConst.USER_FOLDER_PATH+package_name+"/").replace("//","/");
                    JSONObject result = ProbeUtil.deletePackage( ctx, volumeId, folderPath);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_log:{
                    DecompileApk decompiler = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    JSONArray stream = decompiler.getStream();
                    wrapper.put("stream", stream.toString());
                    break;
                }
                case get_package_list:{
                    JSONObject result = ProbeUtil.getPackageList( ctx );
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_status:{
                    DecompileApk decompileApk = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    JSONObject status = decompileApk.getStatus();
                    wrapper.put("status", status.toString());
                    break;
                }
                case installed_apps:{

                    JSONArray apps = TestAndroid.getApps( ctx);
                    wrapper.put("apps", apps.toString());
                    break;
                }
                case stop_thread:{
                    DecompileApk decompileApk = ProbeMgr.getProbe( ctx, package_name).getDecompiler();
                    String action = params.get("action");
                    JSONObject status = decompileApk.stopThread( action);
                    wrapper.put("status", status.toString());
                    break;
                }
                case upload:{

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error += e.toString();
        }
        if( ! error.isEmpty())
            LogUtil.log( ProbeRest.class, "Error: "+error);

        try {
            wrapper.put("error", error);
            wrapper.put("cmd_id", cmd_id.toString());
            wrapper.put("delta_time", String.valueOf(System.currentTimeMillis() - timeStart) + " ms");

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

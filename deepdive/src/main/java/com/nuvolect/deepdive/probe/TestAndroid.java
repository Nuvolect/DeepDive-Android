package com.nuvolect.deepdive.probe;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Run a specific test and return the result
 */
public class TestAndroid {

//    private enum TEST_ID {
//        app_detail,
//        apps,
//        device,
//        get_indexes,
//        get_status,
//        get_stream,
//        get_text,
//        logcat,
//        new_index,
//        search,
//        search_index,
//        search_index_interrupt,
//        shell,
//        folder_get_list,
//    }
//
//    public static ByteArrayInputStream go(Context ctx, Map<String, String> params) {
//
//        try {
//            JSONObject wrapper = new JSONObject();
//
//            String error = "";
//
//            TEST_ID test_id = null;
//            try {
//                test_id = TEST_ID.valueOf(params.get("test_id"));
//            } catch (IllegalArgumentException e) {
//                error = "Error, invalid command: "+params.get("cmd");
//            }
//            long timeStart = System.currentTimeMillis();
//
//            assert test_id != null;
//
//            if( LogUtil.DEBUG){
//                String queryParams = params.get("queryParameterStrings");
//                LogUtil.log(LogUtil.LogType.DEEPDIVE, test_id+", "+queryParams);
//            }
//            try {
//                switch ( test_id ){
//
//                    case folder_get_list:{
//
//                        JSONObject result = ProbeUtil.getPackageList(ctx );
//                        wrapper.put("result", result.toString());
//                        break;
//                    }
//                    case apps:{
//
//                        JSONArray apps = getApps( ctx);
//                        wrapper.put("apps", apps.toString());
//                        break;
//                    }
//                    case app_detail:{
//
//                        String package_name = params.get("package_name");
//                        JSONObject app_detail = SurveyExec.getAppDetail( ctx, package_name);
//                        wrapper.put("app_detail", app_detail.toString());
//                        break;
//                    }
////                    case search:{
////
////                        String search_query = params.get("search_query");
////                        String search_path = params.get("search_path");
////                        JSONObject result = Search.search( ctx, search_query, search_path);
////                        wrapper.put("result", result.toString());
////                        break;
////                    }
////                    case search_index:{
////
////                        String search_path = params.get("search_path");
////                        boolean force_index = false;
////                        if( params.containsKey("force_index"))  // Optional key
////                            force_index = params.get("force_index").contentEquals("true");
////                        JSONObject result = Index.index( ctx, search_path, force_index);
////                        wrapper.put("result", result.toString());
////                        break;
////                    }
//                    case search_index_interrupt:{
//
//                        JSONObject result = Index.interrupt();
//                        wrapper.put("result", result.toString());
//                        break;
//                    }
////                    case get_indexes:{
////
////                        JSONArray result = IndexUtil.getIndexes(ctx);
////                        wrapper.put("result", result.toString());
////                        break;
////                    }
////                    case new_index:{
////
////                        String new_index = params.get("new_path");
////                        JSONObject result = IndexUtil.newIndex(ctx, new_index);
////                        wrapper.put("result", result.toString());
////                        break;
////                    }
//                    case get_text:{
//
//                        String path = params.get("path");
//                        String volumeId = App.getUser().getDefaultVolumeId();
//                        if( params.containsKey("volume_id"))
//                            volumeId = params.get( "volume_id");
//                        JSONObject result = OmniUtil.getText( volumeId, path);
//                        wrapper.put("result", result.toString());
//                        break;
//                    }
////                    case stop_thread:{
////
////                        String method = params.get("method");
////                        JSONObject status = DecompileApk.stopThread( method);
////                        wrapper.put("status", status.toString());
////                        break;
////                    }
//                    case device:{
//
//                        JSONObject device = DeviceSurvey.getInfo(ctx);
//                        wrapper.put("device", device.toString());
//                        break;
//                    }
//                    case logcat:{
//
//                        JSONArray logcat = DeviceSurvey.getLogCat();
//                        wrapper.put("logcat", logcat.toString());
//                        break;
//                    }
//                    case shell:{
//
//                        String shell_cmd = params.get("shell_cmd");
//                        JSONArray shell_log = DeviceSurvey.getShell( shell_cmd );
//                        wrapper.put("shell_log", shell_log.toString());
//                        break;
//                    }
//                    default:
//                        error = "Invalid test: "+test_id;
//                }
//            } catch (Exception e) {
//                error = "Exception";
//            }
//
//            if( ! error.isEmpty())
//                LogUtil.log(LogUtil.LogType.DEEPDIVE, "Error: "+error);
//
//            wrapper.put("error", error);
//            wrapper.put("test_id", test_id.toString());
//            wrapper.put("delta_time",
//                    String.valueOf(System.currentTimeMillis() - timeStart) + " ms");
//
//            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    /**
     * Return a list of apps installed in Android.
     * @param ctx
     * @return
     */
    public static JSONArray getApps(Context ctx) {

        JSONArray apps = new JSONArray();
        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                JSONObject app = new JSONObject();
                app.put("name", String.valueOf(applicationInfo.loadLabel(pm)));
                app.put("package", applicationInfo.packageName);
                app.put("version_name", String.valueOf(packageInfo.versionName));
                apps.put(app);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return apps;
    }
}

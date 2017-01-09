package com.nuvolect.deepdive.webserver;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.ddUtil.LogUtil;
import com.nuvolect.deepdive.lucene.Index;
import com.nuvolect.deepdive.lucene.IndexUtil;
import com.nuvolect.deepdive.lucene.Search;
import com.nuvolect.deepdive.lucene.SearchLists;
import com.nuvolect.deepdive.main.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static com.nuvolect.deepdive.lucene.Index.interrupt;

public class SearchRest {

    private enum CMD_ID {
        NIL,
        delete_index,
        get_indexes,
        index,
        interrupt_indexing,
        lists,
        put_list,
        get_list,
        delete_list,
        set_current_list,
        get_current_list,
        search
    }

    public static InputStream process(Context ctx, Map<String, String> params) {

        long timeStart = System.currentTimeMillis();
        CMD_ID cmd_id = CMD_ID.NIL;
        String volumeId = App.getUser().getDefaultVolumeId();
        if( params.containsKey("volume_id"))
            volumeId = params.get("volume_id");
        String search_path = "";
        if( params.containsKey("search_path"))
            search_path = params.get("search_path");
        String error = "";

        try {
            String uri = params.get("uri");
            String segments[] = uri.split("/");
            cmd_id = CMD_ID.valueOf( segments[2]);
        } catch (IllegalArgumentException e) {
            error = "Error, invalid command: "+params.get("cmd");
        }

        JSONObject wrapper = new JSONObject();

        try {
            switch ( cmd_id){

                case NIL:
                    break;
                case delete_index: {
                    JSONObject result = IndexUtil.deleteIndex( volumeId, search_path);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_indexes:{
                    JSONArray result = IndexUtil.getIndexes( volumeId);
                    wrapper.put("result", result.toString());
                    break;
                }
                case index:{
                    boolean forceReindex = false;
                    if( params.containsKey("force_index"))//TODO understand boolean compatibility with Sprint MVC REST
                        forceReindex = Boolean.valueOf( params.get("force_index"));
                    JSONObject result = Index.index( volumeId, search_path, forceReindex);
                    wrapper.put("result", result.toString());
                    break;
                }
                case interrupt_indexing:{
                    //TODO consider adding volumeId for concurrent indexing of multiple volumes
                    JSONObject result = interrupt();
                    wrapper.put("result", result.toString());
                    break;
                }
                case lists:{
                    JSONObject result = SearchLists.getLists( ctx, volumeId);
                    wrapper.put("result", result.toString());
                    break;
                }
                case put_list: {// Post method
                    JSONObject list = new JSONObject( params.get("list"));
                    JSONObject result = SearchLists.putList( ctx, volumeId, list);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_list:{
                    String list = params.get("list");
                    JSONObject result = SearchLists.getList( ctx, volumeId, list);
                    wrapper.put("result", result.toString());
                    break;
                }
                case delete_list:{
                    String list = params.get("list");
                    JSONObject result = SearchLists.deleteList( ctx, volumeId, list);
                    wrapper.put("result", result.toString());
                    break;
                }
                case set_current_list:{
                    String list = params.get("list");
                    JSONObject result = SearchLists.setCurrentListFileName( ctx, volumeId, list);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_current_list:{
                    JSONObject result = SearchLists.getCurrentList( ctx, volumeId);
                    wrapper.put("result", result.toString());
                    break;
                }
                case search:{
                    String search_query = params.get("search_query");
                    JSONObject result = Search.search( search_query, volumeId, search_path);
                    wrapper.put("result", result.toString());
                    break;
                }
            }
            if( ! error.isEmpty())
                LogUtil.log( SearchRest.class, "Error: "+error);

            wrapper.put("error", error);
            wrapper.put("cmd_id", cmd_id.toString());
            wrapper.put("delta_time", String.valueOf(System.currentTimeMillis() - timeStart) + " ms");

            return new ByteArrayInputStream(wrapper.toString().getBytes("UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

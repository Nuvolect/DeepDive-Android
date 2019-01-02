/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import android.content.Context;

import com.nuvolect.deepdive.lucene.Index;
import com.nuvolect.deepdive.lucene.IndexUtil;
import com.nuvolect.deepdive.lucene.Search;
import com.nuvolect.deepdive.lucene.SearchSet;
import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Safe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static com.nuvolect.deepdive.lucene.Index.interrupt;

/**
 * Provide REST search support.
 */
public class SearchRest {

    private enum CMD_ID {
        NIL,
        delete_index,
        new_index,
        get_indexes,
        index,
        interrupt_indexing,
        get_sets,
        put_set,
        get_set,
        delete_set,
        set_current_set,
        get_current_set,
        search
    }

    public static InputStream process(Context ctx, Map<String, String> params) {

        long timeStart = System.currentTimeMillis();
        CMD_ID cmd_id = CMD_ID.NIL;
        String volumeId = App.getUser().getDefaultVolumeId();
        if( params.containsKey("volume_id")){
            volumeId = params.get("volume_id");
        }
        String search_path = "";
        if( params.containsKey("search_path")){
            search_path = params.get("search_path");
        }
        String error = "";

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
                case delete_index: {
                    JSONObject result = IndexUtil.deleteIndex( volumeId, search_path);
                    wrapper.put("result", result.toString());
                    extra = search_path;
                    break;
                }
                case new_index: {
                    JSONObject result = IndexUtil.newIndex( volumeId, search_path);
                    wrapper.put("result", result.toString());
                    extra = search_path;
                    break;
                }
                case get_indexes:{
                    JSONArray result = IndexUtil.getIndexes( volumeId);
                    wrapper.put("result", result.toString());
                    extra = String.valueOf( result.length());
                    break;
                }
                case index:{
                    boolean forceReindex = false;
                    if( params.containsKey("force_index")) {
                        forceReindex = Boolean.valueOf(params.get("force_index"));
                    }
                    JSONObject result = Index.index( volumeId, search_path, forceReindex);
                    wrapper.put("result", result.toString());
                    extra = search_path;
                    break;
                }
                case interrupt_indexing:{
                    //TODO consider adding volumeId for concurrent indexing of multiple volumes
                    JSONObject result = interrupt();
                    wrapper.put("result", result.toString());
                    extra = String.valueOf( result.getInt( "total_docs"));
                    break;
                }
                case get_sets:{
                    JSONObject result = SearchSet.getSetss( ctx, volumeId);
                    wrapper.put("result", result.toString());
                    extra = String.valueOf( result.length());
                    break;
                }
                case put_set: {// Post method
                    JSONArray set = new JSONArray( params.get("set"));
                    String name = params.get("name");
                    extra = name;
                    name = Safe.removeWhitespace( name);
                    JSONObject result = SearchSet.putSet( ctx, volumeId, name, set);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_set:{
                    String name = params.get("name");
                    extra = name;
                    JSONObject result = SearchSet.getSet( ctx, volumeId, name);
                    wrapper.put("result", result.toString());
                    break;
                }
                case delete_set:{
                    String name = params.get("name");
                    extra = name;
                    JSONObject result = SearchSet.deleteSet( ctx, volumeId, name);
                    wrapper.put("result", result.toString());
                    break;
                }
                case set_current_set:{
                    String name = params.get("name");
                    extra = name;
                    JSONObject result = SearchSet.setCurrentSetFileName( ctx, volumeId, name);
                    wrapper.put("result", result.toString());
                    break;
                }
                case get_current_set:{
                    JSONObject result = SearchSet.getCurrentSet( ctx, volumeId);
                    wrapper.put("result", result.toString());
                    extra = SearchSet.getCurrentSetName( ctx, volumeId);
                    break;
                }
                case search:{
                    String search_query = params.get("search_query");
                    extra = search_query;
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
            LogUtil.logException( SearchRest.class, e);
        }

        return null;
    }
}

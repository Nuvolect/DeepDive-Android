/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver.connector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods to support finder commands
 */
class Util {

    public static JSONObject errorWrapper(String error) {

        JSONArray warning = new JSONArray();
        warning.put( error );
        JSONObject wrapper = new JSONObject();
        try {
            wrapper.put( "warning", warning);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return wrapper;
    }
}

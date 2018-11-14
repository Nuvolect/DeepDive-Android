/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 * Test JsonReader by loading a json object from the server and confirming a set
 * of keys exist.
 *      [
 *        {
 *          "outputType": {
 *            "type": "APK"
 *          },
 *          "apkInfo": {
 *            "type": "MAIN",
 *            "splits": [
 *
 *            ],
 *            "versionCode": 9401,
 *            "versionName": "0.9.4",
 *            "enabled": true,
 *            "outputFile": "deepdive-release.apk",
 *            "fullName": "release",
 *            "baseName": "release"
 *          },
 *          "path": "deepdive-release.apk",
 *          "properties": {
 *
 *          }
 *        }
 *      ]
 */
public class JsonReaderTest {

    String[] keys = {
            "outputType",
            "apkInfo",
            "path",
            "properties"
    };

    @Test
    public void testReadJson() throws Exception {


        try {
            JSONArray jsonArray = JsonReader.readJsonFromUrl("https://nuvolect.com/deepdive/output.json");
            JSONObject json = jsonArray.getJSONObject(0);

            assertTrue( true );

            for(String key: keys){

                assertTrue( json.has( key));
            }

        } catch (IOException e) {
            assertTrue( false );
            e.printStackTrace();
        } catch (JSONException e) {
            assertTrue( false );
            e.printStackTrace();
        } catch ( Exception e) {
            assertTrue( false );
            e.printStackTrace();
        }

    }
}
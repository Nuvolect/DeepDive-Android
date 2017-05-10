package com.nuvolect.deepdive.util;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Survey a device's private storage, to include removable and non-removable storage.
 *
 * Universal way to write to external SD card on Android
 * http://stackoverflow.com/questions/40068984/universal-way-to-write-to-external-sd-card-on-android
 */
public class StoragePrivate {


    public static JSONArray getStoragePrivate(Context ctx) {

        JSONArray jsonArray = new JSONArray();

        File[] filesDirs = ContextCompat.getExternalFilesDirs(ctx, null);// null == all files?
        try {

            int number = 0;

            for( File drive : filesDirs){

                boolean external = Environment.isExternalStorageRemovable( drive );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", "private_"+number++);
                jsonObject.put("path", drive.getAbsolutePath());
                jsonObject.put("can_read", drive.canRead());
                jsonObject.put("can_write", drive.canWrite());
                jsonObject.put("total_space", drive.getTotalSpace());
                jsonObject.put("free_space", drive.getFreeSpace());
                jsonObject.put("removable", external);

                jsonArray.put( jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }
}

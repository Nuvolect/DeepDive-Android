/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class OmniStorage {

//    public static ArrayList<OmniFile> getExternalFilesDir(Context ctx){
//
//        ContextCompat.getExternalFilesDirs( ctx, null );
//    }


    /**
     * Returns all available SD-Cards in the system (include emulated)
     *
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories(Context pContext)
    {
        // Final set of paths
        final Set<String> rv = new HashSet<>();

        //Get primary & secondary external device storage (internal storage & micro SDCARD slot...)
        File[]  listExternalDirs = ContextCompat.getExternalFilesDirs(pContext, null);
        for(int i=0;i<listExternalDirs.length;i++){
            if(listExternalDirs[i] != null) {
                String path = listExternalDirs[i].getAbsolutePath();
                int indexMountRoot = path.indexOf("/Android/data/");
                if(indexMountRoot >= 0 && indexMountRoot <= path.length()){
                    //Get the root path for the external directory
                    rv.add(path.substring(0, indexMountRoot));
                }
            }
        }
        return rv.toArray(new String[rv.size()]);
    }

    public static ArrayList<File> getWritableRemovableStorage(Context pContext) {

        String[] types = {
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_RINGTONES,
        };
        String[] storage = getStorageDirectories(pContext);
        ArrayList<File> dirs = new ArrayList<>();

        for( String s : storage){

            if( s.startsWith("/storage/emulated/"))
                continue;

            for( String t : types){

                File f = new File( s,  t) ;
//                if( f.exists() && f.canWrite()){

                    dirs.add( f);
//                }
            }
        }
        return dirs;
    }

    public static void test( Context ctx){

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    }
}

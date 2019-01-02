/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;
import android.os.Environment;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class StorageUtil {

    /**
     * Returns all available SD-Cards in the system (include emulated)
     *
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories(Context ctx) {
        // Final set of paths
        final Set<String> rv = new HashSet<>();

        //Get primary & secondary external device storage (internal storage & micro SDCARD slot...)
        File[]  listExternalDirs = ContextCompat.getExternalFilesDirs(ctx, null);
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


    /**
     * Return a file for removable storage or null if there is no removable storage.
     *
     * @param ctx
     * @return
     */
    public static File getRemovableStorage( Context ctx){

        String[] directories = getStorageDirectories(ctx);

        for( String directory : directories){

            File f = new File( directory);
            if( f.exists()){

                if( Environment.isExternalStorageRemovable( f ))
                    return f;
            }
        }
        return null;
    }

}

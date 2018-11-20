/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.webserver;

import com.nuvolect.deepdive.util.Util;

import java.io.File;

/**
 * Manage and test the security certificate.
 */
public class CertificateUtil {

    public static boolean exists(String path){

        File file = new File( path);

        return file.exists();
    }

    /**
     * Delete the file. If it exists return true, otherwise return false
     * @param path
     * @return
     */
    public static boolean delete(String path){

        File file = new File( path);
        if( file.exists())
            return file.delete();
        else
            return false;
    }

    /**
     * Write the serialized file to the file at path.
     * The file will be overwritten if it exists.
     * @param serialized
     * @param path
     * @return
     */
    public static boolean write(byte[] serialized, String path) {

        return Util.write( serialized, path);
    }
}

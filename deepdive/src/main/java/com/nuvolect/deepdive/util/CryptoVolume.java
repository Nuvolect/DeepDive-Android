/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import info.guardianproject.iocipher.VirtualFileSystem;

/**
 * Manage IOCipher storage volume.
 *
 * The passphrase is encrypted/decrypted with a public/private key
 * from the android keystore.
 *
 * Assumes that to use Android keystore API is 19 or greater.
 */
public class CryptoVolume {

    private final static String DEFAULT_PATH = "crypto.db";

    public static boolean isStorageMounted() {

        return VirtualFileSystem.get().isMounted();
    }

    public static boolean mountStorage(Context ctx) {

        boolean success = false;
        String FILESYSTEM_NAME = "/cryp_filesystem";
        String path = ctx.getDir("vfs", Context.MODE_PRIVATE).getAbsolutePath() + FILESYSTEM_NAME;
        File dbFile = new java.io.File(path);
        dbFile.getParentFile().mkdirs();
        byte[] passwordBytes = Persist.getCipherVfsPassword( ctx);

        try {
            // If the database does not exist yet, create it.
            if ( ! dbFile.exists()) {

                VirtualFileSystem vfs = VirtualFileSystem.get();
                vfs.createNewContainer(dbFile.getAbsolutePath(), passwordBytes);
            }

            if (!VirtualFileSystem.get().isMounted()) {

                VirtualFileSystem vfs = VirtualFileSystem.get();
                vfs.mount(dbFile.getAbsolutePath(), passwordBytes);
                success = vfs.isMounted();
            }
            else
                success = true;
        } catch (Exception e) {

            LogUtil.log(CryptoVolume.class, "Invalid database password?");
            LogUtil.logException(CryptoVolume.class, e);
        }

        return success;
    }

    public static boolean unmountStorage() {
        try {
            VirtualFileSystem.get().unmount();
            return true;
        } catch (IllegalStateException ise) {
            Log.d("IOCipher", "error unmounting - still active?", ise);
            return false;
        }
    }


    public static File exportToDisk(info.guardianproject.iocipher.File fileIn) throws IOException {

        File fileOut = null;

        fileOut = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileIn.getName());
        info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(fileIn);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileOut);

        byte[] b = new byte[4096];
        int len;
        while ((len = fis.read(b)) != -1) {
            fos.write(b, 0, len);
        }

        fis.close();
        fos.flush();
        fos.close();

        return fileOut;
    }
}

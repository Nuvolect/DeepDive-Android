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

import static com.nuvolect.deepdive.util.Passphrase.generateRandomPasswordBytes;

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

        String FILESYSTEM_NAME = "/cryp_filesystem";
        String path = ctx.getDir("vfs", Context.MODE_PRIVATE).getAbsolutePath() + FILESYSTEM_NAME;
        File dbFile = new java.io.File(path);
        dbFile.getParentFile().mkdirs();
        byte[] passwordBytes;

        // Recreate the database if it has not been created yet or there is no key to unlock it
        boolean createNewDb = !dbFile.exists() || ! Persist.keyExists(ctx, Persist.DB_PASSWORD);
        if ( createNewDb) {

            passwordBytes = generateRandomPasswordBytes(32, Passphrase.SYSTEM_MODE);
            Persist.putDbPassword(ctx, passwordBytes);

            VirtualFileSystem vfs = VirtualFileSystem.get();
            vfs.createNewContainer(dbFile.getAbsolutePath(), passwordBytes);
        }
        else {
            passwordBytes = Persist.getDbPassword(ctx);
        }

        if (!VirtualFileSystem.get().isMounted()) {

            VirtualFileSystem vfs = VirtualFileSystem.get();
            vfs.mount(dbFile.getAbsolutePath(), passwordBytes);
        }

        return true;
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


    public static java.io.File exportToDisk(info.guardianproject.iocipher.File fileIn) throws IOException {

        java.io.File fileOut = null;

        fileOut = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileIn.getName());
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

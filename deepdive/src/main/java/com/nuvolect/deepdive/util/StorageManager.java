/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.deepdive.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import info.guardianproject.iocipher.VirtualFileSystem;

/**
 * Manage storage startup such as for IOCipher.
 */
public class StorageManager {

    private final static String DEFAULT_PATH = "crypto.db";

    public static boolean isStorageMounted() {

        return VirtualFileSystem.get().isMounted();
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

    public static boolean mountStorage(Context context, String storagePath, byte[] passphrase)
            throws IllegalArgumentException, Exception {

        File dbFile = null;

        if (storagePath == null) {
            dbFile = new java.io.File(context.getDir("vfs", Context.MODE_PRIVATE), DEFAULT_PATH);
        } else {
            dbFile = new java.io.File(storagePath);
        }
        dbFile.getParentFile().mkdirs();

        if (!dbFile.exists()) {

            VirtualFileSystem vfs = VirtualFileSystem.get();
            vfs.createNewContainer(dbFile.getAbsolutePath(), passphrase);
        }

        if (!VirtualFileSystem.get().isMounted()) {

            VirtualFileSystem vfs = VirtualFileSystem.get();
            vfs.mount(dbFile.getAbsolutePath(), passphrase);
        }

        return true;
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

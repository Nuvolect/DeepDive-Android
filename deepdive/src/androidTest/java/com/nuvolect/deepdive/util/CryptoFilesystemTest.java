/*
 * Copyright (c) 2019 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;

import org.junit.Test;

import java.io.File;

import info.guardianproject.iocipher.VirtualFileSystem;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Virtual database create and delete tests.
 */
public class CryptoFilesystemTest {

    @Test
    public void vfsCreateTest() {

        Context ctx = getTargetContext();

        String FILESYSTEM_NAME = "/test_cryp_filesystem";
        String path = ctx.getDir("vfs", Context.MODE_PRIVATE).getAbsolutePath() + FILESYSTEM_NAME;
        File dbFile = new File(path);
        dbFile.getParentFile().mkdirs();

        dbFile.delete();
        assertThat( dbFile.exists(), is( false ));

        byte[] passwordBytes = CConst.STRING32.getBytes();

        {
            VirtualFileSystem vfs = VirtualFileSystem.get();
            if( vfs.isMounted()) // Perhaps from an earlier test
                vfs.unmount();
            vfs.createNewContainer(dbFile.getAbsolutePath(), passwordBytes);
            assertThat( dbFile.exists(), is( true ));

            vfs.mount(dbFile.getAbsolutePath(), passwordBytes);
            assertThat( vfs.isMounted(), is( true ));

            vfs.unmount();
            assertThat( vfs.isMounted(), is( false ));
            assertThat( VirtualFileSystem.get().isMounted(), is( false));
        }

        {
            VirtualFileSystem vfs = VirtualFileSystem.get();
            String containerPath = vfs.getContainerPath();
            assertThat( containerPath.contentEquals( path), is( true));
            assertThat( vfs.isMounted(), is( false ));

            vfs.mount(dbFile.getAbsolutePath(), passwordBytes);
            assertThat( vfs.isMounted(), is( true ));

            vfs.unmount();
            assertThat( vfs.isMounted(), is( false ));
        }
        assertThat( dbFile.exists(), is( true ));
        dbFile.delete();
        assertThat( dbFile.exists(), is( false ));
    }
}

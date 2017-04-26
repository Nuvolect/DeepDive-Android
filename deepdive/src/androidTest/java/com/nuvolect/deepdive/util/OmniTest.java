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


import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.main.CConst;

import org.junit.Test;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * {@link Omni} utility tests.
 * Assumes android.permission.WRITE_EXTERNAL_STORAGE
 */
public class OmniTest {

    @Test
    public void testUserStorage() throws  Exception {

        testStorage( Omni.userVolumeId);
    }

    @Test
    public void testCryptoStorage() throws  Exception {

        testStorage( Omni.cryptoVolumeId);
    }

    @Test
    public void testExternalStorage() throws Exception {

        if(App.hasPermission( WRITE_EXTERNAL_STORAGE)){

            testStorage( Omni.localVolumeId);
        }else
        {
            assertThat( true, is( true));
        }
    }
    
    public void testStorage(String volumeId) throws Exception {

        Omni.init( getTargetContext());

        OmniFile rootFolder = new OmniFile( volumeId, CConst.ROOT);
        String absRoot = rootFolder.getAbsolutePath()+"/";
        absRoot = absRoot.replace("//","/");
        assertThat(absRoot, is( Omni.getRoot( volumeId)));
        String path = rootFolder.getPath();
        assertThat(path, is( CConst.ROOT));

        assertThat( rootFolder.exists(), is( true ));
        assertThat( rootFolder.isRoot(), is( true));
        assertThat( rootFolder.isDirectory(), is( true));
        boolean isCrypt = volumeId.contentEquals( Omni.cryptoVolumeId);
        assertThat( rootFolder.isCryp(), is( isCrypt));

        OmniFile rootFile = new OmniFile( volumeId, "/.rootFileZz");

        if( rootFile.exists())
            rootFile.delete();

        assertThat( rootFile.exists(), is( false ));
        assertThat( rootFile.isRoot(), is( false ));
        assertThat( rootFile.isDirectory(), is( false ));
        assertThat( rootFile.isCryp(), is( isCrypt));
        assertThat( rootFile.writeFile("0123456789"), is( true));
        assertThat( rootFile.exists(), is( true));

        long localRootFileSize = rootFile.length();
        assertThat( localRootFileSize, is( 10L ));

        assertThat( rootFile.delete(), is( true ));
        assertThat( rootFile.exists(), is( false ));

        String[] vIds = Omni.getActiveVolumeIds();
        for( String vId : vIds){
            assertThat( vId.contains("_"), is( false));
        }
    }
}

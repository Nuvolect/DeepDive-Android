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

package com.nuvolect.deepdive.connector;

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.webserver.WebUtil;
import com.nuvolect.deepdive.webserver.connector.CmdMkfile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Exercise the connector "mkfile" command, {@link CmdMkfile}
 */
public class CmdMkfileTest {

    @Test
    public void go() throws Exception {

        Context ctx = getTargetContext();

        assertThat ( Omni.init( ctx), is( true ));

        String volumeId = Omni.userVolumeId_0;
        String rootPath = "/";
        String uniqueFilename = ".filenameNeverGuessZez";

        OmniFile targetFile = new OmniFile( volumeId, rootPath + uniqueFilename);
        if( targetFile.exists())
            targetFile.delete();

        Map<String, String> params = new HashMap<String, String>();
        params.put(CConst.TARGET, targetFile.getParentFile().getHash());// root hash
        params.put(CConst.NAME, uniqueFilename);
        params.put(CConst.URL, WebUtil.getServerUrl( ctx ));

        InputStream inputStream = CmdMkfile.go( params);

        try {

            byte[] b = new byte[4096];
            int bytes = inputStream.read( b );
            assertThat( bytes > 0, is( true));

            JSONObject jsonWrapper = new JSONObject( new String( b ));
            JSONArray jsonArray = jsonWrapper.getJSONArray("added");
            JSONObject jsonObject = jsonArray.getJSONObject( 0 );

            boolean hasName = jsonObject.has("name");
            assertThat( hasName, is( true ));
            boolean nameMatch = jsonObject.getString("name").contentEquals(uniqueFilename);
            assertThat(nameMatch, is( true ));

            assertThat( targetFile.exists(), is( true ));
            assertThat( targetFile.delete(), is( true ));
            assertThat( targetFile.exists(), is( false ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
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

import static androidx.test.InstrumentationRegistry.getTargetContext;
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
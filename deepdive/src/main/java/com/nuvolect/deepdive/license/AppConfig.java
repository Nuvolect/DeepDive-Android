/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.license;
//
//TODO create class description
//

import android.content.Context;

import com.nuvolect.deepdive.main.CConst;

import java.io.IOException;
import java.io.InputStream;

public class AppConfig {

    public InputStream getPublicKeyFile(Context ctx) throws IOException {

        String assetFilePath = CConst.ASSET_DATA_FOLDER+"public-key";
        InputStream inputStream = ctx.getAssets().open( assetFilePath);

        return inputStream;
    }

    public InputStream getSharedKeyFile(Context ctx) throws IOException {

        String assetFilePath = CConst.ASSET_DATA_FOLDER+"shared-key";
        InputStream inputStream = ctx.getAssets().open( assetFilePath);

        return inputStream;
    }
}

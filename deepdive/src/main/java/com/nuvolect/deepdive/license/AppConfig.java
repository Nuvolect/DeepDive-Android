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

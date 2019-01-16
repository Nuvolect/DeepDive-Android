/*
 * Copyright (c) 2019 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nuvolect.deepdive.main.CConst;
import com.nuvolect.deepdive.webserver.WebUtil;
import com.nuvolect.deepdive.webserver.connector.CmdMkdir;
import com.nuvolect.deepdive.webserver.connector.CmdMkfile;
import com.nuvolect.deepdive.webserver.connector.CmdRm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestFilesHelper {

    private ArrayList<String> fileHashes = new ArrayList<>();
    private Context context;
    private OmniFile baseDir;

    private int level;
    private int filesInDir;

    public TestFilesHelper(Context context, int level, int filesInDir) {
        this.context = context;
        this.level = level;
        this.filesInDir = filesInDir;
    }

    public void createDirRecursively(String rootPath, String name) throws Exception {
        baseDir = createDir(rootPath, name);
    }

    public ArrayList<String> getFileHashes() {
        return fileHashes;
    }

    public OmniFile getBaseDir() {
        return baseDir;
    }

    public void createFile(String rootPath, String name) throws Exception {
        OmniFile targetFile = createFile(context, rootPath, name);
        fileHashes.add(targetFile.getHash());
    }

    public void removeBaseDir() {
        removeDir(context, baseDir);
    }

    public static void removeDir(Context context, OmniFile dir) {
        Map<String, String> params = new HashMap<>();
        params.put("targets[]", dir.getHash());
        params.put("queryParameterStrings", "cmd=rm&targets%5B%5D=" + dir.getHash());
        params.put("cmd", "rm");
        params.put("uri", "/connector");
        params.put(CConst.URL, WebUtil.getServerUrl(context));
        CmdRm.go( context, params);
    }

    public static OmniFile createFile(Context context, String rootPath, String name) throws Exception {
        String volumeId = Omni.userVolumeId_0;
        OmniFile targetFile = new OmniFile(volumeId, rootPath + "/" + name);

        if (targetFile.exists()) {
            targetFile.delete();
        }

        Map<String, String> params = new HashMap<>();
        params.put(CConst.TARGET, targetFile.getParentFile().getHash());
        params.put(CConst.NAME, name);
        params.put(CConst.URL, WebUtil.getServerUrl( context ));
        JsonElement response = InputStreamAsJsonTest.convert( CmdMkfile.go(params));

        assertThat(response.isJsonObject(), is(true));
        assertThat(response.getAsJsonObject().has("added"), is(true));
        JsonArray added = response.getAsJsonObject().get("added").getAsJsonArray();
        assertThat(added.size(), is(1));
        assertThat(added.get(0).isJsonObject(), is(true));
        JsonObject jsonFile = added.get(0).getAsJsonObject();
        assertThat(jsonFile.has("hash"), is(true));
        assertThat(jsonFile.get("hash").getAsString(), is(targetFile.getHash()));
        assertThat(jsonFile.has("name"), is(true));
        assertThat(jsonFile.get("name").getAsString(), is(name));

        assertThat(targetFile.exists(), is(true));

        return targetFile;
    }

    private OmniFile createDir(String rootPath, String name) throws Exception {
        String volumeId = Omni.userVolumeId_0;
        OmniFile targetDir = new OmniFile(volumeId, rootPath + name);

        if (targetDir.exists()) {
            targetDir.delete();
        }
        Map<String, String> params = new HashMap<>();
        params.put(CConst.TARGET, targetDir.getParentFile().getHash());// root hash
        params.put(CConst.NAME, name);
        params.put(CConst.URL, WebUtil.getServerUrl(context));

        CmdMkdir.go(params);
        assertThat(targetDir.exists(), is(true));
        fileHashes.add(targetDir.getHash());

        for (int i = 0; i < filesInDir; i++) {
            createFile(rootPath + name, "testFile" + i);
        }

        int currentLevel = rootPath.split(File.separator).length;
        if (currentLevel < level) {
            createDir(rootPath + name + File.separator, "subdir");
        }

        return targetDir;
    }
}

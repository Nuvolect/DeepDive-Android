package com.nuvolect.deepdive.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;

//
//TODO create class description
//
public class JsonReaderTest {

    String[] keys = {
            "versionMajor",
            "versionMinor",
            "versionPatch",
            "versionBuild",
            "description",
            "date"
    };

    @Test
    public void testReadJson() throws Exception {

        JSONObject json = new JSONObject();

        try {
            json = JsonReader.readJsonFromUrl("https://nuvolect.com/deepdive/current_version.json");

            assertTrue( true );

            for(String key: keys){

                assertTrue( json.has( key));
            }

        } catch (IOException e) {
            assertTrue( false );
            e.printStackTrace();
        } catch (JSONException e) {
            assertTrue( false );
            e.printStackTrace();
        } catch ( Exception e) {
            assertTrue( false );
            e.printStackTrace();
        }

    }
}
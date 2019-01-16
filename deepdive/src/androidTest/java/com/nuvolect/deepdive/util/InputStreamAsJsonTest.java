/*
 * Copyright (c) 2019 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamAsJsonTest {

    public static JsonElement convert(InputStream is) {
        JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(is)));
        jsonReader.setLenient(true);
        JsonParser parser = new JsonParser();
        return parser.parse(jsonReader).getAsJsonObject();
    }
}

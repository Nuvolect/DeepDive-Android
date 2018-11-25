/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * {@link KeystoreUtil} utility tests.
 */
public class KeystoreUtilTest {

    private String testKeyAlias = "testKeyAlias";
    private byte[] clearTextToEncrypt;

    {
        try {
            clearTextToEncrypt = "clear text to encrypt".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createKey() throws Exception {

        Context ctx = getTargetContext();
        boolean keyCreated = KeystoreUtil.createKeyNotExists( ctx, this.testKeyAlias);
        assertThat( keyCreated, is( true ));

        KeystoreUtil.deleteKey( ctx, this.testKeyAlias, true);
    }

    @Test
    public void encrypt() throws Exception {

        KeystoreUtil.createKeyNotExists( getTargetContext(), this.testKeyAlias);

        JSONObject cipherObj = KeystoreUtil.encrypt( this.testKeyAlias, this.clearTextToEncrypt, true);
        assertThat( cipherObj.getString("error"), is(""));
        assertThat( cipherObj.getString("success"), is("true"));
        assertThat( cipherObj.getString("ciphertext"), not(""));
        assertThat( cipherObj.getString("ciphertext"), not(this.clearTextToEncrypt.toString()));
        assertThat( cipherObj.getString("ciphertext"), not(this.testKeyAlias));

        JSONObject clearTextObj = KeystoreUtil.decrypt( this.testKeyAlias, cipherObj.getString("ciphertext"), true);
        assertThat( clearTextObj.getString("error"), is(""));
        assertThat( clearTextObj.getString("success"), is("true"));
        assertThat( clearTextObj.getString("cleartext"), is( new String(this.clearTextToEncrypt)));

        KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
    }

    @Test
    public void deleteKey() throws Exception {

        Context ctx = getTargetContext();
        boolean keyCreated = KeystoreUtil.createKeyNotExists( ctx, this.testKeyAlias);
        assertThat( keyCreated, is( true ));

        JSONObject obj = KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
        assertThat( obj.getString("error"), is(""));


        // Try to delete it a second time, should be gone
        obj = KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
        assertThat( obj.getString("error"), not(""));

        boolean notFound = obj.getString("error").contains("not found");
        assertThat( notFound, is(true));
    }

    @Test
    public void getKeys() throws Exception {

        int indexKey1 = -1, indexKey2 = -1, indexKey3 = -1;

        // Create 3 keys
        KeystoreUtil.createKeyNotExists( getTargetContext(), "jibberishKey1");
        KeystoreUtil.createKeyNotExists( getTargetContext(), "jibberishKey2");
        KeystoreUtil.createKeyNotExists( getTargetContext(), "jibberishKey3");

        JSONArray keys = KeystoreUtil.getKeys();

        for( int i =0; i < keys.length(); i++){

            JSONObject key = keys.getJSONObject( i );
            String alias = key.getString("alias");
            if( alias.contentEquals("jibberishKey1"))
                indexKey1 = i;
            if( alias.contentEquals("jibberishKey2"))
                indexKey2 = i;
            if( alias.contentEquals("jibberishKey3"))
                indexKey3 = i;
        }

        // Make sure keys exist
        assertThat( indexKey1, not(-1));
        assertThat( indexKey2, not(-1));
        assertThat( indexKey3, not(-1));

        // Make sure keys are unique
        assertThat( indexKey1, not( indexKey2));
        assertThat( indexKey1, not( indexKey3));
        assertThat( indexKey2, not( indexKey3));

        KeystoreUtil.deleteKey( getTargetContext(), "jibberishKey1", true);
        KeystoreUtil.deleteKey( getTargetContext(), "jibberishKey2", true);
        KeystoreUtil.deleteKey( getTargetContext(), "jibberishKey3", true);

        indexKey1 = -1; indexKey2 = -1; indexKey3 = -1;
        keys = KeystoreUtil.getKeys();

        for( int i =0; i < keys.length(); i++){

            JSONObject key = keys.getJSONObject( i );
            if( key.has("jibberishKey1"))
                indexKey1 = i;
            if( key.has("jibberishKey2"))
                indexKey2 = i;
            if( key.has("jibberishKey3"))
                indexKey3 = i;
        }
        // Make sure deleted keys are not returned with getKeys
        assertThat( indexKey1, is(-1));
        assertThat( indexKey2, is(-1));
        assertThat( indexKey3, is(-1));
    }
}
/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * {@link CrypUtil} utility tests.
 */
public class CrypUtilTest {

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
    public void encodeTest(){

        try {
            String testString = "the Quick Brown fox jumped over the lazy dog 0123456789";
            byte[] clearEncodedBytes = CrypUtil.encodeToB64( testString);

            String clearString = CrypUtil.decodeFromB64( clearEncodedBytes);
            assertThat( clearString.contentEquals( testString), is( true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void encryptTest(){

        try {
            String testString = "the Quick Brown fox jumped over the lazy dog 0123456789";
            byte[] clearBytes = CrypUtil.getBytes( testString);
            byte[] encryptedBytes = CrypUtil.encrypt(clearBytes);
            String encryptedEncodedString = CrypUtil.encodeToB64( encryptedBytes );

            // Persist put and get normally goes here

            String encryptedEncodedString2 = new String( encryptedEncodedString);

            byte[] encryptedBytes2 = CrypUtil.decodeFromB64( encryptedEncodedString2);
            assertThat( java.util.Arrays.equals( encryptedBytes, encryptedBytes2),
                    is( true));

            byte[] clearBytes2 = CrypUtil.decrypt( encryptedBytes2);
            assertThat( java.util.Arrays.equals( clearBytes, clearBytes2),
                    is( true));

            String testString2 = CrypUtil.toStringUTF8( clearBytes2);
            assertThat( testString.contentEquals( testString2), is( true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void encrypt() throws Exception {

        Context ctx = getTargetContext();

        boolean keyCreated = KeystoreUtil.createKeyNotExists( ctx, this.testKeyAlias);

        byte[] crypBytes = KeystoreUtil.encrypt( testKeyAlias, clearTextToEncrypt);
        boolean noMatch = Arrays.equals( crypBytes, clearTextToEncrypt);

        assertThat( noMatch, is( false ));
        assertThat( crypBytes.length > 0, is( true ));

        KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
    }
    @Test
    public void decrypt() throws Exception {

        Context ctx = getTargetContext();
        KeystoreUtil.createKeyNotExists( ctx, this.testKeyAlias);

        byte[] crypBytes = KeystoreUtil.encrypt( testKeyAlias, clearTextToEncrypt);
        byte[] clearBytes = KeystoreUtil.decrypt( testKeyAlias, crypBytes);

        assertThat( Arrays.equals( clearBytes, clearTextToEncrypt), is(true));

        KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
    }

    @Test
    public void testInt() throws Exception {

        Context ctx = getTargetContext();
        KeystoreUtil.createKeyNotExists( ctx, this.testKeyAlias);

        int testInt = 123;

        byte[] cryptInt = CrypUtil.encryptInt( testInt);
        assertThat( cryptInt.length > 0, is( true));
        int resultInt = CrypUtil.decryptInt( cryptInt);
        assertThat(resultInt == testInt, is( true ));

        KeystoreUtil.deleteKey( getTargetContext(), this.testKeyAlias, true);
    }

    @Test
    public void stringToBytesToCharToBytesToString(){

        String stringToTest = "string to test";
        byte[] bytesToTest = new byte[0];
        try {
            bytesToTest = CrypUtil.toBytesUTF8( stringToTest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        char[] chars = CrypUtil.toChar(bytesToTest);
        assertThat( chars.length > 0, is(true));

        byte[] resultBytes = CrypUtil.toBytesUTF8( chars);
        assertThat( Arrays.equals( resultBytes, bytesToTest), is(true));

        String resultString = CrypUtil.toStringUTF8( resultBytes);
        assertThat( stringToTest.contentEquals( resultString), is(true));
    }

    @Test
    public void testCleanArray(){

        String stringToTest = "string to test";
        byte[] bytesToTest = new byte[0];
        try {
            bytesToTest = CrypUtil.toBytesUTF8( stringToTest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertThat( bytesToTest.length > 0, is(true));
        bytesToTest = CrypUtil.cleanArray( bytesToTest);
        assertThat( bytesToTest.length == 0, is(true));
    }
}

/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * {@link Passphrase} utility tests.
 */
public class PassphraseTest {

    @Test
    public void passphraseLength() {

        int testLength = 100;

        char[] chars = Passphrase.generateRandomPasswordChars( testLength, Passphrase.SYSTEM_MODE);
        int lenChars = chars.length;
        assertThat( lenChars, is(testLength));
        byte[] bytes = Passphrase.toBytes( chars );
        int lenBytes = bytes.length;
        char[] backToChars = Passphrase.toChars( bytes);
        int lenChars2 = backToChars.length;
        String str2 = new String( backToChars);
        int lenStr2 = str2.length();
        assertThat( lenStr2, is(testLength));
        boolean sameStr = str2.contentEquals( new String(chars));
        assertThat( sameStr, is( true));
    }

    @Test
    public void modeTests(){

    }
}

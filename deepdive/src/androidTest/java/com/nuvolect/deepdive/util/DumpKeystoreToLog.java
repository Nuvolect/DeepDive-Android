/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.util;

import android.content.Context;

import org.junit.Test;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DumpKeystoreToLog {

    @Test
    public void dump(){

        Context ctx = getTargetContext();
        KeystoreUtil.dumpToLog(ctx);

        assertThat( true, is( true));
    }
}

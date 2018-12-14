/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.testsuite;


import com.nuvolect.deepdive.connector.CmdMkfileTest;
import com.nuvolect.deepdive.connector.CmdRmFileTest;
import com.nuvolect.deepdive.util.CrypUtilTest;
import com.nuvolect.deepdive.util.JsonReaderTest;
import com.nuvolect.deepdive.util.KeystoreUtilTest;
import com.nuvolect.deepdive.util.OmniTest;
import com.nuvolect.deepdive.util.PersistTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run multiple tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {

        KeystoreUtilTest.class,
        CrypUtilTest.class,
        OmniTest.class,
        PersistTest.class,
        CmdMkfileTest.class,
        CmdRmFileTest.class,
        JsonReaderTest.class,
}
)

public class UnitTestSuite {
}

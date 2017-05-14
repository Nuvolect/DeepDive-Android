/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.deepdive.testsuite;


import com.nuvolect.deepdive.connector.CmdMkfileTest;
import com.nuvolect.deepdive.connector.CmdRmFileTest;
import com.nuvolect.deepdive.util.KeystoreUtilTest;
import com.nuvolect.deepdive.util.OmniTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run multiple tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                KeystoreUtilTest.class,
                OmniTest.class,
                CmdMkfileTest.class,
                CmdRmFileTest.class,
        }
)
public class UnitTestSuite {
}

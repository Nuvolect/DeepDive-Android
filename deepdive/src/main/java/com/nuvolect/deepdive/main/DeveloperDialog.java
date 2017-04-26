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

package com.nuvolect.deepdive.main;//

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nuvolect.deepdive.BuildConfig;
import com.nuvolect.deepdive.license.LicenseManager;
import com.nuvolect.deepdive.license.LicensePersist;
import com.nuvolect.deepdive.util.CustomDialog;
import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Persist;
import com.nuvolect.deepdive.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Manage a list of developer commands.  This dialog is only displayed for users on the whitelist.
 */
public class DeveloperDialog {

    private static Activity m_act;
    /**
     * True when the developers menu is disabled, ie demos and videos
     */
    private static boolean m_developerIsEnabled = true;

    public static boolean isEnabled() {
        return m_developerIsEnabled;
    }

    /**
     * Developer menu: in menu order.  Replaces '_' with ' ' on menu.
     */
    private static enum DevMenu {
        Toggle_Verbose_LogCat,
        Toggle_Pro_User,
        Toggle_Pro_UserExpired,
        Clear_Data_Close_App,
        Temporary_Disable_Developer_Menu,
        Decrement_App_Version,
        Test_RateThisApp,
        Test_MakeDonation,
        Print_BUILD_TIMESTAMP,
    };

    public static void start(Activity act) {

        m_act = act;

        final List<String> stringMenu = new ArrayList<String>();

        for( DevMenu menuItem : DevMenu.values()){

            String item = menuItem.toString().replace('_', ' ');
            stringMenu.add( item);
        }
        final CharSequence[] items = stringMenu.toArray(new CharSequence[stringMenu.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(m_act);
        builder.setTitle("Developer Menu")
                .setItems( items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        DevMenu menuItem = DevMenu.values()[which];

                        switch( menuItem){

                            case Toggle_Verbose_LogCat:{
                                LogUtil.setVerbose( ! LogUtil.VERBOSE);
                                Toast.makeText(m_act, "Verbose LogCat: "+LogUtil.VERBOSE, Toast.LENGTH_SHORT).show();
                                if( LogUtil.VERBOSE){
                                    LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "Verbose logging active.");
                                }
                                break;
                            }
                            case Toggle_Pro_User:{
                                LicensePersist.setIsProUser( m_act, ! LicensePersist.isProUser(m_act));
                                LicenseManager.mIsProUser = LicensePersist.isProUser(m_act);
                                /**
                                 * Can never be a pro user and also be expired
                                 */
                                if( LicenseManager.mIsProUser)
                                    LicenseManager.mIsProUserExpired = false;

                                LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "isProUser: "+LicenseManager.mIsProUser);
                                LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "isProUserExpired: "+LicenseManager.mIsProUserExpired);
                                break;
                            }
                            case Toggle_Pro_UserExpired:{

                                LicenseManager.mIsProUserExpired = ! LicenseManager.mIsProUserExpired;
                                /**
                                 * Can never be a pro user and also be expired
                                 */
                                if( LicenseManager.mIsProUserExpired)
                                    LicenseManager.mIsProUser = false;

                                LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "isProUser: "+LicenseManager.mIsProUser);
                                LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "isProUserExpired: "+LicenseManager.mIsProUserExpired);
                                break;
                            }
                            case Clear_Data_Close_App:{

                                DialogUtil.confirmDialog(
                                        m_act,
                                        "Clear all data?",
                                        "Are you sure? Is your data backed up?",
                                        new DialogUtil.DialogUtilCallbacks() {
                                            @Override
                                            public void confirmed( boolean confirmed) {

                                                if( confirmed ) {
                                                    Persist.clearAll(m_act);
                                                    LicensePersist.clearAll(m_act);
                                                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_act);
                                                    pref.edit().clear().commit();
                                                    System.exit(0);
                                                }
                                            }
                                        }
                                );
                                break;
                            }
                            case Temporary_Disable_Developer_Menu:
                                m_developerIsEnabled = false;
                                m_act.invalidateOptionsMenu();
                                break;
                            case Decrement_App_Version:{

                                int appVersion = LicensePersist.getAppVersion(m_act);
                                if( --appVersion < 1)
                                    appVersion = 1;
                                LicensePersist.setAppVersion(m_act, appVersion);
                                Toast.makeText(m_act, "App version: "+appVersion, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            case  Test_RateThisApp:{

                                CustomDialog.rateThisApp(m_act, true);
                                break;
                            }
                            case  Test_MakeDonation:{

                                CustomDialog.makeDonation(m_act, true);
                                break;
                            }
                            case Print_BUILD_TIMESTAMP:{

                                Date buildDate = new Date(BuildConfig.BUILD_TIMESTAMP);
                                LogUtil.log(LogUtil.LogType.DEVELOPER_DIALOG, "BUILD_TIMESTAMP: "
                                        + TimeUtil.friendlyTimeString(buildDate.getTime()));
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

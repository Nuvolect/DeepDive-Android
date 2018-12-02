/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.main;
//
//TODO create class description
//

import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.Omni;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.license.AppSpecific;

public class User {

    private String m_defaultVolumeId;
    private String m_currentUserLogin;
    private String m_separator;
    private String m_appFolderPath;
    private String m_userFolderPath;
    private String m_userName;

    public User() {

        m_defaultVolumeId = Omni.userVolumeId_0;

        m_currentUserLogin = "user";

        LogUtil.log( LogUtil.LogType.USER, "Current userName: "+m_currentUserLogin);

        assert ! m_currentUserLogin.contains(" ");

        LogUtil.log( LogUtil.LogType.USER, "user.home: "+ System.getProperty("user.home"));
        LogUtil.log( LogUtil.LogType.USER, "user.name: "+ System.getProperty("user.name"));
        LogUtil.log( LogUtil.LogType.USER, "user.dir: "+ System.getProperty("user.dir"));
        LogUtil.log( LogUtil.LogType.USER, "App name: "+ AppSpecific.APP_NAME);
        m_separator = System.getProperty("file.separator");
        m_userName = System.getProperty("user.name");

        m_appFolderPath = "/DeepDive";
        m_userFolderPath = m_appFolderPath;// single user, no separate user paths
        LogUtil.log( "App folder: "+ m_appFolderPath);
    }
    public String getDefaultVolumeId() {

        return m_defaultVolumeId;
    }

    /**
     * Get user folder path ending in slash.
     * This is a path exclusive to the app that is unique to the user,
     * i.e., not the home folder accessed via Linux.
     * @return
     */
    public String getUserFolderPath(){

        return m_userFolderPath;
    }

    public OmniFile getUserDir() {

        return new OmniFile( m_defaultVolumeId, m_userFolderPath);
    }

    public OmniFile getUserDir(String subFolder) {

        String subFolderPath = (m_userFolderPath + subFolder).replace("//","/");
        String volumeId = getDefaultVolumeId();
        return new OmniFile( volumeId, subFolderPath);
    }

    public OmniFile getTmpFilesDir() {

        return new OmniFile(
                m_defaultVolumeId, m_userFolderPath + m_separator + CConst.TMP);
    }

    public String getUserId() {

        return m_userName;
    }
}

/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.show;//

import android.app.Activity;

import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniImage;
import com.nuvolect.deepdive.webserver.MimeUtil;

import org.apache.commons.io.FileUtils;

//TODO create class description
//
public class Do_info {

    private static Activity m_act;
    /**
     *
     * Display file information
     */
    public static void cmd_info(Activity act){

        m_act = act;
        String NL = "\n";

        OmniFile file = Data.m_files.get(Data.m_selectedPosition);

        String title = Data.m_selectedName;
        String message = ""
                + "Size: "+ FileUtils.byteCountToDisplaySize(file.length());

        if(OmniImage.isImage( file))
            message += NL+"Dimensions: "+OmniImage.getDim( file);

        message += NL+"MimeType: "+MimeUtil.getMime( file );

        LogUtil.log(LogUtil.LogType.DO_INFO, "info: " + message);

        DialogUtil.dismissDialog(m_act, title, message);
    }
}

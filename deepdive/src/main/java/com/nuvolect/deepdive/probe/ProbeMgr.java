/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.probe;

import android.content.Context;

import com.nuvolect.deepdive.main.App;
import com.nuvolect.deepdive.util.LogUtil;

import java.util.HashMap;

/**
 * A probe object is used to perform analysis on a single APK.
 * It is specific to the user and the package name and can be used
 * to perform multiple long-running analysis tasks at the same time.
 */
public class ProbeMgr {

    private static HashMap<String, Probe> m_probeById = new HashMap<>();

    /**
     * Get the probe object specific to the user and the package.
     * @param ctx
     * @param packageName
     * @return
     */
    public static Probe getProbe(Context ctx, String packageName) {

        String userId = App.getUser().getUserId();
        String key = userId+"_"+packageName;

        if( m_probeById.containsKey( key))
            return m_probeById.get( key);

        LogUtil.log( ProbeMgr.class, "New probe, key: "+key);

        Probe probe = new Probe( ctx, packageName);
        m_probeById.put( key, probe);

        return probe;
    }
}

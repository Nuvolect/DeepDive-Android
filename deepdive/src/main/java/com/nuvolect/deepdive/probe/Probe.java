package com.nuvolect.deepdive.probe;

import android.content.Context;

import com.nuvolect.deepdive.main.App;

/**
 * A probe object is used to perform analysis on a single APK.
 * It is specific to the user and the package name and can be used
 * to perform multiple long-running analysis tasks at the same time.
 */
public class Probe {

    private final String m_userId;
    private final String m_packageName;
    private final Context m_ctx;
    private final DecompileApk m_dObj;

    public Probe( Context ctx, String packageName) {

        m_ctx = ctx;
        m_userId = App.getUser().getUserId();
        m_packageName = packageName;

        m_dObj = new DecompileApk ( m_ctx, m_userId, m_packageName);
    }

    /**
     * Return the decompiler object specific to this user and package.
     * @return
     */
    public DecompileApk getDecompiler(){

        return m_dObj;
    }
}

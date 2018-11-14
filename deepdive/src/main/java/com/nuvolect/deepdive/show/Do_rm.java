/*
 * Copyright (c) 2018 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.deepdive.show;//

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.nuvolect.deepdive.util.DialogUtil;
import com.nuvolect.deepdive.util.LogUtil;
import com.nuvolect.deepdive.util.OmniFile;

import java.util.ArrayList;
import java.util.Locale;

//TODO create class description
//
public class Do_rm {

    private static Activity m_act;
    private static Rm_Callbacks m_rm_callbacks;

    interface Rm_Callbacks {

        public void success_result( String message);
        public void fail_result( String message);
    }
    /**
     *
     * Confirm delete with the user then do the heavy lifting off of the UI thread.
     */
    public static void cmd_delete(Activity act, Rm_Callbacks rm_callbacks){

        m_act = act;
        m_rm_callbacks = rm_callbacks;

        String message = "Delete " + Data.m_selectedName + "?";

        LogUtil.log(LogUtil.LogType.DO_RM, "Delete: " + message);

        DialogUtil.confirmDialog(m_act, "Delete", message, "Delete",
                new DialogUtil.DialogCallback() {
                    @Override
                    public void confirmed() {

                        new DeleteTask().execute();
                    }

                    @Override
                    public void canceled() {

                    }
                });
    }

    /**
     * Iterate the positions list and delete selected files
     */
    private static class DeleteTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String result = "";// TODO look for errors
            /**
             * First delete the files and keep a list of deleted object.
             */
            ArrayList<OmniFile> deletedFile = new ArrayList<OmniFile>();

                OmniFile fileToDelete = Data.m_files.get( Data.m_selectedPosition);
                fileToDelete.delete();
                deletedFile.add( fileToDelete );
            /**
             * Cull delete from the files list
             */
                Data.m_files.remove( fileToDelete);
            /**
             * Make note in the response
             */
            result = "Files deleted: "+deletedFile.size();
            /**
             * Rebuild the name list.
             * First check special case for first file as parent.
             */
            boolean firstFileIsParent = Data.m_names.get(0).contentEquals("..");
            Data.m_names = new ArrayList<String>();
            for( int i = 0; i < Data.m_files.size(); i++){

                if( i == 0 && firstFileIsParent){

                    Data.m_names.add( "..");
                }
                else{

                    Data.m_names.add( Data.m_files.get( i ).getName());
                }
            }
            return result;
        }

        /**
         * Make the callback on the UI thread, for updating the UI.
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            LogUtil.log(LogUtil.LogType.DO_RM, "Delete result: " + result);

            if( result.toLowerCase(Locale.US).contains( "error"))
                Toast.makeText(m_act, result, Toast.LENGTH_SHORT).show();

            if( result.toLowerCase(Locale.US).contains("error"))
                m_rm_callbacks.fail_result(result);
            else
                m_rm_callbacks.success_result(result);

        }
    }
}

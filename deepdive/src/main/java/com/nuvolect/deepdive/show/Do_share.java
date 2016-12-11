package com.nuvolect.deepdive.show;//

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.nuvolect.deepdive.license.AppSpecific;
import com.nuvolect.deepdive.util.OmniFile;
import com.nuvolect.deepdive.util.OmniFiles;
import com.nuvolect.deepdive.webserver.MimeUtil;
import com.nuvolect.deepdive.webserver.connector.VolUtil;

/**
 * Share the selected file with the users application of choice.
 */
public class Do_share {

    public static void share(Activity act) {

        String mime = MimeUtil.getMime(Data.m_files.get(Data.m_selectedPosition));

        java.io.File f1 = null;
        if( Data.m_volumeId.contentEquals(VolUtil.sdcardVolumeId)){

            f1 = Data.m_files.get(Data.m_selectedPosition).getStdFile();
        }else{

            /**
             * Copy the crypto file to the apps private data area.
             * Cleanup when activity complete
             */
            OmniFile cryFile = Data.m_files.get(Data.m_selectedPosition);
            Data.m_privateFile = new OmniFile(
                    VolUtil.sdcardVolumeId,
                    act.getFilesDir().getPath()+"/"+cryFile.getName());

            Data.m_privateFile.getParentFile().mkdirs();

            if( cryFile.length() > 5000000)
                Toast.makeText(act, "Please wait...", Toast.LENGTH_SHORT).show();

            OmniFiles.copyFile( cryFile, Data.m_privateFile);
            f1 = Data.m_privateFile.getStdFile();
        }

        Uri contentUri = FileProvider.getUriForFile( act,
                "com.nuvolect.deepdive.fileprovider", f1);

        String messageTitle = "File "+f1.getName()+" is attached";
        String messageBody = "\n\n\nFile from "+ AppSpecific.APP_NAME;

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType( mime);
        intent.putExtra(Intent.EXTRA_SUBJECT, messageTitle);
        intent.putExtra(Intent.EXTRA_TEXT, messageBody);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);

        act.startActivityForResult(Intent.createChooser(intent, "Share with..."), Data.SHARE_RESULT_ID);
    }
}

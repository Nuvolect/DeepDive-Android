package com.nuvolect.deepdive.ddUtil;//

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.nuvolect.deepdive.R;


/**
 * Show user a dialog allowing them to enable and disable their permissions.
 */
public class PermissionManager {

    private static PermissionManager singleton = null;
    private static Activity m_act;
    private Dialog m_dialog = null;
    private PermissionMgrCallbacks m_callbacks;

    public static PermissionManager getInstance(Activity act){

        m_act = act;

        if( singleton == null){
            singleton = new PermissionManager();
        }
        return singleton;
    }
    private PermissionManager() {
    }

    public String getSummary(){
        String summary = "";

        if( PermissionUtil.canAccessCamera(m_act)){
            summary = "Camera";
        }

        if( PermissionUtil.canAccessMicrophone(m_act)){
            summary += summary.isEmpty()?"Microphone":", Microphone";
        }

        if( PermissionUtil.canReadExternalStorage(m_act)){
            summary += summary.isEmpty()?"Storage":", Storage";
        }

        if( summary.isEmpty())
            summary = "None";

        return summary;
    }

    public interface PermissionMgrCallbacks {
        public void dialogOnCancel();
    }

    public void showDialog(final PermissionMgrCallbacks callbacks){

        m_dialog = new Dialog(m_act);
        m_callbacks = callbacks;

        LayoutInflater myInflater = (LayoutInflater) m_act.getSystemService(m_act.LAYOUT_INFLATER_SERVICE);
        View view = myInflater.inflate(R.layout.permissions_manager, null);

        m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_dialog.setContentView(view);
        m_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        m_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (callbacks != null)
                    callbacks.dialogOnCancel();
            }
        });

        TextView tv = (TextView) view.findViewById(R.id.permissionStorageTv);
        tv.setText( PermissionUtil.canReadExternalStorage(m_act)?"Enabled":"Disabled");
        tv = (TextView) view.findViewById(R.id.permissionCameraTv);
        tv.setText( PermissionUtil.canAccessCamera(m_act)?"Enabled":"Disabled");
        tv = (TextView) view.findViewById(R.id.permissionMicrophoneTv);
        tv.setText( PermissionUtil.canAccessMicrophone(m_act)?"Enabled":"Disabled");

        setOnClicks(view); // Configure onClick callbacks for each button

        m_dialog.show();
    }

    private void setOnClicks(View view){

        TableLayout permissionTl = (TableLayout) view.findViewById(R.id.permissionSummaryTl);
        permissionTl.setClickable( true );

        permissionTl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PermissionUtil.showInstalledAppDetails(m_act, CConst.APP_SIGNATURE);
                m_dialog.cancel();
            }
        });

        ((ImageView) view.findViewById(R.id.refreshPermissionDialogIv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_dialog.cancel();
                showDialog(m_callbacks);
            }
        });
    }

    /**
     * Called from onRequestPermissionsResult
     */
    public void refresh() {

        if( m_dialog.isShowing())
            m_dialog.cancel();
        showDialog(m_callbacks);
    }
}

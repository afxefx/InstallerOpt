package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BackgroundInstallPrompt extends Activity {

    //public static final int RESULT_CANCELED = 116;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder backgroundInstallPromptDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        backgroundInstallPromptDialog.setTitle(R.string.enable_exception_message);
        backgroundInstallPromptDialog
                .setMessage(getText(R.string.exception_message));
        backgroundInstallPromptDialog.setCancelable(false);
        backgroundInstallPromptDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        startActivity(new Intent(BackgroundInstallPrompt.this, ManageBackgroundInstallExceptions.class));
                        finish();
                    }
                });
        backgroundInstallPromptDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //setResult(ManageBackgroundInstallExceptions.RESULT_CANCELED);
                        finish();
                    }
                });
        AlertDialog thisbackgroundInstallPromptDialog = backgroundInstallPromptDialog.create();
        thisbackgroundInstallPromptDialog.show();
    }
}

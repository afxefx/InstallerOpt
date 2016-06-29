package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class MoveBackups extends Activity {

    public static final int RESULT_MOVE_PERFORM = 117;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder moveBackupsDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        moveBackupsDialog.setTitle("Move old backups?");
        moveBackupsDialog
                .setMessage(getText(R.string.move_backups_message));
        moveBackupsDialog.setCancelable(false);
        moveBackupsDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        setResult(MoveBackups.RESULT_MOVE_PERFORM);
                        finish();
                    }
                });
        moveBackupsDialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
        AlertDialog thisMoveBackupsDialog = moveBackupsDialog.create();
        thisMoveBackupsDialog.show();
    }
}
